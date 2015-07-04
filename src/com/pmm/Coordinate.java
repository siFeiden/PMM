package com.pmm;
import java.io.Serializable;


public class Coordinate implements Serializable, ICoordinate {
	private double latitude;
	private double longitude;

	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Coordinate(ICoordinate other) {
		if ( other == null )
			throw new NullPointerException("other coordinate must not be null");

		latitude = other.getLatitude();
		longitude = other.getLongitude();
	}

	@Override public double getLatitude() {
		return latitude;
	}

	@Override public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@Override public double getLongitude() {
		return longitude;
	}

	@Override public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;

		Coordinate that = (Coordinate) o;

		return that.latitude == latitude && that.longitude == longitude;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public String toString() {
		return "Coordinate[" + latitude + ", " + longitude + "]";
	}
}
