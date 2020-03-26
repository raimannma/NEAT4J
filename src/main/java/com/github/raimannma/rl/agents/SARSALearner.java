package com.github.raimannma.rl.agents;

import static com.github.raimannma.nn.methods.Utils.convertToNonPrimitiveArray;
import static com.github.raimannma.nn.methods.Utils.indexOf;
import java.util.stream.IntStream;

public class SARSALearner extends QLearner {

	public SARSALearner(final int numStates, final int numActions) {
		super(numStates, numActions);
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

	private double getQValue(final int stateIndex, final boolean isFinalState, final Double[] lastStateQValues) {
		final double lastQValue = lastStateQValues[this.currentExperience.getLastAction()];
		return stateIndex != -1 && !isFinalState
				? lastQValue + this.learningRate * (this.currentExperience.getLastReward() + this.gamma * this.actions.get(stateIndex)[this.currentExperience.getAction()] - lastQValue)
				: lastQValue + this.learningRate * (this.currentExperience.getLastReward() - lastQValue);
	}
}
