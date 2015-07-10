package com.pmm;

import com.pmm.loc.DataPoint;
import com.pmm.loc.DatapointGenerator;
import jMEF.PVectorMatrix;

import java.util.List;

public class Test {

	public static void main(String[] args) {
//		List<DataPoint> locations = DatapointGenerator.generateRandom(4e-3, 10, 10);
		List<DataPoint> locations = DatapointGenerator.gowalla();


		final Pmm pmm = new Pmm(locations);
		final PVectorMatrix pvm = pmm.getFirstHomeGaussian();

/*		Mapper mapper = new Mapper() {
			@Override
			public double f(double x, double y) {
				PVector v = new PVector(2);
				v.array[0] = x;
				v.array[1] = y;

				MultivariateGaussian g = new MultivariateGaussian();
				return g.density(v, pvm);
			}
		};

		// Define range and precision for the function to plot
		Range range = new Range(-150, 150);
		int steps = 50;

		// Create a surface drawing that function
		Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(false);
		surface.setWireframeColor(Color.BLACK);

		// Create a chart and add the surface
		Chart chart = new Chart(Quality.Advanced);
		chart.getScene().getGraph().add(surface);
		ChartLauncher.openChart(chart); */
	}
}
