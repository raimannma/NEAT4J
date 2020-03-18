package architecture;

import static methods.Mutation.SUB_NODE;
import static methods.Utils.pickRandom;
import static methods.Utils.randBoolean;
import static methods.Utils.randDouble;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import methods.Loss;
import methods.Mutation;
import org.jetbrains.annotations.NotNull;

/**
 * The type Network.
 */
public class Network implements Cloneable {
  /**
   * The Input size.
   */
  public final int input;
  /**
   * The Output size.
   */
  public final int output;
  /**
   * The Nodes of the network.
   */
  public List<Node> nodes;
  /**
   * The Score.
   * used for NEAT.
   */
  public double score;
  /**
   * The Connections of the network.
   */
  public List<Connection> connections;
  /**
   * The Self connections of the network.
   */
  public List<Connection> selfConnections;
  /**
   * The Gates of the network.
   */
  public List<Connection> gates;
  /**
   * The Dropout probability.
   */
  private double dropout;

  /**
   * Instantiates a new Network.
   *
   * @param input  the input size
   * @param output the output size
   */
  public Network(final int input, final int output) {
    this.input = input;
    this.output = output;

    this.score = Double.NaN;

    this.nodes = new ArrayList<>();
    this.connections = new ArrayList<>();
    this.gates = new ArrayList<>();
    this.selfConnections = new ArrayList<>();

    this.dropout = 0;
    IntStream.range(0, this.input + this.output)
      .mapToObj(i -> new Node(i < input ? Node.NodeType.INPUT : Node.NodeType.OUTPUT))
      .forEach(this.nodes::add);

    // Create simplest Network with input and output size matching parameters
    final double initWeight = this.input * Math.sqrt((double) 2 / this.input);
    for (int i = 0; i < this.input; i++) { // iterate over the input nodes
      final Node inputNode = this.nodes.get(i);
      for (int j = this.input; j < this.output + this.input; j++) { // iterate over the output nodes
        // connect input and output node
        this.connect(inputNode, this.nodes.get(j), randDouble(initWeight));
      }
    }
  }

