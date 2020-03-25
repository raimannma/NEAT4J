package com.github.raimannma.rl.agents;

import java.util.Set;
import com.github.raimannma.nn.architecture.Network;
import com.github.raimannma.nn.methods.Utils;
import com.github.raimannma.rl.methods.DiscreteStrategy;
import com.github.raimannma.rl.methods.Experience;
import com.github.raimannma.rl.methods.Sampler;

public class DQN extends DiscreteAgent {
	DiscreteStrategy strategy;
	private Network qNetwork;
	private Experience currentExperience;
	private double learningRate;
	private Sampler sampler;
	private int learningStepsPerEpisode;

	public DQN(final int numStates, final int numActions, final DiscreteStrategy strategy) {
		super(numStates, numActions);
	}

	private void study(final Set<Experience> experience) {
		//TODO
	}

	@Override
	public int act(final double[] state) {
		final int action = Utils.getMaxValueIndex(this.qNetwork.activate(state));
		final int sampledAction = this.strategy.sample(action, this.episode, this.numActions);

		this.currentExperience.setEpisode(this.episode);
		this.currentExperience.setLastState(this.currentExperience.getState());
		this.currentExperience.setLastAction(this.currentExperience.getAction());
		this.currentExperience.setState(state);
		this.currentExperience.setAction(sampledAction);

		return sampledAction;
	}

	@Override
	public double learn(final double reward) {
		this.episode++;
		if (this.episode > 1) {

			final Set<Experience> batch = this.replayBuffer.getBatch(this.sampler, this.learningStepsPerEpisode);
			batch.add(this.currentExperience);
			this.study(batch);
			this.replayBuffer.addExperience(this.currentExperience.copy());
		}
		return 0;
	}
}
