package sim.statistics;

import java.util.Random;

public class UniformDistribution extends Distribution{
	private double min; // the min value of the range to choose a value from
	private double range; // the size of the range.
	
	public UniformDistribution(double min, double max) throws NumberFormatException {
		this.min = min;
		this.range = max - min;
		if(range < 0) {
			throw new NumberFormatException("Invalid arguments to create a uniform distribution. The upper bound of the range must be at least as big as the lower bound.");
		}
	}
	
	@Override
	public double nextSample() {
		return min + range * randomGenerator.nextDouble();
	}
	
	public double nextSample(Random r) {
		return min + range * r.nextDouble();
	}
	
	public static double nextUniform(double minRange, double maxRange) {
		Random r = Distribution.getRandom();
		return minRange + r.nextDouble() * (maxRange - minRange);
	}


}