  /**
   * Create an offspring from two parent networks.
   * <p>
   * Networks are not required to have the same size, however input and output size should be the same!
   *
   * @param network1 parent network 1
   * @param network2 parent network 2
   * @param equal    indicate that networks are equally fit
   * @return new network created from mixing parent networks
   */
  public static @NotNull Network crossover(final @NotNull Network network1, final @NotNull Network network2, final boolean equal) {
    if (network1.input != network2.input || network1.output != network2.output) {
      // Networks must have same input/output sizes
      throw new IllegalStateException("Networks don't have the same input/output size!");
    }

    // create offspring
    final Network offspring = new Network(network1.input, network1.output);
    offspring.connections = new ArrayList<>();
    offspring.nodes = new ArrayList<>();

    final double score1 = Double.isNaN(network1.score) ? -Double.MAX_VALUE : network1.score;
    final double score2 = Double.isNaN(network2.score) ? -Double.MAX_VALUE : network2.score;

    // Select size of fittest, if they aren't equal.
    final int size; // size of offspring
    final int size1 = network1.nodes.size(); // num nodes of parent 1
    final int size2 = network2.nodes.size(); // num nodes of parent 2
    if (equal || score1 == score2) {
      final int max = Math.max(size1, size2); // max size
      final int min = Math.min(size1, size2); // min size
      size = (int) Math.floor(randDouble() * (max - min + 1) + min); // select random size between min and max
    } else if (score1 > score2) {
      size = size1;
    } else {
      size = size2;
    }

    network1.setNodeIndices(); // set indices for network 1
    network2.setNodeIndices(); // set indices for network 2

    // Create nodes for the offspring
    for (int i = 0; i < size; i++) {
      Node node; // first choice for the new node
      final Node other; // second choice for the new node
      if (i < size - network1.output) {
        if (randBoolean()) { // choose random
          node = i < size1 ? network1.nodes.get(i) : null;
          other = i < size2 ? network2.nodes.get(i) : null;
        } else {
          node = i < size2 ? network2.nodes.get(i) : null;
          other = i < size1 ? network1.nodes.get(i) : null;
        }
        if (node == null || node.type == Node.NodeType.OUTPUT) {
          node = other;
        }
      } else {
        node = randBoolean()
          ? network1.nodes.get(i + size1 - size)
          : network2.nodes.get(i + size2 - size);
      }

      final Node newNode = new Node();
      newNode.bias = node.bias;
      newNode.activationType = node.activationType;
      newNode.type = node.type;

      // add node to offspring
      offspring.nodes.add(newNode);
    }

    // Maps for offspring connections
    final Map<Integer, Double[]> network1Connections = makeConnections(network1);
    final Map<Integer, Double[]> network2Connections = makeConnections(network2);

    final List<Double[]> connections = new ArrayList<>();

    // List of innovation IDs from both parents
    final List<Integer> innovationIDs1 = new ArrayList<>(network1Connections.keySet());
    final List<Integer> innovationIDs2 = new ArrayList<>(network2Connections.keySet());

    for (int i = innovationIDs1.size() - 1; i >= 0; i--) {
      if (network2Connections.get(innovationIDs1.get(i)) != null) {
        //Choose random connection out of both networks
        connections.add(randBoolean()
          ? network1Connections.get(innovationIDs1.get(i))
          : network2Connections.get(innovationIDs1.get(i)));

        // set to null, because removing is expensive
        network2Connections.put(innovationIDs1.get(i), null);
      } else if (score1 >= score2 || equal) {
        // choose connection from better network, or if they are equal
        connections.add(network1Connections.get(innovationIDs1.get(i)));
      }
    }

    // Excess/disjoint gene
    if (score2 >= score1 || equal) {
      innovationIDs2.stream()
        .map(network2Connections::get)
        .filter(Objects::nonNull)
        .forEach(connections::add);
    }

    // Add common conn genes uniformly
    connections.stream()
      .filter(connectionData -> connectionData[1] < size && connectionData[2] < size)
      .forEach(connectionData -> {
        // ConnectionData
        // [0] -> weight value
        // [1] -> node from index
        // [2] -> node to index
        // [3] -> gate node index
        final Connection connection = offspring.connect(
          offspring.nodes.get((int) (double) connectionData[1]),
          offspring.nodes.get((int) (double) connectionData[2])
        );
        connection.weight = connectionData[0];
        if (!Double.isNaN(connectionData[3]) && connectionData[3] < size) {
          offspring.gate(offspring.nodes.get((int) (double) connectionData[3]), connection);
        }
      });
    return offspring;
  }

  /**
   * Create a connections map with InnovationID as key and ConnectionData as value.
   *
   * @param network the network
   * @return the resulting map
   */
  private static @NotNull Map<Integer, Double[]> makeConnections(final @NotNull Network network) {
    final Map<Integer, Double[]> connections = new HashMap<>();
    Stream.concat(network.connections.stream(), network.selfConnections.stream()) // create stream with all connections
      .forEach(connection -> {
        // ConnectionData
        // [0] -> weight value
        // [1] -> node from index
        // [2] -> node to index
        // [3] -> gate node index
        final Double[] data = new Double[4];
        data[0] = connection.weight;
        data[1] = (double) connection.from.index;
        data[2] = (double) connection.to.index;
        data[3] = connection.gateNode == null ? Double.NaN : connection.gateNode.index;
        connections.put(Connection.getInnovationID(connection.from.index, connection.to.index), data);
      });
    return connections;
  }

  /**
   * Convert a json object to a network
   *
   * @param json A network represented as a JsonObject
   * @return the network created out of the JsonObject
   */
  public static @NotNull Network fromJSON(final @NotNull JsonObject json) {
    // create default network with input and output size from json
    final Network network = new Network(json.get("input").getAsInt(), json.get("output").getAsInt());
    network.dropout = json.get("dropout").getAsDouble(); // set dropout probability
    network.score = json.get("score").getAsDouble(); // set score value

    // add nodes and connections to the network
    final JsonArray nodes = json.get("nodes").getAsJsonArray();
    final JsonArray connections = json.get("connections").getAsJsonArray();
    nodes.forEach(jsonNode -> network.nodes.add(Node.fromJSON(jsonNode.getAsJsonObject())));
    for (int i = 0; i < connections.size(); i++) {
      final JsonObject connJSON = connections.get(i).getAsJsonObject(); // get connection json
      // create connection by connecting "from" and "to" node
      final Connection connection = network.connect(
        network.nodes.get(connJSON.get("from").getAsInt()), // get connection input node
        network.nodes.get(connJSON.get("to").getAsInt()) // get connection output node
      );
      connection.weight = connJSON.get("weight").getAsDouble(); // set connection weight

      if (connJSON.has("gateNode") && connJSON.get("gateNode").getAsInt() != -1) {
        network.gate(network.nodes.get(connJSON.get("gateNode").getAsInt()), connection);
      }
    }
    return network;
  }

