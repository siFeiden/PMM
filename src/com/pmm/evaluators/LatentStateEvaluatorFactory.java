package com.pmm.evaluators;

import jMEF.PVector;

import java.util.Vector;

public class LatentStateEvaluatorFactory {
	public static LatentStateEvaluator getInstance() {
		return new MinDeviationEvaluator();
	}

	private static class MinDeviationEvaluator implements LatentStateEvaluator {

		@Override
		public double evaluate(Vector<PVector>[] clusters, PVector home, PVector work) {
			double homeDist = 0;
			for ( PVector p : clusters[0] ) {
				homeDist += p.Minus(home).norm2();
			}
			homeDist /= clusters[0].size();

			double workDist = 0;
			for ( PVector p : clusters[1] ) {
				workDist += p.Minus(work).norm2();
			}
			workDist /= clusters[1].size();

			return (homeDist + workDist) / 2d;
		}
	}
}
