package com.github.raimannma.rl.agents;

public class DQN extends DiscreteAgent {
	public DQN(final int numStates, final int numActions) {
		super(numStates, numActions);
	}

	@Override
	public int act(final double[] state) {
		return 0;
	}

	@Override
	public double learn(final double reward) {
		return 0;
	}
}
