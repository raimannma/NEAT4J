package com.github.raimannma.rl.agents;

import com.github.raimannma.rl.methods.ReplayBuffer;

public abstract class Agent {
	public final int numStates;
	public final int numActions;
	public int episode;
	public ReplayBuffer replayBuffer;

	Agent(final int numStates, final int numActions) {
		this.numStates = numStates;
		this.numActions = numActions;
		this.episode = 0;
	}

	public double learn(final double reward) {
		return this.learn(reward, false);
	}

	public abstract double learn(double reward, boolean isFinalState);

	public int getNumStates() {
		return this.numStates;
	}

	public int getNumActions() {
		return this.numActions;
	}

	public int getEpisode() {
		return this.episode;
	}

	public void setEpisode(final int episode) {
		this.episode = episode;
	}

	public ReplayBuffer getReplayBuffer() {
		return this.replayBuffer;
	}

	public void setReplayBuffer(final ReplayBuffer replayBuffer) {
		this.replayBuffer = replayBuffer;
	}
}
