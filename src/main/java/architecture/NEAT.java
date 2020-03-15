package architecture;

import static methods.Utils.pickRandom;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import methods.Mutation;
import methods.Selection;
import methods.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NEAT {
  private final int output;
  private final int input;
  private final ToDoubleFunction<Network> fitnessFunction;
  private final boolean equal;
  private final boolean clear;
  private final double mutationRate;
  private final int mutationAmount;
  private final int elitism;
  private final int maxGates;
  private final int maxConnections;
  private final int maxNodes;
  private final Network template;
  private final Mutation[] mutation;
  private final Selection selection;
  public int generation;
  public List<Network> population;
  private int populationSize;

  NEAT(final int input, final int output, final @NotNull EvolveOptions options) {
    this.input = input;
    this.output = output;

    this.fitnessFunction = options.getFitnessFunction();
    this.equal = options.isEqual();
    this.clear = options.isClear();
    this.populationSize = options.getPopulationSize();
    this.elitism = options.getElitism();
    this.mutationRate = options.getMutationRate();
    this.mutationAmount = options.getMutationAmount();
    this.selection = options.getSelection();
    this.mutation = options.getMutations();
    this.template = options.getTemplate();
    this.maxNodes = options.getMaxNodes();
    this.maxConnections = options.getMaxConnections();
    this.maxGates = options.getMaxGates();

    this.generation = 0;

    this.createPool(this.template);
  }

  private void createPool(final Network template) {
    this.population = new ArrayList<>();
    for (int i = 0; i < this.populationSize; i++) {
      final Network copy = template != null
        ? template.copy()
        : new Network(this.input, this.output);
      copy.score = Double.NaN;
      this.population.add(copy);
    }
  }

  public Network evolve() {
    if (Double.isNaN(this.population.get(this.population.size() - 1).score)) {
      this.evaluate();
    }
    this.sort();
    final Network fittest = this.population.get(0).copy();
    fittest.score = this.population.get(0).score;

    final List<Network> elitists = this.population.subList(0, this.elitism);
    final Set<Network> newPopulation = new HashSet<>();
    while (newPopulation.size() < this.populationSize - this.elitism) {
      newPopulation.add(Network.crossover(this.getParent(), this.getParent(), this.equal));
    }

    this.population = new ArrayList<>(newPopulation);
    this.mutate();

    this.population.addAll(elitists);

    this.population.forEach(network -> network.score = Double.NaN);

    this.generation++;
    return fittest;
  }

  private void evaluate() {
    if (this.clear) {
      this.population.forEach(Network::clear);
    }
    this.population
      .parallelStream()
      .forEach(genome -> genome.score = this.fitnessFunction.applyAsDouble(genome));
  }

  private void sort() {
    this.population.sort((o1, o2) -> Double.compare(o2.score, o1.score));
  }

  private Network getParent() {
    return this.selection.select(this.population);
  }

  private void mutate() {
    for (final Network network : this.population) {
      if (Utils.randDouble() <= this.mutationRate) {
        for (int j = 0; j < this.mutationAmount; j++) {
          network.mutate(this.selectMutationMethod(network));
        }
      }
    }
  }

  private @Nullable Mutation selectMutationMethod(final Network genome) {
    final Mutation mutationMethod = pickRandom(this.mutation);

    return (mutationMethod != Mutation.ADD_NODE || genome.nodes.size() < this.maxNodes)
      && (mutationMethod != Mutation.ADD_CONN || genome.connections.size() < this.maxConnections)
      && (mutationMethod != Mutation.ADD_GATE || genome.gates.size() < this.maxGates)
      ? mutationMethod
      : null;
  }

  public Network getFittest() {
    this.evaluate();
    this.sort();
    return this.population.get(0);
  }

  public double getAverage() {
    if (Double.isNaN(this.population.get(this.population.size() - 1).score)) {
      this.evaluate();
    }
    return this.population.stream().mapToDouble(network -> network.score).average().orElseThrow();
  }

  public JsonObject toJson() {
    final JsonArray jsonArray = new JsonArray();
    this.population.stream().map(Network::toJSON).forEach(jsonArray::add);
    final JsonObject jsonObject = new JsonObject();
    jsonObject.add("genomes", jsonArray);
    return jsonObject;
  }

  public void fromJson(final @NotNull JsonObject jsonObject) {
    final JsonArray arr = jsonObject.get("genomes").getAsJsonArray();
    IntStream.range(0, arr.size())
      .forEach(i -> this.population.add(Network.fromJSON(arr.get(i).getAsJsonObject())));
    this.populationSize = this.population.size();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.mutation)
      + 31 * Objects.hash(this.output, this.input, this.fitnessFunction, this.equal, this.clear, this.mutationRate, this.mutationAmount, this.elitism, this.maxGates, this.maxConnections, this.maxNodes, this.template, this.selection, this.generation, this.population, this.populationSize);
  }
}
