package com.pmm;

import Jama.Matrix;

import java.util.Objects;

import static java.lang.Math.*;

/**
 * A two dimensional gaussian distribution with its density function.
 */
public class Gaussian {

  /**
   * Mean of a gaussian.
   * Must be a 2x1 matrix.
   */
  private final Matrix mean;

  /**
   * Covariance of a gaussian.
   * Must be a 2x2 matrix.
   */
  private final Matrix covariance;


  /**
   * Create a gaussian with the specified parameters.
   *
   * @param mean       non null mean of the gaussian
   * @param covariance non null covariance of the gaussian
   * @throws NullPointerException if mean or covariance are null
   */
  public Gaussian(Matrix mean, Matrix covariance) {
    Objects.requireNonNull(mean);
    Objects.requireNonNull(covariance);

    if ( mean.getColumnDimension() != 1 || mean.getRowDimension() != 2 ) {
      throw new IllegalArgumentException("mean must be 2x1");
    }

    if ( covariance.getColumnDimension() != 2 || covariance.getRowDimension() != 2 ) {
      throw new IllegalArgumentException("covariance must be 2x2");
    }

    this.mean = mean;
    this.covariance = covariance;
  }

  /**
   * Calculates the density function of the gaussian.
   *
   * @param x first coordinate of the argument
   * @param y second coordinate of the argument
   * @return density of (x, y)
   */
  public double density(double x, double y) {
    Matrix p = new Matrix(new double[] { x, y }, 2);

    p.minusEquals(mean);
    Matrix inv;
    try {
      inv = covariance.inverse();
    } catch ( RuntimeException e ) {
      inv = Matrix.identity(2, 2);
    }
    Matrix a = p.transpose().times(inv.times(p));
    double v1 = exp(-.5 * a.get(0, 0));
    double v2 = sqrt(4 * PI * PI * covariance.det());

    return v1 / v2;
  }

  public double getMeanX() {
    return mean.get(0, 0);
  }

  public double getMeanY() {
    return mean.get(1, 0);
  }

  public double getCovariance1() {
    return covariance.get(0, 0);
  }

  public double getCovariance2() {
    return covariance.get(1, 1);
  }
}
