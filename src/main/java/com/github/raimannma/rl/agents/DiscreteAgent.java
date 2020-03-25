package com.github.raimannma.rl.agents;

public abstract class DiscreteAgent extends Agent {

	public DiscreteAgent(final int numStates, final int numActions) {
		super(numStates, numActions);
	}

	public abstract int act(double[] state);
}
