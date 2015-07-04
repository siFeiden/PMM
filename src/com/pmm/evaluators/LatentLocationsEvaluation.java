package com.pmm.evaluators;

import jMEF.PVector;

public class LatentLocationsEvaluation {

	private final PVector home, work;
	private final double evaluation;

	public LatentLocationsEvaluation(PVector home, PVector work, double evaluation) {
		this.home = home;
		this.work = work;
		this.evaluation = evaluation;
	}

	public PVector getHome() {
		return home;
	}

	public PVector getWork() {
		return work;
	}

	public double getEvaluation() {
		return evaluation;
	}
}
