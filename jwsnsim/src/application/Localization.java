package application;

import org.la4j.LinearAlgebra;
import org.la4j.linear.LinearSystemSolver;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import application.tools.AvtSimple;
import sim.node.Position;

public class Localization {
	public static void main(String[] args) {			
		
		Position [] anchorCoordinates = new Position[]{
				new Position(0.0,0.0,0.0),
				new Position(0.0,50.0,0.0),
				new Position(50.0,50.0,0.0),
				new Position(50.0,0.0,0.0)
		};
		
		double[] anchorDistances = new double[]{
			25.0 * Math.sqrt(2),
			25.0 * Math.sqrt(2),
			25.0 * Math.sqrt(2),
			25.0 * Math.sqrt(2),
		};
		
		int N = anchorCoordinates.length;
				
		double [][] A = new double[N][2];
		double [] b = new double[N];
		
		AvtSimple da1 = new AvtSimple(0,50,25,0.1f, 50.0f);
		AvtSimple da2 = new AvtSimple(0,50,25,0.1f, 50.0f);
		AvtSimple da3 = new AvtSimple(0,50,25,0.1f, 50.0f);
		
		for (int j = 0; j < 50; j++) {
			
			double dn = anchorDistances[N-1] + Math.random()*20.0;
			System.out.println(dn);
			
			for (int i = 0; i < N-1; i++) {
				A[i][0] = anchorCoordinates[N-1].xCoord-anchorCoordinates[i].xCoord; 
				A[i][1] = anchorCoordinates[N-1].yCoord-anchorCoordinates[i].yCoord;
				
				double d1 = anchorDistances[i]  + Math.random()*20.0;
				System.out.println(d1);
				
						
				b[i] = (d1*d1-dn*dn)-
						(anchorCoordinates[i].xCoord*anchorCoordinates[i].xCoord-anchorCoordinates[N-1].xCoord*anchorCoordinates[N-1].xCoord)-
						(anchorCoordinates[i].yCoord*anchorCoordinates[i].yCoord-anchorCoordinates[N-1].yCoord*anchorCoordinates[N-1].yCoord);
			}
			
			Matrix A_ = new Basic2DMatrix(A);
			//Matrix A_T = A_.transpose();
			
			//Matrix M = (A_.multiply(A_T)).multiply(A_);
			
			Vector b_ = new BasicVector(b);
			
			LinearSystemSolver solver = A_.withSolver(LinearAlgebra.LEAST_SQUARES);
					
			Vector x = solver.solve(b_, LinearAlgebra.DENSE_FACTORY);		
			System.out.println(x.toString());
			//Matrix A_ = new Basic2DMatrix(A);		
			
		}
	}
}
