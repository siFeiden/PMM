package com.pmm;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static java.lang.Math.*;

public class Timestamps {

	private Timestamps() { }

	public static void test(String[] args) {
		Random r = new Random();

		long now = new Date().getTime();

		long[] stamps = new long[10];

		for (int i = 0; i < stamps.length; i++) {
			stamps[i] = now + r.nextInt(16 * 3600 * 1000);
		}

		double[] cmean = circularMeanAndVariance(stamps);
		System.out.println(cmean[0]);
		System.out.println(cmean[1]);
	}

	public static double[] circularMeanAndVariance(long[] stamps) {
		int n = stamps.length;
		double angles[] = new double[n];

		// extract hh:mm from timestamps und transform to angles in a circle
		for (int i = 0; i < n; i++) {
			angles[i] = toRadians(
				getField(stamps[i], Calendar.HOUR_OF_DAY) * 15 + // split circle in 24h, 1h is 15°
				getField(stamps[i], Calendar.MINUTE) / 60d * 15); // fraction of one hour * 15°
		}

		// calculate circular mean
		double sinSum = 0.0, cosSum = 0.0;
		for (double a : angles) {
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
