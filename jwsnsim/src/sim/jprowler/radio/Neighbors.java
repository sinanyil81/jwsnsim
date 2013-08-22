package sim.jprowler.radio;

import java.util.Iterator;
import java.util.Vector;

import sim.jprowler.Node;
import sim.jprowler.Simulator;

public class Neighbors {

	Node node;

	Node[] neighbors;
	double[] staticFadings;
	double[] dynamicStrengths;

	/**
	 * The exhibitor of the radio signal degradation, typically it is 2/2 if the
	 * motes are well above ground, though at ground level this factor is closer
	 * to 3/2. For efficiency reasons, this number is the half of the usually
	 * defined falling factor.
	 */
	public static final double fallingFactorHalf = 1.1;

	/**
	 * This coefficient is used to "simulate" the static part of environmental
	 * noise. It is used when the mote field is first set up.
	 */
	 public static final double staticRandomFactor = 0.0;
//	public static final double staticRandomFactor = 0.3;

	/**
	 * This limits the number of neighbours used to calculate interference
	 * ratio. Using neighborhood instead of endless radio signals makes the
	 * simulation somewhat faster.
	 */
	public static final double radioStrengthCutoff = 0.1;

	/**
	 * This coefficient is used to "simulate" the dynamic part of environmental
	 * noise. The dynamic noise is recalculated for each transmission.
	 */
	public static final double dynamicRandomFactor = 0.05;

	/**
	 * Calculates the static part of the radio fading between two nodes based on
	 * distance and a random factor.
	 * 
	 * @return The radio fading coefficient
	 */
	public static double getStaticFading(Node sender, Node receiver) {
		double staticRandomFading = 1.0 + staticRandomFactor
				* Simulator.random.nextGaussian();

		return staticRandomFading <= 0.0 ? 0.0 : 
			sender.getRadio().getMaxRadioStrength()* staticRandomFading / (1.0 + Math.pow(
						sender.getPosition().squareDistanceTo(
								receiver.getPosition()), fallingFactorHalf));
	}

	/**
	 * Calculates the received radio strength based on the static signal
	 * strength determined by the {@link GaussianRadioModel#getStaticFading}, a
	 * dynamic random factor and the signal strength of the node, which can be
	 * time variant.
	 * 
	 * @param signalStrength
	 *            the signal strength of the sender node
	 * @param staticFading
	 *            the static fading as returned by
	 *            {@link GaussianRadioModel#getStaticFading}.
	 * @return The signal strength at the receiver.
	 */
	public static double getDynamicStrength(double signalStrength,
			double staticFading) {
		double dynamicRandomFading = 1.0 + dynamicRandomFactor
				* Simulator.random.nextGaussian();
		return dynamicRandomFading <= 0.0 ? 0.0 : signalStrength * staticFading
				* dynamicRandomFading;
	}

	public Neighbors(Node node) {
		this.node = node;
	}

	/**
	 * (Re)calculates the neighborhoods of every node in the network. This
	 * operation should be called whenever the location of the nodes changed.
	 * This operation is extremely expensive and should be used sparsely.
	 */
	public void updateNeighborhood() {

		Vector<Node> nodes = Simulator.getInstance().getNodes();
		int nodeNum = nodes.size(); // get number of nodes

		Node[] neighbors = new Node[nodeNum];
		double[] staticFadings = new double[nodeNum];

		if (!node.isOn())
			return;

		int i = 0;

		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			Node node2 = (Node) iterator.next();

			if (!node2.isOn())
				continue;

			double staticRadioStrength = getStaticFading(node, node2);
			if (staticRadioStrength >= radioStrengthCutoff && node != node2) {
				neighbors[i] = node2;
				staticFadings[i] = staticRadioStrength;
				i++;
				System.out.println(node2.getId());
			}
		}

		System.out.println("-------------------------------");

		this.neighbors = new Node[i];
		System.arraycopy(neighbors, 0, this.neighbors, 0, i);
		this.staticFadings = new double[i];
		System.arraycopy(staticFadings, 0, this.staticFadings, 0, i);
		this.dynamicStrengths = new double[i];
	}

	public void startTransmission(double strength) {

		int i = neighbors.length;
		while (--i >= 0) {
			double dynamicStrength = getDynamicStrength(strength,
					staticFadings[i]);
			dynamicStrengths[i] = dynamicStrength;
			neighbors[i].getRadio().addNoise(dynamicStrength, node.getRadio());
		}
	}
	
	public void endTransmission(){
		int i = neighbors.length;
		while( --i >= 0 )
			neighbors[i].getRadio().removeNoise(dynamicStrengths[i], node.getRadio());
	}
}
