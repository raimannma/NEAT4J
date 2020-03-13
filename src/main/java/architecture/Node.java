package architecture;

import static methods.Activation.LOGISTIC;
import static methods.Utils.pickRandom;
import static methods.Utils.randDouble;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import methods.Activation;
import methods.Mutation;

public class Node {

  public final List<Connection> in;
  public final List<Connection> out;
  public final Connection self;
  public final List<Connection> gated;
  public Activation activationType;
  public double bias;
  public int index;
  public NodeType type;
  public double mask;
  private double state;
  private double activation;

  Node() {
    this(NodeType.HIDDEN);
  }

  public Node(final NodeType type) {
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

  public static Node fromJSON(final JsonObject jsonObject) {
    final Node node = new Node();
    node.bias = jsonObject.get("bias").getAsDouble();
    node.type = NodeType.valueOf(jsonObject.get("type").getAsString());
    node.activationType = Activation.valueOf(jsonObject.get("activationType").getAsString());
    node.mask = jsonObject.get("mask").getAsDouble();
    return node;
  }

  public double activate() {
    this.updateState();
    this.activation = this.activationType.calc(this.state);
    this.gated.forEach(conn -> conn.gain = this.activation);
    return this.activation;
  }

  private void updateState() {
    this.state = this.self.gain * this.self.weight * this.state + this.bias;
    this.in.forEach(connection -> this.state += connection.from.activation * connection.weight * connection.gain);
  }

  public void activate(final double input) {
    this.activation = input;
  }

  public List<Connection> connect(final Node target, final double weight) {
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

  public boolean isNotProjectingTo(final Node node) {
    return (!this.equals(node) || this.self.weight == 0)
      && this.out.stream().noneMatch(connection -> connection.to.equals(node));
  }

  public void clear() {
    this.gated.forEach(connection -> connection.gain = 0);
    this.state = 0;
    this.activation = 0;
  }

  public void disconnect(final Node node) {
    if (this.equals(node)) {
      this.self.weight = 0;
      return;
    }
    new ArrayList<>(this.out)
      .stream()
      .filter(connection -> connection.to.equals(node))
      .forEach(connection -> {
        this.out.remove(connection);
        connection.to.in.remove(connection);
        if (connection.gateNode != null) {
          connection.gateNode.removeGate(connection);
        }
      });
  }

  public void removeGate(final Connection connection) {
    this.gated.remove(connection);
    connection.gateNode = null;
    connection.gain = 1;
  }

  public void gate(final Connection connection) {
    connection.gateNode = this;
    this.gated.add(connection);
  }

  public void mutate(final Mutation method) {
    if (method == Mutation.MOD_ACTIVATION) {
      this.activationType = pickRandom(method.allowed);
    } else if (method == Mutation.MOD_BIAS) {
      this.bias += randDouble(method.min, method.max);
    }
  }

  public JsonObject toJSON() {
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

  public enum NodeType {HIDDEN, INPUT, OUTPUT}

}
