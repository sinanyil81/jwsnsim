package application;

import sim.gui.GUI;
import sim.mobility.MobilityManager;
import sim.node.NodeFactory;
import sim.topology.Circle2D;
import sim.topology.Grid2D;
import sim.topology.Line2D;
import sim.topology.RandomDeployment;

public class Main {

	public static void main(String[] args) {
//		sample();	
		mobilitySample();
	}
	
	static void sample(){
		/* create nodes */
		NodeFactory.createNodes("application.appPI.PINode", 42, new Grid2D());
			
		GUI.start();
		
		/* start simulation */
		new SynchronizationSimulation("logFile.txt",20000);
	}
	
	static void mobilitySample(){
		
		/* Uncomment to have fixed mobility */
//		Distribution.setSeed(0x123456L);
//		RandomDeployment.rand = new Random(0x123456L);
//		RandomWayPoint.random = new Random(0x123456L);
		
		/* create nodes */
		NodeFactory.createNodes("application.appSelfFlooding.SelfFloodingNode", 300, new RandomDeployment());
		new MobilityManager("sim.mobility.RandomWayPoint");
		
		GUI.start();
		
		/* start simulation */
		new SynchronizationSimulation("AVTSMobility.txt",20000);
	}
}
