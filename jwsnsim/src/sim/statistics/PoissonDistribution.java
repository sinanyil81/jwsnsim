package sim.statistics;

import java.util.Random;

public class PoissonDistribution extends Distribution{
	private double expLambda; // e^-lambda
	
	/**
	 * Constructs a new poisson distribution sample generator.
	 * @param lambda The mean (and also variance) of the distribution.
	 */
	public PoissonDistribution(double lambda) {
		expLambda = Math.exp(- lambda);
	}
	
	@Override
	/**
	 * Returns the next sample of this poisson distribution sample generator.
	 * 
	 * In fact, the method returns an integer value casted to a double.
	 * @return The next sample of this poisson distribution sample generator casted to a double.
	 */
	public double nextSample() {
		double product = 1;
		int count =  0;
		int result = 0;
		while (product >= expLambda) {
			product *= randomGenerator.nextDouble();
			result = count;
			count++; // keep result one behind
		}
		return result;
	}
	
	public double nextSample(Random r) {
		double product = 1;
		int count =  0;
		int result = 0;
		while (product >= expLambda) {
			product *= r.nextDouble();
			result = count;
			count++; // keep result one behind
		}
		return result;
	}
	
	/**
	 * Creates a random sample drawn from a poissson distribution with given lambda.
	 * 
	 * Note: for a poisson distribution, E(X) = Var(X) = lambda.
	 * 
	 * The value returned is an integer in the range from 0 to positive infinity (in theory)
	 * 
	 * @param lambda The expectation and variance of the distribution.
	 * @return a random sample drawn from a poissson distribution with given lambda.
	 */
	public static int nextPoisson(double lambda) {
		Random r = Distribution.getRandom();
		double elambda = Math.exp(- lambda);
		double product = 1;
		int count =  0;
		int result = 0;
		while (product >= elambda) {
			product *= r.nextDouble();
			result = count;
			count++; // keep result one behind
		}
		return result;
	}

}
