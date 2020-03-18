package architecture;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import methods.Loss;
import methods.Mutation;
import methods.Selection;

/**
 * The type Evolve options.
 *
 * @author Manuel Raimann
 */
public class EvolveOptions {
  /**
   * The Fitness function.
   */
  private ToDoubleFunction<Network> fitnessFunction;
  /**
   * The Population size.
   */
  private int populationSize;
  /**
   * The Elitism.
   */
  private int elitism;
  /**
   * The Mutation rate.
   */
  private double mutationRate;
  /**
   * The Mutation amount.
   */
  private int mutationAmount;
  /**
   * The Selection.
   */
  private Selection selection;
  /**
   * The Mutations.
   */
  private Mutation[] mutations;
  /**
   * The Template.
   */
  private Network template;
  /**
   * The Max nodes.
   */
  private int maxNodes;
  /**
   * The Max connections.
   */
  private int maxConnections;
  /**
   * The Max gates.
   */
  private int maxGates;
  /**
   * The Equal.
   */
  private boolean equal;
  /**
   * The Clear.
   */
  private boolean clear;
  /**
   * The Error.
   */
  private double error;
  /**
   * The Growth.
   */
  private double growth;
  /**
   * The Amount.
   */
  private int amount;
  /**
   * The Loss.
   */
  private Loss loss;
  /**
   * The Iterations.
   */
  private int iterations;
  /**
   * The Network.
   */
  private Network network;
  /**
   * The Log.
   */
  private int log;