  /**
   * Connect two nodes with given weight.
   *
   * @param from   the connection input node
   * @param to     the connection output node
   * @param weight the connection weight
   * @return the created connection
   */
  private Connection connect(final @NotNull Node from, final Node to, final double weight) {
    final Connection connection = from.connect(to, weight); // connect from with to
    if (from.equals(to)) {
      // if from equals to
      // add connection to self connections
      this.selfConnections.add(connection);
    } else {
      // if from unequals to
      // add connection to connections
      this.connections.add(connection);
    }
    // return created connection
    return connection;
  }

  /**
   * Connect two nodes with weight equals 0.
   *
   * @param from the connection input node
   * @param to   the connection output node
   * @return the created connection
   */
  public Connection connect(final Node from, final Node to) {
    return this.connect(from, to, 0);
  }

  /**
   * Sets node indices.
   */
  private void setNodeIndices() {
    // node index equals to position in this.nodes list
    IntStream.range(0, this.nodes.size()).forEach(i -> this.nodes.get(i).index = i);
  }

  /**
   * Makes a network node gate a connection
   *
   * @param node       the gating node
   * @param connection the connection to gate with node
   */
  public void gate(final Node node, final Connection connection) {
    if (!this.nodes.contains(node)) {
      throw new ArrayIndexOutOfBoundsException("This node is not part of the network!");
    } else if (connection.gateNode == null) {
      // only gate when connection isn't gated already
      node.gate(connection); // gate node-level
      this.gates.add(connection); // add connection to gates list
    }
  }

  /**
   * Mutates the network with the given method.
   *
   * @param method the mutation method
   */
  public void mutate(final Mutation method) {
    if (Arrays.stream(Mutation.ALL).noneMatch(meth -> meth == method)) {
      throw new IllegalArgumentException("No correct mutate method given!");
    }
    method.mutate(this);
  }

  /**
   * Evolve double.
   *
   * @param inputs  the inputs
   * @param outputs the outputs
   * @return the double
   */
  private double evolve(final double[][] inputs, final double[][] outputs) {
    return this.evolve(inputs, outputs, new EvolveOptions());
  }

  /**
   * Calculates growth score of this network.
   *
   * @param growth the growth rate
   * @return the growth score
   */
  private double getGrowthScore(final double growth) {
    return growth * (this.nodes.size()
      + this.connections.size()
      + this.gates.size()
      - this.input
      - this.output);
  }

  /**
   * Test the network on dataset.
   *
   * @param inputs  the inputs of the dataset
   * @param outputs the outputs of the dataset
   * @return the error
   */
  public double test(final double[][] inputs, final double[][] outputs) {
    return this.test(inputs, outputs, Loss.MSE);
  }

  /**
   * Test the network on dataset with given loss function.
   *
   * @param inputs  the inputs of the dataset
   * @param outputs the outputs of the dataset
   * @param loss    the loss function
   * @return the error
   */
  public double test(final double[][] inputs, final double[][] outputs, final Loss loss) {
    if (loss == null) {
      return this.test(inputs, outputs);
    } else if (this.dropout != 0) {
      this.nodes.stream()
        .filter(node -> node.type == Node.NodeType.HIDDEN)
        .forEach(node -> node.mask = 1 - this.dropout);
    }
    return IntStream.range(0, inputs.length)
      .mapToDouble(i -> loss.calc(outputs[i], this.activate(inputs[i])))
      .sum()
      / inputs.length;
  }

