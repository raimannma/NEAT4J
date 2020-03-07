package architecture;

import java.util.Arrays;

public class DataEntry {
    final double[] input;
    final double[] output;

    public DataEntry(final double[] input, final double[] output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(this.input) + Arrays.hashCode(this.output);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || this.getClass() != o.getClass()) {
            return false;
        } else {
            final DataEntry dataEntry = (DataEntry) o;
            return Arrays.equals(this.input, dataEntry.input) &&
                    Arrays.equals(this.output, dataEntry.output);
        }
    }
}
