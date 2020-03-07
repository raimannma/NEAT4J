package architecture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import methods.Mutation;
import methods.Selection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static methods.Utils.pickRandom;
import static methods.Utils.randDouble;

class NEAT {
    private final int output;
    private final int input;
    private final ToDoubleFunction<Network> fitnessFunction;
    private final boolean equal;
    private final boolean clear;
    private final double mutationRate;
    private final int mutationAmount;
    private final int provenance;
    private final int elitism;
    private final int maxGates;
    private final int maxConns;
    private final int maxNodes;
    private final Network template;
    private final Mutation[] mutation;
    private final Selection selection;
    int generation;
    List<Network> population;
    private int popSize;

    NEAT(final int input, final int output, final EvolveOptions options) {
        this.input = input;
        this.output = output;

        this.fitnessFunction = options.getFitnessFunction();
        this.equal = options.isEqual();
        this.clear = options.getClear();
        this.popSize = options.getPopulationSize();
        this.elitism = options.getElitism();
        this.provenance = options.getProvenance();
        this.mutationRate = options.getMutationRate();
        this.mutationAmount = options.getMutationAmount();
        this.selection = options.getSelection();
        this.mutation = options.getMutations();
        this.template = options.getTemplate();
        this.maxNodes = options.getMaxNodes();
        this.maxConns = options.getMaxConns();
        this.maxGates = options.getMaxGates();

        this.generation = 0;

        this.createPool(this.template);
    }

    private void createPool(final Network template) {
        this.population = new ArrayList<>();
        for (int i = 0; i < this.popSize; i++) {
            final Network copy = template != null ? Network.fromJSON(template.toJSON()) : new Network(this.input, this.output);
            copy.score = Double.NaN;
            this.population.add(copy);
        }
    }

    Network evolve() {
        if (Double.isNaN(this.population.get(this.population.size() - 1).score)) {
            this.evaluate();
        }
        this.sort();
        final Network fittest = Network.fromJSON(this.population.get(0).toJSON());
        fittest.score = this.population.get(0).score;

        final List<Network> elitists = this.population.subList(0, this.elitism);

        final List<Network> newPopulation = IntStream.range(0, this.provenance)
                .mapToObj(i -> Network.fromJSON(this.template.toJSON()))
                .collect(Collectors.toList());

        IntStream.range(0, this.popSize - this.elitism - this.provenance)
                .mapToObj(value -> new Network[]{NEAT.this.getParent(), NEAT.this.getParent()})
                .sequential()
                .map(this::getOffspring)
                .forEach(newPopulation::add);

        this.population = newPopulation;
        this.mutate();

        this.population.addAll(elitists);

        this.population.parallelStream().forEach(network -> network.score = Double.NaN);

        this.generation++;
        return fittest;
    }

    private void evaluate() {
        if (this.clear) {
            this.population.forEach(Network::clear);
        }
        this.population.forEach(genome -> genome.score = this.fitnessFunction.applyAsDouble(genome));
    }

    private void sort() {
        this.population.sort((o1, o2) -> Double.compare(o2.score, o1.score));
    }

    private Network getParent() {
        switch (this.selection) {
            case POWER:
                if (this.population.get(0).score < this.population.get(1).score) {
                    this.population.sort((o1, o2) -> Double.compare(o2.score, o1.score));
                }
                return this.population.get((int) Math.floor(Math.pow(Math.random(), this.selection.power) * this.population.size()));
            case FITNESS_PROPORTIONATE:
                double totalFitness = 0;
                double minimalFitness = 0;
                for (final Network network : this.population) {
                    minimalFitness = Math.min(network.score, minimalFitness);
                    totalFitness += network.score;
                }

                minimalFitness = Math.abs(minimalFitness);
                final double random = randDouble(totalFitness + minimalFitness * this.population.size());
                double value = 0;
                for (final Network genome : this.population) {
                    value += genome.score + minimalFitness;
                    if (random < value) {
                        return genome;
                    }
                }
                return pickRandom(this.population);
            case TOURNAMENT:
                if (this.selection.size > this.popSize) {
                    throw new RuntimeException("Your tournament size should be lower than the population size, please change methods.selection.TOURNAMENT.size");
                }
                return IntStream.range(0, this.selection.size)
                        .mapToObj(i -> pickRandom(this.population))
                        .sorted((o1, o2) -> Double.compare(o2.score, o1.score))
                        .filter(net -> Math.random() < this.selection.probability)
                        .findAny()
                        .orElse(this.population.get(0));
        }
        return this.population.get(0);
    }

    private Network getOffspring(final Network[] parents) {
        return Network.crossover(parents[0], parents[1], this.equal);
    }

    private void mutate() {
        this.population.stream()
                .filter(network -> Math.random() <= this.mutationRate)
                .forEach(network -> IntStream.range(0, this.mutationAmount)
                        .forEach(j -> network.mutate(this.selectMutationMethod(network))));
    }

    private Mutation selectMutationMethod(final Network genome) {
        final Mutation mutationMethod = pickRandom(this.mutation);

        return (mutationMethod != Mutation.ADD_NODE || genome.nodes.size() < this.maxNodes)
                && (mutationMethod != Mutation.ADD_CONN || genome.connections.size() < this.maxConns)
                && (mutationMethod != Mutation.ADD_GATE || genome.gates.size() < this.maxGates)
                ? mutationMethod
                : null;
    }

    Network getFittest() {
        this.evaluate();
        this.sort();
        return this.population.get(0);
    }

    double getAverage() {
        if (Double.isNaN(this.population.get(this.population.size() - 1).score)) {
            this.evaluate();
        }
        return this.population.stream().mapToDouble(network -> network.score).average().orElseThrow();
    }

    JsonObject toJSON() {
        final JsonArray jsonArray = new JsonArray();
        this.population.stream().map(Network::toJSON).forEach(jsonArray::add);
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("genomes", jsonArray);
        return jsonObject;
    }

    void fromJSON(final JsonObject jsonObject) {
        final JsonArray arr = jsonObject.get("genomes").getAsJsonArray();
        IntStream.range(0, arr.size())
                .forEach(i -> this.population.add(Network.fromJSON(arr.get(i).getAsJsonObject())));
        this.popSize = this.population.size();
    }

}
