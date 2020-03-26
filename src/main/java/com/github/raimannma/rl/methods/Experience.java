package com.github.raimannma.rl.methods;

import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class Experience implements Comparable<Experience> {
	private double tdError;
	private double[] lastState;
	private int lastAction;
	private double[] state;
	private int action;
	private int episode;
	private double lastReward;

	public Experience() {
		this(0, null, 0, 0, null, 0, 0);
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
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		final Experience that = (Experience) o;
		return Double.compare(that.tdError, this.tdError) == 0 &&
				this.lastAction == that.lastAction &&
				this.action == that.action &&
				Double.compare(that.lastReward, this.lastReward) == 0 &&
				Arrays.equals(this.lastState, that.lastState) &&
				Arrays.equals(this.state, that.state);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(this.tdError, this.lastAction, this.action, this.lastReward);
		result = 31 * result + Arrays.hashCode(this.lastState);
		result = 31 * result + Arrays.hashCode(this.state);
		return result;
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
