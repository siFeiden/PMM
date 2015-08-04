package com.pmm;

import com.pmm.loc.Location;
import jMEF.PMatrix;
import jMEF.PVector;
import jMEF.PVectorMatrix;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Math.*;

public class Pmm {

	private static final int NUM_FITS = 10;

	private List<FittingParams> fittedParams = new ArrayList<>();


	public Pmm(List<Location> previousLocations) throws FittingFailedException {
		this.fillFittingParams(previousLocations);

		if ( fittedParams.isEmpty() ) {
			throw new FittingFailedException();
		}
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
		return new GaussianPair(p.homeGaussian, p.workGaussian);
	}

	private void fillFittingParams(List<Location> locations) {
		for ( int i = 0; i < NUM_FITS; i++ ) {
			PmmFitter fitter = new PmmFitter(locations);
			try {
				FittingParams fit = fitter.fit();
				fittedParams.add(fit);
//				System.out.printf("home: %.2f, work: %.2f\n", fit.homeTimeMean, fit.workTimeMean);
			} catch ( PmmFitter.AlgorithmDivergedException e ) {
//				System.out.println("Pmm fitting diverged");
			}
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

		PVector v = new PVector(2);
		v.array[0] = nextLocation.getLatitude();
		v.array[1] = nextLocation.getLongitude();

		double p = normalDistribution(v, isAtHome ? param.homeGaussian : param.workGaussian);
//		System.out.println("p: " + p);
		return p;
	}

	private double normalDistribution(PVector x, PVectorMatrix params) {
		PMatrix E = params.M; // covariance matrix
		PVector m = params.v; // mean

		PVector d = x.Minus(m);
		double v1 = d.InnerProduct(E.Inverse().MultiplyVectorRight(d));
		double v2 = exp(-.5 * v1);
		double v3 = sqrt(pow(2.0 * PI, x.dim) * E.Determinant());
		return v2 / v3;
	}

	private double truncatedGaussian(double mean, double variance, double t) {
		double varSq = variance * variance;
		double r = 0.5 / sqrt(2 * PI * varSq);
		r *= exp(-pow(PI / 12, 2) * pow(t - mean, 2) / (2 * varSq));
		return r;
	}

	static class FittingParams {
		PVectorMatrix homeGaussian, workGaussian;
		double homeTimeMean, homeTimeVariance;
		double workTimeMean, workTimeVariance;

		FittingParams(PVectorMatrix homeGaussian, PVectorMatrix workGaussian, double homeTimeMean, double homeTimeVariance, double workTimeMean, double workTimeVariance) {
			this.homeGaussian = homeGaussian;
			this.workGaussian = workGaussian;
			this.homeTimeMean = homeTimeMean;
			this.homeTimeVariance = homeTimeVariance;
			this.workTimeMean = workTimeMean;
			this.workTimeVariance = workTimeVariance;
		}
	}

	public static class FittingFailedException extends Exception {
	}
}
