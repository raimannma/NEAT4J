package architecture;

import com.google.gson.JsonObject;
import methods.Activation;
import methods.Mutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static methods.Activation.LOGISTIC;

public class Node {

    final ConnectionList connections;
    int index;
    Activation activationType;
    double bias;
    NodeType type;
    double mask;
    private double old;
    private double state;
    private double activation;

    Node() {
        this(NodeType.HIDDEN);
    }

    Node(final NodeType type) {
        this.bias = type == NodeType.INPUT ? 0 : Math.random() * 2 - 1;
        this.activationType = LOGISTIC;
        this.type = type;

        this.activation = 0;
        this.state = 0;
        this.old = 0;
        this.mask = 1;

        this.connections = new ConnectionList(this);

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
        this.old = this.state;
        this.updateState();

        this.activation = this.activationType.calc(this.state) * this.mask;
        final double derivative = this.activationType.calc(this.state, true);

        final List<Node> nodes = new ArrayList<>();
        final ArrayList<Double> influences = IntStream.range(0, this.connections.gated.size()).mapToObj(i -> 0.0).collect(Collectors.toCollection(ArrayList::new));

        this.connections.gated.forEach(connection -> {
            final Node node = connection.to;
            final int index = nodes.indexOf(node);
            if (index > -1) {
                influences.set(index, influences.get(index) + connection.weight * connection.from.activation);
            } else {
                nodes.add(node);
                influences.add(node.connections.self.gater == null || !node.connections.self.gater.equals(this)
                        ? connection.weight * connection.from.activation
                        : connection.weight * connection.from.activation + node.old);
            }
            connection.gain = this.activation;
        });

        for (final Connection connection : this.connections.in) {
            connection.eligibility = this.connections.self.gain * this.connections.self.weight * connection.eligibility + connection.from.activation * connection.gain;

            for (int j = 0; j < nodes.size(); j++) {
                final Node node = nodes.get(j);
                final double influence = influences.get(j);
                final int index = connection.xTraceNodes.indexOf(node);
                if (index > -1) {
                    connection.xTraceValues.set(index,
                            node.connections.self.gain * node.connections.self.weight * connection.xTraceValues.get(index) +
                                    derivative * connection.eligibility * influence);
                } else {
                    connection.xTraceNodes.add(node);
                    connection.xTraceValues.add(derivative * connection.eligibility * influence);
                }
            }
        }
        return this.activation;
    }

    private void updateState() {
        this.state = this.connections.self.gain * this.connections.self.weight * this.state + this.bias;
        this.connections.in.forEach(connection -> this.state += connection.from.activation * connection.weight * connection.gain);
    }

    void activate(final double input) {
        this.activation = input;
    }

    double noTraceActivation() {
        this.updateState();
        this.activation = this.activationType.calc(this.state);
        this.connections.gated.forEach(conn -> conn.gain = this.activation);
        return this.activation;
    }

    void noTraceActivation(final double input) {
        this.activation = input;
    }

    List<Connection> connect(final Node target, final double weight) {
        final List<Connection> connections = new ArrayList<>();
        if (target == this) {
            if (this.connections.self.weight == 0) {
                this.connections.self.weight = weight;
            }
            connections.add(this.connections.self);
        } else {
            Connection connection = this.connections.out.stream()
                    .filter(conn -> conn.to.equals(target))
                    .findAny()
                    .orElse(null);
            if (connection == null) {
                connection = new Connection(this, target, weight);
                target.connections.in.add(connection);
                this.connections.out.add(connection);
                connections.add(connection);
            } else {
                connections.add(connection);
            }
        }
        return connections;
    }

    boolean isNotProjectingTo(final Node node) {
        return (!this.equals(node) || this.connections.self.weight == 0)
                && this.connections.out.stream().noneMatch(connection -> connection.to.equals(node));
    }

    void disconnect(final Node node) {
        this.disconnect(node, false);
    }

    void clear() {
        for (final Connection connection : this.connections.in) {
            connection.eligibility = 0;
            connection.xTraceNodes = new ArrayList<>();
            connection.xTraceValues = new ArrayList<>();
        }
        this.connections.gated.forEach(connection -> connection.gain = 0);
        this.old = 0;
        this.state = 0;
        this.activation = 0;
    }

    private void disconnect(final Node node, final boolean twoSided) {
        if (this.equals(node)) {
            this.connections.self.weight = 0;
            return;
        }
        for (final Connection connection : new ArrayList<>(this.connections.out)) {
            if (connection.to.equals(node)) {
                this.connections.out.remove(connection);
                connection.to.connections.in.remove(connection);
                if (connection.gater != null) {
                    connection.gater.ungate(connection);
                }
                break;
            }
        }
        if (twoSided) {
            node.disconnect(this);
        }
    }

    void ungate(final Connection connection) {
        this.ungate(new Connection[]{connection});
    }

    private void ungate(final Connection[] connections) {
        for (int i = connections.length - 1; i >= 0; i--) {
            final Connection connection = connections[i];
            this.connections.gated.remove(connection);
            connection.gater = null;
            connection.gain = 1;
        }
    }

    void gate(final Connection connection) {
        this.gate(new Connection[]{connection});
    }

    private void gate(final Connection[] connections) {
        Arrays.stream(connections).forEach(connection -> connection.gater = this);
        this.connections.gated.addAll(Arrays.asList(connections));
    }

    void mutate(final Mutation method) {
        switch (method) {
            case MOD_ACTIVATION:
                this.activationType = method.allowed[(int) Math.floor(Math.random() * method.allowed.length)];
                break;
            case MOD_BIAS:
                this.bias += Math.random() * (method.max - method.min) + method.min;
                break;
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

    enum NodeType {HIDDEN, INPUT, OUTPUT, CONSTANT}

}
