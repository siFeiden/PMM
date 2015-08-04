package com.pmm;

public abstract class EstimationStrategy {

	abstract void nextEstimate(double nextEstimate);

	abstract double get(int numFits);


	private EstimationStrategy() {
	}

	static EstimationStrategy getInstance() {
		return new Normalize();
	}


	static class Max extends EstimationStrategy {
		double estimate = Double.MIN_VALUE;

		void nextEstimate(double nextEstimate) {
			if ( nextEstimate > estimate )
				estimate = nextEstimate;
		}

		@Override
		double get(int numFits) {
			return estimate;
		}
	}

	static class Average extends EstimationStrategy {
		double estimate = 0;

		@Override
		void nextEstimate(double nextEstimate) {
			estimate += nextEstimate;
		}

		@Override
		double get(int numFits) {
			return estimate / numFits;
		}
	}

	static class Normalize extends EstimationStrategy {
		double max = Double.MIN_VALUE, sum = 0;

		@Override
		void nextEstimate(double nextEstimate) {
			if ( nextEstimate > max )
				max = nextEstimate;

			sum += nextEstimate;
		}

		@Override
		double get(int numFits) {
			return Math.min(max / sum, 1.0);
		}
	}
}
