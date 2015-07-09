package com.pmm;

import jMEF.MultivariateGaussian;
import jMEF.PVector;
import jMEF.PVectorMatrix;

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

	static Predicate<PVector> mostProbable(final PVectorMatrix firstParams, final double firstWeight,
	                                       final PVectorMatrix secondParams, final double secondWeight) {
		return new Predicate<PVector>() {
			MultivariateGaussian gaussian = new MultivariateGaussian();

			@Override
			public boolean test(PVector point) {
				double firstDensity = firstWeight * gaussian.density(point, firstParams);
				double secondDensity = secondWeight * gaussian.density(point, secondParams);

				return firstDensity > secondDensity;
			}
		};
	}

	public static Predicate<PVector> mostProbable(PVectorMatrix firstParams, PVectorMatrix secondParams) {
		return mostProbable(firstParams, 1.0, secondParams, 1.0);
	}

}
