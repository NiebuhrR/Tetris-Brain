//package org.gandhim.pso;

/* author: gandhi - gandhi.mtm [at] gmail [dot] com - Depok, Indonesia */

// this is the heart of the PSO program
// the code is for 2-dimensional space problem
// but you can easily modify it to solve higher dimensional space problem

import java.util.Random;
import java.util.Vector;

public class PSOProcess implements PSOConstants {
	private Vector<Particle> swarm = new Vector<Particle>();
	private double[] pBest = new double[SWARM_SIZE];
	private Vector<Location> pBestLocation = new Vector<Location>();
	private double gBest;
	private Location gBestLocation;
	private double[] fitnessValueList = new double[SWARM_SIZE];
	private double[] features;
	private double[] weights = new double[11];
	
	Random generator = new Random();

	public PSOProcess(double[] features, double[] weights){
		this.features = features;
		this.weights = weights;
	}
	
	public double[] execute(double[] features) {
		initializeSwarm(features);
		updateFitnessList();

//		double[] features = {15.0, 10.0, 15.0, 20.0, 1.0, 10.0, 10.0, 7.0, 15.0, 10.0, 5.0};
		
		for(int i=0; i<SWARM_SIZE; i++) {
			pBest[i] = fitnessValueList[i];
			pBestLocation.add(swarm.get(i).getLocation());
		}
		
		int t = 0;
		double w;
		double err = 9999;
		
		while(t < MAX_ITERATION && err > ProblemSet.ERR_TOLERANCE) {
			// step 1 - update pBest
			for(int i=0; i<SWARM_SIZE; i++) {
				if(fitnessValueList[i] < pBest[i]) {
					pBest[i] = fitnessValueList[i];
					pBestLocation.set(i, swarm.get(i).getLocation());
				}
			}
				
			// step 2 - update gBest
			int bestParticleIndex = PSOUtility.getMinPos(fitnessValueList);
			if(t == 0 || fitnessValueList[bestParticleIndex] < gBest) {
				gBest = fitnessValueList[bestParticleIndex];
				gBestLocation = swarm.get(bestParticleIndex).getLocation();
			}
			
			w = W_UPPERBOUND - (((double) t) / MAX_ITERATION) * (W_UPPERBOUND - W_LOWERBOUND);
			
			for(int i=0; i<SWARM_SIZE; i++) {
				double r1 = generator.nextDouble();
				double r2 = generator.nextDouble();

				Particle p = swarm.get(i);
				
				// step 3 - update velocity
				double[] newVel = new double[PROBLEM_DIMENSION];
				newVel[0] = (w * p.getVelocity().getPos()[0]) + 
							(r1 * C1) * (pBestLocation.get(i).getLoc()[0] - p.getLocation().getLoc()[0]) +
							(r2 * C2) * (gBestLocation.getLoc()[0] - p.getLocation().getLoc()[0]);
				newVel[1] = (w * p.getVelocity().getPos()[1]) + 
							(r1 * C1) * (pBestLocation.get(i).getLoc()[1] - p.getLocation().getLoc()[1]) +
							(r2 * C2) * (gBestLocation.getLoc()[1] - p.getLocation().getLoc()[1]);
				newVel[2] = (w * p.getVelocity().getPos()[2]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[2] - p.getLocation().getLoc()[2]) +
						(r2 * C2) * (gBestLocation.getLoc()[2] - p.getLocation().getLoc()[2]);
				newVel[3] = (w * p.getVelocity().getPos()[3]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[3] - p.getLocation().getLoc()[3]) +
						(r2 * C2) * (gBestLocation.getLoc()[3] - p.getLocation().getLoc()[3]);
				newVel[4] = (w * p.getVelocity().getPos()[4]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[4] - p.getLocation().getLoc()[4]) +
						(r2 * C2) * (gBestLocation.getLoc()[4] - p.getLocation().getLoc()[4]);
				newVel[5] = (w * p.getVelocity().getPos()[5]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[5] - p.getLocation().getLoc()[5]) +
						(r2 * C2) * (gBestLocation.getLoc()[5] - p.getLocation().getLoc()[5]);
				newVel[6] = (w * p.getVelocity().getPos()[6]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[6] - p.getLocation().getLoc()[6]) +
						(r2 * C2) * (gBestLocation.getLoc()[6] - p.getLocation().getLoc()[6]);
				newVel[7] = (w * p.getVelocity().getPos()[7]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[7] - p.getLocation().getLoc()[7]) +
						(r2 * C2) * (gBestLocation.getLoc()[7] - p.getLocation().getLoc()[7]);
				newVel[8] = (w * p.getVelocity().getPos()[8]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[8] - p.getLocation().getLoc()[8]) +
						(r2 * C2) * (gBestLocation.getLoc()[8] - p.getLocation().getLoc()[8]);
				newVel[9] = (w * p.getVelocity().getPos()[9]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[9] - p.getLocation().getLoc()[9]) +
						(r2 * C2) * (gBestLocation.getLoc()[9] - p.getLocation().getLoc()[9]);
				newVel[10] = (w * p.getVelocity().getPos()[10]) +
						(r1 * C1) * (pBestLocation.get(i).getLoc()[10] - p.getLocation().getLoc()[10]) +
						(r2 * C2) * (gBestLocation.getLoc()[10] - p.getLocation().getLoc()[10]);


				Velocity vel = new Velocity(newVel);
				p.setVelocity(vel);
				
				// step 4 - update location
				double[] newLoc = new double[PROBLEM_DIMENSION];
				newLoc[0] = p.getLocation().getLoc()[0] + newVel[0];
				newLoc[1] = p.getLocation().getLoc()[1] + newVel[1];
				newLoc[2] = p.getLocation().getLoc()[2] + newVel[2];
				newLoc[3] = p.getLocation().getLoc()[3] + newVel[3];
				newLoc[4] = p.getLocation().getLoc()[4] + newVel[4];
				newLoc[5] = p.getLocation().getLoc()[5] + newVel[5];
				newLoc[6] = p.getLocation().getLoc()[6] + newVel[6];
				newLoc[7] = p.getLocation().getLoc()[7] + newVel[7];
				newLoc[8] = p.getLocation().getLoc()[8] + newVel[8];
				newLoc[9] = p.getLocation().getLoc()[9] + newVel[9];
				newLoc[10] = p.getLocation().getLoc()[10] + newVel[10];
				Location loc = new Location(newLoc);
				p.setLocation(loc);
			}
			
			err = ProblemSet.evaluate(gBestLocation, features) - 0; // minimizing the functions means it's getting closer to 0
			
			System.out.println("ITERATION " + t + ": ");
			System.out.println("     Best weightHoles: " + gBestLocation.getLoc()[0]);
			System.out.println("     Best weightMaxHeight: " + gBestLocation.getLoc()[1]);
			System.out.println("     Best weightAverageHeight: " + gBestLocation.getLoc()[2]);
			System.out.println("     Best weightBumpiness: " + gBestLocation.getLoc()[3]);
			System.out.println("     Best weightCompleteLines: " + gBestLocation.getLoc()[4]);
			System.out.println("     Best weightRowTransitions: " + gBestLocation.getLoc()[5]);
			System.out.println("     Best weightColumnTransitions: " + gBestLocation.getLoc()[6]);
			System.out.println("     Best weightWells: " + gBestLocation.getLoc()[7]);
			System.out.println("     Best weightFilledAboveHoles: " + gBestLocation.getLoc()[8]);
			System.out.println("     Best weightRowsWithHoles: " + gBestLocation.getLoc()[9]);
			System.out.println("     Best weightHeightDiff: " + gBestLocation.getLoc()[10]);

			System.out.println("     Final_Score: " + ProblemSet.evaluate(gBestLocation, features));
			
			t++;
			updateFitnessList();
		}
		
		System.out.println("\nSolution found at iteration " + (t - 1) + ", the solutions is:");
		System.out.println("     Best weightHoles: " + gBestLocation.getLoc()[0]);
		System.out.println("     Best weightMaxHeight: " + gBestLocation.getLoc()[1]);
		System.out.println("     Best weightAverageHeight: " + gBestLocation.getLoc()[2]);
		System.out.println("     Best weightBumpiness: " + gBestLocation.getLoc()[3]);
		System.out.println("     Best weightCompleteLines: " + gBestLocation.getLoc()[4]);
		System.out.println("     Best weightRowTransitions: " + gBestLocation.getLoc()[5]);
		System.out.println("     Best weightColumnTransitions: " + gBestLocation.getLoc()[6]);
		System.out.println("     Best weightWells: " + gBestLocation.getLoc()[7]);
		System.out.println("     Best weightFilledAboveHoles: " + gBestLocation.getLoc()[8]);
		System.out.println("     Best weightRowsWithHoles: " + gBestLocation.getLoc()[9]);
		System.out.println("     Best weightHeightDiff: " + gBestLocation.getLoc()[10]);

		double[] newWeights = new double[11];

		newWeights[0] = gBestLocation.getLoc()[0];
		newWeights[1] = gBestLocation.getLoc()[1];
		newWeights[2] = gBestLocation.getLoc()[2];
		newWeights[3] = gBestLocation.getLoc()[3];
		newWeights[4] = gBestLocation.getLoc()[4];
		newWeights[5] = gBestLocation.getLoc()[5];
		newWeights[6] = gBestLocation.getLoc()[6];
		newWeights[7] = gBestLocation.getLoc()[7];
		newWeights[8] = gBestLocation.getLoc()[8];
		newWeights[9] = gBestLocation.getLoc()[9];
		newWeights[10] = gBestLocation.getLoc()[10];


		return newWeights;
	}
	