  /**
   * Evolves the network to reach a lower error on a dataset using the NEAT algorithm.
   *
   * @param inputs  the inputs of the dataset
   * @param outputs the outputs of the dataset
   * @param options options evolution options
   * @return the error of the network after evolution
   */
  public double evolve(final double[][] inputs, final double[][] outputs, final EvolveOptions options) {
    if (options == null) {
      return this.evolve(inputs, outputs);
    }
    if (inputs[0].length != this.input || outputs[0].length != this.output) {
      // check dataset dimensions
      throw new IllegalStateException("Dataset input/output size should be same as network input/output size!");
    }

    double targetError = options.getError();
    final double growth = options.getGrowth();
    final Loss loss = options.getLoss();
    final int amount = options.getAmount();
    if (options.getIterations() == -1 && Double.isNaN(options.getError())) {
      throw new IllegalArgumentException("At least one of the following options must be specified: error, iterations");
    } else if (Double.isNaN(options.getError())) {
      targetError = -1; // run until iterations
    }
    if (options.getFitnessFunction() == null) {
      // if user doesn't specified a fitness function -> create default fitness function
      options.setFitnessFunction(genome -> {
        final double sum = IntStream.range(0, amount)
          .mapToDouble(i -> -genome.test(inputs, outputs, loss))
          .sum();
        return (sum - genome.getGrowthScore(growth)) / amount;
      });
    }
    options.setNetwork(this); // set network to start evolution from

    final NEAT neat = new NEAT(this.input, this.output, options); // create NEAT instance

    double error = -Double.MAX_VALUE;
    double bestScore = -Double.MAX_VALUE;
    Network bestGenome = null;

    // start evolution loop
    while (error < -targetError || neat.generation < options.getIterations()) {
      final Network fittest = neat.evolve(); // run one evolution step
      error = fittest.score + fittest.getGrowthScore(growth); // calculate error of the fittest genome
      if (fittest.score > bestScore) {
        // if fittest is fitter than the best genome so far
        // set fittest to new global best genome
        bestScore = fittest.score;
        bestGenome = fittest;
      }
      // Log
      if (options.getLog() > 0 && neat.generation % options.getLog() == 0) {
        System.out.println("Iteration: " + neat.generation + "; Fitness: " + fittest.score + "; Error: " + -error + "; Population: " + neat.population.size());
      }
    }

    if (bestGenome != null) {
      // set this network equal to fittest network of evolution
      this.nodes = bestGenome.nodes;
      this.connections = bestGenome.connections;
      this.selfConnections = bestGenome.selfConnections;
      this.gates = bestGenome.gates;
      if (options.isClear()) {
        this.clear();
      }
    }
    return -error;
  }

  /**
   * Activate the network with given input data.
   *
   * @param input the input data
   * @return the activation values of the output nodes
   */
  private double[] activate(final double[] input) {
    if (input.length != this.nodes.stream().filter(node -> node.type == Node.NodeType.INPUT).count()) {
      throw new IllegalStateException("Dataset input size should be same as network input size!");
    }

    final List<Double> output = new ArrayList<>();
    int inputIndex = 0;
    for (final Node node : this.nodes) {
      if (node.type == Node.NodeType.INPUT) {
        // input node
        node.activate(input[inputIndex++]);
      } else if (node.type == Node.NodeType.HIDDEN) {
        // hidden node
        node.activate();
      } else {
        // output node
        output.add(node.activate());
      }
    }
    return output.stream().mapToDouble(i -> i).toArray(); // convert list to array
  }

  /**
   * Removes a node from a network.
   * All its connections will be redirected.
   * If it gates a connection, the gate will be removed.
   *
   * @param node Node to remove from the network
   */
  public void remove(final Node node) {
    if (!this.nodes.contains(node)) {
      throw new IllegalArgumentException("This node does not exist in the network!");
    }

    this.disconnect(node, node); // remove self connection

    // nodes which are directly connected into this node
    final List<Node> inputs = node.in.stream()
      .map(connection -> connection.from)
      .collect(Collectors.toList());

    // nodes which are directly connected out of this node
    final List<Node> outputs = node.out.stream()
      .map(connection -> connection.to)
      .collect(Collectors.toList());

    // disconnect all inputs and outputs
    inputs.forEach(input -> this.disconnect(input, node));
    outputs.forEach(output -> this.disconnect(node, output));

    // create new connections from input nodes to output nodes
    final List<Connection> connections = new ArrayList<>();
    for (final Node input : inputs) {
      for (final Node output : outputs) {
        if (input.isNotProjectingTo(output)) {
          // if input and output aren't connected
          // connect them
          connections.add(this.connect(input, output, 0));
        }
      }
    }

    Stream.concat(node.in.stream(), node.out.stream())
      .filter(connection -> SUB_NODE.keepGates) // Should keep gates?
      .map(connection -> connection.gateNode)
      .filter(Objects::nonNull)
      .filter(gateNode -> !gateNode.equals(node)) // should be unequal to currently removing node
      .takeWhile(gateNode -> !connections.isEmpty()) // as long as connections are available
      .forEach(gateNode -> {
        // gate connection and remove it
        final Connection connection = pickRandom(connections); // pick random connection
        this.gate(gateNode, connection); // gate this connection
        connections.remove(connection); // remove connection from list
      });

    // remove all gates
    for (int i = node.gated.size() - 1; i >= 0; i--) {
      this.removeGate(node.gated.get(i));
    }

    this.nodes.remove(node); // remove node from list
  }

