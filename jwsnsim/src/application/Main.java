package application;

import sim.gui.GUI;
import sim.mobility.MobilityManager;
import sim.node.NodeFactory;
import sim.topology.Line2D;
import sim.topology.RandomDeployment;

public class Main {

	public static void main(String[] args) {
//		sample();	
		mobilitySample();
	}
	
	static void sample(){
		/* create nodes */
		NodeFactory.createNodes("application.appPI.PINode", 20, new Line2D());
			
		/* start simulation */
		new Simulation("logFile.txt",20000);
	}
	
	static void mobilitySample(){
		/* create nodes */
		NodeFactory.createNodes("application.appPI.PINode", 1, new RandomDeployment());
		new MobilityManager("sim.mobility.RandomWayPoint");
		
		GUI.start();
		
		/* start simulation */
		new Simulation("logFile.txt",2000000);
	}
	
//	private static void diameterSimulations(String className) {
//		System.out.println(className);
//		try {
//			for (int i = 10; i <= 100; i += 10) {
//				for (int j = 1; j <= 5; j++) {
//					System.out.println("Diamater:" + i + " Counter:" + j);
//					new Simulation(className,i,className 
//							+"_diameter:" + i 
//							+"_count:" + j +".txt", Simulation.LINE, 0);					
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
