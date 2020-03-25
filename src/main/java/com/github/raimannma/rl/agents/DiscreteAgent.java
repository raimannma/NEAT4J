package com.github.raimannma.rl.agents;

public abstract class DiscreteAgent extends Agent {
	public int numActions;
	public int numStates;

	DiscreteAgent(final int numStates, final int numActions) {
		super(numStates, numActions);
	}

	abstract int act(double[] state);

	abstract double learn(double reward);
}
