package com.pmm.loc;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Reads a list of users with their trajectories from the Gowalla dataset.
 */
class GowallaUserLoader {

	private static final String DATASET_TRAJECTORY_PATH = "./SanFrancisco_ActiveUserCheckIns.csv";


	protected static List<List<DataPoint>> readLocations(int maximumTrajectories) {
		try ( Scanner scanner = new Scanner(new File(DATASET_TRAJECTORY_PATH)) ) {
			Map<String, List<DataPoint>> traj = new HashMap<>();

			while ( scanner.hasNextLine() ) {
				String line = scanner.nextLine();
				String[] parts = line.split("\t");

				Date time = DatatypeConverter.parseDateTime(parts[1]).getTime();
				double lat = Double.parseDouble(parts[2]);
				double lon = Double.parseDouble(parts[3]);

				List<DataPoint> l = traj.get(parts[0]);
				if ( l == null ) {
					if ( traj.size() == maximumTrajectories )
						break;

					l = new ArrayList<>();
					traj.put(parts[0], l);
				}
				l.add(new DataPoint(lat, lon, time));

			}

			return new ArrayList<>(traj.values());
		} catch ( FileNotFoundException e ) {
			System.out.println("Gowalla file not found");
			return new ArrayList<>();
		}
	}



	/*
	protected static List<User> loadUsers() throws IOException {
		return loadUsers(Integer.MAX_VALUE);
	}

	/**
	 * Reads the data (users with trajectories and edges) of the Gowalla dataset.
	 * @param cutoff maximum number of users to read
	 * @return list of read users
	 * @throws IOException from file IO
	 *
	public static List<User> loadUsers(int cutoff) throws IOException {
		BufferedReader data = new BufferedReader(
				new InputStreamReader(
						new ProgressMonitorInputStream(null, "Loading Gowalla dataset.",
								new FileInputStream(DATASET_TRAJECTORY_PATH)))
				);
		
		// read trajectories from file
		Map<String, Trajectory> trajectories = readTrajectories(data, cutoff);
		data.close();

		Map<Integer, User> users = new HashMap<>(trajectories.size());
		
		// and create user for each
		for ( Map.Entry<String, Trajectory> e : trajectories.entrySet() ) {
			int userId = Integer.parseInt(e.getKey());
			Image profile = getUserProfile();
			
			users.put(userId, new User(e.getValue(), userId, "u" + userId, profile));
		}

		// add friends to users
		data = new BufferedReader(new FileReader(DATASET_EDGES_PATH));
		Map<Integer, User> usersWithFriends = readUserFriends(data, users);
		data.close();

		// create list from users
		return new ArrayList<>(usersWithFriends.values());
	}

	/**
	 * Reads trajectories from a csv(tab separated) file in the following format:
	 * userid, timestamp, latitude, longitude
	 * @param data a reader that provides the csv line by line
	 * @param cutoff maximum number of users to read
	 * @return a map from userId to {@link Trajectory}
	 * @throws IOException from the BufferedReader
	 *
	private static Map<String, Trajectory> readTrajectories(BufferedReader data, int cutoff) throws IOException {
		// ensure every user id exists only once
		Map<String, Trajectory.Builder> allTrajs = new HashMap<>();
		
		for (String s = data.readLine(); s != null; s = data.readLine()) {
			// csv (with tabs): id, timestamp, lat, lon
			String[] v = s.split("\t");
			
			// create or get trajectory for id
			Trajectory.Builder t = allTrajs.get(v[0]);
			if ( t == null ) {
				// Stop parsing instead of creating the cutoff + 1st user.
				if ( allTrajs.size() >= cutoff )
					break;
				
				t = new Trajectory.Builder(v[0], "", null);
				allTrajs.put(v[0], t);
			}
			
			// create DataPoint from line
			Date time = DatatypeConverter.parseDateTime(v[1]).getTime();
			double latitude  = Double.parseDouble(v[2]);
			double longitude = Double.parseDouble(v[3]);
			
			DataPoint p = new DataPoint(time, latitude, longitude);
			t.addPoint(p);
		}

		// Call build on all Builders.
		Map<String, Trajectory> result = new HashMap<>();
		for ( Map.Entry<String, Trajectory.Builder> e : allTrajs.entrySet() ) {
			result.put(e.getKey(), e.getValue().build());
		}
		
		return result;
	}
	*/
}
