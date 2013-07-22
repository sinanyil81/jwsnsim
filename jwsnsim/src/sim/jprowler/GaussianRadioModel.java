/*
 * Copyright (c) 2003, Vanderbilt University
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE VANDERBILT UNIVERSITY BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE VANDERBILT
 * UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE VANDERBILT UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE VANDERBILT UNIVERSITY HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Author: Gyorgy Balogh, Gabor Pap, Miklos Maroti
 * Date last modified: 02/09/04
 */

package sim.jprowler;

import java.util.Iterator;
import java.util.Vector;

/**
 * This radio model uses the assumption that nodes are mainly static, they don't
 * change their positions to often. So it modulates the ideal radio strength 
 * first when the {@link RadioModel#updateNeighborhoods} is called adding a 
 * Gaussian noise to it, then it is further modulated on a per-transmission basis
 * again adding a Gaussian noise.
 * 
 * @author Gabor Pap, Gyorgy Balogh, Miklos Maroti
 */
public class GaussianRadioModel extends RadioModel{
	/** 
	 * The simulator, it is needed to access nodes, so that the radio model can
	 * do operations on them.
     */
	private Simulator sim = null;

	/**
	 * The exhibitor of the radio signal degradation, typically it is 2/2 if the
	 * motes are well above ground, though at ground level this factor is 
	 * closer to 3/2. For efficiency reasons, this number is the half of the
	 * usually defined falling factor.
	 */ 
	public double fallingFactorHalf = 1.1;
    
	/**
	 * This coefficient is used to "simulate" the static part of environmental
	 * noise. It is used when the mote field is first set up. 
	 */ 
	public double staticRandomFactor = 0.3;
 
	/** 
	 * This limits the number of neighbours used to calculate interference ratio.
	 * Using neighborhood instead of endless radio signals makes the simulation
	 * somewhat faster.
	 */
	public double radioStrengthCutoff = 0.1;
    
	/**
	 * This coefficient is used to "simulate" the dynamic part of environmental
	 * noise. The dynamic noise is recalculated for each transmission. 
	 */ 
	public double dynamicRandomFactor = 0.05;

	/**
	 * A parameterized constructor used to set the simulator at creation time.
	 * 
	 * @param sim a reference to the simulator
	 */
	public GaussianRadioModel(Simulator sim){
		this.sim = sim;
	}

	/**
	 * (Re)calculates the neighborhoods of every node  in the network. 
	 * This operation should be called whenever the location of the nodes 
	 * changed. This operation is extremely expensive and should be used sparsely.
	 */
	public void updateNeighborhoods() {
        // count nodes
		Vector<Node> nodes = sim.getNodes();		
		int nodeNum = nodes.size(); // get number of nodes
        
        Node[] neighbors = new Node[nodeNum];
        double[] staticFadings = new double[nodeNum];
        
        for (Iterator<Node> iterator1 = nodes.iterator(); iterator1.hasNext();) {
			Node node1 = (Node) iterator1.next();		
			int i = 0;
			
			for (Iterator<Node> iterator2 = nodes.iterator(); iterator2.hasNext();) {
				Node node2 = (Node) iterator2.next();				
				
				double staticRadioStrength = getStaticFading(node1, node2);
				if( staticRadioStrength >= radioStrengthCutoff && node1 != node2){
                    neighbors[i] = node2;
                    staticFadings[i] = staticRadioStrength;                    
					i++;
				}				
			}
			
			Neighborhood neighborhood = (Neighborhood)node1.getNeighborhood();
			neighborhood.neighbors = new Node[i];
            System.arraycopy(neighbors, 0, neighborhood.neighbors, 0, i );           
			neighborhood.staticFadings = new double[i];
            System.arraycopy(staticFadings, 0, neighborhood.staticFadings, 0, i );
			neighborhood.dynamicStrengths = new double[i];			
		}                                
	}

	/**
	 * This is a factory method for creating radio model specific neigborhoods.
	 */
	protected RadioModel.Neighborhood createNeighborhood(){
		return new Neighborhood();
	}

	/**
	 * Calculates the static part of the radio fading between two nodes based 
	 * on distance and a random factor.
	 * 
	 * @return The radio fading coefficient
	 */
	protected double getStaticFading(Node sender, Node receiver){
		double staticRandomFading = 1.0 + staticRandomFactor * Simulator.random.nextGaussian();

		return staticRandomFading <= 0.0 ? 0.0 : sender.getMaximumRadioStrength() * staticRandomFading 
			/ (1.0 + Math.pow(sender.getDistanceSquare(receiver), fallingFactorHalf));
	}

	/**
	 * Calculates the received radio strength based on the static signal strength
	 * determined by the {@link GaussianRadioModel#getStaticFading}, a dynamic
	 * random factor and the signal strength of the node, which can be time 
	 * variant.   
	 * 
	 * @param signalStrength the signal strength of the sender node
	 * @param staticFading the static fading as returned by {@link GaussianRadioModel#getStaticFading}.
	 * @return The signal strength at the receiver.
	 */
	protected double getDynamicStrength(double signalStrength, double staticFading){
		double dynamicRandomFading = 1.0 + dynamicRandomFactor * Simulator.random.nextGaussian();
		return dynamicRandomFading <= 0.0 ? 0.0 :
			signalStrength * staticFading * dynamicRandomFading;
	}
	
	/**
	 * This class stores all the node related data the GaussianRadioModel needs, 
	 * this includes an array of neighboring notes, the static fading and 
	 * the dynamic strentgh as well for every neighboring nodes plus the entity
	 * being transmitted by the node. 
	 */
	protected class Neighborhood extends RadioModel.Neighborhood{
		
		/** The vector of the neighboring nodes. */
		protected Node[] neighbors; 
		
		/**
		 * The vector of static fading factors. These numbers shall 
		 * be in the <code>[0,1]</code> interval.
		 */
		protected double[] staticFadings;

		/**
		 * This vector holds the signal strength values that we 
		 * contributed to the neighbours in the last transmission. 
		 * We must keep these values because we have to use the 
		 * same strengths for the matching {@link Node#receptionEnd} call. 
		 */
		protected double[] dynamicStrengths;

		/**
		 * Contains the stream object during an active transmission,
		 * or <code>null</code> if we do not transmit.
		 */
		protected Object stream = null;

		/**
		 * Calculates the dynamic signal strength based on the static fading 
		 * factors and a per-transmission dynamic random factor. Then calls the 
		 * {@link Node#receptionBegin} method on all neighbors.
		 */
		protected void beginTransmission(double strength, Object stream){
			if( stream == null )
				throw new IllegalArgumentException("The stream object must be non-null");
			else if( this.stream != null )
				throw new IllegalStateException("No nested transmissions are allowed");
			
			this.stream = stream;
			
			int i = neighbors.length;
			while( --i >= 0 ){
				double dynamicStrength = getDynamicStrength(strength, staticFadings[i]);
				dynamicStrengths[i] = dynamicStrength;
				neighbors[i].receptionBegin(dynamicStrength, stream);
			}
		}
		
		/**
		 * Calls the {@link Node#receptionEnd} method on all neighboring nodes.
		 * This method should always be called as the pair of the 
		 * {@link RadioModel.Neighborhood#beginTransmission} method. 
		 */
		protected void endTransmission(){
			int i = neighbors.length;
			while( --i >= 0 )
				neighbors[i].receptionEnd(dynamicStrengths[i], stream);
				
			stream = null;
		}
	}
}
