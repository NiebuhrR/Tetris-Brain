//package org.gandhim.pso;

/* author: gandhi - gandhi.mtm [at] gmail [dot] com - Depok, Indonesia */

// this is the problem to be solved
// to find an x and a y that minimize the function below:
// f(x, y) = (2.8125 - x + x * y^4)^2 + (2.25 - x + x * y^2)^2 + (1.5 - x + x*y)^2
// where 1 <= x <= 4, and -1 <= y <= 1

// you can modify the function depends on your needs
// if your problem space is greater than 2-dimensional space
// you need to introduce a new variable (other than x and y)

public class ProblemSet {

	public static final double VEL_LOW = 0;
	public static final double VEL_HIGH = 2;
	
	public static final double ERR_TOLERANCE = 1E-20; // the smaller the tolerance, the more accurate the result, 
	                                                  // but the number of iteration is increased
	
	public static double evaluate(Location location, double[] features) {
		double result = 0;
		double weightHoles = location.getLoc()[0];
		double weightMaxHeight = location.getLoc()[1];
		double weightAverageHeight = location.getLoc()[2];
		double weightBumpiness = location.getLoc()[3];
		double weightCompleteLines = location.getLoc()[4];
		double weightRowTransitions = location.getLoc()[5];
		double weightColumnTransitions = location.getLoc()[6];
		double weightWells = location.getLoc()[7];
		double weightFilledAboveHoles = location.getLoc()[8];
		double weightRowsWithHoles = location.getLoc()[9];
		double weightHeightDiff = location.getLoc()[10];
		
		result = features[0]*weightHoles + features[1]*weightMaxHeight + features[2]*weightAverageHeight
				+ features[3]*weightBumpiness + features[4]*weightCompleteLines + features[5]*weightRowTransitions
				+ features[6]*weightColumnTransitions + features[7]*weightWells + features[8]*weightFilledAboveHoles
				+ features[9]*weightRowsWithHoles + features[10]*weightHeightDiff;
		
		return result;
	}
}
