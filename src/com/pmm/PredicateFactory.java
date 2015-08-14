package com.pmm;

import Jama.Matrix;
import com.pmm.loc.Location;

import java.util.Random;

public class PredicateFactory {
	private static final Predicate<?> RANDOM = new Predicate<Object>() {
		private final Random random = new Random();

		@Override
		public boolean test(Object value) {
			return random.nextBoolean();
		}
	};

	public static <T> Predicate<T> random() {
		@SuppressWarnings("unchecked")
		Predicate<T> p = (Predicate<T>) RANDOM;
		return p;
	}

	public static Predicate<Location> mostProbable(final Gaussian first, final Gaussian second) {
		return new Predicate<Location>() {
			@Override
			public boolean test(Location l) {
				Matrix x = new Matrix(l.latLonArray(), 2);
				double firstDensity = first.density(l.getLatitude(), l.getLongitude());
				double secondDensity = second.density(l.getLatitude(), l.getLongitude());

				return firstDensity > secondDensity;
			}
		};
	}

}
