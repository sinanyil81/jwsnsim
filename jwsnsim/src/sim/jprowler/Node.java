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

import sim.jprowler.RadioModel.Neighborhood;
import sim.jprowler.clock.Clock;

/**
 * This class is the base class of all nodes. Nodes are entities in a simulator
 * which act on behalf of themselves, they also have some basic attributes like 
 * location. Nodes also take part in radio transmissions, they initiate 
 * transmissions and receive incomming radio messages.
 * 
 * @author Gabor Pap, Gyorgy Balogh, Miklos Maroti
 */
public abstract class Node {
	
	/**
	 * The applications of the TinyOs node are linked together
	 * in a single linked list. This points to the first,
	 * and then {@link Protocol#nextApplication} to the next. 
	 */
	protected Protocol application = null;

	/** 
	 * This field defines the relative strength of a mote. If it is set to
	 * a high value for a given mote it can supress other motes.
	 */
	double maxRadioStrength = 100;	

	/** positions x in meters (not that it metters much) */
	protected double    x = 0;

	/** positions y in meters (not that it metters much) */
	protected double    y = 0;
	
	/** positions z in meters (not that it metters much) */
	protected double    z = 0;
    
	/** A reference to the simulator in which the Node exists. */
	public Simulator simulator;

	/**
	 * The id of the node. It is allowed that two nodes have
	 * the same id in the simulator.
	 */
	protected int id;
	
	/**
	 * The neighborhood of this node, meaning all the neighboring nodes which 
	 * interact with this one.
	 */
	private Neighborhood neighborhood;
	
	private Clock clock;
	
	private boolean isOn = false;
	
	private RadioModel radioModel;

	/**
	 * Parameterized constructor, sets the simulator and creates an initial 
	 * neighborhood using the RadioModel as a factory.
	 * 
	 * @param sim the Simulator
	 * @param radioModel the RadioModel used to create the nodes neighborhood
	 */
	public Node(Simulator sim, RadioModel radioModel,Clock clock){
		this.simulator = sim;
		this.clock = clock;
		this.radioModel = radioModel;
		neighborhood = radioModel.createNeighborhood();
	} 
	
	public boolean isOn(){
		return isOn;
	}
	
	/**
	 * 
	 */
	public void turnOn(){
		isOn = true;
		clock.start();
	}
	
	public void turnOff(){
		isOn = false;
		clock.stop();
	}
	
	/**
	 * A getter method used by the RadioModels to manipulate neighborhood of nodes. 
	 */
	public Neighborhood getNeighborhood(){
		return neighborhood;
	}
	
	public RadioModel getRadioModel(){
		return this.radioModel;
	}

	/**
	 * Calculates the square of the distance between two nodes. This method is 
	 * used by the radio models to calculate the fading of radio signals.
	 * 
	 * @param other The other node
	 * @return The square of the distance between this and the other node
	 */		
	public double getDistanceSquare(Node other){
		return (x-other.x)*(x-other.x) + (y-other.y)*(y-other.y) + (z-other.z)*(z-other.z);
	} 

	/**
	 * Returns the maximum radio strength this node will ever transmit with. 
	 * This must be a positive number.
	 * 
	 * @return the maximum transmit radio power
	 * @see Node#beginTransmission
	 */
	public double getMaximumRadioStrength(){
		return maxRadioStrength;
	}
	
	/**
	 * A setter function for the {@link Node#maxRadioStrength}
	 * field.
	 * 
	 * @param d the desired new transmit strength of this mote
	 */
	public void setMaximumRadioStrength(double d) {
		maxRadioStrength = d;
	}
	
	/**
	 * Called by the drived class implementing the MAC layer when
	 * radio transmission is initiated. This method will call the
	 * {@link Node#receptionBegin} method in each of the neighboring
	 * nodes with the same <code>stream</code> object but with
	 * a diminished radio signal strength. Derived classes must
	 * avoid nested transmissions. 
	 * 
	 * @param strength The signal strength of the transmission. This
	 * must be positive and less than or equal to the maximum transmit
	 * strength.
	 * @param stream The object that is beeing sent. This parameter
	 * cannot be <code>null</code> and two nodes cannot send
	 * the same object at the same time.
	 * @see Node#getTransmitStrengthMultiplicator
	 */		
	protected final void beginTransmission(double strength, Object stream) {
		neighborhood.beginTransmission(strength, stream);
	}

