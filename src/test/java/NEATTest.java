import architecture.EvolveOptions;
import architecture.Network;
import architecture.Node;
import methods.Mutation;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static methods.Utils.pickRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NEATTest {

    @Test
    void testJSON() {
        final Network network = new Network(2, 2);
        IntStream.range(0, 10)
                .mapToObj(i -> pickRandom(Mutation.ALL))
                .forEach(network::mutate);
        testEquality(network, Network.fromJSON(network.toJSON()));
    }

    private static void testEquality(final Network original, final Network copied) {
        assertEquals(original.input, copied.input);
        assertEquals(original.output, copied.output);
        for (final Node node : original.nodes) {
            assertTrue(copied.nodes.contains(node));
        }
    }

    @Test
    void testAND() {
        final double[][] inputs = new double[][]{
                new double[]{0, 0},
                new double[]{0, 1},
                new double[]{1, 0},
                new double[]{1, 1},
        };
        final double[][] outputs = new double[][]{
                new double[]{0},
                new double[]{0},
                new double[]{0},
                new double[]{1},
        };

        final Network network = new Network(2, 1);
        learnSet(network, inputs, outputs);
    }

    private static void learnSet(final Network network, final double[][] inputs, final double[][] outputs) {
        final EvolveOptions options = new EvolveOptions();
        options.setMutations(Mutation.ALL);
        options.setPopulationSize(1000);
        options.setElitism(10);
        options.setMutationRate(0.7);
        options.setError(0.05);
        assertTrue(network.evolve(inputs, outputs, options) <= 0.05);
    }

    @Test
    void testXOR() {
        final double[][] inputs = new double[][]{
                new double[]{0, 0},
                new double[]{0, 1},
                new double[]{1, 0},
                new double[]{1, 1},
        };
        final double[][] outputs = new double[][]{
                new double[]{0},
                new double[]{1},
                new double[]{1},
                new double[]{0},
        };

        final Network network = new Network(2, 1);
        learnSet(network, inputs, outputs);
    }

    @Test
    void testXNOR() {
        final double[][] inputs = new double[][]{
                new double[]{0, 0},
                new double[]{0, 1},
                new double[]{1, 0},
                new double[]{1, 1},
        };
        final double[][] outputs = new double[][]{
                new double[]{1},
                new double[]{0},
                new double[]{0},
                new double[]{1},
        };

        final Network network = new Network(2, 1);
        learnSet(network, inputs, outputs);
    }

    @Test
    void testNot() {
        final double[][] inputs = new double[][]{
                new double[]{0},
                new double[]{1},
        };
        final double[][] outputs = new double[][]{
                new double[]{1},
                new double[]{0},
        };

        final Network network = new Network(1, 1);
        learnSet(network, inputs, outputs);
    }

    @Test
    void testNAND() {
        final double[][] inputs = new double[][]{
                new double[]{0, 0},
                new double[]{0, 1},
                new double[]{1, 0},
                new double[]{1, 1},
        };
        final double[][] outputs = new double[][]{
                new double[]{1},
                new double[]{1},
                new double[]{1},
                new double[]{0},
        };

        final Network network = new Network(2, 1);
        learnSet(network, inputs, outputs);
    }

    @Test
    void testNOR() {
        final double[][] inputs = new double[][]{
                new double[]{0, 0},
                new double[]{0, 1},
                new double[]{1, 0},
                new double[]{1, 1},
        };
        final double[][] outputs = new double[][]{
                new double[]{1},
                new double[]{0},
                new double[]{0},
                new double[]{0},
        };

        final Network network = new Network(2, 1);
        learnSet(network, inputs, outputs);
    }
}
