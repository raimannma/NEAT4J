package methods;

import static methods.Utils.pickRandom;
import static methods.Utils.randDouble;
import static methods.Utils.randInt;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import architecture.Connection;
import architecture.Network;
import architecture.Node;

public enum Mutation {
  ADD_NODE(false) {
    @Override
    public void mutate(final Network network) {
      if (network.connections.isEmpty()) {
        return;
      }
      final Connection connection = pickRandom(network.connections);
      network.disconnect(connection.from, connection.to);

      final Node node = new Node(Node.NodeType.HIDDEN);
      node.mutate(MOD_ACTIVATION);
      network.nodes.add(Math.max(0, Math.min(network.nodes.indexOf(connection.to), network.nodes.size() - network.output)), node);

      if (connection.gateNode != null && randDouble() >= 0.5) {
        network.gate(connection.gateNode, network.connect(connection.from, node).get(0));
      } else if (connection.gateNode != null) {
        network.gate(connection.gateNode, network.connect(node, connection.to).get(0));
      }
    }
  },
  SUB_NODE(false) {
    @Override
    public void mutate(final Network network) {
      if (network.nodes.size() == network.input + network.output) {
        return;
      }

      network.remove(network.nodes.get((int) Math.floor(randDouble() * (network.nodes.size() - network.output - network.input) + network.input)));
    }
  },
  ADD_CONN(false) {
    @Override
    public void mutate(final Network network) {
      final ArrayList<Node[]> availableNodes = new ArrayList<>();
      for (int i = 0; i < network.nodes.size() - network.output; i++) {
        final Node node = network.nodes.get(i);
        for (int j = Math.max(i + 1, network.input); j < network.nodes.size(); j++) {
          final Node node2 = network.nodes.get(j);
          if (node.isNotProjectingTo(node2)) {
            availableNodes.add(new Node[] {node, node2});
          }
        }
      }
      if (availableNodes.isEmpty()) {
        return;
      }
      final Node[] pair = pickRandom(availableNodes);
      network.connect(pair[0], pair[1]);
    }
  },
  SUB_CONN(false) {
    @Override
    public void mutate(final Network network) {
      final ArrayList<Connection> availableConnections = new ArrayList<>();
      network.connections.stream()
        .filter(conn -> !conn.from.in.isEmpty()
          && !conn.to.out.isEmpty()
          && network.nodes.indexOf(conn.to) > network.nodes.indexOf(conn.from))
        .forEach(availableConnections::add);
      if (availableConnections.isEmpty()) {
        return;
      }
      final Connection randomConn = pickRandom(availableConnections);
      network.disconnect(randomConn.from, randomConn.to);
    }
  },
  MOD_WEIGHT(false) {
    @Override
    public void mutate(final Network network) {
      final ArrayList<Connection> allConnections = new ArrayList<>(network.connections);
      allConnections.addAll(network.selfConnections);
      if (allConnections.isEmpty()) {
        return;
      }

      final Connection connection = pickRandom(allConnections);
      connection.weight += randDouble(this.min, this.max);
    }
  },
  MOD_BIAS(false) {
    @Override
    public void mutate(final Network network) {
      network.nodes.get(randInt(network.input, network.nodes.size())).mutate(MOD_BIAS);
    }
  },
  MOD_ACTIVATION(Activation.values()) {
    @Override
    public void mutate(final Network network) {
      if (!this.mutateOutput && network.input + network.output == network.nodes.size()) {
        return;
      }
      final int index = (int) Math.floor(randDouble() * (network.nodes.size() - (this.mutateOutput ? 0 : network.output) - network.input) + network.input);
      network.nodes.get(index).mutate(MOD_ACTIVATION);
    }
  },
  ADD_SELF_CONN(false) {
    @Override
    public void mutate(final Network network) {
      final List<Node> poss = IntStream.range(network.input, network.nodes.size())
        .mapToObj(network.nodes::get)
        .filter(node1 -> node1.self.weight == 0)
        .collect(Collectors.toList());
      if (poss.isEmpty()) {
        return;
      }
      final Node node = pickRandom(poss);
      network.connect(node, node);
    }
  },
  SUB_SELF_CONN(false) {
    @Override
    public void mutate(final Network network) {
      if (network.selfConnections.isEmpty()) {
        return;
      }
      final Connection connection = pickRandom(network.selfConnections);
      network.disconnect(connection.from, connection.to);
    }
  },
  ADD_GATE(false) {
    @Override
    public void mutate(final Network network) {
      final ArrayList<Connection> allConnections = new ArrayList<>(network.connections);
      allConnections.addAll(network.selfConnections);

      final List<Connection> availableConnections = allConnections.stream()
        .filter(connection1 -> connection1.gateNode == null)
        .collect(Collectors.toList());
      if (availableConnections.isEmpty()) {
        return;
      }

      network.gate(network.nodes.get(randInt(network.input, network.nodes.size())), pickRandom(availableConnections));
    }
  },
  SUB_GATE(false) {
    @Override
    public void mutate(final Network network) {
      if (network.gates.isEmpty()) {
        return;
      }
      network.removeGate(pickRandom(network.gates));
    }
  },
  ADD_BACK_CONN(false) {
    @Override
    public void mutate(final Network network) {
      final ArrayList<Node[]> availableNodes = new ArrayList<>();
      for (int i = network.input; i < network.nodes.size(); i++) {
        final Node node = network.nodes.get(i);
        for (int j = network.input; j < i; j++) {
          final Node node2 = network.nodes.get(j);
          if (node.isNotProjectingTo(node2)) {
            availableNodes.add(new Node[] {node, node2});
          }
        }
      }

      if (availableNodes.isEmpty()) {
        return;
      }

      final Node[] pair1 = pickRandom(availableNodes);
      network.connect(pair1[0], pair1[1]);
    }
  },
  SUB_BACK_CONN(false) {
    @Override
    public void mutate(final Network network) {
      final ArrayList<Connection> availableConnections = new ArrayList<>();
      network.connections.stream()
        .filter(connection1 -> !connection1.from.out.isEmpty()
          && !connection1.to.in.isEmpty()
          && network.nodes.indexOf(connection1.from) > network.nodes.indexOf(connection1.to))
        .forEach(availableConnections::add);
      if (availableConnections.isEmpty()) {
        return;
      }
      final Connection randomConn = pickRandom(availableConnections);
      network.disconnect(randomConn.from, randomConn.to);
    }
  },
  SWAP_NODES(true) {
    @Override
    public void mutate(final Network network) {
      if (this.mutateOutput && network.nodes.size() - network.input < 2
        || !this.mutateOutput && network.nodes.size() - network.input - network.output < 2) {
        return;
      }
      int index = (int) Math.floor(randDouble() * (network.nodes.size() - (this.mutateOutput ? 0 : network.output) - network.input) + network.input);
      final Node node = network.nodes.get(index);
      index = (int) Math.floor(randDouble() * (network.nodes.size() - (this.mutateOutput ? 0 : network.output) - network.input) + network.input);
      final Node node2 = network.nodes.get(index);

      final double biasTemp = node.bias;
      final Activation activationType = node.activationType;

      node.bias = node2.bias;
      node.activationType = node2.activationType;
      node2.bias = biasTemp;
      node2.activationType = activationType;
    }
  };

  public static final Mutation[] ALL = Mutation.values();
  public static final Mutation[] FFW = new Mutation[] {
    ADD_CONN,
    ADD_NODE,
    MOD_ACTIVATION,
    MOD_BIAS,
    MOD_WEIGHT,
    SUB_CONN,
    SUB_NODE,
    SWAP_NODES
  };
  public final boolean mutateOutput;
  public Activation[] allowed;
  public int min;
  public int max;
  public boolean keepGates;

  Mutation(final boolean mutateOutput) {
    this.min = -1;
    this.max = 1;
    this.mutateOutput = mutateOutput;
    this.keepGates = !mutateOutput;
  }

  Mutation(final Activation[] allowed) {
    this.mutateOutput = true;
    this.allowed = allowed;
  }

  public abstract void mutate(Network network);

  public void setAllowed(final Activation[] allowed) {
    this.allowed = allowed;
  }

  public void setMin(final int min) {
    this.min = min;
  }

  public void setMax(final int max) {
    this.max = max;
  }

  public void setKeepGates(final boolean keepGates) {
    this.keepGates = keepGates;
  }
}