  /**
   * Instantiates a new Evolve options.
   */
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
    this.selection = new Selection.Power();
    this.mutations = Mutation.FFW;
    this.template = null;
    this.maxNodes = Integer.MAX_VALUE;
    this.maxConnections = Integer.MAX_VALUE;
    this.maxGates = Integer.MAX_VALUE;
    this.fitnessFunction = null;
  }

  /**
   * Gets fitness function.
   *
   * @return the fitness function
   */
  public ToDoubleFunction<Network> getFitnessFunction() {
    return this.fitnessFunction;
  }

  /**
   * Sets fitness function.
   *
   * @param fitnessFunction the fitness function
   */
  public void setFitnessFunction(final ToDoubleFunction<Network> fitnessFunction) {
    this.fitnessFunction = fitnessFunction;
  }

  /**
   * Gets population size.
   *
   * @return the population size
   */
  public int getPopulationSize() {
    return this.populationSize;
  }

  /**
   * Sets population size.
   *
   * @param populationSize the population size
   */
  public void setPopulationSize(final int populationSize) {
    this.populationSize = populationSize;
  }

  /**
   * Gets elitism.
   *
   * @return the elitism
   */
  public int getElitism() {
    return this.elitism;
  }

  /**
   * Sets elitism.
   *
   * @param elitism the elitism
   */
  public void setElitism(final int elitism) {
    this.elitism = elitism;
  }

  /**
   * Gets mutation rate.
   *
   * @return the mutation rate
   */
  public double getMutationRate() {
    return this.mutationRate;
  }

  /**
   * Sets mutation rate.
   *
   * @param mutationRate the mutation rate
   */
  public void setMutationRate(final double mutationRate) {
    this.mutationRate = mutationRate;
  }

  /**
   * Gets mutation amount.
   *
   * @return the mutation amount
   */
  public int getMutationAmount() {
    return this.mutationAmount;
  }

  /**
   * Sets mutation amount.
   *
   * @param mutationAmount the mutation amount
   */
  public void setMutationAmount(final int mutationAmount) {
    this.mutationAmount = mutationAmount;
  }

  /**
   * Gets selection.
   *
   * @return the selection
   */
  public Selection getSelection() {
    return this.selection;
  }

  /**
   * Sets selection.
   *
   * @param selection the selection
   */
  public void setSelection(final Selection selection) {
    this.selection = selection;
  }

  /**
   * Get mutations.
   *
   * @return the mutations
   */
  public Mutation[] getMutations() {
    return this.mutations;
  }

  /**
   * Sets mutations.
   *
   * @param mutations the mutations
   */
  public void setMutations(final Mutation[] mutations) {
    this.mutations = mutations;
  }

  /**
   * Gets template.
   *
   * @return the template
   */
  public Network getTemplate() {
    return this.template;
  }

  /**
   * Sets template.
   *
   * @param template the template
   */
  public void setTemplate(final Network template) {
    this.template = template;
  }

  /**
   * Gets max nodes.
   *
   * @return the max nodes
   */
  public int getMaxNodes() {
    return this.maxNodes;
  }

  /**
   * Sets max nodes.
   *
   * @param maxNodes the max nodes
   */
  public void setMaxNodes(final int maxNodes) {
    this.maxNodes = maxNodes;
  }

  /**
   * Gets max connections.
   *
   * @return the max connections
   */
  public int getMaxConnections() {
    return this.maxConnections;
  }

  /**
   * Sets max connections.
   *
   * @param maxConnections the max connections
   */
  public void setMaxConnections(final int maxConnections) {
    this.maxConnections = maxConnections;
  }

  /**
   * Gets max gates.
   *
   * @return the max gates
   */
  public int getMaxGates() {
    return this.maxGates;
  }

  /**
   * Sets max gates.
   *
   * @param maxGates the max gates
   */
  public void setMaxGates(final int maxGates) {
    this.maxGates = maxGates;
  }

  /**
   * Is equal.
   *
   * @return the boolean
   */
  public boolean isEqual() {
    return this.equal;
  }

  /**
   * Sets equal.
   *
   * @param equal the equal
   */
  public void setEqual(final boolean equal) {
    this.equal = equal;
  }

  /**
   * Is clear.
   *
   * @return the boolean
   */
  public boolean isClear() {
    return this.clear;
  }

  /**
   * Sets clear.
   *
   * @param clear the clear
   */
  public void setClear(final boolean clear) {
    this.clear = clear;
  }

  /**
   * Gets error.
   *
   * @return the error
   */
  public double getError() {
    return this.error;
  }

  /**
   * Sets error.
   *
   * @param error the error
   */
  public void setError(final double error) {
    this.error = error;
  }

  /**
   * Gets growth.
   *
   * @return the growth
   */
  public double getGrowth() {
    return this.growth;
  }

  /**
   * Sets growth.
   *
   * @param growth the growth
   */
  public void setGrowth(final double growth) {
    this.growth = growth;
  }

  /**
   * Gets amount.
   *
   * @return the amount
   */
  public int getAmount() {
    return this.amount;
  }

  /**
   * Sets amount.
   *
   * @param amount the amount
   */
  public void setAmount(final int amount) {
    this.amount = amount;
  }

  /**
   * Gets loss.
   *
   * @return the loss
   */
  public Loss getLoss() {
    return this.loss;
  }

  /**
   * Sets loss.
   *
   * @param loss the loss
   */
  public void setLoss(final Loss loss) {
    this.loss = loss;
  }

  /**
   * Gets iterations.
   *
   * @return the iterations
   */
  public int getIterations() {
    return this.iterations;
  }

  /**
   * Sets iterations.
   *
   * @param iterations the iterations
   */
  public void setIterations(final int iterations) {
    this.iterations = iterations;
  }

  /**
   * Gets network.
   *
   * @return the network
   */
  public Network getNetwork() {
    return this.network;
  }

  /**
   * Sets network.
   *
   * @param network the network
   */
  public void setNetwork(final Network network) {
    this.network = network;
  }

  /**
   * Gets log.
   *
   * @return the log
   */
  public int getLog() {
    return this.log;
  }

  /**
   * Sets log.
   *
   * @param log the log
   */
  public void setLog(final int log) {
    this.log = log;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.mutations)
      + 31 * Objects.hash(this.fitnessFunction, this.populationSize, this.elitism, this.mutationRate, this.mutationAmount, this.selection, this.template, this.maxNodes, this.maxConnections, this.maxGates, this.equal, this.clear, this.error, this.growth, this.amount, this.loss, this.iterations, this.network, this.log);
  }
}
