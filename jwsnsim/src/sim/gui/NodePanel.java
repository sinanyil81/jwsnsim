package sim.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class NodePanel extends JPanel {
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

		/**
		 * Constructor for the GraphPanel class.
		 * @param p The parent Frame (GUI) where the Graph Panel is added.
		 */
		public NodePanel(){
			
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			draw(g);
		}
		
	
		/**
		 * Draws the graph to a given graphics object.
		 * @param g The graphics to paint to
		 */
		private void draw(Graphics g) {
			
				g.setColor(Color.BLACK);
				
				// Draw the graph
//					// First draw all edges, only then the nodes
//					Enumeration<Node> nodeEnumer;
//					if(Configuration.drawEdges) {
//						nodeEnumer = Runtime.nodes.getSortedNodeEnumeration(true);
//						while(nodeEnumer.hasMoreElements()){
//							Node node = nodeEnumer.nextElement();
//							// first draw all outgoing edges of this node
//							Iterator<Edge> edgeIter = node.outgoingConnections.iterator();
//							while(edgeIter.hasNext()){
//								Edge e = edgeIter.next();
//								e.draw(g, pt);
//							}
//						}
//					}
//					// Draw the nodes in a separate loop
//					if(Configuration.drawNodes) {
//						// Draw the nodes in a separate loop
//						nodeEnumer = Runtime.nodes.getSortedNodeEnumeration(true);
//						while(nodeEnumer.hasMoreElements()){
//							Node node = nodeEnumer.nextElement();
//							node.draw(g, pt, false);
//						}
//					}
		}
		
		/**	
		/**
		 * Draws a dotted line on the graphics
		 * @param g The graphics to paint on
		 * @param fromX 
		 * @param fromY
		 * @param toX
		 * @param toY
		 */
		public static void drawDottedLine(Graphics g, int fromX, int fromY, int toX, int toY) {
			int dx = toX - fromX;
			int dy = toY - fromY;
			if(dx == 0 && dy == 0) {
				return;
			}
			boolean swapped = false;
			if(Math.abs(dx) < Math.abs(dy)) {
				int temp = fromX; fromX = fromY; fromY = temp;
				temp = toX; toX = toY; toY = temp;
				temp = dy; dy = dx; dx = temp;
				swapped = true;
			}
			if(dx < 0) { // swap 'from' and 'to' 
				int temp = fromX; fromX = toX; toX = temp;
				temp = fromY; fromY = toY; toY = temp;
				dx = -dx; dy = -dy;
			}
			double delta = ((double) dy) / dx;
			boolean paint = true;
			for(int i=0; i<= dx; i++) {
				int y = fromY + (int) (i * delta);
				if(paint) {
					if(swapped) {
						g.fillRect(y, i+fromX, 1, 1); // only a single dot
					} else {
						g.fillRect(i+fromX, y, 1, 1); // only a single dot
					}
				}
				paint = !paint;
			}
		}
		
		/**
		 * Draws a bold line between two points. This method produces
		 * an approximation by drawing several lines. It is possible, that
		 * the line is not tightly filled. 
		 * @param g The graphics to paint the line on
		 * @param fromX 
		 * @param fromY
		 * @param toX
		 * @param toY
		 * @param strokeWidth The width (in pixels) to draw the line
		 */
		public static void drawBoldLine(Graphics g, int fromX, int fromY, int toX, int toY, int strokeWidth) {
			for(int i=1; i<strokeWidth; i++) {
				g.drawLine(fromX+i, fromY, toX+i, toY);
				g.drawLine(fromX-i, fromY, toX-i, toY);
				g.drawLine(fromX, fromY+i, toX, toY+i);
				g.drawLine(fromX, fromY-i, toX, toY-i);
			}
		}
		
}
