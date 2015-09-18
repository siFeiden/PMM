package com.pmm;

import java.util.Calendar;

import static java.lang.Math.*;

/**
 * Utility class.
 */
public class Timestamps {

  /* do not instantiate */
  private Timestamps() {
  }

  /**
   * Calculate the circular mean and variance of hour and minute of day.
   * For example 23:55 and 00:05 are very close on a clock but not when calculating
   * the arithmetic mean (it would be 12:00). The circular mean improves on this
   * by treating times as angles in a circle, giving 24:00 as a better mean.
   *
   * @param stamps timestamps to use in calculation
   * @return mean at index 0, variance at index 1
   */
  public static double[] circularMeanAndVariance(long[] stamps) {
    int n = stamps.length;
    double angles[] = new double[n];

    // extract hh:mm from timestamps und transform to angles in a circle
    for ( int i = 0; i < n; i++ ) {
      angles[i] = toRadians(
          getField(stamps[i], Calendar.HOUR_OF_DAY) * 15 + // split circle in 24h, 1h is 15°
              getField(stamps[i], Calendar.MINUTE) / 60d * 15); // fraction of one hour * 15°
    }

    // calculate circular mean
    double sinSum = 0.0, cosSum = 0.0;
    for ( double a : angles ) {
      sinSum += sin(a);
      cosSum += cos(a);
    }
    sinSum /= n;
    cosSum /= n;

    double meanAngle = atan2(sinSum, cosSum);
    meanAngle += Math.PI; // transform from [-pi, pi] to [0, 2*pi]
    double meanHour = toDegrees(meanAngle) / 15;

		/* This is an approx. to the variance of the random variable meanAngle
       which generates (surprise!) angles in radians. */
    double variance = sqrt(1 - hypot(sinSum, cosSum));

		/* However, our return value is meanHour which is a transformation of meanAngle.
       meanHour = 180 / (15 * pi) * meanAngle
		   Therefore we also have to transform the variance according to
		   Var(a * X) = a^2 * Var(X) */
    variance *= pow(180 / (15 * PI), 2);

    return new double[] { meanHour, variance };
  }

  private static Calendar c = Calendar.getInstance();

  private static int getField(long stamp, int field) {
    c.setTimeInMillis(stamp);
    return c.get(field);
  }
}
