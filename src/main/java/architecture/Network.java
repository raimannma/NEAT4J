package architecture;

import static methods.Mutation.SUB_NODE;
import static methods.Utils.pickRandom;
import static methods.Utils.randDouble;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import methods.Loss;
import methods.Mutation;
import org.jetbrains.annotations.NotNull;

public class Network implements Cloneable {
  public final int input;
  public final int output;
  public List<Node> nodes;
  public double score;
  public List<Connection> connections;
  public List<Connection> selfConnections;
  public List<Connection> gates;
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

  private List<Connection> connect(final @NotNull Node from, final Node to, final double weight) {
    final List<Connection> connections = from.connect(to, weight);
    if (from.equals(to)) {
      this.selfConnections.addAll(connections);
    } else {
      this.connections.addAll(connections);
    }
    return connections;
  }

  public static @NotNull Network crossover(final @NotNull Network network1, final @NotNull Network network2, final boolean equal) {
    if (network1.input != network2.input || network1.output != network2.output) {
      throw new IllegalStateException("Networks don't have the same input/output size!");
    }

    final Network offspring = new Network(network1.input, network1.output);
    offspring.connections = new ArrayList<>();
    offspring.nodes = new ArrayList<>();

    final double score1 = Double.isNaN(network1.score) ? 0 : network1.score;
    final double score2 = Double.isNaN(network2.score) ? 0 : network2.score;

    final int size;
    final int size1 = network1.nodes.size();
    final int size2 = network2.nodes.size();
    if (equal || score1 == score2) {
      final int max = Math.max(size1, size2);
      final int min = Math.min(size1, size2);
      size = (int) Math.floor(randDouble() * (max - min + 1) + min);
    } else if (score1 > score2) {
      size = size1;
    } else {
      size = size2;
    }

    network1.setNodeIndizes();
    network2.setNodeIndizes();

    for (int i = 0; i < size; i++) {
      Node node;
      final Node other;
      if (i < size - network1.output) {
        if (randDouble() >= 0.5) {
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
        node = randDouble() >= 0.5
          ? network1.nodes.get(i + size1 - size)
          : network2.nodes.get(i + size2 - size);
      }

      final Node newNode = new Node();
      newNode.bias = node.bias;
      newNode.activationType = node.activationType;
      newNode.type = node.type;
      offspring.nodes.add(newNode);
    }

    final Map<Integer, Double[]> network1Connections = makeConnections(network1);
    final Map<Integer, Double[]> network2Connections = makeConnections(network2);

    final List<Double[]> connections = new ArrayList<>();
    final List<Integer> innovationIDs1 = new ArrayList<>(network1Connections.keySet());
    final List<Integer> innovationIDs2 = new ArrayList<>(network2Connections.keySet());

    for (int i = innovationIDs1.size() - 1; i >= 0; i--) {
      final Double[] remove = network2Connections.remove(innovationIDs1.get(i));
      if (remove != null) {
        connections.add(randDouble() >= 0.5
          ? network1Connections.get(innovationIDs1.get(i))
          : remove);
      } else if (score1 >= score2 || equal) {
        connections.add(network1Connections.get(innovationIDs1.get(i)));
      }
    }

    if (score2 >= score1 || equal) {
      innovationIDs2.stream()
        .map(network2Connections::get)
        .filter(Objects::nonNull)
        .forEach(connections::add);
    }

    connections.stream()
      .filter(connectionData -> connectionData[2] < size && connectionData[1] < size)
      .forEach(connectionData -> {
        final Node from = offspring.nodes.get((int) (double) connectionData[1]);
        final Node to = offspring.nodes.get((int) (double) connectionData[2]);
        final Connection connection = offspring.connect(from, to).get(0);
        connection.weight = connectionData[0];
        if (!Double.isNaN(connectionData[3]) && connectionData[3] < size) {
          offspring.gate(offspring.nodes.get((int) (double) connectionData[3]), connection);
        }
      });
    return offspring;
  }

  private void setNodeIndizes() {
    IntStream.range(0, this.nodes.size()).forEach(i -> this.nodes.get(i).index = i);
  }

  private static @NotNull Map<Integer, Double[]> makeConnections(final @NotNull Network network) {
    final Map<Integer, Double[]> connections = new HashMap<>();
    Stream.concat(network.connections.stream(), network.selfConnections.stream())
      .forEach(connection -> {
        final Double[] data = new Double[4];
        data[0] = connection.weight;
        data[1] = (double) connection.from.index;
        data[2] = (double) connection.to.index;
        data[3] = connection.gateNode == null ? Double.NaN : connection.gateNode.index;
        connections.put(Connection.getInnovationID(connection.from.index, connection.to.index), data);
      });
    return connections;
  }

  public List<Connection> connect(final Node from, final Node to) {
    return this.connect(from, to, 0);
  }

  public void gate(final Node node, final Connection connection) {
    if (!this.nodes.contains(node)) {
      throw new ArrayIndexOutOfBoundsException("This node is not part of the network!");
    } else if (connection.gateNode != null) {
      return;
    }
    node.gate(connection);
    this.gates.add(connection);
  }

  public void mutate(final Mutation method) {
    if (Arrays.stream(Mutation.ALL).noneMatch(meth -> meth == method)) {
      throw new IllegalArgumentException("No (correct) mutate method given!");
    }
    method.mutate(this);
  }

  private double evolve(final double[][] inputs, final double[][] outputs) {
    return this.evolve(inputs, outputs, new EvolveOptions());
  }

  private double getGrowthScore(final double growth) {
    return growth * (this.nodes.size()
      + this.connections.size()
      + this.gates.size()
      - this.input
      - this.output);
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

  public double evolve(final double[][] inputs, final double[][] outputs, final EvolveOptions options) {
    if (options == null) {
      return this.evolve(inputs, outputs);
    }
    if (inputs[0].length != this.input || outputs[0].length != this.output) {
      throw new IllegalStateException("Dataset input/output size should be same as network input/output size!");
    }

    double targetError = options.getError();
    final double growth = options.getGrowth();
    final Loss loss = options.getLoss();
    final int amount = options.getAmount();
    if (options.getIterations() == -1 && Double.isNaN(options.getError())) {
      throw new IllegalArgumentException("At least one of the following options must be specified: error, iterations");
    } else if (Double.isNaN(options.getError())) {
      targetError = -1;
    } else if (options.getIterations() == -1) {
      options.setIterations(Integer.MAX_VALUE);
    }
    if (options.getFitnessFunction() == null) {
      options.setFitnessFunction(genome ->
        (IntStream.range(0, amount)
          .mapToDouble(i -> -genome.test(inputs, outputs, loss))
          .sum() - genome.getGrowthScore(growth))
          / amount);
    }
    options.setNetwork(this);
    final NEAT neat = new NEAT(this.input, this.output, options);

    double error = -Double.MAX_VALUE;
    double bestScore = -Double.MAX_VALUE;
    Network bestGenome = null;

    while (error < -targetError && neat.generation < options.getIterations()) {
      final Network fittest = neat.evolve();
      error = fittest.score + fittest.getGrowthScore(growth);
      if (fittest.score > bestScore) {
        bestScore = fittest.score;
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

  private double[] activate(final double[] input) {
    final List<Double> output = new ArrayList<>();
    int inputIndex = 0;
    for (final Node node : this.nodes) {
      if (node.type == Node.NodeType.INPUT) {
        node.activate(input[inputIndex++]);
      } else if (node.type == Node.NodeType.HIDDEN) {
        node.activate();
      } else if (node.type == Node.NodeType.OUTPUT) {
        output.add(node.activate());
      }
    }
    return output.stream().mapToDouble(i -> i).toArray();
  }

  public void remove(final Node node) {
    if (!this.nodes.contains(node)) {
      throw new IllegalArgumentException("This node does not exist in the network!");
    }

    final List<Node> gateNodes = new ArrayList<>();
    this.disconnect(node, node);

    final List<Node> inputs = new ArrayList<>();
    for (int i = node.in.size() - 1; i >= 0; i--) {
      final Connection connection = node.in.get(i);
      if (SUB_NODE.keepGates
        && connection.gateNode != null
        && !connection.gateNode.equals(node)) {
        gateNodes.add(connection.gateNode);
      }
      inputs.add(connection.from);
      this.disconnect(connection.from, node);
    }
    final List<Node> outputs = new ArrayList<>();
    for (int i = node.out.size() - 1; i >= 0; i--) {
      final Connection connection = node.out.get(i);
      if (SUB_NODE.keepGates
        && connection.gateNode != null
        && !connection.gateNode.equals(node)) {
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

  public void disconnect(final @NotNull Node from, final Node to) {
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

  public void removeGate(final Connection connection) {
    if (connection != null
      && connection.gateNode != null
      && this.gates.remove(connection)) {
      connection.gateNode.removeGate(connection);
    }
  }

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

  public Network copy() {
    return Network.fromJSON(this.toJSON());
  }

  public static @NotNull Network fromJSON(final @NotNull JsonObject json) {
    final Network network = new Network(json.get("input").getAsInt(), json.get("output").getAsInt());
    network.dropout = json.get("dropout").getAsDouble();
    network.nodes = new ArrayList<>();
    network.connections = new ArrayList<>();

    final JsonArray nodes = json.get("nodes").getAsJsonArray();
    final JsonArray connections = json.get("connections").getAsJsonArray();
    nodes.forEach(jsonNode -> network.nodes.add(Node.fromJSON(jsonNode.getAsJsonObject())));
    for (int i = 0; i < connections.size(); i++) {
      final JsonObject connJSON = connections.get(i).getAsJsonObject();
      final Connection connection = network.connect(network.nodes.get(connJSON.get("from").getAsInt()), network.nodes.get(connJSON.get("to").getAsInt())).get(0);
      connection.weight = connJSON.get("weight").getAsDouble();

      if (connJSON.has("gateNode") && connJSON.get("gateNode").getAsInt() != -1) {
        network.gate(network.nodes.get(connJSON.get("gateNode").getAsInt()), connection);
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

    this.setNodeIndizes();
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
}
