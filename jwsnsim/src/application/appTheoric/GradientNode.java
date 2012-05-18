package application.appTheoric;

import java.util.Iterator;
import java.util.Vector;

public class GradientNode implements TimerHandler{
	
	/**
	 * Gradient Clock Synchronization Parameters
	 */
	public static final double beta = 1.0013;
	public static final double kappa = 766;
	public static final double epsilon = 0.00008;
//	private static final double sigma = 2;
	public static final int PERIOD = 1024*128;	
		
	private int id;
	
	private HardwareClock hardwareClock = new HardwareClock();	
	private LogicalClock  logicalClock = new LogicalClock(hardwareClock);		
	public EstimateLayer  estimateLayer = new EstimateLayer(hardwareClock);
	
	private Timer sendTimer = new Timer(hardwareClock, this);
	private Timer checkTimer = new Timer(hardwareClock, this);
	
	Vector<GradientNode> neighbors = new Vector<GradientNode>();

	public GradientNode(int id,double clockVal) {
		this.id = id;
		hardwareClock.start();
		logicalClock.setValue(new SimTime(clockVal));
		sendTimer.startPeriodic(PERIOD);
	}
	
	public void addNeighbor(GradientNode node){
		neighbors.add(node);
		estimateLayer.addNeighbor(node);
	}
	
	public void sendMessage(){
		for (Iterator<GradientNode> iterator = neighbors.iterator(); iterator.hasNext();) {
			GradientNode node = (GradientNode) iterator.next();
			node.receiveMessage(this, new SimTime(logicalClock.getValue()));			
		}		
	}
	
	public void receiveMessage(GradientNode node, SimTime value){
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
		SimTime max = estimateLayer.getMaximumEstimate();
		SimTime min = estimateLayer.getMinimumEstimate();
		SimTime value = logicalClock.getValue();
		
		double ahead = 0;
		double behind = 0;
		
		if(max!=null && min !=null){
			ahead = max.sub(value).toDouble();
			behind = value.sub(min).toDouble();	
		}
		
		
		double a = Math.floor(ahead/kappa -0.25);
		double b = Math.floor(behind/kappa +0.25);
			
		logicalClock.setMult((a>=b) ? beta : 1);
		
		if(logicalClock.getMult() !=1){
			checkTimer.startOneshot(1);
		}
		else{
			checkTimer.stop();
		}
	}
	
	public String toString(){
		String s ="";
		
		s += Simulator.getInstance().getTime().getTimeHigh()/32/1024;		
		s += " " + id;
		s += " " + logicalClock.getValue().toString();
		s += " 0";
		s += " 0";
		s += " 0";
		s += " 0";
		s += " 0";
		
		return s;		
	}
}
