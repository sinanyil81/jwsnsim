package application;

import graph.XYGraph;

import java.awt.Color;
import java.util.Random;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import application.appTheoric.Simulator;
import sim.gui.GUI;
import sim.mobility.MobilityManager;
import sim.mobility.RandomWayPoint;
import sim.node.NodeFactory;
import sim.simulator.Simulation;
import sim.statistics.Distribution;
import sim.statistics.GaussianDistribution;
import sim.topology.Circle2D;
import sim.topology.Grid2D;
import sim.topology.Line2D;
import sim.topology.RandomDeployment;

public class Main {

	public static void main(String[] args) {
//		sample();
		// mobilitySample();
		// simulations();
		 gradientDescentAlgorithm();
	}

	static void sample() {
		/* create nodes */
		// Distribution.setSeed(0x123456L);
		NodeFactory.createNodes("application.appGradientDescent.GDNode", 100,
				new Line2D());

		GUI.start();

		/* start simulation */
		new SynchronizationSimulation("logFile.txt", 20000);
	}

	static void mobilitySample() {

		/* Uncomment to have fixed mobility */
		// Distribution.setSeed(0x123456L);
		// RandomDeployment.rand = new Random(0x123456L);
		// RandomWayPoint.random = new Random(0x123456L);

		/* create nodes */
		NodeFactory.createNodes("application.appEgtsp.GradientNode", 300,
				new RandomDeployment());

		new MobilityManager("sim.mobility.RandomWayPoint");

		GUI.start();

		/* start simulation */
		new SynchronizationSimulation("AVTSMobility.txt", 25000);
	}

	static void simulations() {
		String[] titles = new String[] {
				"application.appPIFlooding.PIFloodingNode",
				"application.appPIFlooding.PIFastFloodingNode",
				"application.appPI.PINode",
				"application.appEgtsp.GradientNode",
				"application.appPulseSync.PulseSyncNode" };
		for (int k = 0; k < 5; k++) {
			for (int i = 4; i <= 128; i *= 2) {
				for (int j = 0; j < titles.length; j++) {
					Simulator.getInstance().reset();
					NodeFactory.createNodes(titles[j], i, new Grid2D());
					GUI.start();
					/* start simulation */
					new SynchronizationSimulation(titles[j] + "." + i + "." + k
							+ ".txt", 20000);
					GUI.stop();
				}
			}

		}
	}

	static void gradientDescentAlgorithm() {
		double f = 1000000.0;
		double B = 30;
		double f_i = 1000100.0;
		double delta_i = 1.0;		
		double alpha_i = 0.5;
		
		double delta_i_1 = 1.0;
		

		XYSeriesCollection dataset = new XYSeriesCollection();

		XYSeries drift = new XYSeries("drift 1");
		XYSeries drift_1 = new XYSeries("Frequency of the logical clock");
		
		drift_1.add(0, delta_i_1*f_i);
		drift.add(0, delta_i*f_i);

		for (int i = 1;i<1000; i++) {
			double error = (delta_i * f_i - f);
			error *= B;

			delta_i -= alpha_i * error / (B * f);
			
			error = (delta_i_1 * f_i - f);
			error *= B;
			double delay = GaussianDistribution.nextGaussian(0, 100);
			error += delay;
			
			delta_i_1 -= alpha_i * error / (B * f);
			
			drift.add(i, delta_i*f_i);
			drift_1.add(i,delta_i_1*f_i);
		}

//		dataset.addSeries(drift);	
		dataset.addSeries(drift_1);
			
		XYGraph graph = new XYGraph("ss", new NumberAxis("seconds"),
				new NumberAxis("error (microseconds)"), dataset);
		
		graph.setPlotColor(new Color[] {Color.BLUE,Color.RED,Color.GREEN,Color.ORANGE});
		graph.setPlotThickness(new float[]{1.3f,1.3f,1.0f,1.0f});
		graph.getRenderer().setBaseShapesVisible(false);

	}
}
