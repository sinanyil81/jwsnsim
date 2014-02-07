package sim.gui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sim.simulator.Simulator;

public class InfoPanel extends JPanel{
	
	JLabel simulationSecond = new JLabel("0");
	JButton stopButton = new JButton("Exit");
	
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
		simulationSecond.setText(""+Simulator.getInstance().getSecond());		
	}
}
