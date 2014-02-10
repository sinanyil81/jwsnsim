package sim.statistics;

import java.util.Random;

public abstract class Distribution {
	
	protected static Random randomGenerator; // the singleton instance of the random object. Be sure to initialize before using the first time! 
	private static long randomSeed = -1; // the seed used for the random object

	public static void setSeed(long seed){
		randomSeed = seed;
	}
	
	public static long getSeed() {
		getRandom(); // initialize the random generator if it's not already done
		return randomSeed;
	}

	/**
	 * The super-class for all distributions, ensures that the random generator instance exists 
	 */
	protected Distribution(){
		getRandom(); // initialize the random generator if it's not already done
	}
	
	/**
	 * Returns the singleton random generator object of this simulation. You should only use this
	 * random number generator in this project to ensure that the simulatoin can be repeated by
	 * using a fixed seed. (The usage of a fixed seed can be enforced in the XML configuration file.)  
	 *
	 * @return the singleton random generator object of this simulation
	 */
	public static Random getRandom() {
		// construct the singleton random object if it does not yet exist
		if(randomGenerator == null) {
			if(randomSeed == -1)
				randomSeed = (new java.util.Random()).nextLong();
			randomGenerator = new Random(randomSeed); // use a random seed
		}
		return randomGenerator;
	}
	
	/**
	 * Returns the next random sample of this distribution. 
	 * 
	 * This method must be implemented in all proper subclasses.
	 * @return the next random sample of this distribution.
	 */
	public abstract double nextSample();
	public abstract double nextSample(Random r);

}
