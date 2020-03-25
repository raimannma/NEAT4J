package com.github.raimannma.nn.methods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.SplittableRandom;
import org.jetbrains.annotations.NotNull;

/**
 * Utils.
 * <p>
 * Here are some useful functions.
 *
 * @author Manuel Raimann
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
		return max > min ? rand.nextInt(min, max) : min;
	}

	/**
	 * Generates a random double between min and max.
	 *
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return the random double between min and max
	 */
	public static double randDouble(final double min, final double max) {
		return max > min ? rand.nextDouble(min, max) : min;
	}

	/**
	 * Generates a random double between min and max.
	 *
	 * @param max the maximum value
	 * @return the random double between 0 and max
	 */
	public static double randDouble(final double max) {
		return max > 0 ? rand.nextDouble(max) : 0;
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
		return max > 0 ? rand.nextInt(max) : 0;
	}

	/**
	 * Chooses a random element from the given collection.
	 *
	 * @param <T>        the type of the collection elements
	 * @param collection the input collection
	 * @return the random element from the input collection
	 */
	public static <T> T pickRandom(final @NotNull Collection<T> collection) {
		return new ArrayList<>(collection).get(randInt(collection.size()));
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

	/**
	 * Checks if two sets are equal
	 * <p>
	 * DOES NOT CHECK ORDER !
	 *
	 * @param set  first set
	 * @param set1 second set
	 * @param <T>  Generic type parameter
	 * @return are both sets equal?
	 */
	public static <T> boolean setsEqual(final Set<T> set, final Set<T> set1) {
		upperLoop:
		for (final T elem : set) {
			for (final T t : set1) {
				if (elem.equals(t)) {
					continue upperLoop;
				}
			}
			return false;
		}

		upperLoop:
		for (final T elem : set1) {
			for (final T t : set) {
				if (elem.equals(t)) {
					continue upperLoop;
				}
			}
			return false;
		}
		return true;
	}
}
