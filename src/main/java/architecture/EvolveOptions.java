package architecture;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import methods.Loss;
import methods.Mutation;
import methods.Selection;

public class EvolveOptions {
  private ToDoubleFunction<Network> fitnessFunction;
  private int populationSize;
  private int elitism;
  private double mutationRate;
  private int mutationAmount;
  private Selection selection;
  private Mutation[] mutations;
  private Network template;
  private int maxNodes;
  private int maxConnections;
  private int maxGates;
  private boolean equal;
  private boolean clear;
  private double error;
  private double growth;
  private int amount;
  private Loss loss;
  private int iterations;
  private Network network;
  private int log;

  public EvolveOptions() {
    this.error = Double.NaN;
    this.growth = 0.0001;
    this.loss = Loss.MSE;
    this.amount = 1;
    this.iterations = -1;
    this.network = null;
    this.clear = false;
    this.populationSize = 500;
    this.elitism = 0;
    this.mutationRate = 0.3;
    this.mutationAmount = 1;
    this.selection = Selection.POWER;
    this.mutations = Mutation.FFW;
    this.template = null;
    this.maxNodes = Integer.MAX_VALUE;
    this.maxConnections = Integer.MAX_VALUE;
    this.maxGates = Integer.MAX_VALUE;
    this.fitnessFunction = null;
  }

  ToDoubleFunction<Network> getFitnessFunction() {
    return this.fitnessFunction;
  }

  void setFitnessFunction(final ToDoubleFunction<Network> fitnessFunction) {
    this.fitnessFunction = fitnessFunction;
  }

  int getLog() {
    return this.log;
  }

  public void setLog(final int log) {
    this.log = log;
  }

  int getPopulationSize() {
    return this.populationSize;
  }

  public void setPopulationSize(final int populationSize) {
    this.populationSize = populationSize;
  }

  int getElitism() {
    return this.elitism;
  }

  public void setElitism(final int elitism) {
    this.elitism = elitism;
  }

  double getMutationRate() {
    return this.mutationRate;
  }

  public void setMutationRate(final double mutationRate) {
    this.mutationRate = mutationRate;
  }

  int getMutationAmount() {
    return this.mutationAmount;
  }

  public void setMutationAmount(final int mutationAmount) {
    this.mutationAmount = mutationAmount;
  }

  Selection getSelection() {
    return this.selection;
  }

  public void setSelection(final Selection selection) {
    this.selection = selection;
  }

  Mutation[] getMutations() {
    return this.mutations;
  }

  public void setMutations(final Mutation[] mutations) {
    this.mutations = mutations;
  }

  Network getTemplate() {
    return this.template;
  }

  public void setTemplate(final Network template) {
    this.template = template;
  }

  int getMaxNodes() {
    return this.maxNodes;
  }

  public void setMaxNodes(final int maxNodes) {
    this.maxNodes = maxNodes;
  }

  int getMaxConnections() {
    return this.maxConnections;
  }

  public void setMaxConnections(final int maxConnections) {
    this.maxConnections = maxConnections;
  }

  int getMaxGates() {
    return this.maxGates;
  }

  public void setMaxGates(final int maxGates) {
    this.maxGates = maxGates;
  }

  boolean isEqual() {
    return this.equal;
  }

  public void setEqual(final boolean equal) {
    this.equal = equal;
  }

  double getError() {
    return this.error;
  }

  public void setError(final double error) {
    this.error = error;
  }

  double getGrowth() {
    return this.growth;
  }

  public void setGrowth(final double growth) {
    this.growth = growth;
  }

  int getAmount() {
    return this.amount;
  }

  public void setAmount(final int amount) {
    this.amount = amount;
  }

  boolean isClear() {
    return this.clear;
  }

  Loss getLoss() {
    return this.loss;
  }

  public void setLoss(final Loss loss) {
    this.loss = loss;
  }

  int getIterations() {
    return this.iterations;
  }

  void setIterations(final int iterations) {
    this.iterations = iterations;
  }

  public Network getNetwork() {
    return this.network;
  }

  void setNetwork(final Network network) {
    this.network = network;
  }

  boolean getClear() {
    return this.clear;
  }

  public void setClear(final boolean clear) {
    this.clear = clear;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.mutations)
      + 31 * Objects.hash(this.fitnessFunction, this.populationSize, this.elitism, this.mutationRate, this.mutationAmount, this.selection, this.template, this.maxNodes, this.maxConnections, this.maxGates, this.equal, this.clear, this.error, this.growth, this.amount, this.loss, this.iterations, this.network, this.log);
  }
}
