package sim.jprowler.applications;

import sim.jprowler.GaussianRadioModel;
import sim.jprowler.Simulator;
import sim.jprowler.applications.PISync.PIProtocol;

public class Main {
	
	public static void main(String[] args) throws Exception{       
		System.out.println("creating nodes...");        
		Simulator sim = Simulator.getInstance();
		
		GaussianRadioModel radioModel = new GaussianRadioModel(sim);
		
		PIProtocol node1 = new PIProtocol(1,5,5,0,radioModel);
		PIProtocol node2 = new PIProtocol(2,10,10,0,radioModel);

		radioModel.updateNeighborhoods();
        
		sim.run(20000);       
	}
}
