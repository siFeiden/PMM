package com.pmm.evaluators;

import jMEF.PVector;

import java.util.Vector;

public interface LatentStateEvaluator {
	double evaluate(Vector<PVector>[] clusters, PVector home, PVector work);
}
