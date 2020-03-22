package architecture;

import static methods.Utils.pickRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.stream.IntStream;
import methods.Mutation;
import methods.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class NEATTest {
	private static double learnSet(final @NotNull Network network, final double[][] inputs, final double[][] outputs) {
		final EvolveOptions options = new EvolveOptions();
		options.setMutations(Mutation.ALL);
		options.setPopulationSize(1000);
		options.setElitism(10);
		options.setMutationRate(0.7);
		options.setError(0.05);
		return network.evolve(inputs, outputs, options);
	}

	@Test
	public void testJSON() {
		final Network original = new Network(10, 10);
		IntStream.range(0, 20)
			.mapToObj(i -> pickRandom(Mutation.ALL))
			.forEach(original::mutate);

		final Network copied = original.copy();

		assertEquals(original.input, copied.input);
		assertEquals(original.output, copied.output);
		assertEquals(original.nodes, copied.nodes);
		assertTrue(Utils.collectionsEqual(original.connections, copied.connections));
		assertTrue(Utils.collectionsEqual(original.selfConnections, copied.selfConnections));
		assertTrue(Utils.collectionsEqual(original.gates, copied.gates));
	}

	@Test
	public void testAND() {
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
		assertTrue(learnSet(network, inputs, outputs) <= 0.05);
	}

	@Test
	public void testXOR() {
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
		assertTrue(learnSet(network, inputs, outputs) <= 0.05);
	}

	@Test
	public void testXNOR() {
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
		assertTrue(learnSet(network, inputs, outputs) <= 0.05);
	}

	@Test
	public void testNot() {
		final double[][] inputs = new double[][]{
			new double[]{0},
			new double[]{1},
		};
		final double[][] outputs = new double[][]{
			new double[]{1},
			new double[]{0},
		};

		final Network network = new Network(1, 1);
		assertTrue(learnSet(network, inputs, outputs) <= 0.05);
	}

	@Test
	public void testNAND() {
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
		assertTrue(learnSet(network, inputs, outputs) <= 0.05);
	}

	@Test
	public void testNOR() {
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
		assertTrue(learnSet(network, inputs, outputs) <= 0.05);
	}
}
