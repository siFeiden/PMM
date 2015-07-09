package com.pmm;

import jMEF.MixtureModel;
import jMEF.PVectorMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class GpxWriter {

	public static final File GPX_TRACKS = new File("coords.gpx");
	public static final File GPX_POINTS = new File("points.gpx");

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
}
