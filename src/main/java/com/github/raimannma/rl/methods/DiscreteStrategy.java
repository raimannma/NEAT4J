package com.github.raimannma.rl.methods;

import static com.github.raimannma.nn.methods.Utils.randDouble;
import static com.github.raimannma.nn.methods.Utils.randInt;

public abstract class DiscreteStrategy {
	public abstract int sample(int action, int episode, int numActions);

	public static final class EpsilonGreedy extends DiscreteStrategy {
		private final double epsilon;
		private final double epsilonDecay;
		private final double epsilonMin;

		public EpsilonGreedy(final double epsilon, final double epsilonDecay, final double epsilonMin) {
			this.epsilon = epsilon;
			this.epsilonDecay = epsilonDecay;
			this.epsilonMin = epsilonMin;
		}

		@Override
		public int sample(final int action, final int episode, final int numActions) {
			final double currentEpsilon = Math.max(this.epsilonMin, this.epsilon * Math.pow(this.epsilonDecay, episode));
			return randDouble() >= currentEpsilon
				? action
				: randInt(numActions);
		}
	}
}
