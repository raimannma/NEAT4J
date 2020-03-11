package architecture;

import static methods.Mutation.MOD_ACTIVATION;
import static methods.Mutation.SUB_NODE;
import static methods.Utils.pickRandom;
import static methods.Utils.randDouble;
import static methods.Utils.randInt;
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
import methods.Activation;
import methods.Loss;
import methods.Mutation;

public class Network implements Cloneable {
  public final int input;
  public final int output;
  public List<Node> nodes;
  List<Connection> connections;
  List<Connection> gates;
  double score;
  private List<Connection> selfConnections;
  private double dropout;

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

    final double initWeight = this.input * Math.sqrt((double) 2 / this.input);
    for (int i = 0; i < this.input; i++) {
      final Node node = this.nodes.get(i);
      for (int j = this.input; j < this.output + this.input; j++) {
        this.connect(node, this.nodes.get(j), randDouble(initWeight));
      }
    }
  }

  private List<Connection> connect(final Node from, final Node to, final double weight) {
    final List<Connection> connections = from.connect(to, weight);
    if (from.equals(to)) {
      this.selfConnections.addAll(connections);
    } else {
      this.connections.addAll(connections);
    }
    return connections;
  }

  static Network crossover(final Network network1, final Network network2, final boolean equal) {
    if (network1.input != network2.input || network1.output != network2.output) {
      throw new RuntimeException("Networks don't have the same input/output size!");
    }

    final Network offspring = new Network(network1.input, network1.output);
    offspring.connections = new ArrayList<>();
    offspring.nodes = new ArrayList<>();

    final double score1 = Double.isNaN(network1.score) ? 0 : network1.score;
    final double score2 = Double.isNaN(network2.score) ? 0 : network2.score;

    final int size;
    if (equal || score1 == score2) {
      final int max = Math.max(network1.nodes.size(), network2.nodes.size());
      final int min = Math.min(network1.nodes.size(), network2.nodes.size());
      size = (int) Math.floor(Math.random() * (max - min + 1) + min);
    } else if (score1 > score2) {
      size = network1.nodes.size();
    } else {
      size = network2.nodes.size();
    }

    IntStream.range(0, network1.nodes.size()).forEach(i -> network1.nodes.get(i).index = i);
    IntStream.range(0, network2.nodes.size()).forEach(i -> network2.nodes.get(i).index = i);

    for (int i = 0; i < size; i++) {
      Node node;
      final Node other;
      if (i < size - network1.output) {
        final double random = Math.random();
        if (random < 0.5) {
          node = i < network2.nodes.size() ? network2.nodes.get(i) : null;
          other = i < network1.nodes.size() ? network1.nodes.get(i) : null;
        } else {
          node = i < network1.nodes.size() ? network1.nodes.get(i) : null;
          other = i < network2.nodes.size() ? network2.nodes.get(i) : null;
        }
        if (node == null || node.type == Node.NodeType.OUTPUT) {
          node = other;
        }
      } else if (Math.random() >= 0.5) {
        node = network1.nodes.get(network1.nodes.size() + i - size);
      } else {
        node = network2.nodes.get(network2.nodes.size() + i - size);
      }

      final Node newNode = new Node();
      assert node != null;
      newNode.bias = node.bias;
      newNode.activationType = node.activationType;
      newNode.type = node.type;
      offspring.nodes.add(newNode);
    }

    final Map<Integer, Double[]> n1Connections = makeConnections(network1);
    final Map<Integer, Double[]> n2Connections = makeConnections(network2);

    final List<Double[]> connections = new ArrayList<>();
    final List<Integer> keys1 = new ArrayList<>(n1Connections.keySet());
    final List<Integer> keys2 = new ArrayList<>(n2Connections.keySet());

    for (int i = keys1.size() - 1; i >= 0; i--) {
      if (n2Connections.get(keys1.get(i)) != null) {
        connections.add(Math.random() >= 0.5 ? n1Connections.get(keys1.get(i)) : n2Connections.get(keys1.get(i)));
        n2Connections.put(keys1.get(i), null);
      } else if (score1 >= score2 || equal) {
        connections.add(n1Connections.get(keys1.get(i)));
      }
    }

    if (score2 >= score1 || equal) {
      keys2.stream()
        .filter(integer -> n2Connections.get(integer) != null)
        .map(n2Connections::get)
        .forEach(connections::add);
    }

    for (final Double[] connData : connections) {
      if (connData[2] < size && connData[1] < size) {
        final Node from = offspring.nodes.get((int) (double) connData[1]);
        final Node to = offspring.nodes.get((int) (double) connData[2]);
        final Connection connection = offspring.connect(from, to).get(0);
        connection.weight = connData[0];
        if (connData[3] != -1 && connData[3] < size) {
          offspring.gate(offspring.nodes.get((int) (double) connData[3]), connection);
        }
      }
    }
    return offspring;
  }

  private static Map<Integer, Double[]> makeConnections(final Network network) {
    final Map<Integer, Double[]> connections = new HashMap<>();
    Stream.concat(network.connections.stream(), network.selfConnections.stream())
      .forEach(connection -> {
        final Double[] data = new Double[4];
        data[0] = connection.weight;
        data[1] = (double) connection.from.index;
        data[2] = (double) connection.to.index;
        data[3] = (double) (connection.gateNode != null ? connection.gateNode.index : -1);
        connections.put(Connection.getInnovationID(connection.from.index, connection.to.index), data);
      });
    return connections;
  }

  private List<Connection> connect(final Node from, final Node to) {
    return this.connect(from, to, 0);
  }

  private void gate(final Node node, final Connection connection) {
    if (!this.nodes.contains(node)) {
      throw new RuntimeException("This node is not part of the network!");
    } else if (connection.gateNode != null) {
      return;
    }
    node.gate(connection);
    this.gates.add(connection);
  }

  private double evolve(final double[][] inputs, final double[][] outputs) {
    return this.evolve(inputs, outputs, new EvolveOptions());
  }

  public double evolve(final double[][] inputs, final double[][] outputs, final EvolveOptions options) {
    if (options == null) {
      return this.evolve(inputs, outputs);
    }
    if (inputs[0].length != this.input || outputs[0].length != this.output) {
      throw new RuntimeException("Dataset input/output size should be same as network input/output size!");
    }

    double targetError = Double.isNaN(options.getError()) ? 0.05 : options.getError();
    final double growth = options.getGrowth();
    final Loss loss = options.getLoss();
    final int amount = options.getAmount();
    if (options.getIterations() == -1 && Double.isNaN(options.getError())) {
      throw new RuntimeException("At least one of the following options must be specified: error, iterations");
    } else if (Double.isNaN(options.getError())) {
      targetError = -1;
    } else if (options.getIterations() == -1) {
      options.setIterations(0);
    }
    if (options.getFitnessFunction() == null) {
      options.setFitnessFunction(genome -> {
        final double score = IntStream.range(0, amount)
          .parallel()
          .mapToDouble(i -> -genome.test(inputs, outputs, loss))
          .sum()
          - growth * (genome.nodes.size() + genome.connections.size() + genome.gates.size() - genome.input - genome.output);
        return Double.isNaN(score) ? -Double.MAX_VALUE : score / amount;
      });
    }
    options.setNetwork(this);
    final NEAT neat = new NEAT(this.input, this.output, options);

    double error = -Double.MAX_VALUE;
    double bestFitness = -Double.MAX_VALUE;
    Network bestGenome = null;

    while (error < -targetError && (options.getIterations() == 0 || neat.generation < options.getIterations())) {
      final Network fittest = neat.evolve();
      error = fittest.score + growth * (fittest.nodes.size() + fittest.connections.size() + fittest.gates.size() - fittest.input - fittest.output);
      if (fittest.score > bestFitness) {
        bestFitness = fittest.score;
        bestGenome = fittest;
      }
      if (options.getLog() > 0 && neat.generation % options.getLog() == 0) {
        System.out.println("Iteration: " + neat.generation + "; Fitness: " + fittest.score + "; Error: " + -error + "; Population: " + neat.population.size());
      }
    }

    if (bestGenome != null) {
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

  private double test(final double[][] inputs, final double[][] outputs) {
    return this.test(inputs, outputs, Loss.MSE);
  }

  private double test(final double[][] inputs, final double[][] outputs, final Loss loss) {
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

  private double[] activate(final double[] input) {
    final List<Double> output = new ArrayList<>();
    int inputIndex = 0;
    for (final Node node : this.nodes) {
      switch (node.type) {
        case INPUT:
          if (inputIndex < input.length) {
            node.activate(input[inputIndex++]);
          } else {
            node.type = Node.NodeType.HIDDEN;
            node.activate();
          }
          break;
        case HIDDEN:
          node.activate();
          break;
        case OUTPUT:
          output.add(node.activate());
          break;
      }
    }
    return output.stream().mapToDouble(i -> i).toArray();
  }

  public void mutate(final Mutation method) {
    if (Arrays.stream(Mutation.ALL).noneMatch(meth -> meth == method)) {
      throw new RuntimeException("No (correct) mutate method given!");
    }

    final List<Connection> allConnections;
    int index;
    final Connection connection;
    final Connection randomConn;
    Node node;
    Node node2;
    final List<Connection> availableConnections;
    final List<Node[]> availableNodes;
    switch (method) {
      case ADD_NODE:
        if (this.connections.isEmpty()) {
          break;
        }
        connection = pickRandom(this.connections);
        this.disconnect(connection.from, connection.to);

        node = new Node(Node.NodeType.HIDDEN);
        node.mutate(MOD_ACTIVATION);
        this.nodes.add(Math.max(0, Math.min(this.nodes.indexOf(connection.to), this.nodes.size() - this.output)), node);

        if (connection.gateNode != null && Math.random() >= 0.5) {
          this.gate(connection.gateNode, this.connect(connection.from, node).get(0));
        } else if (connection.gateNode != null) {
          this.gate(connection.gateNode, this.connect(node, connection.to).get(0));
        }
        break;
      case SUB_NODE:
        if (this.nodes.size() == this.input + this.output) {
          break;
        }

        this.remove(this.nodes.get((int) Math.floor(Math.random() * (this.nodes.size() - this.output - this.input) + this.input)));
        break;
      case ADD_CONN:
        availableNodes = new ArrayList<>();
        for (int i = 0; i < this.nodes.size() - this.output; i++) {
          node = this.nodes.get(i);
          for (int j = Math.max(i + 1, this.input); j < this.nodes.size(); j++) {
            node2 = this.nodes.get(j);
            if (node.isNotProjectingTo(node2)) {
              availableNodes.add(new Node[] {node, node2});
            }
          }
        }
        if (availableNodes.isEmpty()) {
          break;
        }
        final Node[] pair = pickRandom(availableNodes);
        this.connect(pair[0], pair[1]);
        break;
      case SUB_CONN:
        availableConnections = new ArrayList<>();
        this.connections.stream()
          .filter(conn -> !conn.from.out.isEmpty() && !conn.to.in.isEmpty()
            && this.nodes.indexOf(conn.to) > this.nodes.indexOf(conn.from))
          .forEach(availableConnections::add);
        if (availableConnections.isEmpty()) {
          break;
        }
        randomConn = pickRandom(availableConnections);
        this.disconnect(randomConn.from, randomConn.to);
        break;
      case MOD_WEIGHT:
        allConnections = new ArrayList<>(this.connections);
        allConnections.addAll(this.selfConnections);
        if (allConnections.isEmpty()) {
          break;
        }

        connection = pickRandom(allConnections);
        connection.weight += randDouble(method.min, method.max);
        break;
      case MOD_BIAS:
        this.nodes.get(randInt(this.input, this.nodes.size())).mutate(method);
        break;
      case MOD_ACTIVATION:
        if (!method.mutateOutput && this.input + this.output == this.nodes.size()) {
          break;
        }
        index = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
        this.nodes.get(index).mutate(method);
        break;
      case ADD_SELF_CONN:
        final List<Node> poss = IntStream.range(this.input, this.nodes.size()).mapToObj(this.nodes::get).filter(node1 -> node1.self.weight == 0).collect(Collectors.toList());
        if (poss.isEmpty()) {
          break;
        }
        node = pickRandom(poss);
        this.connect(node, node);
        break;
      case SUB_SELF_CONN:
        if (this.selfConnections.isEmpty()) {
          break;
        }
        connection = pickRandom(this.selfConnections);
        this.disconnect(connection.from, connection.to);
        break;
      case ADD_GATE:
        allConnections = new ArrayList<>(this.connections);
        allConnections.addAll(this.selfConnections);

        availableConnections = allConnections.stream()
          .filter(connection1 -> connection1.gateNode == null)
          .collect(Collectors.toList());
        if (availableConnections.isEmpty()) {
          break;
        }

        this.gate(this.nodes.get(randInt(this.input, this.nodes.size())), pickRandom(availableConnections));
        break;
      case SUB_GATE:
        if (this.gates.isEmpty()) {
          break;
        }
        this.removeGate(pickRandom(this.gates));
        break;
      case ADD_BACK_CONN:
        availableNodes = new ArrayList<>();
        for (int i = this.input; i < this.nodes.size(); i++) {
          node = this.nodes.get(i);
          for (int j = this.input; j < i; j++) {
            node2 = this.nodes.get(j);
            if (node.isNotProjectingTo(node2)) {
              availableNodes.add(new Node[] {node, node2});
            }
          }
        }

        if (availableNodes.isEmpty()) {
          break;
        }

        final Node[] pair1 = pickRandom(availableNodes);
        this.connect(pair1[0], pair1[1]);
        break;
      case SUB_BACK_CONN:
        availableConnections = new ArrayList<>();
        this.connections.stream().filter(connection1 -> !connection1.from.out.isEmpty() && !connection1.to.in.isEmpty() &&
          this.nodes.indexOf(connection1.from) > this.nodes.indexOf(connection1.to))
          .forEach(availableConnections::add);
        if (availableConnections.isEmpty()) {
          break;
        }
        randomConn = pickRandom(availableConnections);
        this.disconnect(randomConn.from, randomConn.to);
        break;
      case SWAP_NODES:
        if (method.mutateOutput && this.nodes.size() - this.input < 2
          || !method.mutateOutput && this.nodes.size() - this.input - this.output < 2) {
          break;
        }
        index = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
        node = this.nodes.get(index);
        index = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
        node2 = this.nodes.get(index);

        final double biasTemp = node.bias;
        final Activation activationType = node.activationType;

        node.bias = node2.bias;
        node.activationType = node2.activationType;
        node2.bias = biasTemp;
        node2.activationType = activationType;
        break;
    }
  }

  private void disconnect(final Node from, final Node to) {
    final List<Connection> connections = from.equals(to) ? this.selfConnections : this.connections;
    for (final Connection connection : connections) {
      if (connection.from.equals(from) && connection.to.equals(to)) {
        if (connection.gateNode != null) {
          this.removeGate(connection);
        }
        connections.remove(connection);
        break;
      }
    }
    from.disconnect(to);
  }

  private void remove(final Node node) {
    if (!this.nodes.contains(node)) {
      throw new RuntimeException("This node does not exist in the network!");
    }

    final List<Node> gateNodes = new ArrayList<>();
    this.disconnect(node, node);

    final List<Node> inputs = new ArrayList<>();
    for (int i = node.in.size() - 1; i >= 0; i--) {
      final Connection connection = node.in.get(i);
      if (SUB_NODE.keepGates && connection.gateNode != null && !connection.gateNode.equals(node)) {
        gateNodes.add(connection.gateNode);
      }
      inputs.add(connection.from);
      this.disconnect(connection.from, node);
    }
    final List<Node> outputs = new ArrayList<>();
    for (int i = node.out.size() - 1; i >= 0; i--) {
      final Connection connection = node.out.get(i);
      if (SUB_NODE.keepGates && connection.gateNode != null && !connection.gateNode.equals(node)) {
        gateNodes.add(connection.gateNode);
      }
      outputs.add(connection.to);
      this.disconnect(node, connection.to);
    }

    final List<Connection> connections = new ArrayList<>();
    inputs.forEach(input -> outputs.stream()
      .filter(input::isNotProjectingTo)
      .map(output -> this.connect(input, output, 0).get(0))
      .forEach(connections::add));

    gateNodes.stream()
      .takeWhile(gate -> connections.size() > 0)
      .forEach(gateNode -> {
        final Connection connection = pickRandom(connections);
        this.gate(gateNode, connection);
        connections.remove(connection);
      });
    for (int i = node.gated.size() - 1; i >= 0; i--) {
      this.removeGate(node.gated.get(i));
    }

    this.disconnect(node, node);
    this.nodes.remove(node);
  }

  private void removeGate(final Connection connection) {
    if (connection != null && connection.gateNode != null && this.gates.remove(connection)) {
      connection.gateNode.removeGate(connection);
    }
  }

  void clear() {
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
  protected Network clone() {
    return Network.fromJSON(this.toJSON());
  }

  public static Network fromJSON(final JsonObject json) {
    final Network network = new Network(json.get("input").getAsInt(), json.get("output").getAsInt());
    network.dropout = json.get("dropout").getAsDouble();
    network.nodes = new ArrayList<>();
    network.connections = new ArrayList<>();

    final JsonArray nodes = json.get("nodes").getAsJsonArray();
    final JsonArray connections = json.get("connections").getAsJsonArray();
    nodes.forEach(jsonNode -> network.nodes.add(Node.fromJSON(jsonNode.getAsJsonObject())));
    for (int i = 0; i < connections.size(); i++) {
      final JsonObject connJSON = connections.get(i).getAsJsonObject();
      final List<Connection> connection = network.connect(network.nodes.get(connJSON.get("from").getAsInt()), network.nodes.get(connJSON.get("to").getAsInt()));
      connection.get(0).weight = connJSON.get("weight").getAsDouble();

      if (connJSON.has("gateNode") && connJSON.get("gateNode").getAsInt() != -1) {
        network.gate(network.nodes.get(connJSON.get("gateNode").getAsInt()), connection.get(0));
      }
    }
    return network;
  }

  public JsonObject toJSON() {
    final JsonObject json = new JsonObject();
    json.addProperty("input", this.input);
    json.addProperty("output", this.output);
    json.addProperty("dropout", this.dropout);
    final JsonArray nodes = new JsonArray();
    final JsonArray connections = new JsonArray();

    IntStream.range(0, this.nodes.size()).forEach(i -> this.nodes.get(i).index = i);
    IntStream.range(0, this.nodes.size()).forEach(i -> {
      final Node node = this.nodes.get(i);
      final JsonObject nodeJSON = node.toJSON();
      nodeJSON.addProperty("index", i);
      nodes.add(nodeJSON);
      if (node.self.weight != 0) {
        final JsonObject connectionJSON = node.self.toJSON();
        connectionJSON.addProperty("from", i);
        connectionJSON.addProperty("to", i);
        connectionJSON.addProperty("gateNode", node.self.gateNode == null ? -1 : node.self.gateNode.index);
        connections.add(connectionJSON);
      }
    });

    this.connections.forEach(connection -> {
      final JsonObject toJSON = connection.toJSON();
      toJSON.addProperty("from", connection.from.index);
      toJSON.addProperty("to", connection.to.index);
      toJSON.addProperty("gateNode", connection.gateNode != null ? connection.gateNode.index : -1);
      connections.add(toJSON);
    });

    json.add("nodes", nodes);
    json.add("connections", connections);
    return json;
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
}
