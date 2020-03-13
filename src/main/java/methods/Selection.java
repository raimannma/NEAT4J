package methods;

import static methods.Utils.pickRandom;
import static methods.Utils.randDouble;
import java.util.List;
import java.util.stream.IntStream;
import architecture.Network;

public abstract class Selection {

  public abstract Network select(List<Network> population);

  public final static class FitnessProportionate extends Selection {
    @Override
    public Network select(final List<Network> population) {
      double totalFitness = 0;
      double minimalFitness = 0;
      for (final Network network : population) {
        minimalFitness = Math.min(network.score, minimalFitness);
        totalFitness += network.score;
      }

      minimalFitness = Math.abs(minimalFitness);
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

  public final static class Power extends Selection {
    private final int power;

    public Power() {
      this(4);
    }

    public Power(final int power) {
      this.power = power;
    }

    @Override
    public Network select(final List<Network> population) {
      if (population.get(0).score < population.get(1).score) {
        population.sort((o1, o2) -> Double.compare(o2.score, o1.score));
      }
      final int index = (int) Math.floor(Math.pow(randDouble(), this.power) * population.size());
      return population.get(index);
    }
  }

  public final static class Tournament extends Selection {
    private final double probability;
    private final int size;

    public Tournament(final int size, final double probability) {
      this.size = size;
      this.probability = probability;
    }

    @Override
    public Network select(final List<Network> population) {
      return IntStream.range(0, Math.min(population.size(), this.size))
        .mapToObj(i -> pickRandom(population))
        .sorted((o1, o2) -> Double.compare(o2.score, o1.score))
        .filter(net -> randDouble() < this.probability)
        .findFirst()
        .orElse(pickRandom(population));
    }
  }
}
