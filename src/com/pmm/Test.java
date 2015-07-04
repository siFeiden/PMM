package com.pmm;

import com.pmm.evaluators.LatentLocationsEvaluation;
import com.pmm.evaluators.LatentStateEvaluator;
import com.pmm.evaluators.LatentStateEvaluatorFactory;
import jMEF.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Test {

	private static final File GPX_TRACKS = new File("coords.gpx");
	private static final File GPX_POINTS = new File("points.gpx");
	private static final int NUM_ITERATIONS = 10;


	public static void main(String[] args) {
//		List<DataPoint> locations = RandomDataPointGenerator.generate(4e-3, 10, 10);
		List<DataPoint> locations = RandomDataPointGenerator.generate(RandomDataPointGenerator.LatentState.HOME, 10, 4e-3);
		locations.addAll(RandomDataPointGenerator.generate(RandomDataPointGenerator.LatentState.WORK, 10, 4e-3));

		System.out.println(locations.size());

		Test t = new Test();
		LatentLocationsEvaluation model = t.multiFit(locations, NUM_ITERATIONS);
//		t.gpxify(model, GPX_POINTS);
//		System.out.println(mm);

		if ( model != null ) {
			System.out.println("h, w");
			System.out.printf("%f,%f\n", model.getHome().array[0], model.getHome().array[1]);
			System.out.printf("%f,%f\n", model.getWork().array[0], model.getWork().array[1]);
			System.out.printf("https://www.google.de/maps/dir/%f,%f/%f,%f/@50.768312,6.0564359,14z/",
					model.getHome().array[0], model.getHome().array[1],
					model.getWork().array[0], model.getWork().array[1]);
		} else {
			System.out.println("no model found");
		}
	}

	/**
	 * Performs several EMs to find latent point by using {@link #singleFit(List)}.
	 * Multiple EMs are done as an EM only converges against local optima.
	 * @param locations  locations to fit the latent points to
	 * @param numFits number of EMs to perform
	 * @return the latent points that were evaluated best
	 */
	public LatentLocationsEvaluation multiFit(List<DataPoint> locations, int numFits) {
		List<LatentLocationsEvaluation> models = new ArrayList<>();
		int divergeCount = 0;

		for ( int i = 0; i < numFits; i++ ) {
			try {
				LatentLocationsEvaluation clusters = singleFit(locations);
				models.add(clusters);
			} catch ( AlgorithmDivergedException e ) {
				// ignore exception
				divergeCount++;
			}
		}

		System.out.printf("%d diverges\n", divergeCount);

		LatentLocationsEvaluation minEntry = null;
		double minEvaluation = Double.MAX_VALUE;
		for ( LatentLocationsEvaluation lla : models ) {
			if ( lla.getEvaluation() < minEvaluation )
				minEntry = lla;
		}

		return minEntry;
	}

	/**
	 * Use expectation maximization (EM) to find two latent points of some locations.
	 * @param locations locations to fit the latent points to
	 * @return Object containing two latent locations and an evaluation of their accuracy
	 * @throws AlgorithmDivergedException thrown if the EM diverges
	 * @see #multiFit(List, int)
	 */
	public LatentLocationsEvaluation singleFit(List<DataPoint> locations) throws AlgorithmDivergedException {
		if ( locations == null || locations.size() < 2 ) {
			throw new NullPointerException("locations must contain > 1 point");
		}

		PVector[] points = new PVector[locations.size()];
		for ( int i = 0; i < points.length; i++ ) {
			points[i] = convertDatapointToPVector(locations.get(i));
		}


		// random initial distribution
		Vector<PVector>[] clusters = assignPointsToLatentsPoint(points, PredicateFactory.<PVector>random());


		MixtureModel mm = null;
		for ( int i = 0; i < NUM_ITERATIONS; i++ ) {
			mm = fitModelParams(points, clusters); // EM / MLE fitting

			final PVectorMatrix homeGaussian = (PVectorMatrix) mm.param[0];
			final PVectorMatrix workGaussian = (PVectorMatrix) mm.param[1];
			clusters = assignPointsToLatentsPoint(points,
					PredicateFactory.mostProbable(homeGaussian, mm.weight[0], workGaussian, mm.weight[1]));

			if ( clusters[0].size() == 0 || clusters[1].size() == 0 )
				throw new AlgorithmDivergedException();
		}


		final PVector home = ((PVectorMatrix) mm.param[0]).v;
		final PVector work = ((PVectorMatrix) mm.param[1]).v;

		LatentStateEvaluator evaluator = LatentStateEvaluatorFactory.getInstance();
		double evaluation = evaluator.evaluate(clusters, home, work);

		return new LatentLocationsEvaluation(home, work, evaluation);
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


		MixtureModel home = dostuff(clusters[0].toArray(new PVector[clusters[0].size()]), clusters[0]);
		MixtureModel work = dostuff(clusters[1].toArray(new PVector[clusters[1].size()]), clusters[1]);

		MixtureModel result = new MixtureModel(2);
		result.weight[0] = result.weight[1] = 0.5;
		result.param[0] = home.param[0];
		result.param[1] = work.param[0];

		return result;
	}

	private MixtureModel dostuff(PVector[] points, Vector<PVector>... clusters) {
		MixtureModel model = BregmanSoftClustering.initialize(clusters, new MultivariateGaussian());
		model = BregmanSoftClustering.run(points, model);
		return model;
	}

	/**
	 * Group points into two clusters using a predicate.
	 * Point p is assigned to cluster 0 if the predicate test true, else to cluster 1.
	 * @param points points to assign
	 * @param categorizer used to assign points
	 * @return clustering induced by categorizer
	 */
	Vector<PVector>[] assignPointsToLatentsPoint(PVector[] points, Predicate<PVector> categorizer) {
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
		return vector;
	}

	/**
	 * Print points in clusters to stdout
	 * @param clusters clusters to print
	 */
	private void printClusters(Vector<PVector>[] clusters) {
		for ( int i = 0; i < clusters.length; i++ ) {
			System.out.println(i);
			System.out.println(clusters[i]);
			System.out.println();
		}
		System.out.println();
	}

	/*private void gpxify(List<Vector<PVector>[]> clustersList, File outFile) {
		try ( PrintWriter writer = new PrintWriter(outFile, "UTF-8") ) {
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
			writer.println("<gpx version=\"1.1\" creator=\"john doe\">");

			for ( Vector<PVector>[] clusters : clustersList ) {
				for ( Vector<PVector> cluster : clusters ) {
					writer.println("\t<rte>");
					for ( PVector point : cluster ) {
						writer.printf("\t\t<rtept lat=\"%f\" lon=\"%f\" ></rtept>\n", point.array[0], point.array[1]);
					}
					writer.println("\t</rte>");
				}
			}

			writer.println("</gpx>");
		} catch ( FileNotFoundException | UnsupportedEncodingException e ) {
			e.printStackTrace();
		}
	}*/

	/**
	 * Write home and work locations of all MixtureModel to a file using gpx-format
	 * @param models models to write to file
	 * @param outFile file to write to
	 */
	private void gpxify(List<MixtureModel> models, File outFile) {
		try ( PrintWriter writer = new PrintWriter(outFile, "UTF-8") ) {
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
			writer.println("<gpx version=\"1.1\" creator=\"john doe\">");

			final String wptFormat = "\t<wpt lat=\"%f\" lon=\"%f\"><name>%s</name></wpt>\n";

			for ( int i = 0; i < models.size(); i++ ) {
				MixtureModel model = models.get(i);

				PVectorMatrix mean = (PVectorMatrix) model.param[0];
				writer.printf(wptFormat, mean.v.array[0], mean.v.array[1], "home" + i);

				mean = (PVectorMatrix) model.param[1];
				writer.printf(wptFormat, mean.v.array[0], mean.v.array[1], "work" + i);
			}

			writer.println("</gpx>");
		} catch ( FileNotFoundException | UnsupportedEncodingException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Thrown by {@link #singleFit(List)} if one cluster is empty.
	 */
	public static class AlgorithmDivergedException extends Throwable { }
}
