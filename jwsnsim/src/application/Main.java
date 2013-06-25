package application;

import application.appEgtsp.GradientApp;
import application.appFcsa.FloodingApp;
import application.appFtsp.FtspApp;
import application.appPI.PIApp;
import application.appPulseSync.PulseSyncApp;
import application.appRateDetection.RateApp;
import application.appSelf.SelfApp;
import application.appSelfFlooding.SelfFloodingApp;

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
//			selfSimulations();
		/*------------------------------------------*/	
		
		/*------------------------------------------*/
		piSimulations();
		/*------------------------------------------*/
	}

	private static void piSimulations() {
		try {
//			new application.appPI.PIApp(100,"PILine.txt",PIApp.LINE);
//			new application.appPI.PIApp(100,"PILine.txt",PIApp.DENSE,10);
//			new application.appPIFlooding.PIFloodingApp(100,"PILine.txt",PIApp.LINE);
			
//			for (int i = 10; i <= 100; i += 10) {
//				for (int j = 1; j <= 5; j++) {
//					System.out.println("Diamater:"+i+" Counter:"+j);
//					new application.appPIFlooding.PIFloodingApp(i,"PILine_"+i+"#"+j+".txt",PIApp.LINE);		
//				}
//			}
			
//			for (int i = 5; i <= 25; i += 5) {
//				for (int j = 1; j <= 5; j++) {
//					System.out.println("Diamater:"+i+" Counter:"+j);
//					new application.appPI.PIApp(100,"PIDense_"+i+"#"+j+".txt",PIApp.DENSE,i);		
//					new application.appPIFlooding.PIFloodingApp(100,"PIFloodingDense_"+i+"#"+j+".txt",PIApp.DENSE,i);		
//					new application.appEgtsp.GradientApp(100,"GTSPDense_"+i+"#"+j+".txt",GradientApp.DENSE,i);
//					new application.appPulseSync.PulseSyncApp(100,"PulseDense_"+i+"#"+j+".txt",PulseSyncApp.DENSE,i);
//			}
				
			for (int i = 10; i <= 100; i += 10) {
					for (int j = 1; j <= 5; j++) {
						System.out.println("Diamater:"+i+" Counter:"+j);
						new application.appPI.PIApp(i,"PI_"+i+"#"+j+".txt",PIApp.LINE);		
						new application.appPIFlooding.PIFloodingApp(i,"PIFlooding_"+i+"#"+j+".txt",PIApp.LINE);		
						new application.appEgtsp.GradientApp(i,"GTSP_"+i+"#"+j+".txt",GradientApp.LINE);
						new application.appPulseSync.PulseSyncApp(i,"Pulse_"+i+"#"+j+".txt",PulseSyncApp.LINE);
					}

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
			new application.appPulseSync.PulseSyncApp(100, "NewPulse.txt", PulseSyncApp.LINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void selfSimulations() {
		try {
//			new SelfFloodingApp(25, "PILine.txt", FloodingApp.LINE);
			new application.appSelf.SelfApp(100, "Self.txt", SelfApp.DENSE,20);
//			new application.appEgtsp.GradientApp(100, "Self.txt", GradientApp.DENSE,20);
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
			
			new application.appFtsp.FtspApp(20,"PILine.txt",PIApp.LINE);
			
//			for (int i = 10; i <= 50; i += 10) {
//				for (int j = 1; j <= 5; j++) {
//					new application.appFtsp.FtspApp(i, "sims/ftsp/_FtspLine_"
//							+ i + "_" + j + "_" + ".txt", FtspApp.LINE);
//				}
//			}
//			
//			System.gc();

//			for (int i = 20; i <= 80; i += 20) {
//				for (int j = 1; j <= 3; j++) {
//					new application.appFtsp.FtspApp(i, "sims/ftsp/_FtspRing_"
//							+ i + "_" + j + "_" + ".txt", FtspApp.RING);
//				}
//			}

//			for (int i = 6; i < 31; i = i + 5) {
//				for (int j = 1; j <= 5; j++) {
//					new application.appFtsp.FtspApp(i * i,
//							"sims/ftsp/_FtspSyncGrid_" + i + "_" + j + "_" + ".txt",
//							FtspApp.GRID);
//				}
//			}
//			
//			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Flooding Time Synchronization With Common Speed Agreement
	 */
	public static void fcsaSimulations() {
		try {
			new application.appFcsa.FloodingApp(20,"skew.txt",FloodingApp.LINE);		
			
//			for(int j = 1;j<=5;j++)
//				for(int i = 10;i<=100;i+=10)
//					new application.appFcsa.FloodingApp(i,"FCSALine_"+i+"#"+j,FloodingApp.LINE);

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
