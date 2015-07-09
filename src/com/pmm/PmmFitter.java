package com.pmm;

import jMEF.*;

import java.util.List;
import java.util.Vector;

class PmmFitter {

	private static final int NUM_ITERATIONS = 10;

	private List<DataPoint> locations;


	protected PmmFitter(List<DataPoint> locations) {
		if ( locations == null || locations.size() < 2 )
			throw new IllegalArgumentException("locations must contain > 1 point");

		this.locations = locations;
	}

	/**
	 * Use expectation maximization (EM) to find two latent states of some locations.
	 * @throws AlgorithmDivergedException thrown if the EM diverges
	 */
	public Pmm.FittingParams fit() throws AlgorithmDivergedException {
		PVector[] points = new PVector[locations.size()];
		for ( int i = 0; i < points.length; i++ ) {
			points[i] = convertDatapointToPVector(locations.get(i));
		}

		// random initial distribution
		Vector<PVector>[] clusters = assignPointsToLatentStates(points, PredicateFactory.<PVector>random());


		MixtureModel mm = null;
		for ( int i = 0; i < NUM_ITERATIONS; i++ ) {
			mm = fitModelParams(points, clusters); // EM / MLE fitting

			final PVectorMatrix homeGaussian = (PVectorMatrix) mm.param[0];
			final PVectorMatrix workGaussian = (PVectorMatrix) mm.param[1];
			clusters = assignPointsToLatentStates(points,
					PredicateFactory.mostProbable(homeGaussian, mm.weight[0], workGaussian, mm.weight[1]));

			if ( clusters[0].size() == 0 || clusters[1].size() == 0 )
				throw new AlgorithmDivergedException();
		}


		final PVectorMatrix homeGaussian = (PVectorMatrix) mm.param[0];
		final PVectorMatrix workGaussian = (PVectorMatrix) mm.param[1];

		long[] homeStamps = clusterToTimestampsArray(clusters[0]);
		long[] workStamps = clusterToTimestampsArray(clusters[1]);

		double[] homeMeanAndVar = Timestamps.circularMeanAndVariance(homeStamps);
		double[] workMeanAndVar = Timestamps.circularMeanAndVariance(workStamps);

		return new Pmm.FittingParams(
				homeGaussian, workGaussian,
				homeMeanAndVar[0], homeMeanAndVar[1],
				workMeanAndVar[0], workMeanAndVar[1]);
	}

	private long[] clusterToTimestampsArray(Vector<PVector> cluster) {
		long[] stamps = new long[cluster.size()];

		for ( int i = 0; i < cluster.size(); i++ ) {
			PVectorWithTime vt = (PVectorWithTime) cluster.get(i);
			stamps[i] = vt.time;
		}

		return stamps;
	}

	/**
	 * Convert {@link DataPoint} to {@link PVector}
	 * @param dataPoint the DataPoint to convert
	 * @return 2-dimensional PVector with latitude as x-coordinate, longitude as y-coordinate
	 */
	private PVector convertDatapointToPVector(DataPoint dataPoint) {
		if ( dataPoint == null )
			return null;

		PVector vector = new PVector(2);
		vector.array[0] = dataPoint.getLatitude();
		vector.array[1] = dataPoint.getLongitude();
//		vector.array[2] = dataPoint.getLongTime();

		PVectorWithTime v = new PVectorWithTime(2, dataPoint.getLongTime());
		v.array[0] = dataPoint.getLatitude();
		v.array[1] = dataPoint.getLongitude();


		return v;
	}

	/**
	 * Group points into two clusters using a predicate.
	 * Point p is assigned to cluster 0 if the predicate tests true, else to cluster 1.
	 * @param points points to assign
	 * @param categorizer used to assign points
	 * @return clustering induced by categorizer
	 */
	Vector<PVector>[] assignPointsToLatentStates(PVector[] points, Predicate<PVector> categorizer) {
		@SuppressWarnings("unchecked")
		Vector<PVector>[] clusters = new Vector[2];
		clusters[0] = new Vector<>();
		clusters[1] = new Vector<>();

		for ( PVector point : points ) {
			if ( categorizer.test(point) )
				clusters[0].add(point);
			else
				clusters[1].add(point);
		}

//		System.out.println("sizes: " + clusters[0].size() + ", " + clusters[1].size());

		return clusters;
	}

	/**
	 * Expectation maximization for two clusters of points.
	 * clusters should be a parition of points.
	 * @param points all points
	 * @param clusters index 0 is the cluster of points that belong to home, index 1 for work
	 * @return MixtureModel fitted to the clusters using MLE
	 */
	MixtureModel fitModelParams(PVector[] points, Vector<PVector>[] clusters) {
//		MixtureModel mm = BregmanSoftClustering.initialize(clusters, new MultivariateGaussian());
//		mm = BregmanSoftClustering.run(points, mm);


		MixtureModel home = doClustering(clusters[0].toArray(new PVector[clusters[0].size()]), clusters[0]);
		MixtureModel work = doClustering(clusters[1].toArray(new PVector[clusters[1].size()]), clusters[1]);

		MixtureModel result = new MixtureModel(2);
		result.weight[0] = result.weight[1] = 0.5;
		result.param[0] = home.param[0];
		result.param[1] = work.param[0];

		return result;
	}

	@SafeVarargs
	private final MixtureModel doClustering(PVector[] points, Vector<PVector>... clusters) {
		MixtureModel model = BregmanSoftClustering.initialize(clusters, new MultivariateGaussian());
		model = BregmanSoftClustering.run(points, model);
		return model;
	}

	/**
	 * Thrown by {@link #fit()} if the EM diverges.
	 */
	public static class AlgorithmDivergedException extends Throwable { }
}
