package methods;

import java.util.List;

public enum Utils {
    ;

    public static int randInt(final int min, final int max) {
        return (int) Math.floor(Math.random() * (max - min) + min);
    }

    public static double randDouble(final double min, final double max) {
        return Math.random() * (max - min) + min;
    }

    public static double randDouble(final double max) {
        return Math.random() * max;
    }

    public static <T> T pickRandom(final T[] arr) {
        return arr[randInt(arr.length)];
    }

    public static int randInt(final int max) {
        return (int) Math.floor(Math.random() * max);
    }

    public static <T> T pickRandom(final List<T> list) {
        return list.get(randInt(list.size()));
    }
}