	public void initializeSwarm(double[] features) {
		Particle p;
		for(int i=0; i<SWARM_SIZE; i++) {
			p = new Particle();

//			double[] features = {15.0, 10.0, 15.0, 20.0, 1.0, 10.0, 10.0, 7.0, 15.0, 10.0, 5.0};


			// randomize location inside a space defined in Problem Set
			double[] loc = new double[PROBLEM_DIMENSION];
			loc[0] = weights[0];
			loc[1] = weights[1];
			loc[2] = weights[2];
			loc[3] = weights[3];
			loc[4] = weights[4];
			loc[5] = weights[5];
			loc[6] = weights[6];
			loc[7] = weights[7];
			loc[8] = weights[8];
			loc[9] = weights[9];
			loc[10] = weights[10];
			Location location = new Location(loc);
			
			// randomize velocity in the range defined in Problem Set
			double[] vel = new double[PROBLEM_DIMENSION];
			vel[0] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[1] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[2] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[3] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[4] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[5] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[6] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[7] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[8] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[9] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[10] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);

			Velocity velocity = new Velocity(vel);
			
			p.setLocation(location);
			p.setVelocity(velocity);
			p.setFeatures(features);
			swarm.add(p);
		}
	}
	
	public void updateFitnessList() {
		for(int i=0; i<SWARM_SIZE; i++) {
			fitnessValueList[i] = swarm.get(i).getFitnessValue();
		}
	}

	public void setWeights(double[] weights){ this.weights = weights; }
}
