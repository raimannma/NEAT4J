package com.github.raimannma.rl.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import com.github.raimannma.nn.architecture.Network;
import com.github.raimannma.nn.methods.EvolveOptions;
import com.github.raimannma.nn.methods.Loss;
import com.github.raimannma.nn.methods.Mutation;
import com.github.raimannma.nn.methods.Utils;
import com.github.raimannma.rl.methods.DiscreteStrategy;
import com.github.raimannma.rl.methods.Experience;
import com.github.raimannma.rl.methods.ReplayBuffer;
import com.github.raimannma.rl.methods.Sampler;

public class DQN extends DiscreteAgent {
	private final Sampler sampler;
	private final int learningStepsPerEpisode;
	private final double gamma;
	private final Experience currentExperience;
	private final EvolveOptions evolveOptions;
	private final Network qNetwork;
	DiscreteStrategy strategy;

	public DQN(final int numStates, final int numActions) {
		super(numStates, numActions);
		this.gamma = 0;
		this.learningStepsPerEpisode = 20;
		this.sampler = new Sampler.RandomSampler();
		this.currentExperience = new Experience();
		this.qNetwork = new Network(numStates, numActions);
		this.evolveOptions = new EvolveOptions()
				.setError(0)
				.setIterations(50)
				.setPopulationSize(500)
				.setElitism(50)
				.setMutations(Mutation.ONLY_WEIGHTS)
				.setTemplate(this.qNetwork);
		this.strategy = new DiscreteStrategy.EpsilonGreedy(0.1, 0.99, 0.05);
		final int bufferSize = 100;
		this.replayBuffer = new ReplayBuffer(bufferSize);
	}

	private double study(final List<Experience> experiences) {
		final double[][] inputs = new double[experiences.size()][this.numStates];
		final double[][] outputs = new double[experiences.size()][this.numActions];

		IntStream.range(0, experiences.size()).forEach(i -> {
			final Experience experience = experiences.get(i);
			final double[] lastStateActivation = this.qNetwork.activate(experience.getLastState());
			final double[] temp = lastStateActivation.clone();
			this.qNetwork.clear();
			final double[] stateActivation = this.qNetwork.activate(experience.getState());

			final double qValue = experience.getLastReward() + this.gamma * Utils.max(stateActivation);
			lastStateActivation[experience.getLastAction()] = qValue;

			inputs[i] = experience.getLastState();
			outputs[i] = lastStateActivation;

			experience.setTdError(Loss.MSE.calc(lastStateActivation, temp));
		});
		this.evolveOptions.setTemplate(this.qNetwork.copy());
		return this.qNetwork.evolve(inputs, outputs, this.evolveOptions);
	}

	@Override
	public int act(final double[] state) {
		final double[] actions = this.qNetwork.activate(state);
		final int action = Utils.getMaxValueIndex(actions);
		final int sampledAction = this.strategy.sample(action, this.episode, this.numActions);

		this.currentExperience.setEpisode(this.episode);
		this.currentExperience.setLastState(this.currentExperience.getState());
		this.currentExperience.setLastAction(this.currentExperience.getAction());
		this.currentExperience.setState(state);
		this.currentExperience.setAction(sampledAction);

		return sampledAction;
	}

	@Override
	public double learn(final double reward, final boolean isFinalState) {
		this.episode++;
		double error = Double.NaN;
		if (this.episode > 2) {
			final Set<Experience> batch = this.replayBuffer.getBatch(this.sampler, this.learningStepsPerEpisode);
			batch.add(this.currentExperience.copy());
			error = this.study(new ArrayList<>(batch));

			this.replayBuffer.addExperience(this.currentExperience.copy());
		}
		this.currentExperience.setLastReward(reward);
		return error;
	}
}
