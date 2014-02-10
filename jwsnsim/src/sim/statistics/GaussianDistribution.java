package sim.statistics;

import java.util.Random;

public class GaussianDistribution extends Distribution{
	private double mean; // the mean of the distribution
	private double var; // the variance of the distribution
	
	public GaussianDistribution(double mean, double var) {
		this.mean = mean;
		this.var = var;
	}

	@Override
	public double nextSample() {
		return mean + randomGenerator.nextGaussian() * Math.sqrt(var);
	}
	
	public double nextSample(Random r) {
		return mean + r.nextGaussian() * Math.sqrt(var);
	}

	public static double nextGaussian(double mean, double variance) {
		Random r = Distribution.getRandom();
		return mean + r.nextGaussian() * Math.sqrt(variance);
	}

}
