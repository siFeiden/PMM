package com.pmm;

/**
 * A container for two gaussians.
 */
public class GaussianPair {

  public Gaussian home, work;

  public GaussianPair(Gaussian home, Gaussian work) {
    this.home = home;
    this.work = work;
  }
}
