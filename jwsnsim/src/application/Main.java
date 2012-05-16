package application;

import application.appEgtsp.GradientApp;
import application.appFcsa.FloodingApp;
import application.appFtsp.FtspApp;
import application.appRateDetection.RateApp;

public class Main {
	
	public static void main(String[] args) {
		
		/*------------------------------------------*/
		//rateSimulations();
		/*------------------------------------------*/
		
		/*------------------------------------------*/
		// ftspSimulations();
		/*------------------------------------------*/	
			
		/*------------------------------------------*/
		//fcsaSimulations();
		/*------------------------------------------*/
		
		/*------------------------------------------*/
		//gradientSimulations();
		/*------------------------------------------*/
		
		/*------------------------------------------*/
		theoricSimulations();
		/*------------------------------------------*/
	}		


	public static void rateSimulations(){

		try{			
			new application.appRateDetection.RateApp(50,"RateConvergence.txt",RateApp.LINE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}									
	}


	/**
	 * FTSP simulations 
	 * with least-squares and minimum variance slope estimator 
	 */
	public static void ftspSimulations(){

		try{
			
//			for (int i = 10; i <= 100; i+= 10) {
//				for(int j=1;j<=10;j++){
//					new application.appFtsp.FtspApp(i,"FtspSim"+i+"#"+j,FtspApp.LINE);
//					new application.appFtspMinimumVariance.FtspApp(i,"FtspSimMV"+i+"#"+j,FtspApp.LINE);
//					
//				}					
//			}
			new application.appFtsp.FtspApp(20,"FTSP.txt",FtspApp.LINE);
//			new application.appRate.FloodingApp(40,"Rate.txt",FtspApp.LINE);
//			new application.appFcsa.FloodingApp(40,"Fcsa.txt",FloodingApp.LINE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}									
	}
	
	/**
	 * Flooding Time Synchronization With Common Speed Agreement
	 */
	public static void fcsaSimulations(){
		try{
//			for(int j = 1;j<=10;j++)
//				for(int i = 10;i<=100;i+=10)
//					new application.appFlooding.FloodingApp(i,"FloodingSimLine"+i+"#"+j,FloodingApp.LINE);
			
			/* tests for different topologies */
			//new application.appFcsa.FloodingApp(16,"FCSA_LINE_16.txt",FloodingApp.LINE);
			//new application.appFcsa.FloodingApp(16,"FCSA_GRID_16.txt",FloodingApp.GRID);
			//new application.appFcsa.FloodingApp(64,"FCSA_GRID_64.txt",FloodingApp.GRID);
			new application.appFcsa.FloodingApp(256,"FCSA_GRID_256.txt",FloodingApp.GRID);
			//new application.appFcsa.FloodingApp(1024,"FCSA_GRID_1024.txt",FloodingApp.GRID);
			
			//new application.appFcsa.FloodingApp(6,"FCSA_LINE_6.txt",FloodingApp.LINE);
			//new application.appFcsa.FloodingApp(14,"FCSA_LINE_14.txt",FloodingApp.LINE);
			//new application.appFcsa.FloodingApp(30,"FCSA_LINE_30.txt",FloodingApp.LINE);
			//new application.appFcsa.FloodingApp(62,"FCSA_LINE_62.txt",FloodingApp.LINE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}									
	}
	
	/**
	 * Flooding Time Synchronization With Common Speed Agreement
	 */
	public static void gradientSimulations(){
		try{
			for(int i = 20;i<=100;i+=20)
				new application.appEgtsp.GradientApp(i,"EGTSPR_"+i+".txt",GradientApp.RING);
//				for(int i = 10;i<=100;i+=10)
//			new application.appFcsaRt.FloodingApp(50,"FCSA.txt",FloodingApp.LINE);

//			new application.appGtsp.GtspApp(20,"Gtsp.txt",GtspApp.LINE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}									
	}
	
	private static void theoricSimulations() {
		try{
			new application.appTheoric.GradientApp(20,"gradient.txt",GradientApp.LINE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
		
	}

}
