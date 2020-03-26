package com.github.raimannma.rl.agents;

import static com.github.raimannma.nn.methods.Utils.convertToNonPrimitiveArray;
import static com.github.raimannma.nn.methods.Utils.convertToPrimitiveArray;
import static com.github.raimannma.nn.methods.Utils.indexOf;
import static com.github.raimannma.nn.methods.Utils.randInt;
import java.util.ArrayList;
import java.util.stream.IntStream;
import com.github.raimannma.nn.methods.Utils;
import com.github.raimannma.rl.methods.DiscreteStrategy;
import com.github.raimannma.rl.methods.Experience;

public class QLearner extends DiscreteAgent {
	protected final double gamma;
	protected final double learningRate;
	ArrayList<Double[]> states;
	ArrayList<Double[]> actions;
	DiscreteStrategy strategy;
	Experience currentExperience;

	public QLearner(final int numStates, final int numActions) {
		super(numStates, numActions);

		this.gamma = 0.7;
		this.learningRate = 0.1;
		this.strategy = new DiscreteStrategy.EpsilonGreedy(0.3, 0.999, 0.01);

		this.states = new ArrayList<>();
		this.actions = new ArrayList<>();

		this.currentExperience = new Experience();
	}

	@Override
	public int act(final double[] state) {
		final Double[] nonPrimitiveArray = convertToNonPrimitiveArray(state);
		final int index = indexOf(this.states, nonPrimitiveArray);

		final int action;
		if (index == -1) {
			action = randInt(this.numActions);
		} else {
			action = Utils.getMaxValueIndex(convertToPrimitiveArray(this.actions.get(index)));
		}
		final int sampledAction = this.strategy.sample(action, this.episode, this.numActions);

		this.currentExperience.setLastState(this.currentExperience.getState());
		this.currentExperience.setLastAction(this.currentExperience.getAction());
		this.currentExperience.setState(state);
		this.currentExperience.setAction(sampledAction);
		this.currentExperience.setEpisode(this.episode);

		return sampledAction;
	}

	@Override
	public double learn(final double reward, final boolean isFinalState) {
		this.episode++;

		if (this.episode > 2) {
			final Double[] lastState = convertToNonPrimitiveArray(this.currentExperience.getLastState());
			int index = indexOf(this.states, lastState);

			final Double[] lastStateQValues = index != -1
					? this.actions.get(index)
					: IntStream.range(0, this.numActions).mapToDouble(v -> Double.NaN).boxed().toArray(Double[]::new);
			if (Double.isNaN(lastStateQValues[this.currentExperience.getLastAction()])) {
				lastStateQValues[this.currentExperience.getLastAction()] = reward;
			}

			final double qValue = this.getQValue(index, isFinalState, lastStateQValues);
			lastStateQValues[this.currentExperience.getLastAction()] = qValue;

			if (index == -1) {
				this.states.add(lastState);
				index = this.states.size() - 1;
			}
			if (this.actions.size() <= index) {
				this.actions.add(index, lastStateQValues);
			} else {
				this.actions.set(index, lastStateQValues);
			}
		}
		this.currentExperience.setLastReward(reward);
		return Double.NaN;
	}

	protected double getQValue(final int stateIndex, final boolean isFinalState, final Double[] lastStateQValues) {
		final double lastQValue = lastStateQValues[this.currentExperience.getLastAction()];
		return stateIndex != -1 && !isFinalState
				? lastQValue + this.learningRate * (this.currentExperience.getLastReward() + this.gamma * Utils.max(convertToPrimitiveArray(this.actions.get(stateIndex))) - lastQValue)
				: lastQValue + this.learningRate * (this.currentExperience.getLastReward() - lastQValue);
	}
}
