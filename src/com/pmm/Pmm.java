package com.pmm;

import com.pmm.loc.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Math.*;

public class Pmm {

	private static final int NUM_FITS = 10;

	private List<FittingParams> fittedParams = new ArrayList<>();


	public Pmm(List<Location> previousLocations) {
		this.fillFittingParams(previousLocations);
	}

	public double estimateNextLocationProbability(Location nextLocation) {
		EstimationStrategy strategy = EstimationStrategy.getInstance();

		for ( FittingParams param : fittedParams ) {
			double currEstimation = estimateFromFit(param, nextLocation);
			strategy.nextEstimate(currEstimation);
		}

		return strategy.get(fittedParams.size());
	}

	public GaussianPair getFittedGaussianPair() {
		FittingParams p = fittedParams.get(0);
		return new GaussianPair(p.home, p.work);
	}

	private void fillFittingParams(List<Location> locations) {
		for ( int i = 0; i < NUM_FITS; i++ ) {
			PmmFitter fitter = new PmmFitter(locations);
			fittedParams.add(fitter.fit());
		}
	}

	private double estimateFromFit(FittingParams param, Location nextLocation) {
		Calendar c = Calendar.getInstance();
		c.setTime(nextLocation.getTime());
		double t = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60d;

		double N_H = truncatedGaussian(param.homeTimeMean, param.homeTimeVariance, t);
		double N_W = truncatedGaussian(param.workTimeMean, param.workTimeVariance, t);

//		System.out.printf("N_H: %.5f, N_W: %.5f \n", (N_H), (N_W));

		boolean isAtHome = N_H / (N_H + N_W) > 0.5;
		Gaussian estim = isAtHome ? param.home : param.work;

		return  estim.density(nextLocation.getLatitude(), nextLocation.getLongitude());
	}

	private double truncatedGaussian(double mean, double variance, double t) {
		double varSq = variance * variance;
		double r = 0.5 / sqrt(2 * PI * varSq);
		r *= exp(-pow(PI / 12, 2) * pow(t - mean, 2) / (2 * varSq));
		return r;
	}

	static class FittingParams {
		Gaussian home, work;
		double homeTimeMean, homeTimeVariance;
		double workTimeMean, workTimeVariance;

		FittingParams(GaussianPair fit, double homeTimeMean, double homeTimeVariance, double workTimeMean, double workTimeVariance) {
			this.home = fit.home;
			this.work = fit.work;
			this.homeTimeMean = homeTimeMean;
			this.homeTimeVariance = homeTimeVariance;
			this.workTimeMean = workTimeMean;
			this.workTimeVariance = workTimeVariance;
		}
	}
}
