package sim.gui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.simulator.Simulator;

public class InfoPanel extends JPanel implements TimerHandler{
	
	JLabel simulationSecond = new JLabel("0");
	JButton stopButton = new JButton("Exit");
	protected Clock clock = new ConstantDriftClock(1.0);
	protected Timer timer = new Timer(clock,this);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InfoPanel(int w, int h) {
		this.setSize(w, h);
		
		stopButton.addActionListener(new ActionListener() {
		       public void actionPerformed(ActionEvent ae){
		           Simulator.getInstance().stopSimulation();
		           System.exit(0);
		       } 
	    });
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(simulationSecond);
		add(stopButton);
		
		clock.start();
		timer.startOneshot(1000000);	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	/**
	 * Draws the graph to a given graphics object.
	 * 
	 * @param g
	 *            The graphics to paint to
	 */
	private void draw(Graphics g) {
		simulationSecond.setText("Simulation second:"+Simulator.getInstance().getSecond());		
	}

	@Override
	public void fireEvent(Timer timer) {
		this.repaint();
		timer.startOneshot(1000000);	
	}
}
