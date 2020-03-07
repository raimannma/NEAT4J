package architecture;

import java.util.ArrayList;
import java.util.List;

class ConnectionList {
    final Connection self;
    final List<Connection> in;
    final List<Connection> out;
    final List<Connection> gated;

    ConnectionList(final Node node) {
        this.in = new ArrayList<>();
        this.out = new ArrayList<>();
        this.gated = new ArrayList<>();
        this.self = new Connection(node, node, 0);
    }
}
