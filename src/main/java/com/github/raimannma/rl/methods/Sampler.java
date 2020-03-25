package com.github.raimannma.rl.methods;

import static com.github.raimannma.nn.methods.Utils.pickRandom;
import static com.github.raimannma.nn.methods.Utils.randDouble;
import java.util.ArrayList;
import java.util.TreeSet;
import com.github.raimannma.nn.methods.Utils;
import org.jetbrains.annotations.NotNull;

public abstract class Sampler {
	public Sampler() {

	}

	public abstract Experience select(TreeSet<Experience> experiences);

	public static final class RandomSampler extends Sampler {
		@Override
		public Experience select(final TreeSet<Experience> experiences) {
			return pickRandom(experiences);
		}
	}

	public static final class PrioritisedSampler extends Sampler {
		private final double alpha;

		public PrioritisedSampler(final double alpha) {
			this.alpha = alpha;
		}

		@Override
		public Experience select(final TreeSet<Experience> experiences) {

			final double denominator = experiences.stream()
				.mapToDouble(exp -> Math.abs(Math.pow(exp.tdError, this.alpha)))
				.sum();

			double sum = 0;
			final double rand = randDouble();
			for (final Experience experience : experiences) {
				sum += Math.abs(Math.pow(experience.tdError, this.alpha)) / denominator;
				if (rand < sum) {
					return experience;
				}
			}

			return this.select(experiences);
		}
	}

	public final static class FitnessProportionateSampler extends Sampler {
		@Override
		public Experience select(final @NotNull TreeSet<Experience> experiences) {
			final double minimalFitness = experiences.stream().mapToDouble(experience -> experience.tdError).min().orElseThrow();
			final double totalFitness = experiences.stream().mapToDouble(experience -> experience.tdError).sum();

			final double random = Utils.randDouble(totalFitness + minimalFitness * experiences.size());
			double value = 0;
			for (final Experience experience : experiences) {
				value += experience.tdError + minimalFitness;
				if (random < value) {
					return experience;
				}
			}
			return Utils.pickRandom(experiences);
		}
	}

	public final static class PowerSampler extends Sampler {
		private final int power;

		public PowerSampler() {
			this(4);
		}

		public PowerSampler(final int power) {
			this.power = Math.max(0, power);
		}

		@Override
		public Experience select(final @NotNull TreeSet<Experience> experiences) {
			final ArrayList<Experience> list = new ArrayList<>(experiences);
			list.sort((o1, o2) -> Double.compare(o2.tdError, o1.tdError));
			return list.get((int) Math.floor(Math.pow(Utils.randDouble(), this.power) * list.size()));
		}
	}

	public final static class TournamentSampler extends Sampler {
		private final int size;

		public TournamentSampler(final int size) {
			this.size = size;
		}

		@Override
		public Experience select(final @NotNull TreeSet<Experience> experiences) {
			Experience best = null;
			for (int i = 0; i < Math.min(experiences.size(), this.size); i++) {
				final Experience experience = pickRandom(experiences);
				if (best == null || experience.tdError > best.tdError) {
					best = experience;
				}
			}
			return best;
		}
	}
}
