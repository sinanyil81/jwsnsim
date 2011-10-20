package application;

import application.appFlooding.FloodingApp;
import application.appFtspMinimumVariance.FtspApp;

public class Main {
	
	static int MAX_DIAMETER = 60;
	private static final int DIAMETER_OFFSET = 5;
	
	public static void main(String[] args) {
		/*------------------------------------------*/
		ftspSimulations();
		/*------------------------------------------*/	
			
		/*------------------------------------------*/
		//fcsaSimulations();
		/*------------------------------------------*/		
	}		


	/**
	 * FTSP simulations 
	 * with least-squares and minimum variance slope estimator 
	 */
	public static void ftspSimulations(){

		try{
			
			for (int i = 10; i <= 100; i+= 10) {
				for(int j=1;j<=10;j++){
					new application.appFtsp.FtspApp(i,"FtspSim"+i+"#"+j,FtspApp.LINE);
					new application.appFtspMinimumVariance.FtspApp(i,"FtspSimMV"+i+"#"+j,FtspApp.LINE);
					
				}
			}
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
			for(int j = 1;j<=10;j++)
				for(int i = 10;i<=100;i+=10)
					new application.appFlooding.FloodingApp(i,"FloodingSimLine"+i+"#"+j,FloodingApp.LINE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}									
	}
}
