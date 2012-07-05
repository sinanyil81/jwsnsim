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
		// rateSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
//		ftspSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
//		pulseSyncSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
		// fcsaSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
		// gradientSimulations();
		/*------------------------------------------*/

		/*------------------------------------------*/
		  selfSimulations();
		/*------------------------------------------*/
	}

	private static void pulseSyncSimulations() {

		try {
			for (int i = 10; i <= 40; i += 10) {
				for (int j = 1; j <= 3; j++) {
					new application.appPulseSync.PulseSyncApp(i,
							"sims/pulse/_PulseSyncLine_" + i + "_" + j + "_" + ".txt",
							PulseSyncApp.LINE);
				}
			}

			for (int i = 20; i <= 80; i += 20) {
				for (int j = 1; j <= 3; j++) {
					new application.appPulseSync.PulseSyncApp(i,
							"sims/pulse/_PulseSyncRing_" + i + "_" + j + "_" + ".txt",
							PulseSyncApp.RING);
				}
			}

			for (int i = 6; i < 26; i = i + 5) {
				for (int j = 1; j <= 3; j++) {
					new application.appPulseSync.PulseSyncApp(i * i,
							"sims/pulse/_PulseSyncGrid_" + i + "_" + j + "_" + ".txt",
							PulseSyncApp.GRID);
				}
			}
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
			for (int i = 10; i <= 40; i += 10) {
				for (int j = 1; j <= 3; j++) {
					new application.appFtsp.FtspApp(i, "sims/ftsp/_FtspLine_"
							+ i + "_" + j + "_" + ".txt", FtspApp.LINE);
				}
			}

			for (int i = 20; i <= 80; i += 20) {
				for (int j = 1; j <= 3; j++) {
					new application.appFtsp.FtspApp(i, "sims/ftsp/_FtspRing_"
							+ i + "_" + j + "_" + ".txt", FtspApp.RING);
				}
			}

			for (int i = 6; i < 26; i = i + 5) {
				for (int j = 1; j <= 3; j++) {
					new application.appFtsp.FtspApp(i * i,
							"sims/ftsp/_FtspSyncGrid_" + i + "_" + j + "_" + ".txt",
							FtspApp.GRID);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Flooding Time Synchronization With Common Speed Agreement
	 */
	public static void fcsaSimulations() {
		try {
			// for(int j = 1;j<=10;j++)
			// for(int i = 10;i<=100;i+=10)
			// new
			// application.appFlooding.FloodingApp(i,"FloodingSimLine"+i+"#"+j,FloodingApp.LINE);

			/* tests for different topologies */
			// new
			// application.appFcsa.FloodingApp(16,"FCSA_LINE_16.txt",FloodingApp.LINE);
			// new
			// application.appFcsa.FloodingApp(16,"FCSA_GRID_16.txt",FloodingApp.GRID);
			// new
			// application.appFcsa.FloodingApp(64,"FCSA_GRID_64.txt",FloodingApp.GRID);
			new application.appFcsa.FloodingApp(256, "FCSA_GRID_256.txt",
					FloodingApp.GRID);
			// new
			// application.appFcsa.FloodingApp(1024,"FCSA_GRID_1024.txt",FloodingApp.GRID);

			// new
			// application.appFcsa.FloodingApp(6,"FCSA_LINE_6.txt",FloodingApp.LINE);
			// new
			// application.appFcsa.FloodingApp(14,"FCSA_LINE_14.txt",FloodingApp.LINE);
			// new
			// application.appFcsa.FloodingApp(30,"FCSA_LINE_30.txt",FloodingApp.LINE);
			// new
			// application.appFcsa.FloodingApp(62,"FCSA_LINE_62.txt",FloodingApp.LINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Flooding Time Synchronization With Common Speed Agreement
	 */
	public static void gradientSimulations() {
		try {
			for (int i = 20; i <= 100; i += 20)
				new application.appEgtsp.GradientApp(i, "EGTSPR_" + i + ".txt",
						GradientApp.RING);
			// for(int i = 10;i<=100;i+=10)
			// new
			// application.appFcsaRt.FloodingApp(50,"FCSA.txt",FloodingApp.LINE);

			// new application.appGtsp.GtspApp(20,"Gtsp.txt",GtspApp.LINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
