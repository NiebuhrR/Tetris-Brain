//package org.gandhim.pso;

public class Particle {
	private double fitnessValue;
	private Velocity velocity;
	private Location location;

	//adding values for our features to problem set
	private double[] features;
	
	public Particle() {
		super();
	}

	public Particle(double fitnessValue, Velocity velocity, Location location, double[] features) {
		super();
		this.fitnessValue = fitnessValue;
		this.velocity = velocity;
		this.location = location;

		//adding values for our features to problem set
		this.features = features;
	}

	public Velocity getVelocity() {
		return velocity;
	}

	public void setVelocity(Velocity velocity) {
		this.velocity = velocity;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setFeatures(double[] features) { this.features = features; }

	public double getFitnessValue() {

		fitnessValue = ProblemSet.evaluate(location, features);
		return fitnessValue;
	}
}
