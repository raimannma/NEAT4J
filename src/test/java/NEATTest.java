import architecture.*;
import methods.Mutation;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NEATTest {

    @Test
    void testJSON() {
        final Network network = new Network(2, 2);
        IntStream.range(0, 10).mapToObj(i -> Mutation.ALL[(int) Math.floor(Math.random() * Mutation.ALL.length)]).forEach(network::mutate);
        testEquality(network, Network.fromJSON(network.toJSON()));
    }

    private static void testEquality(final Network original, final Network copied) {
        assertEquals(original.input, copied.input);
        assertEquals(original.output, copied.output);
        for (final Node node : original.nodes) {
            assertTrue(copied.nodes.contains(node));
        }
        for (final Connection connection : original.connections) {
            assertTrue(copied.connections.contains(connection));
        }
    }

    @Test
    void testAND() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{0}),
                new DataEntry(new double[]{0, 1}, new double[]{0}),
                new DataEntry(new double[]{1, 0}, new double[]{0}),
                new DataEntry(new double[]{1, 1}, new double[]{1})
        };

        final Network network = new Network(2, 1);
        learnSet(network, trainingSet);
    }

    private static void learnSet(final Network network, final DataEntry[] set) {
        final EvolveOptions options = new EvolveOptions();
        options.setMutations(Mutation.ALL);
        options.setPopulationSize(1000);
        options.setEqual(true);
        options.setElitism(10);
        options.setMutationRate(0.7);
        options.setError(0.03);
        options.setLog(1);
        assertTrue(network.evolve(set, options) <= 0.03);
        System.out.println(network.toString());
    }

    @Test
    void testXOR() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{0}),
                new DataEntry(new double[]{0, 1}, new double[]{1}),
                new DataEntry(new double[]{1, 0}, new double[]{1}),
                new DataEntry(new double[]{1, 1}, new double[]{0})
        };

        final Network network = new Network(2, 1);
        learnSet(network, trainingSet);
    }

    @Test
    void testXNOR() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{1}),
                new DataEntry(new double[]{0, 1}, new double[]{0}),
                new DataEntry(new double[]{1, 0}, new double[]{0}),
                new DataEntry(new double[]{1, 1}, new double[]{1})
        };

        final Network network = new Network(2, 1);
        learnSet(network, trainingSet);
    }

    @Test
    void testNot() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0}, new double[]{1}),
                new DataEntry(new double[]{1}, new double[]{0})
        };

        final Network network = new Network(1, 1);
        learnSet(network, trainingSet);
    }

    @Test
    void testNAND() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{1}),
                new DataEntry(new double[]{0, 1}, new double[]{1}),
                new DataEntry(new double[]{1, 0}, new double[]{1}),
                new DataEntry(new double[]{1, 1}, new double[]{0})
        };

        final Network network = new Network(2, 1);
        learnSet(network, trainingSet);
    }

    @Test
    void testNOR() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{1}),
                new DataEntry(new double[]{0, 1}, new double[]{0}),
                new DataEntry(new double[]{1, 0}, new double[]{0}),
                new DataEntry(new double[]{1, 1}, new double[]{0})
        };

        final Network network = new Network(2, 1);
        learnSet(network, trainingSet);
    }
}
