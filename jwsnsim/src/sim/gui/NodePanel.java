package sim.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import sim.node.Node;
import sim.node.NodeFactory;

public class NodePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for the GraphPanel class.
	 * 
	 * @param p
	 *            The parent Frame (GUI) where the Graph Panel is added.
	 */
	public NodePanel() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		draw(g);
	}

	/**
	 * Draws the graph to a given graphics object.
	 * 
	 * @param g
	 *            The graphics to paint to
	 */
	private void draw(Graphics g) {

		g.setColor(Color.BLACK);

		for (int i = 0; i < NodeFactory.numNodes; i++) {
			Node node = NodeFactory.nodes[i];
			sim.node.Position pos1 = node.getPosition();

			g.setColor(Color.RED);
			g.fillOval(pos1.xCoord-3, pos1.yCoord-3, 6, 6);

			Node[] neighbors = node.getRadio().getNeighbors();
			for (int j = 0; j < neighbors.length; j++) {
				sim.node.Position pos2 = neighbors[j].getPosition();
				g.setColor(Color.BLUE);
				g.drawLine(pos1.xCoord, pos1.yCoord, pos2.xCoord, pos2.yCoord);
			}
		}
	}
}
