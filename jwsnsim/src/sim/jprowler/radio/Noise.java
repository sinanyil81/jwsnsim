package sim.jprowler.radio;

public class Noise {

	/**
	 * The constant self noise level.
	 */
	public static final double noiseVariance = 0.025;

	/**
	 * The maximum noise level that is allowed on sending. This is actually a
	 * multiplicator of the noiseVariance.
	 */
	public static final double maxAllowedNoiseOnSending = 5;

	/** The minimum signal to noise ratio required to spot a message in the air. */
	public static final double receivingStartSNR = 4.0;

	/**
	 * The maximum signal to noise ratio below which a message is marked
	 * corrupted.
	 */
	public static final double corruptionSNR = 2.0;
	
	public static boolean isMessageCorrupted(double signal, double noise) {
		return calcSNR(signal, noise) < corruptionSNR;
	}

	protected static double calcSNR(double signal, double noise) {
		return signal / (noiseVariance + noise);
	}

	public static boolean isReceivable(double signal, double noise) {
		return calcSNR(signal, noise) > receivingStartSNR;
	}
	
	public static boolean isChannelFree(double noiseStrength) {
		return noiseStrength < maxAllowedNoiseOnSending * noiseVariance;
	}
	
}
