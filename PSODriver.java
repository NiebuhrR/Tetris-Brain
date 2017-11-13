//package org.gandhim.pso;

/* author: gandhi - gandhi.mtm [at] gmail [dot] com - Depok, Indonesia */

// this is a driver class to execute the PSO process

public class PSODriver {
	public static void main(String args[]) {
		double[] features = {15.0, 10.0, 15.0, 20.0, 1.0, 10.0, 10.0, 7.0, 15.0, 10.0, 5.0};

		double[] weights = new double[11];

		weights[0] = 99;
		weights[1] = 10;
		weights[2] = 20;
		weights[3] = 10;
		weights[4] = 0.25;
		weights[5] = 59;
		weights[6] = 59;
		weights[7] = 45;
		weights[8] = 29;
		weights[9] = 29;
		weights[10] = 20;

		PSOProcess pso = new PSOProcess(features, weights);
		double[] newWeights = pso.execute(features);
		//System.out.println(newWeights[0]);
	}
}
