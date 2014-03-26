package application;

import java.util.Random;

import sim.gui.GUI;
import sim.mobility.MobilityManager;
import sim.mobility.RandomWayPoint;
import sim.node.NodeFactory;
import sim.statistics.Distribution;
import sim.topology.Circle2D;
import sim.topology.Grid2D;
import sim.topology.Line2D;
import sim.topology.RandomDeployment;

public class Main {

	public static void main(String[] args) {
		sample();	
//		mobilitySample();
	}
	
	static void sample(){
		/* create nodes */
		Distribution.setSeed(0x123456L);
//		NodeFactory.createNodes("application.appPIFlooding.PIFloodingNode", 20, new Grid2D());
//		NodeFactory.createNodes("application.appPIFlooding.PIFastFloodingNode", 20, new Line2D());
//		NodeFactory.createNodes("application.appPulseSync.PulseSyncNode", 20, new Grid2D());
//		NodeFactory.createNodes("application.appFtsp.FtspNode", 20, new Grid2D());
		NodeFactory.createNodes("application.appPI.PINode", 100, new Grid2D());
//		NodeFactory.createNodes("application.appEgtsp.GradientNode", 20, new Grid2D());

			
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
		NodeFactory.createNodes("application.appEgtsp.GradientNode", 300, new RandomDeployment());
		
		new MobilityManager("sim.mobility.RandomWayPoint");
		
		GUI.start();
		
		/* start simulation */
		new SynchronizationSimulation("AVTSMobility.txt",25000);
	}
}
