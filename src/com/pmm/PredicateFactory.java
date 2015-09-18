package com.pmm;

import Jama.Matrix;
import com.pmm.loc.ILocation;

import java.util.Objects;
import java.util.Random;

/**
 * A factory for certain {@link Predicate}s.
 */
public class PredicateFactory {

  private static final Predicate<?> RANDOM = new Predicate<Object>() {
    private final Random random = new Random();

    @Override
    public boolean test(Object value) {
      return random.nextBoolean();
    }
  };

  /**
   * Creates a predicate that returns true or false randomly, based on a uniform distribution.
   *
   * @param <T> type parameter of the Predicate
   * @return the random predicate
   */
  public static <T> Predicate<T> random() {
    @SuppressWarnings("unchecked")
    Predicate<T> p = (Predicate<T>) RANDOM;
    return p;
  }

  /**
   * Creates a predicate that tests to which {@link Gaussian} a {@link ILocation} belongs.
   *
   * @param first  the first gaussian to test
   * @param second the second gaussian to test
   * @return true if the density of the first gaussian is greater
   * than the density of the second gaussian at the location to test.
   * @throws NullPointerException if first or second are null
   */
  public static Predicate<ILocation> mostProbable(final Gaussian first, final Gaussian second) {
    Objects.requireNonNull(first);
    Objects.requireNonNull(second);

    return new Predicate<ILocation>() {
      @Override
      public boolean test(ILocation l) {
        double[] latLonArray = { l.getLatitude(), l.getLongitude() };
        Matrix x = new Matrix(latLonArray, 2);
        double firstDensity = first.density(l.getLatitude(), l.getLongitude());
        double secondDensity = second.density(l.getLatitude(), l.getLongitude());

        return firstDensity > secondDensity;
      }
    };
  }

}
