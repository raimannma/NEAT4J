package architecture;

import static methods.Activation.LOGISTIC;
import static methods.Utils.pickRandom;
import static methods.Utils.randDouble;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import methods.Activation;
import methods.Mutation;

public class Node {

  final List<Connection> in;
  final List<Connection> out;
  final List<Connection> gated;
  final Connection self;
  int index;
  Activation activationType;
  double bias;
  NodeType type;
  double mask;
  private double state;
  private double activation;

  Node() {
    this(NodeType.HIDDEN);
  }

  Node(final NodeType type) {
    this.bias = type == NodeType.INPUT ? 0 : randDouble(-1, 1);
    this.activationType = LOGISTIC;
    this.type = type;

    this.activation = 0;
    this.state = 0;
    this.mask = 1;

    this.in = new ArrayList<>();
    this.out = new ArrayList<>();
    this.gated = new ArrayList<>();
    this.self = new Connection(this, this, 0);
  }

  static Node fromJSON(final JsonObject jsonObject) {
    final Node node = new Node();
    node.bias = jsonObject.get("bias").getAsDouble();
    node.type = NodeType.valueOf(jsonObject.get("type").getAsString());
    node.activationType = Activation.valueOf(jsonObject.get("activationType").getAsString());
    node.mask = jsonObject.get("mask").getAsDouble();
    return node;
  }

  double activate() {
    this.updateState();
    this.activation = this.activationType.calc(this.state);
    this.gated.forEach(conn -> conn.gain = this.activation);
    return this.activation;
  }

  private void updateState() {
    this.state = this.self.gain * this.self.weight * this.state + this.bias;
    this.in.forEach(connection -> this.state += connection.from.activation * connection.weight * connection.gain);
  }

  void activate(final double input) {
    this.activation = input;
  }

  List<Connection> connect(final Node target, final double weight) {
    final List<Connection> connections = new ArrayList<>();
    if (target == this) {
      if (this.self.weight == 0) {
        this.self.weight = weight;
      }
      connections.add(this.self);
    } else {
      Connection connection = this.out.stream()
        .filter(conn -> conn.to.equals(target))
        .findAny()
        .orElse(null);
      if (connection == null) {
        connection = new Connection(this, target, weight);
        target.in.add(connection);
        this.out.add(connection);
      }
      connections.add(connection);
    }
    return connections;
  }

  boolean isNotProjectingTo(final Node node) {
    return (!this.equals(node) || this.self.weight == 0)
      && this.out.stream().noneMatch(connection -> connection.to.equals(node));
  }

  void clear() {
    this.in.forEach(connection -> {
      connection.eligibility = 0;
      connection.xTraceNodes = new ArrayList<>();
      connection.xTraceValues = new ArrayList<>();
    });
    this.gated.forEach(connection -> connection.gain = 0);
    this.state = 0;
    this.activation = 0;
  }

  void disconnect(final Node node) {
    if (this.equals(node)) {
      this.self.weight = 0;
      return;
    }
    for (final Connection connection : new ArrayList<>(this.out)) {
      if (connection.to.equals(node)) {
        this.out.remove(connection);
        connection.to.in.remove(connection);
        if (connection.gateNode != null) {
          connection.gateNode.removeGate(connection);
        }
        break;
      }
    }
  }

  void removeGate(final Connection connection) {
    this.removeGate(new Connection[] {connection});
  }

  private void removeGate(final Connection[] connections) {
    for (int i = connections.length - 1; i >= 0; i--) {
      final Connection connection = connections[i];
      this.gated.remove(connection);
      connection.gateNode = null;
      connection.gain = 1;
    }
  }

  void gate(final Connection connection) {
    this.gate(new Connection[] {connection});
  }

  private void gate(final Connection[] connections) {
    Arrays.stream(connections).forEach(connection -> connection.gateNode = this);
    this.gated.addAll(Arrays.asList(connections));
  }

  void mutate(final Mutation method) {
    if (method == Mutation.MOD_ACTIVATION) {
      this.activationType = pickRandom(method.allowed);
    } else if (method == Mutation.MOD_BIAS) {
      this.bias += randDouble(method.min, method.max);
    }
  }

  JsonObject toJSON() {
    final JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("bias", this.bias);
    jsonObject.addProperty("type", this.type.name());
    jsonObject.addProperty("activationType", this.activationType.name());
    jsonObject.addProperty("mask", this.mask);
    return jsonObject;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.activationType, this.bias, this.type);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    final Node node = (Node) o;
    return node.bias == this.bias &&
      this.activationType == node.activationType &&
      this.type == node.type;
  }

  @Override
  public String toString() {
    return "Node{" +
      "type=" + this.type +
      '}';
  }

  enum NodeType {HIDDEN, INPUT, OUTPUT}

}
