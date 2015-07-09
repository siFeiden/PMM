package com.pmm;

import jMEF.PVector;

public class PVectorWithTime extends PVector {

	// imitate style of PVector for consistency
	public long time;


	public PVectorWithTime(int dim) {
		super(dim);
	}

	public PVectorWithTime(int dim, long time) {
		super(dim);
		this.time = time;
	}
}
