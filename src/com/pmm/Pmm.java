package com.pmm;

import com.pmm.loc.ILocation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Math.*;

/**
 * Implements the Periodic Mobility Model (PMM) algorithm by Cho et al.,
 * introduced in Friendship and Mobility: User Movement In Location-Based Social Networks.
 */
public class Pmm {

  /**
   * The number of fits for the PMM that should be calculated.
   */
  private static final int NUM_FITS = 10;

  private List<FittingParams> fittedParams = new ArrayList<>();


  /**
   * Create a PMM fit using the given location history of the user.
   *
   * @param previousLocations the user's previous locations
   */
  public Pmm(List<ILocation> previousLocations) {
    this.fillFittingParams(previousLocations);
  }

  /**
   * Estimate the probability that the user will next be in the given location.
   * This will estimate the probability for every fit and choose the best one.
   *
   * @param nextLocation the location to estimate
   * @return the estimated probability
   */
  public double estimateNextLocationProbability(ILocation nextLocation) {
    EstimationStrategy strategy = EstimationStrategy.getInstance();

    for ( FittingParams param : fittedParams ) {
      double currEstimation = estimateFromFit(param, nextLocation);
      strategy.nextEstimate(currEstimation);
    }

    return strategy.get(fittedParams.size());
  }

  /**
   * Creates NUM_FITS fits for the PMM algorithm using the given user locations.
   *
   * @param locations the user's previous locations.
   */
  private void fillFittingParams(List<ILocation> locations) {
    for ( int i = 0; i < NUM_FITS; i++ ) {
      PmmFitter fitter = new PmmFitter(locations);
      fittedParams.add(fitter.fit());
    }
  }

  /**
   * Estimate the probability that the user will next be in the given location using the given fit.
   *
   * @param param        the fit for the PMM
   * @param nextLocation the location to estimate
   * @return the estimated probability
   */
  private double estimateFromFit(FittingParams param, ILocation nextLocation) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(nextLocation.getTimestamp());
    double t = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60d;

    double N_H = truncatedGaussian(param.homeTimeMean, param.homeTimeVariance, t);
    double N_W = truncatedGaussian(param.workTimeMean, param.workTimeVariance, t);

    boolean isAtHome = N_H / (N_H + N_W) > 0.5;
    Gaussian estim = isAtHome ? param.home : param.work;

    return estim.density(nextLocation.getLatitude(), nextLocation.getLongitude());
  }

  /**
   * A function similar to the gaussian density function.
   *
   * @param mean     mean of the distribution
   * @param variance variance of the distribution
   * @param t        time, variable of the function
   * @return the functions value
   */
  private double truncatedGaussian(double mean, double variance, double t) {
    double varSq = variance * variance;
    double r = 0.5 / sqrt(2 * PI * varSq);
    r *= exp(-pow(PI / 12, 2) * pow(t - mean, 2) / (2 * varSq));
    return r;
  }

  /**
   * Holds the parameters of a fit for the PMM.
   */
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
