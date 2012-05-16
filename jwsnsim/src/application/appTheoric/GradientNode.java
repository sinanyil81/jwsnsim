package application.appTheoric;

import java.util.Iterator;
import java.util.Vector;

import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.simulator.Simulator;

public class GradientNode implements TimerHandler{
	
	/**
	 * Gradient Clock Synchronization Parameters
	 */
	public static final double beta = 1.0005;
	public static final double kappa = 53;
	public static final double epsilon = 0;
//	private static final double sigma = 2;
	public static final int PERIOD = 2^17;	
		
	private int id;
	
	private HardwareClock hardwareClock = new HardwareClock();	
	private LogicalClock  logicalClock = new LogicalClock(hardwareClock);		
	public EstimateLayer  estimateLayer = new EstimateLayer(hardwareClock);
	
	private Timer sendTimer = new Timer(hardwareClock, this);
	private Timer checkTimer = new Timer(hardwareClock, this);
	
	Vector<GradientNode> neighbors = new Vector<GradientNode>();

	public GradientNode(int id) {
		this.id = id;
		hardwareClock.start();
		sendTimer.startPeriodic(PERIOD);
	}
	
	public void addNeighbor(GradientNode node){
		neighbors.add(node);
		estimateLayer.addNeighbor(node);
	}
	
	public void sendMessage(){
		double value = logicalClock.getValue();
		for (Iterator<GradientNode> iterator = neighbors.iterator(); iterator.hasNext();) {
			GradientNode node = (GradientNode) iterator.next();
			node.receiveMessage(this, value);			
		}		
	}
	
	public void receiveMessage(GradientNode node, double value){
		estimateLayer.updateEstimate(node, value);
		adjustRate();
	}

	@Override
	public void fireEvent(Timer timer) {
		if(timer == this.sendTimer)
			sendMessage();
		else if(timer == checkTimer)
			adjustRate();
	}

	private void adjustRate() {
		double max = estimateLayer.getMaximumEstimate();
		double min = estimateLayer.getMinimumEstimate();
		double value = logicalClock.getValue();
		
		/* adjust rates */ 
		if( (max > 0) && (min > 0) ){			
			
			double a = Math.floor((max-value)/kappa -0.25);
			double b = Math.floor((value-min)/kappa +0.25);
			
			logicalClock.setMult((a>=b) ? beta : 1);
		}
		
		if(logicalClock.getMult() !=1){
			checkTimer.startOneshot(1);
		}
		else{
			checkTimer.stop();
		}
	}
	
	public String toString(){
		String s = Simulator.getInstance().getSecond().toString(10);
		
		s += " " + id;
		s += " " + (long)logicalClock.getValue();
		s += " 0";
		s += " 0";
		s += " 0";
		s += " 0";
		s += " 0";
		
		return s;		
	}
}
