package com.github.raimannma.rl.agents;

public abstract class Agent {
	public int numStates;
	public int numActions;
	public int episode;

	Agent(final int numStates, final int numActions) {
		this.numStates = numStates;
		this.numActions = numActions;
	}


	public abstract double learn(double reward);
}
