package com.github.raimannma.rl.agents;

public abstract class ContinuousAgent extends Agent {

	public ContinuousAgent(final int numStates, final int numActions) {
		super(numStates, numActions);
	}

	public abstract double[] act(double[] state);
}
