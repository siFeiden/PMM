package com.pmm.loc;

/**
 * A geographic coordinate equipped with a timestamp.
 */
public interface ILocation {

  /**
   * Get the latitude of this location.
   * @return the latitude
   */
  double getLatitude();

  /**
   * Get the longitude of this location.
   * @return the longitude
   */
  double getLongitude();

  /**
   * Get the timestamp of this location.
   * @return the timestamp in milliseconds since the Unix epoch.
   * @see java.util.Date#getTime()
   */
  long getTimestamp();
}
