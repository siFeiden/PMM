package com.pmm;

import com.pmm.loc.Location;
import com.pmm.loc.LocationGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Test {

	public static final int MAXIMUM_TRAJECTORIES = 10;


	public static void main(String[] args) {
//		List<Location> locations = LocationGenerator.generateRandom(4e-3, 10, 10);
//		List<Location> locations = LocationGenerator.singleGowalla();
		List<List<Location>> gowallaList = LocationGenerator.gowalla(MAXIMUM_TRAJECTORIES);
		System.out.println("num trajs: " + gowallaList.size());

		List<Double> estimationsGiven = new ArrayList<>();

		long startStamp = System.currentTimeMillis();
		for ( List<Location> traj : gowallaList ) {
			Location last = traj.remove(traj.size() - 1);

			try {
				Pmm pmm = new Pmm(traj);
				estimationsGiven.add(pmm.estimateNextLocationProbability(last));
			} catch ( Pmm.FittingFailedException e ) {
				// ignore in test
			}
		}
		long durationMillis = System.currentTimeMillis() - startStamp;

		System.out.println("fitting finished");
		System.out.printf("took %.2f seconds,\n%.2f s in average\n",
				durationMillis / 1e3,
				durationMillis / gowallaList.size() / 1e3);

		try ( FileWriter logger = new FileWriter("test.log") ) {
			for ( double estimation : estimationsGiven ) {
				logger.write(Double.toString(estimation) + "\n");
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		System.out.println("avg. given prob.: " + avg(estimationsGiven));
	}

	private static double avg(List<Double> values) {
		double avg = 0;
		for ( double estimation : values ) {
			avg += estimation;
		}
		avg /= values.size();
		return avg;
	}
}
