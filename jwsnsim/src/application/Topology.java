package application;

import sim.node.Position;

public class Topology {
	
	private static int counter = 1;

	public static void reset(){
		counter = 1;
	}
	
	public static Position getNextLinePosition(){
		Position position =  new Position(counter*10,0,0);
		counter++;
		return position;
	}
	
	public static Position getNext4x4GridPosition(){
		double x = ((counter-1)%4)*14;
		double y = ((counter-1)/4)*20;
		
		Position position =  new Position(x,y,0);
		counter++;
		return position;
	}
	
	public static Position getNextRingPosition(int numNodes){
		double oneStep = 360.0 / numNodes;		
    	double radius = 15.0*360.0/(oneStep*2.0*Math.PI);

		
		Position position = new Position(radius * Math.cos(Math.toRadians(counter * oneStep)),
								radius * Math.sin(Math.toRadians(counter * oneStep)),0);
		counter++;
		return position;
	}
	
	public static Position getNextDensePosition(int density){
		double stepsize = 2.0*15.0/(double)density;
		
		Position position = new Position(counter*stepsize,counter*stepsize,0);
		counter++;
		return position;
	}
}
