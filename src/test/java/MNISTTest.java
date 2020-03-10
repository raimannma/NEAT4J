import architecture.EvolveOptions;
import architecture.Network;
import methods.Mutation;
import mnist.MnistReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MNISTTest {
    @Test
    void testMNIST() throws IOException {
        final int[] labels = MnistReader.getLabels("src/test/resources/mnist/label.idx1-ubyte");
        final List<int[][]> images = MnistReader.getImages("src/test/resources/mnist/images.idx3-ubyte");

        final double[][] output = new double[labels.length][10];
        for (int i = 0; i < 500; i++) {
            Arrays.fill(output[i], 0);
            output[i][labels[i]] = 1;
        }

        final double[][] input = new double[images.size()][images.get(0).length * images.get(0)[0].length];
        for (int i = 0; i < 500; i++) {
            final int[][] image = images.get(i);
            final double[] inputRow = new double[image.length * image[0].length];
            int inputIndex = 0;
            for (final int[] row : image) {
                for (final int pixel : row) {
                    inputRow[inputIndex++] = (double) pixel / 256;
                }
            }
            input[i] = inputRow;
        }

        final double[][] trainInput = Arrays.copyOfRange(input, 0, 50000);
        final double[][] testInput = Arrays.copyOfRange(input, 10000, input.length);
        final double[][] trainOutput = Arrays.copyOfRange(output, 0, 50000);
        final double[][] testOutput = Arrays.copyOfRange(output, 10000, output.length);

        final Network network = new Network(784, 10);

        final EvolveOptions options = new EvolveOptions();
        options.setLog(1);
        options.setError(0.05);
        options.setPopulationSize(100);
        options.setElitism(5);
        options.setMutations(Mutation.FFW);
        options.setEqual(true);

        network.evolve(trainInput, trainOutput, options);

        assertTrue(network.test(testInput, testOutput) <= 0.05);
    }
}
