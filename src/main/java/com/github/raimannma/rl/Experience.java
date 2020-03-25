package com.github.raimannma.rl;

import org.jetbrains.annotations.NotNull;

public class Experience implements Comparable<Experience> {
	protected final int generation;
	public double tdError;
	protected double[] lastState;
	protected int lastAction;
	protected double lastReward;
	protected double[] state;
	protected int action;

	public Experience(final int generation, final double[] lastState, final int lastAction, final double lastReward, final double[] state, final int action) {
		this(generation, lastState, lastAction, lastReward, state, action, 0);
	}

	public Experience(final int generation, final double[] lastState, final int lastAction, final double lastReward, final double[] state, final int action, final double tdError) {
		this.generation = generation;
		this.tdError = tdError;
		this.lastState = lastState;
		this.lastAction = lastAction;
		this.lastReward = lastReward;
		this.state = state;
		this.action = action;
	}

	@Override
	public int compareTo(@NotNull final Experience o) {
		return Integer.compare(this.generation, o.generation);
	}
}
