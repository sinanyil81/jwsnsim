package application;

import java.util.Random;

import application.appTheoric.Simulator;
import sim.gui.GUI;
import sim.mobility.MobilityManager;
import sim.mobility.RandomWayPoint;
import sim.node.NodeFactory;
import sim.simulator.Simulation;
import sim.statistics.Distribution;
import sim.topology.Circle2D;
import sim.topology.Grid2D;
import sim.topology.Line2D;
import sim.topology.RandomDeployment;

public class Main {

	public static void main(String[] args) {
//		sample();	
//		mobilitySample();
		simulations();
	}
	
	static void sample(){
		/* create nodes */
		Distribution.setSeed(0x123456L);
//		NodeFactory.createNodes("application.appPIFlooding.PIFloodingNode", 20, new Grid2D());
//		NodeFactory.createNodes("application.appPIFlooding.PIFastFloodingNode", 20, new Line2D());
//		NodeFactory.createNodes("application.appPulseSync.PulseSyncNode", 20, new Grid2D());
//		NodeFactory.createNodes("application.appFtsp.FtspNode", 20, new Grid2D());
//		NodeFactory.createNodes("application.appPI.PINode", 100, new Grid2D());
		NodeFactory.createNodes("application.appEgtsp.GradientNode", 100, new Line2D());

			
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
	
	static void simulations(){
		String [] titles = new String[]{
				"application.appPIFlooding.PIFloodingNode",
				"application.appPIFlooding.PIFastFloodingNode",
				"application.appPI.PINode",
				"application.appEgtsp.GradientNode",
				"application.appPulseSync.PulseSyncNode"
				};
		for(int k=0;k<5;k++){
			for (int i = 10; i <= 150; i+=10) {
				for (int j = 0; j < titles.length; j++) {
					Simulator.getInstance().reset();
//					Distribution.setSeed(0x0L);
					NodeFactory.createNodes(titles[j], i, new Grid2D());
					GUI.start();
					/* start simulation */
					new SynchronizationSimulation(titles[j]+"."+i+"."+k+".txt",10000);
					GUI.stop();
				}
			}
			
		}				
	}
}
