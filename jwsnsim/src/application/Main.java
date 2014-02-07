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
		NodeFactory.createNodes("application.appPI.PINode", 100, new Grid2D());
			
		GUI.start();
		
		/* start simulation */
		new SynchronizationSimulation("logFile.txt",20000);
	}
	
	static void mobilitySample(){
		/* create nodes */
		NodeFactory.createNodes("application.appSelfFlooding.SelfFloodingNode", 500, new RandomDeployment());
		new MobilityManager("sim.mobility.RandomWayPoint");
		
		GUI.start();
		
		/* start simulation */
		new SynchronizationSimulation("logFile.txt",2000000);
	}
}
