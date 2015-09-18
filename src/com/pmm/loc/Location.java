package com.pmm.loc;

/**
 * An implementation of a mutable {@link ILocation}.
 */
public class Location implements ILocation {

  private double latitude;
  private double longitude;
  private long timestamp;


  /**
   * Create a Location using the given parameters.
   *
   * @param latitude  latitude of the location
   * @param longitude longitude of the location
   * @param timestamp time of the location
   */
  public Location(double latitude, double longitude, long timestamp) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.timestamp = timestamp;
  }

  @Override
  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  @Override
  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Two Locations are equal iff their latitude, longitude and time are the same.
   *
   * @param o object to test for equality
   * @return true if o is a Location and o is equal to this coordinate, otherwise false.
   */
  @Override
  public boolean equals(Object o) {
    if ( this == o ) {
      return true;
    }
    if ( !(o instanceof Location) ) {
      return false;
    }

    Location location = (Location) o;

    if ( location.latitude != latitude ) {
      return false;
    }
    if ( location.longitude != longitude ) {
      return false;
    }
    return timestamp == location.timestamp;

  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(latitude);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return String.format("{lat: %.4f, lon: %.4f, time: %d}", latitude, longitude, timestamp);
  }
}
