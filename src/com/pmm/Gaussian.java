package com.pmm;

import Jama.Matrix;

import static java.lang.Math.*;

public class Gaussian {
	/**
	 * Mean of a gaussian
	 * Must be a 2x1 matrix.
	 */
	Matrix m;

	/**
	 *  Covariance of a gaussian.
	 *  Must be a 2x2 matrix.
	 */
	Matrix S;


	public Gaussian(Matrix m, Matrix S) {
		this.m = m;
		this.S = S;
	}

	public double density(double x, double y) {
		Matrix p = new Matrix(new double[] { x, y }, 2);

		p.minusEquals(m);
		Matrix inv;
		try {
			inv = S.inverse();
		} catch ( RuntimeException e ) {
			inv = Matrix.identity(2, 2);
		}
		Matrix a = p.transpose().times(inv.times(p));
		double v1 = exp(-.5 * a.get(0, 0));
		double v2 = sqrt(4 * PI * PI * S.det());

		return v1 / v2;
	}

	public void print() {
		System.out.print("mean");
		m.print(10, 5);
		System.out.print("cov");
		S.print(10, 5);
	}

	public double getMeanX() {
		return m.get(0, 0);
	}

	public double getMeanY() {
		return m.get(1, 0);
	}

	public double getCovariance1() {
		return S.get(0, 0);
	}

	public double getCovariance2() {
		return S.get(1, 1);
	}
}
