package com.pmm;

import io.github.benas.jpopulator.api.Populator;
import io.github.benas.jpopulator.api.Randomizer;
import io.github.benas.jpopulator.impl.PopulatorBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomDatapointGenerator {

	public enum LatentState {
		HOME(50.7653773, 6.0724425),
//		WORK(49.7778813, 6.6494598); // Trier
		WORK(50.7793687, 6.0522158); // Aachen

		private final double lat, lon;

		LatentState(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}
	}

	/**
	 * Generate random DataPoints around the locations of the {@link LatentState}s.
	 * @param radius radius of the spread of the points
	 * @param nums the number of points to generate for each latent state,
	 *             i.e. nums[0] = 10 -> 10 points for first latent state.
	 * @return list of the generated random DataPoints
	 */
	static List<DataPoint> generate(double radius, int... nums) {
		if ( nums == null  ) {
			return null;
		}

		LatentState[] states = LatentState.values();
		int minIndex = Math.min(nums.length, states.length);

		List<DataPoint> points = new ArrayList<>();
		for ( int i = 0; i < minIndex; i++ ) {
			points.addAll(generate(states[i], nums[i], radius));
		}

		return points;
	}

	static List<DataPoint> generate(LatentState state, int n, double radius) {
		Populator populator = new PopulatorBuilder()
				.registerRandomizer(DataPoint.class, double.class, "latitude", new DoubleRandomizer(state.lat, radius))
				.registerRandomizer(DataPoint.class, double.class, "longitude", new DoubleRandomizer(state.lon, radius))
				.build();

		return populator.populateBeans(DataPoint.class, n);
	}

	private static class DoubleRandomizer implements Randomizer<Double> {

		private Random random = new Random();
		private final double center;
		private final double radius;

		public DoubleRandomizer(double center, double radius) {
			this.center = center;
			this.radius = radius;
		}

		@Override
		public Double getRandomValue() {
			return center + radius * (2 * random.nextDouble() - 1);
		}
	}
}