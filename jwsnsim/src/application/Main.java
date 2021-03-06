package application;

import graph.XYGraph;

import java.awt.Color;
import java.util.Random;

import nodes.NodeFactory;
import nodes.mobility.MobilityManager;
import nodes.mobility.RandomWayPoint;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import core.Simulation;
import application.appTheoric.Simulator;
import application.tools.AvtSimple;
import sim.gui.GUI;
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
//		 gradientDescentAlgorithm();
		avtRobustness();
	}

	static void sample() {
		/* create nodes */
//		 Distribution.setSeed(0x123456L);
		NodeFactory.createNodes("application.appGradientDescent.GDNode", 20,
				new Line2D());
//		NodeFactory.createNodes("application.appPIFlooding.PIFloodingNode", 50,
//				new Line2D());

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
		double alpha_i = 1.0;
		double error_i = 1.0;
		
		double delta_j = 1.0;		
		double alpha_j = 0.5;
		double error_j = 1.0;
		
		double delta_k = 1.0;		
		double alpha_k = 0.1;
		double error_k = 1.0;
			
		double delta_l = 1.0;		
		double alpha_l = 1.0;
		double error_l = 1.0;
		double t_h_1 = 0;
		double lastDerivative = 0.0;
		

		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeriesCollection dataset1 = new XYSeriesCollection();

		XYSeries drift_i = new XYSeries("alpha=1");
		XYSeries drift_j = new XYSeries("alpha=0.5");
		XYSeries drift_k = new XYSeries("alpha=0.1");
		XYSeries drift_l = new XYSeries("adaptive alpha");
		
		XYSeries alpha_adaptive = new XYSeries("");
		
		drift_i.add(0, delta_i*f_i);
		drift_j.add(0, delta_j*f_i);
		drift_k.add(0, delta_k*f_i);
		drift_l.add(0, delta_l*f_i);
		

		for (int i = 1;i<1000; i++) {
			double delay = GaussianDistribution.nextGaussian(0, 100);
			
			error_i = (delta_i * f_i - f);
			error_i *= B;
			error_i += delay;
			delta_i -= alpha_i * error_i / (B * f);
			
			error_j = (delta_j * f_i - f);
			error_j *= B;
			error_j += delay;
			delta_j -= alpha_j * error_j / (B * f);
			
			error_k = (delta_k * f_i - f);
			error_k *= B;
			error_k += delay;
			delta_k -= alpha_k * error_k / (B * f);
			
			error_l = (delta_l * f_i - f);
			double derivative = error_l;
			error_l *= B;
			error_l += delay;
			 
			
			if(Math.signum(derivative) == Math.signum(lastDerivative)){
				alpha_l *= 2.0f;			
			}
			else{
				alpha_l /=3.0f;
			}
			
			if (alpha_l > 1.0f) alpha_l = 1.0f;		
			
			alpha_adaptive.add(i,alpha_l);
			
//			double delay = GaussianDistribution.nextGaussian(0, 100);
//			error += delay;
			
			delta_l -= alpha_l * error_l / (B * f);
			lastDerivative = derivative;
			
			drift_i.add(i, delta_i*f_i);
			drift_j.add(i, delta_j*f_i);
			drift_k.add(i, delta_k*f_i);
			drift_l.add(i, delta_l*f_i);
			
			if(i==20)
				f_i = 1000050.0;
		}

//		dataset.addSeries(drift);
		dataset.addSeries(drift_l);
		dataset.addSeries(drift_i);
		dataset.addSeries(drift_j);
		dataset.addSeries(drift_k);
		
			
		XYGraph graph = new XYGraph("ss", new NumberAxis("iteration"),
				new NumberAxis("frequency"), dataset);
		
		graph.setPlotColor(new Color[] {Color.BLUE,Color.ORANGE,Color.RED,Color.GREEN});
		graph.setPlotThickness(new float[]{1.3f,1.3f,1.0f,1.0f});
		graph.getRenderer().setBaseShapesVisible(false);
		
		dataset1.addSeries(alpha_adaptive);
		graph = new XYGraph("ss", new NumberAxis("iteration"),
				new NumberAxis("step size"), dataset1);
		
		graph.getChart().removeLegend();
		graph.setPlotColor(new Color[] {Color.BLUE,Color.ORANGE,Color.RED,Color.GREEN});
		graph.setPlotThickness(new float[]{1.3f,1.3f,1.0f,1.0f});
		graph.getRenderer().setBaseShapesVisible(false);

	}
	
	static void avtRobustness() {
		
		AvtSimple avt1 = new AvtSimple(0, 10, 0, 0.00001f, 10.0f);
		AvtSimple avt2 = new AvtSimple(0, 10, 0, 0.00001f, 10.0f);
		AvtSimple avt3 = new AvtSimple(0, 10, 0, 0.00001f, 10.0f);
		AvtSimple avt4 = new AvtSimple(0, 10, 0, 0.00001f, 10.0f);
		
		double value = 5.0f;
		

		XYSeriesCollection dataset = new XYSeriesCollection();

		XYSeries drift_i = new XYSeries("var=0.5");
		XYSeries drift_j = new XYSeries("adaptive value tracker");
		XYSeries drift_k = new XYSeries("var=1.5");
		XYSeries drift_l = new XYSeries("var=2.0");
		
		XYSeries val = new XYSeries("noisy value");
		
		drift_l.add(0, 0);
		drift_k.add(0, 0);
		drift_j.add(0, 0);
		drift_i.add(0, 0);			

		for (int i = 1;i < 1000; i++) {
			double delay = GaussianDistribution.nextGaussian(0, 0.5);
						
			double error = avt1.getValue() - value - delay;
			System.out.println(error);
			
			if (error > 0.0) {
				avt1.adjustValue(AvtSimple.FEEDBACK_LOWER);
			} else if (error < 0.0) {
				avt1.adjustValue(AvtSimple.FEEDBACK_GREATER);
			} else {
				avt1.adjustValue(AvtSimple.FEEDBACK_GOOD);
			}	
			
			delay = GaussianDistribution.nextGaussian(0, 1);
			val.add(i, value+delay);
			
			error = avt2.getValue() - value - delay;
			
			if (error > 0.0) {
				avt2.adjustValue(AvtSimple.FEEDBACK_LOWER);
			} else if (error < 0.0) {
				avt2.adjustValue(AvtSimple.FEEDBACK_GREATER);
			} else {
				avt2.adjustValue(AvtSimple.FEEDBACK_GOOD);
			}	
			
			delay = GaussianDistribution.nextGaussian(0, 1.5);
			error = avt3.getValue() - value - delay;
			
			if (error > 0.0) {
				avt3.adjustValue(AvtSimple.FEEDBACK_LOWER);
			} else if (error < 0.0) {
				avt3.adjustValue(AvtSimple.FEEDBACK_GREATER);
			} else {
				avt3.adjustValue(AvtSimple.FEEDBACK_GOOD);
			}	
			
			delay = GaussianDistribution.nextGaussian(0, 2);
			error = avt4.getValue() - value - delay;
			
			if (error > 0.0) {
				avt4.adjustValue(AvtSimple.FEEDBACK_LOWER);
			} else if (error < 0.0) {
				avt4.adjustValue(AvtSimple.FEEDBACK_GREATER);
			} else {
				avt4.adjustValue(AvtSimple.FEEDBACK_GOOD);
			}	
						
			drift_i.add(i, avt1.getValue());
			drift_j.add(i, avt2.getValue());
			drift_k.add(i, avt3.getValue());
			drift_l.add(i, avt4.getValue());
		}

		dataset.addSeries(val);
		
//		dataset.addSeries(drift_i);
		dataset.addSeries(drift_j);
//		dataset.addSeries(drift_k);
//		dataset.addSeries(drift_l);
		
		
		
			
		XYGraph graph = new XYGraph("AVT", new NumberAxis("iteration"),
				new NumberAxis("value"), dataset);
		
		graph.setPlotColor(new Color[] {Color.BLUE,Color.RED,Color.BLUE,Color.RED,Color.GREEN});
		graph.setPlotThickness(new float[]{1.3f,1.3f,1.0f,1.0f});
		graph.getRenderer().setBaseShapesVisible(false);
	}
}