	/**
	 * Called by the derived class implementing the MAC layer when
	 * radio transmission is finished. This method will call the
	 * {@link Node#receptionEnd} method in each  of the neighboring
	 * nodes with the same <code>stream</code> object but
	 * with a diminished radio strength. Derived classes must make
	 * sure that this method is invoked only once for each matching
	 * {@link Node#beginTransmission} call.
	 */
	protected final void endTransmission() {
		neighborhood.endTransmission();
	}

	/**
	 * Called for each transmission of a neighboring node by the 
	 * radio model. The <code>recpetionBegin</code> and 
	 * <code>receptionEnd</code> calles can be nested or interleaved, 
	 * but they are always coming in pairs. The derived class 
	 * implementing the MAC protocol must select the transmission 
	 * that it wants to receive based on some heuristics on the 
	 * radio signal stregths. Note that these methods are called 
	 * even when the nodes is currently transmitting. 
	 * 
	 * @param strength The radio strength of the incoming signal.
	 * @param stream The object representing the incoming data.
	 * This stream object is never <code>null</code>.
	 * @see #receptionEnd
	 */
	protected abstract void receptionBegin(double strength, Object stream);

	/**
	 * Called for each transmission of a neighboring node by the 
	 * radio model. This method is always invoked after a corresponding
	 * {@link #receptionBegin} method invokation with the exact same 
	 * parameters.
	 * 
	 * @param strength The radio strength of the incoming signal.
	 * @param stream The received object message.
	 */
	protected abstract void receptionEnd(double strength, Object stream);
	
	/**
	 * Sends out a radio message. If the node is in receiving mode the sending is 
	 * postponed until the receive is finished. This method should behave
	 * exactly as the SendMsg.send command in TinyOS.
	 * 
	 * @param message the message to be sent
	 * @param app the application sending the message
	 * @return If the node is in sending state it returns false otherwise true.
	 */
	public abstract boolean sendMessage(RadioPacket message, Protocol app);
	
	/**
	 * Sets the id of the node. It is allowed that two nodes have the
	 * same id for experimentation.
	 * 
	 * @param id the new id of the node.
	 */
	public void setId( int id ){
		this.id = id;
	}

	/**
	 * Sets the position of the mote in space. Please call the 
	 * {@link RadioModel#updateNeighborhoods} to update the network topology 
	 * information before starting the simulation.
	 * 
	 * @param x the x position
	 * @param y the y position
	 * @param z the z position
	 */
	public void setPosition( double x, double y, double z ){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * A getter function for position X.
	 * 
	 * @return Returns the x coordinate of the node.
	 */
	public double getX(){
		return x;
	}

	/**
	 * A getter function for position Y.
	 * 
	 * @return Returns the y coordinate of the node.
	 */
	public double getY(){
		return y;
	}

	/**
	 * A getter function for position Z.
	 * 
	 * @return Returns the z coordinate of the node.
	 */
	public double getZ(){
		return z;
	}
	
	public int getId(){
		return id;
	}
	
	/**
	 * @return simply returns the simulator in which this Node exists
	 */
	public Simulator getSimulator(){
		return simulator;
	}
	
	public Clock getClock(){
		return clock;
	}
	
	/**
	 * This function is part of the application management. Adds an 
	 * {@link Protocol} to the list of applications running on this Node.
	 * Note that applications on a node represent TinyOS components, so do not 
	 * try to solve all your problems in a derived class of Node. Also note
	 * that there can be only one instance of an Application class running on 
	 * every Node, unlike components in TinyOS! Yes, this is a reasonable
	 * constraint that makes message demultiplexing easier.   
	 * 
	 * @param app the Application 
	 */
	public void addApplication(Protocol app){
		application = app;
	}

	/**
	 * Visiting the elements of the application list, it returns the first with
	 * the given application class.
	 * 
	 * @param appClass the class that identifies the needed application for us
	 * @return Returns the application instance running on this node
	 */
	protected Protocol getApplication(){
	
		return application;
	}
}
