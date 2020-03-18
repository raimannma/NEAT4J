package methods;

import java.util.List;
import java.util.SplittableRandom;
import org.jetbrains.annotations.NotNull;

/**
 * Utils.
 * <p>
 * Here are some useful functions.
 */
public enum Utils {
  ;
  /**
   * The Random object used for methods.
   */
  private static final SplittableRandom rand = new SplittableRandom();

  /**
   * Generates a random integer between min and max.
   *
   * @param min the minimum value
   * @param max the maximum value
   * @return the random integer between min and max
   */
  public static int randInt(final int min, final int max) {
    return rand.nextInt(min, max);
  }

  /**
   * Generates a random double between min and max.
   *
   * @param min the minimum value
   * @param max the maximum value
   * @return the random double between min and max
   */
  public static double randDouble(final double min, final double max) {
    return rand.nextDouble(min, max);
  }

  /**
   * Generates a random double between min and max.
   *
   * @param max the maximum value
   * @return the random double between 0 and max
   */
  public static double randDouble(final double max) {
    return rand.nextDouble(max);
  }

  /**
   * Chooses a random element from the given array.
   *
   * @param <T> the type of the array elements
   * @param arr the input array
   * @return the random element from the input array
   */
  public static <T> T pickRandom(@NotNull final T[] arr) {
    return arr[randInt(arr.length)];
  }

  /**
   * Generates a random integer between min and max.
   *
   * @param max the maximum value
   * @return the random integer between 0 and max
   */
  public static int randInt(final int max) {
    return rand.nextInt(max);
  }

  /**
   * Chooses a random element from the given list.
   *
   * @param <T>  the type of the list elements
   * @param list the input list
   * @return the random element from the input list
   */
  public static <T> T pickRandom(final @NotNull List<T> list) {
    return list.get(randInt(list.size()));
  }

  /**
   * Generates a random double between min and max.
   *
   * @return the random double between 0 and 1
   */
  public static double randDouble() {
    return rand.nextDouble();
  }

  /**
   * Generates a random boolean.
   *
   * @return the random boolean
   */
  public static boolean randBoolean() {
    return rand.nextBoolean();
  }
}
