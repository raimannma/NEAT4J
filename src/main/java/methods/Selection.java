package methods;

import static methods.Utils.pickRandom;
import static methods.Utils.randDouble;
import java.util.Comparator;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import architecture.Network;
import org.jetbrains.annotations.NotNull;

/**
 * Selection methods.
 *
 * @author Manuel Raimann
 */
public abstract class Selection {

	/**
	 * Select a genome from the population by applying a selection method.
	 *
	 * @param population the population
	 * @return the network
	 */
	public abstract Network select(List<Network> population);

	/**
	 * Fitness-Proportionate-Selection.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Fitness_proportionate_selection">Wikipedia article</a>
	 */
	public final static class FitnessProportionate extends Selection {
		@Override
		public Network select(final @NotNull List<Network> population) {
			final DoubleStream scoreStream = population.stream().mapToDouble(network -> network.score);
			final double minimalFitness = Math.abs(scoreStream.min().orElseThrow());
			final double totalFitness = scoreStream.sum();

			final double random = randDouble(totalFitness + minimalFitness * population.size());
			double value = 0;
			for (final Network genome : population) {
				value += genome.score + minimalFitness;
				if (random < value) {
					return genome;
				}
			}
			return pickRandom(population);
		}
	}

	/**
	 * Power-Selection.
	 */
	public final static class Power extends Selection {
		/**
		 * The power value.
		 * <p>
		 * Minimum: 0
		 * <p>
		 * Lower value: more random
		 * Higher value: more fitter genomes gets selected
		 */
		private final int power;

		/**
		 * Creates a new Power instance.
		 */
		public Power() {
			this(4);
		}

		/**
		 * Creates a new Power instance.
		 *
		 * @param power the power
		 */
		public Power(final int power) {
			this.power = Math.max(0, power);
		}

		@Override
		public Network select(final @NotNull List<Network> population) {
			if (population.get(0).score < population.get(1).score) {
				population.sort((o1, o2) -> Double.compare(o2.score, o1.score));
			}
			return population.get((int) Math.floor(Math.pow(randDouble(), this.power) * population.size()));
		}
	}

	/**
	 * Tournament-Selection.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Tournament_selection">Wikipedia article</a>
	 */
	public final static class Tournament extends Selection {
		/**
		 * The group size of one tournament.
		 * Minimum: 0
		 * Maximum: population size
		 */
		private final int size;

		/**
		 * Creates a new Tournament-Selection instance.
		 *
		 * @param size the size
		 */
		public Tournament(final int size) {
			this.size = size;
		}

		@Override
		public Network select(final @NotNull List<Network> population) {
			return IntStream.range(0, Math.min(population.size(), this.size))
				.mapToObj(i -> pickRandom(population))
				.max(Comparator.comparingDouble(o -> o.score))
				.orElse(pickRandom(population));
		}
	}
}