  /**
   * Removes the connection between two nodes
   *
   * @param from the incoming node
   * @param to   the outgoing node
   */
  public void disconnect(final @NotNull Node from, final Node to) {
    final Connection connection;
    if (from.equals(to)) {
      connection = from.self;
      this.selfConnections.remove(connection); // remove connection from list
    } else {
      connection = this.connections.stream()
        .filter(conn -> conn.from.equals(from))
        .filter(conn -> conn.to.equals(to))
        .findAny()
        .orElseThrow();

      this.connections.remove(connection); // remove connection from list
    }

    if (connection.gateNode != null) {
      // if connection has gate
      // remove it
      this.removeGate(connection);
    }

    from.disconnect(to); // run node-level disconnect
  }

  /**
   * Remove the gate of a connection.
   *
   * @param connection the connection to remove the gate from
   */
  public void removeGate(final Connection connection) {
    // assume that connection is gated
    this.gates.remove(connection); // remove connection from gates list
    connection.gateNode.removeGate(connection); // run node-level removeGate()
  }

  /**
   * Resets every node in this network.
   */
  public void clear() {
    this.nodes.forEach(Node::clear);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.input, this.output, this.nodes, this.connections, this.gates, this.selfConnections, this.dropout);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    final Network network = (Network) o;
    return this.input == network.input &&
      this.output == network.output &&
      Double.compare(network.dropout, this.dropout) == 0 &&
      Objects.equals(this.nodes, network.nodes) &&
      Objects.equals(this.connections, network.connections) &&
      Objects.equals(this.gates, network.gates) &&
      Objects.equals(this.selfConnections, network.selfConnections);
  }

  @Override
  public String toString() {
    return "Network{" +
      "input=" + this.input +
      ", output=" + this.output +
      ", gates=" + this.gates +
      ", nodes=" + this.nodes +
      ", connections=" + this.connections +
      ", selfConnections=" + this.selfConnections +
      '}';
  }

  /**
   * Copies a network.
   *
   * @return the copied network
   */
  public Network copy() {
    return Network.fromJSON(this.toJSON()); // simply convert to json and back
  }

  /**
   * Convert a network to JsonObject.
   *
   * @return the resulting json object
   */
  public JsonObject toJSON() {
    final JsonObject json = new JsonObject();
    json.addProperty("input", this.input);
    json.addProperty("output", this.output);
    json.addProperty("dropout", this.dropout);
    json.addProperty("score", this.score);
    final JsonArray jsonNodes = new JsonArray();
    final JsonArray jsonConnections = new JsonArray();

    this.setNodeIndices(); // set node indices

    // creating nodes json array
    this.nodes.forEach(node -> {
      final JsonObject nodeJSON = node.toJSON(); // run Node.toJSON()
      nodeJSON.addProperty("index", node.index); // add node index to json
      jsonNodes.add(nodeJSON); // add to json array of all nodes
    });

    // creating connections json array
    Stream.concat(this.connections.stream(), this.selfConnections.stream()) // stream with all connections
      .forEach(connection -> { // iterate over all connections
        final JsonObject toJSON = connection.toJSON(); // run Connection.toJSON()
        toJSON.addProperty("from", connection.from.index); // add from index
        toJSON.addProperty("to", connection.to.index); // add to index
        toJSON.addProperty("gateNode", connection.gateNode != null ? connection.gateNode.index : -1); // add gate node, if it exists
        jsonConnections.add(toJSON); // add to json array of all connections
      });

    json.add("nodes", jsonNodes); // add nodes json array
    json.add("connections", jsonConnections); /// add connections json array
    return json;
  }
}
