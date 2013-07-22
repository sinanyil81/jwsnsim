package sim.jprowler.applications;

import sim.jprowler.Position;

public class Topology {
	
	private static int counter = 1;

	public static void reset(){
		counter = 1;
	}
	
	public static Position getNextLinePosition(){
		Position position =  new Position(counter*5,counter*5,0);
		counter++;
		return position;
	}
	
	public static Position getNextRingPosition(int numNodes){
		double oneStep = 360.0 / numNodes;
		double radius = 10/Math.toRadians(oneStep); 

		
		Position position = new Position(radius * Math.cos(Math.toRadians(counter * oneStep)),
								radius * Math.sin(Math.toRadians(counter * oneStep)),0);
		counter++;
		return position;
	}
	
	public static Position getNextDensePosition(int density){
		double stepsize = 2.0*10.0/(double)density;
		
		Position position = new Position(counter*stepsize,0,0);
		counter++;
		return position;
	}
}
