package com.github.raimannma.rl.methods;

import org.jetbrains.annotations.NotNull;

public class Experience implements Comparable<Experience> {
	private double tdError;
	private double[] lastState;
	private int lastAction;
	private double[] state;
	private int action;
	private int episode;
	private double lastReward;

	public Experience(final int episode, final double[] lastState, final int lastAction, final double lastReward, final double[] state, final int action) {
		this(episode, lastState, lastAction, lastReward, state, action, 0);
	}

	public Experience(final int episode, final double[] lastState, final int lastAction, final double lastReward, final double[] state, final int action, final double tdError) {
		this.episode = episode;
		this.tdError = tdError;
		this.lastState = lastState;
		this.lastAction = lastAction;
		this.lastReward = lastReward;
		this.state = state;
		this.action = action;
	}

	public int getEpisode() {
		return this.episode;
	}

	public void setEpisode(final int episode) {
		this.episode = episode;
	}

	public double getTdError() {
		return this.tdError;
	}

	public void setTdError(final double tdError) {
		this.tdError = tdError;
	}

	public double[] getLastState() {
		return this.lastState;
	}

	public void setLastState(final double[] lastState) {
		this.lastState = lastState;
	}

	public int getLastAction() {
		return this.lastAction;
	}

	public void setLastAction(final int lastAction) {
		this.lastAction = lastAction;
	}

	public double[] getState() {
		return this.state;
	}

	public void setState(final double[] state) {
		this.state = state;
	}

	public int getAction() {
		return this.action;
	}

	public void setAction(final int action) {
		this.action = action;
	}

	public double getLastReward() {
		return this.lastReward;
	}

	public void setLastReward(final double lastReward) {
		this.lastReward = lastReward;
	}

	@Override
	public int compareTo(@NotNull final Experience o) {
		return Integer.compare(this.episode, o.episode);
	}

	public Experience copy() {
		return new Experience(
			this.episode,
			this.lastState.clone(),
			this.lastAction,
			this.lastReward,
			this.state.clone(),
			this.action,
			this.tdError
		);
	}
}
