package methods;

import java.util.List;
import java.util.SplittableRandom;
import org.jetbrains.annotations.NotNull;

public enum Utils {
    ;
    private static final SplittableRandom rand = new SplittableRandom();

    public static int randInt(final int min, final int max) {
        return rand.nextInt(min, max);
    }

    public static double randDouble(final double min, final double max) {
        return rand.nextDouble(min, max);
    }

    public static double randDouble(final double max) {
        return rand.nextDouble(max);
    }

    public static <T> T pickRandom(@NotNull final T[] arr) {
        return arr[randInt(arr.length)];
    }

    public static int randInt(final int max) {
        return rand.nextInt(max);
    }

    public static <T> T pickRandom(final @NotNull List<T> list) {
        return list.get(randInt(list.size()));
    }

    public static double randDouble() {
        return rand.nextDouble();
    }
}
