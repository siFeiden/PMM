package com.pmm;

import Jama.Matrix;
import com.pmm.loc.ILocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates a fit for the PMM algorithm.
 */
class PmmFitter {

  private static final int NUM_ITERATIONS = 100;

  /**
   * List of the locations that belong to the "home" latent state.
   */
  private List<ILocation> homeCluster;

  /**
   * List of the locations that belong to the "work" latent state.
   */
  private List<ILocation> workCluster;

  /**
   * Create a fit for the PMM algorithm.
   * @param locations the locations to use for the fit
   */
  PmmFitter(List<ILocation> locations) {
    if ( locations == null || locations.size() <= 1 ) {
      throw new IllegalArgumentException("locations must contain more than 1 point");
    }

    this.homeCluster = new ArrayList<>(locations);
    this.workCluster = new ArrayList<>();
  }

  /**
   * Use expectation maximization (EM) to find two latent states of the given locations.
   */
  public Pmm.FittingParams fit() {
    // random initial distribution
    assignPointsToLatentStates(PredicateFactory.<ILocation>random());


    GaussianPair fit = new GaussianPair(null, null);
    for ( int i = 0; i < NUM_ITERATIONS; i++ ) {
      fit.home = fitModelParams(homeCluster); // EM / MLE fitting
      fit.work = fitModelParams(workCluster);

      assignPointsToLatentStates(PredicateFactory.mostProbable(fit.home, fit.work));
    }

    long[] homeStamps = clusterToTimestampsArray(homeCluster);
    long[] workStamps = clusterToTimestampsArray(workCluster);

    double[] homeMeanAndVar = Timestamps.circularMeanAndVariance(homeStamps);
    double[] workMeanAndVar = Timestamps.circularMeanAndVariance(workStamps);

    return new Pmm.FittingParams(fit,
        homeMeanAndVar[0], homeMeanAndVar[1],
        workMeanAndVar[0], workMeanAndVar[1]);
  }

  /**
   * Extract the timestamps from the parameter.
   * @param locations the locations to get the timestamps from
   * @return array with the timestamps of the given locations. The order is the same as in the list.
   */
  private long[] clusterToTimestampsArray(List<ILocation> locations) {
    long[] stamps = new long[locations.size()];

    for ( int i = 0; i < locations.size(); i++ ) {
      stamps[i] = locations.get(i).getTimestamp();
    }

    return stamps;
  }

  /**
   * Group points into two clusters using a predicate.
   * Point p is assigned to homeCluster if the predicate tests true, else to workCluster.
   *
   * @param categorizer used to assign points
   */
  void assignPointsToLatentStates(Predicate<ILocation> categorizer) {
    ArrayList<ILocation> all = new ArrayList<>(homeCluster);
    all.addAll(workCluster);

    homeCluster.clear();
    workCluster.clear();

    for ( ILocation l : all ) {
      if ( categorizer.test(l) ) {
        homeCluster.add(l);
      } else {
        workCluster.add(l);
      }
    }
  }

  /**
   * Expectation maximization for two clusters of points.
   * clusters should be a partition of points.
   */
  private Gaussian fitModelParams(List<ILocation> locations) {
    int n = locations.size();

    Matrix X = new Matrix(n, 2);
    double latSum = 0;
    double lonSum = 0;
    int row = 0;

    for ( ILocation l : locations ) {
      latSum += l.getLatitude();
      lonSum += l.getLongitude();
      X.set(row, 0, l.getLatitude());
      X.set(row, 1, l.getLongitude());
      row++;
    }

    Matrix mean = new Matrix(1, 2);
    mean.set(0, 0, latSum / n);
    mean.set(0, 1, lonSum / n);

    X.minusEquals(new Matrix(n, 1, 1).times(mean));
    Matrix S = X.transpose().times(X).timesEquals(1.0 / (n - 1.0));

    return new Gaussian(mean.transpose(), S);
  }
}
