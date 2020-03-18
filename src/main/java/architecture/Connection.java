package architecture;

import com.google.gson.JsonObject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * The type Connection.
 *
 * @author Manuel Raimann
 */
public class Connection {
  /**
   * The output node of this connection.
   */
  public final Node to;
  /**
   * The input node of this connection.
   */
  public final Node from;
  /**
   * The node which gates this connection.
   */
  public Node gateNode;
  /**
   * The weight of this connection.
   */
  public double weight;
  /**
   * The connection gain.
   * <p>
   * Used for gating, gets multiplied with weight
   */
  public double gain;

  /**
   * Instantiates a new Connection.
   *
   * @param from   The input node of this connection.
   * @param to     The output node of this connection.
   * @param weight The weight of this connection.
   */
  Connection(final Node from, final Node to, final double weight) {
    this.from = from;
    this.to = to;
    this.gain = 1;
    this.weight = weight;
    this.gateNode = null;
  }

  /**
   * Calculates the innovation id.
   *
   * @param a the a
   * @param b the b
   * @return An Integer that uniquely represents a pair of Integers
   * @see <a href="https://en.wikipedia.org/wiki/Pairing_function">(Cantor pairing function)|Pairing function (Cantor pairing function)}</a>
   */
  public static int getInnovationID(final int a, final int b) {
    return (int) Math.floor(0.5
      * (a + b)
      * (a + b + 1)
      + b);
  }


  /**
   * Stores connection data in an array.
   * Used for crossover.
   * <p>
   * ConnectionData
   * [0] weight value
   * [1] node from index
   * [2] node to index
   * [3] gate node index
   *
   * @return array containing information about the connection
   */
  @NotNull
  protected Double[] getConnectionData() {
    final Double[] data = new Double[4];
    data[0] = this.weight;
    data[1] = (double) this.from.index;
    data[2] = (double) this.to.index;
    data[3] = this.gateNode != null ? this.gateNode.index : Double.NaN;
    return data;
  }

  /**
   * Converts a node to JsonObject
   *
   * @return the connection as JsonObject
   */
  public JsonObject toJSON() {
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
    return this.weight == that.weight
      && this.from.toJSON().toString().equals(that.from.toJSON().toString())
      && this.to.toJSON().toString().equals(that.to.toJSON().toString());
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
