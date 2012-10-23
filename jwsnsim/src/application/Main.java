package application;

import application.appEgtsp.GradientApp;
import application.appFcsa.FloodingApp;
import application.appFtsp.FtspApp;
import application.appPulseSync.PulseSyncApp;
import application.appRateDetection.RateApp;
import application.appTheoric.SimTime;

public class Main {

	public static void main(String[] args) {

		/*------------------------------------------*/
//			rateSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
//			ftspSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
//			pulseSyncSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
//		 	fcsaSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
//			gradientSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
			selfSimulations();
		/*------------------------------------------*/	
	}

	private static void pulseSyncSimulations() {

		try {
			for (int i = 10; i <= 100; i += 10) {
				for (int j = 1; j <= 5; j++) {
					new application.appPulseSync.PulseSyncApp(i,"PulseSyncLine_" + i + "_" + j + "_" + ".txt",
							PulseSyncApp.LINE);
				}
			}
//			
//			System.gc();

//			for (int i = 6; i < 31; i = i + 5) {
//				for (int j = 1; j <= 5; j++) {
//					new application.appPulseSync.PulseSyncApp(i * i,
//							"sims/pulse/_PulseSyncGrid_" + i + "_" + j + "_" + ".txt",
//							PulseSyncApp.GRID);
//					System.gc();
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void pulseSync() {

		try {
			new application.appPulseSync.PulseSyncApp(20, "NewPulse.txt", PulseSyncApp.LINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void selfSimulations() {
		try {
			new application.appSelf.SelfApp(20, "Self.txt", RateApp.LINE);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void rateSimulations() {

		try {
			new application.appRateDetection.RateApp(50, "RateConvergence.txt",
					RateApp.LINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * FTSP simulations with least-squares and minimum variance slope estimator
	 */
	public static void ftspSimulations() {

		try {
			for (int i = 10; i <= 50; i += 10) {
				for (int j = 1; j <= 5; j++) {
					new application.appFtsp.FtspApp(i, "sims/ftsp/_FtspLine_"
							+ i + "_" + j + "_" + ".txt", FtspApp.LINE);
				}
			}
			
			System.gc();

//			for (int i = 20; i <= 80; i += 20) {
//				for (int j = 1; j <= 3; j++) {
//					new application.appFtsp.FtspApp(i, "sims/ftsp/_FtspRing_"
//							+ i + "_" + j + "_" + ".txt", FtspApp.RING);
//				}
//			}

			for (int i = 6; i < 31; i = i + 5) {
				for (int j = 1; j <= 5; j++) {
					new application.appFtsp.FtspApp(i * i,
							"sims/ftsp/_FtspSyncGrid_" + i + "_" + j + "_" + ".txt",
							FtspApp.GRID);
				}
			}
			
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Flooding Time Synchronization With Common Speed Agreement
	 */
	public static void fcsaSimulations() {
		try {
			for(int j = 1;j<=5;j++)
				for(int i = 10;i<=100;i+=10)
					new application.appFcsa.FloodingApp(i,"FCSALine_"+i+"#"+j,FloodingApp.LINE);

//			for(int j = 1;j<=5;j++){
//				new application.appFcsa.FloodingApp(16,"FCSA_GRID_4x4_"+j+".txt",FloodingApp.GRID);
//				new application.appFcsa.FloodingApp(6,"FCSA_LINE_6_"+j+".txt",FloodingApp.LINE);
//				
//				new application.appFcsa.FloodingApp(64,"FCSA_GRID_8x8_"+j+".txt",FloodingApp.GRID);
//				new application.appFcsa.FloodingApp(14,"FCSA_LINE_14_"+j+".txt",FloodingApp.LINE);
//				
//				new application.appFcsa.FloodingApp(256,"FCSA_GRID_16x16_"+j+".txt",FloodingApp.GRID);
//				new application.appFcsa.FloodingApp(30,"FCSA_LINE_30_"+j+".txt",FloodingApp.LINE);
//				
//				new application.appFcsa.FloodingApp(1024,"FCSA_GRID_32x32_"+j+".txt",FloodingApp.GRID);
//				new application.appFcsa.FloodingApp(62,"FCSA_LINE_62_"+j+".txt",FloodingApp.LINE);		
//			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Flooding Time Synchronization With Common Speed Agreement
	 */
	public static void gradientSimulations() {
		try {
			for(int j = 1;j<=5;j++)
				for(int i = 80;i<=100;i+=10)
					new application.appEgtsp.GradientApp(i,"GTSPLine_"+i+"#"+j,GradientApp.LINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
