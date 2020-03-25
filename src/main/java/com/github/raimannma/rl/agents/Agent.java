package com.github.raimannma.rl.agents;

import com.github.raimannma.rl.methods.ReplayBuffer;

public abstract class Agent {
	public int numStates;
	public int numActions;
	public int episode;
	public ReplayBuffer replayBuffer;

	Agent(final int numStates, final int numActions) {
		this.numStates = numStates;
		this.numActions = numActions;
		this.episode = 0;
	}

	public abstract double learn(double reward);
}
