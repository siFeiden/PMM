package com.pmm.loc;

import java.util.Date;

public class Location implements ICoordinate
{
    private double latitude;
    private double longitude;
    private Date time;

	public Location() {
	}

	public Location(double latitude, double longitude, Date date) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = date;
	}

	@Override
	public double getLatitude() {
		return latitude;
	}

	@Override
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@Override
	public double getLongitude() {
		return longitude;
	}

	@Override
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public Date getTime() {
		return time;
	}

	public long getLongTime() {
		return time.getTime();
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Coordinate toCoordinate() {
        return new Coordinate(latitude, longitude);
    }

    /**
     * Creates a Location from a String of the form "time lat lon" where time is optional and in the format of {@link Date#getTime()}.
     * @param serial String representation of a Location
     * @return Location representing the input
     */
    public static Location fromString(String serial) {
        if ( serial == null )
            return null;

        double lat = 0.0, lon = 0.0;
        Date time = new Date();

        String[] split = serial.trim().split(" ");
        if ( split.length == 2 ) {
            lat = Double.parseDouble(split[0]);
            lon = Double.parseDouble(split[1]);
        } else if ( split.length == 3 ) {
            time = new Date(Long.parseLong(split[0]));
            lat  = Double.parseDouble(split[1]);
            lon  = Double.parseDouble(split[2]);
        }

        return new Location(lat, lon, time);
    }

    public double[] latLonArray() {
        return new double[] { latitude, longitude };
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Location location = (Location) o;

        if ( location.latitude != latitude ) return false;
        if ( location.longitude != longitude ) return false;
        return time.equals(location.time);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = time.hashCode();
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

	@Override
	public String toString() {
//		return "Location{" +
//				"latitude=" + latitude +
//				", longitude=" + longitude +
//				", time=" + time +
//				'}';

		return String.format("{lat: %.4f, lon: %.4f, time: %d}", latitude, longitude, time.getTime());
	}
}
