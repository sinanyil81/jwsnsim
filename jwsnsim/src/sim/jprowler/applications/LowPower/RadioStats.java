package sim.jprowler.applications.LowPower;

import org.jfree.data.xy.XYSeries;

import sim.jprowler.Node;
import sim.jprowler.Simulator;

public class RadioStats {
	long startEpoch = -1;
	long endEpoch = -1;
	
	long offStart = -1;
	long offEnd = -1;
	
	long totalOff = 0;
	
	long packetLoss = 0;
	
	double dutyCycle = 1.0;
	
	TDMANode node = null;
//	Logger logger = null;
	
    XYSeries duty = new XYSeries("On/Off Cycle");
	XYSeries sync = new XYSeries("Max Local Error");
	XYSeries packet = new XYSeries("LostPackets");
	
	public RadioStats(TDMANode node){
		this.node = node;
//		logger = new Logger("DutyCycle"+node.getId()+".txt");
	}
	
	public void on(){
//		String s ="";
//		s += node.getId() + " 1 ";
//		s += Simulator.getInstance().getTime();
//		logger.log(s);
//		duty.add(Simulator.getInstance().getTime().getSecond(),0);
//		duty.add(Simulator.getInstance().getTime().getSecond(),1);			
		
		if(offStart != -1){
			offEnd = (long) Simulator.getInstance().getTime().toDouble();
			totalOff += (offEnd - offStart);
			offStart = -1;
			offEnd = -1;
		}
	}
	
	public void off(){
//		String s ="";
//		s += node.getId() + " 0 ";
//		s += Simulator.getInstance().getTime();
//		logger.log(s);
//		duty.add(Simulator.getInstance().getTime().getSecond(),1);
//		duty.add(Simulator.getInstance().getTime().getSecond(),0);
		
		offStart = (long) Simulator.getInstance().getTime().toDouble();
	}
	
	public XYSeries getDutyCycle(){
		return duty;
	}
		
	public XYSeries getLocalSkew(){
		return sync;
	}
	
	public XYSeries getLostPackets(){
		return packet;
	}
	
	public void startEpoch(){
		if(startEpoch != -1){
			endEpoch = (long) Simulator.getInstance().getTime().toDouble();
			if(totalOff > -1){
				dutyCycle =(double)(endEpoch - startEpoch - totalOff)/(double)(endEpoch - startEpoch);
			}
			else{
				System.out.println("khkj");
			}
			
			if((endEpoch - startEpoch)> 0xDFFFF){
				duty.add(Simulator.getInstance().getTime().getSecond(),dutyCycle);
				sync.add(Simulator.getInstance().getTime().getSecond(),((TDMANode)node).synchronizer.getMaxError());
				packet.add(Simulator.getInstance().getTime().getSecond(),packetLoss);
			}
			
					
		}
		
		totalOff = 0;			
		startEpoch = (long) Simulator.getInstance().getTime().toDouble(); 
		dutyCycle = 1.0;
	}
	
	public void incrementPacketLoss(){
		packetLoss++;
	}
}
