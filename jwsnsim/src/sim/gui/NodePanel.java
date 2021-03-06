package sim.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import nodes.Node;
import nodes.NodeFactory;
import core.SimulationEvent;
import core.SimulationEventObserver;

public class NodePanel extends JPanel implements SimulationEventObserver {
	
	private static final long serialVersionUID = 1L;
	SimulationEvent event = new SimulationEvent(this);
	
	public static int SimulationSpeed = 0;

	public NodePanel(int w, int h) {
		this.setSize(w, h);
		this.setPreferredSize(new Dimension(w, h));
		event.register(1000000);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.WHITE);	
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		draw(g);
	}

	/**
	 * Draws the graph to a given graphics object.
	 * 
	 * @param g
	 *            The graphics to paint to
	 */
	private void draw(Graphics g) {

		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.BLACK);
		
		for (int i = 0; i < NodeFactory.numNodes; i++) {
			Node node = NodeFactory.nodes[i];
			nodes.Position pos1 = node.getPosition();

			if(node.getID() == 1)
				g2.setColor(Color.RED);
			else
				g2.setColor(Color.LIGHT_GRAY);
			
			g2.fillOval((int)pos1.xCoord-6, (int)pos1.yCoord-6, 12, 12);

			Node[] neighbors = node.getRadio().getNeighbors();
			if(neighbors!=null){
				for (int j = 0; j < neighbors.length; j++) {
					nodes.Position pos2 = neighbors[j].getPosition();
					g2.setColor(Color.BLUE);
					g2.setStroke(new BasicStroke(2));
					g2.drawLine((int)pos1.xCoord, (int)pos1.yCoord, (int)pos2.xCoord, (int)pos2.yCoord);
				}				
			}
		}
	}

	@Override
	public void signal(SimulationEvent event) {
		event.register(1000000);
		
		if(SimulationSpeed>0){
			try {
				Thread.sleep(SimulationSpeed);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		this.repaint();
	}
}
