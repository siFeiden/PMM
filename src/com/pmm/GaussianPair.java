package com.pmm;

import jMEF.PVectorMatrix;

public class GaussianPair {
	public PVectorMatrix homeGaussian, workGaussian;

	public GaussianPair(PVectorMatrix homeGaussian, PVectorMatrix workGaussian) {
		this.homeGaussian = homeGaussian;
		this.workGaussian = workGaussian;
	}
}
