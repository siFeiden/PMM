package com.pmm;

public abstract class EstimationStrategy {

	abstract void nextEstimate(double newEstimate);
	abstract double get(int numFits);

	private EstimationStrategy() {
	}

	static EstimationStrategy getInstance() {
		return new EstimationStrategy.Max();
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
}
