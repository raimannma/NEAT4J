import static methods.Utils.pickRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.stream.IntStream;
import architecture.EvolveOptions;
import architecture.Network;
import methods.Mutation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NEATTest {
  @Test
  void testJSON() {
    final Network original = new Network(2, 2);
    IntStream.range(0, 10)
      .mapToObj(i -> pickRandom(Mutation.ALL))
      .forEach(original::mutate);

    final Network copied = Network.fromJSON(original.toJSON());

    assertEquals(original.input, copied.input);
    assertEquals(original.output, copied.output);
    original.nodes
      .parallelStream()
      .map(node -> copied.nodes.contains(node))
      .forEach(Assertions::assertTrue);
  }

  @Test
  void testAND() {
    final double[][] inputs = new double[][] {
      new double[] {0, 0},
      new double[] {0, 1},
      new double[] {1, 0},
      new double[] {1, 1},
    };
    final double[][] outputs = new double[][] {
      new double[] {0},
      new double[] {0},
      new double[] {0},
      new double[] {1},
    };

    final Network network = new Network(2, 1);
    assertTrue(learnSet(network, inputs, outputs) <= 0.05);
  }

  private static double learnSet(final Network network, final double[][] inputs, final double[][] outputs) {
    final EvolveOptions options = new EvolveOptions();
    options.setMutations(Mutation.ALL);
    options.setPopulationSize(1000);
    options.setElitism(10);
    options.setMutationRate(0.7);
    options.setError(0.05);
    return network.evolve(inputs, outputs, options);
  }

  @Test
  void testXOR() {
    final double[][] inputs = new double[][] {
      new double[] {0, 0},
      new double[] {0, 1},
      new double[] {1, 0},
      new double[] {1, 1},
    };
    final double[][] outputs = new double[][] {
      new double[] {0},
      new double[] {1},
      new double[] {1},
      new double[] {0},
    };

    final Network network = new Network(2, 1);
    assertTrue(learnSet(network, inputs, outputs) <= 0.05);
  }

  @Test
  void testXNOR() {
    final double[][] inputs = new double[][] {
      new double[] {0, 0},
      new double[] {0, 1},
      new double[] {1, 0},
      new double[] {1, 1},
    };
    final double[][] outputs = new double[][] {
      new double[] {1},
      new double[] {0},
      new double[] {0},
      new double[] {1},
    };

    final Network network = new Network(2, 1);
    assertTrue(learnSet(network, inputs, outputs) <= 0.05);
  }

  @Test
  void testNot() {
    final double[][] inputs = new double[][] {
      new double[] {0},
      new double[] {1},
    };
    final double[][] outputs = new double[][] {
      new double[] {1},
      new double[] {0},
    };

    final Network network = new Network(1, 1);
    assertTrue(learnSet(network, inputs, outputs) <= 0.05);
  }

  @Test
  void testNAND() {
    final double[][] inputs = new double[][] {
      new double[] {0, 0},
      new double[] {0, 1},
      new double[] {1, 0},
      new double[] {1, 1},
    };
    final double[][] outputs = new double[][] {
      new double[] {1},
      new double[] {1},
      new double[] {1},
      new double[] {0},
    };

    final Network network = new Network(2, 1);
    assertTrue(learnSet(network, inputs, outputs) <= 0.05);
  }

  @Test
  void testNOR() {
    final double[][] inputs = new double[][] {
      new double[] {0, 0},
      new double[] {0, 1},
      new double[] {1, 0},
      new double[] {1, 1},
    };
    final double[][] outputs = new double[][] {
      new double[] {1},
      new double[] {0},
      new double[] {0},
      new double[] {0},
    };

    final Network network = new Network(2, 1);
    assertTrue(learnSet(network, inputs, outputs) <= 0.05);
  }
}
