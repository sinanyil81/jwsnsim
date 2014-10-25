package application.appSelf;

import hardware.Register32;

import java.util.Hashtable;
import java.util.Iterator;

import application.appSelf.ClockSpeedAdapter3.NeighborData;
import fr.irit.smac.util.avt.AVT;
import fr.irit.smac.util.avt.AVTBuilder;
import fr.irit.smac.util.avt.Feedback;

public class ClockSpeedAdapter5 {

	private static float TOLERANCE = 0.00000001f;

	class NeighborData {
		public Register32 timestamp;
		public float decision;

		public NeighborData(Register32 timestamp, float decision) {
			this.timestamp = new Register32(timestamp);
			this.decision = decision;
		}
	}

	// public AvtSimple rate = new AvtSimple(-0.0001f, 0.0001f, 0.0f,
	// 0.00000000001f, 0.0001f);
	public AVT rate = new AVTBuilder().upperBound(0.0001).lowerBound(-0.0001)
			.deltaMin(0.000000001).isDeterministicDelta(true).deltaMax(0.0001)
			.startValue(0.0)
			// .deltaDecreaseFactor(30)
			// .deltaIncreaseFactor(20)
			.build();

	private Hashtable<Integer, NeighborData> neighbors = new Hashtable<Integer, NeighborData>();

	public ClockSpeedAdapter5() {
		rate.getAdvancedAVT().getDeltaManager().getAdvancedDM()
				.setDelta(0.000001f);
	}

	private float getDecision(int nodeid, Register32 progress, Register32 timestamp) {

		float decision = 0.0f;

		NeighborData neighbor = neighbors.get(nodeid);

		if (neighbor != null) {

			int neighborProgress = progress.toInteger();

			int timePassed = timestamp.subtract(neighbor.timestamp).toInteger();
			int myProgress = timePassed
					+ (int) (((float) timePassed) * (float) this.rate
							.getValue());

			decision = (float) (neighborProgress - myProgress);
			decision /= (float) myProgress;
		}

		return decision;
	}

	public void adjust(int nodeid, Register32 progress, Register32 timestamp) {
		float neighborDecision = getDecision(nodeid, progress, timestamp);

		adjustRate(neighborDecision);

		neighbors.remove(nodeid);
		neighbors.put(nodeid, new NeighborData(timestamp, neighborDecision));
	}

	private void adjustRate(float currentDecision) {
		if (currentDecision < -TOLERANCE) {
			this.rate.adjustValue(Feedback.LOWER);
		} else if (currentDecision > TOLERANCE) {
			this.rate.adjustValue(Feedback.GREATER);
		} else {
			this.rate.adjustValue(Feedback.GOOD);
		}
	}

	public float getSpeed() {

		// return this.rate.getValue();
		return (float) this.rate.getValue();
	}
}
