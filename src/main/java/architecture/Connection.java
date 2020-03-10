package architecture;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class Connection {
    final Node to;
    final Node from;
    double weight;
    Node gateNode;
    double gain;
    ArrayList<Node> xTraceNodes;
    ArrayList<Double> xTraceValues;
    double eligibility;

    Connection(final Node from, final Node to, final double weight) {
        this.from = from;
        this.to = to;
        this.gain = 1;
        this.weight = weight;
        this.gateNode = null;
        this.eligibility = 0;

        this.xTraceNodes = new ArrayList<>();
        this.xTraceValues = new ArrayList<>();
    }

    static int getInnovationID(final int a, final int b) {
        return (int) Math.floor(0.5 * (a + b) * (a + b + 1) + b);
    }

    JsonObject toJSON() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("weight", this.weight);
        return jsonObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.to, this.from, this.weight, this.gateNode, this.gain);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Connection that = (Connection) o;
        return Double.compare(that.weight, this.weight) == 0;
    }

    @Override
    public String toString() {
        return "architecture.Connection{" +
                "to=" + this.to +
                ", weight=" + this.weight +
                ", from=" + this.from +
                '}';
    }
}
