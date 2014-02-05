package application;

import javax.swing.JFrame;

import sim.gui.MainFrame;
import sim.gui.NodePanel;

public class Main {

	public static void main(String[] args) {
		
		new Application("application.appSelf.SelfNode", 100, "Self.txt",Application.LINE, 0);
//		new Application("application.appPI.PINode", 100, "AvgPISync.txt",Application.LINE, 0);

		/*------------------------------------------*/
//		diameterSimulations("application.appPI.PINode");
//		diameterSimulations("application.appPIFlooding.PIFloodingNode");
//		diameterSimulations("application.appEgtsp.GradientNode");
//		diameterSimulations("application.appFcsa.FloodingNode");
//		diameterSimulations("application.appFtsp.FtspNode");
//		diameterSimulations("application.appPulseSync.PulseSyncNode");
//		diameterSimulations("application.appSelfFlooding.SelfFloodingNode");
//		diameterSimulations("application.appPIFlooding.PIFastFloodingNode");
		/*------------------------------------------*/
		
	}
	
	private static void diameterSimulations(String className) {
		System.out.println(className);
		try {
			for (int i = 10; i <= 100; i += 10) {
				for (int j = 1; j <= 5; j++) {
					System.out.println("Diamater:" + i + " Counter:" + j);
					new Application(className,i,className 
							+"_diameter:" + i 
							+"_count:" + j +".txt", Application.LINE, 0);					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
