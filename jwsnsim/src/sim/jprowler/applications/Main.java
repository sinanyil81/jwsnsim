package sim.jprowler.applications;

import sim.jprowler.GaussianRadioModel;
import sim.jprowler.Simulator;
import sim.jprowler.applications.PISync.PISyncProtocol;

public class Main {
	
	public static void main(String[] args) throws Exception{       
		System.out.println("creating nodes...");        
		Simulator sim = Simulator.getInstance();
		
		GaussianRadioModel radioModel = new GaussianRadioModel(sim);
		
		PISyncProtocol node1 = new PISyncProtocol(1,5,5,0,radioModel);
		PISyncProtocol node2 = new PISyncProtocol(2,10,10,0,radioModel);

		radioModel.updateNeighborhoods();
        
		sim.run(20000);       
	}
}
