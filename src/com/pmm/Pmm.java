package com.pmm;

import jMEF.MultivariateGaussian;
import jMEF.PVector;
import jMEF.PVectorMatrix;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class Pmm {

	private static final int NUM_FITS = 10;

	private List<DataPoint> locations;

	private List<FittingParams> fittedParams = new ArrayList<>();


	public Pmm(List<DataPoint> previousLocations) {
		this.locations = previousLocations;
		this.fillFittingParams();
	}

	public double estimateNextLocationProbability(DataPoint nextLocation) {
		double bestEstimation = Double.MIN_VALUE;

		for ( FittingParams param : fittedParams ) {
			double currEstimation = estimateFromFit(param, nextLocation);
			bestEstimation = Math.max(bestEstimation, currEstimation);
		}

		return bestEstimation;
	}

	private void fillFittingParams() {
		for ( int i = 0; i < NUM_FITS; i++ ) {
			PmmFitter fitter = new PmmFitter(locations);
			try {
				FittingParams fit = fitter.fit();
				fittedParams.add(fit);
			} catch ( PmmFitter.AlgorithmDivergedException e ) {
				System.out.println("Pmm fitting diverged");
			}
		}
	}

	private double estimateFromFit(FittingParams param, DataPoint nextLocation) {
		double N_H = truncatedGaussian(param.homeTimeMean, param.homeTimeVariance, nextLocation.getLongTime());
		double N_W = truncatedGaussian(param.workTimeMean, param.workTimeVariance, nextLocation.getLongTime());

		boolean isAtHome = N_H / (N_H + N_W) > 0.5;
		MultivariateGaussian g = new MultivariateGaussian();

		PVector v = new PVector(2);
		v.array[0] = nextLocation.getLatitude();
		v.array[1] = nextLocation.getLongitude();

		return g.density(v, isAtHome ? param.homeGaussian : param.workGaussian);
	}

	private double truncatedGaussian(double mean, double variance, double t) {
		double varSq = variance * variance;
		double r = 1 / sqrt(2 * PI * varSq);
		r *= exp(-pow(PI / 12, 2) * pow(t - mean, 2) / (2 * varSq));
		return r;
	}

	protected static class FittingParams {
		PVectorMatrix homeGaussian, workGaussian;
		double homeTimeMean, homeTimeVariance;
		double workTimeMean, workTimeVariance;

		protected FittingParams(PVectorMatrix homeGaussian, PVectorMatrix workGaussian, double homeTimeMean, double homeTimeVariance, double workTimeMean, double workTimeVariance) {
			this.homeGaussian = homeGaussian;
			this.workGaussian = workGaussian;
			this.homeTimeMean = homeTimeMean;
			this.homeTimeVariance = homeTimeVariance;
			this.workTimeMean = workTimeMean;
			this.workTimeVariance = workTimeVariance;
		}
	}
}
