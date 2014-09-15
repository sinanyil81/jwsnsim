package application;

import sim.node.Position;

public class Localization {
	public static void main(String[] args) {
		
		int N = 10;
		
		float[] x_values = new float[N];
		float[] y_values = new float[N];
		float[] distances = new float[N];
		
		float [][] A = new float[N][2];
		float [][] X = new float[2][1];
		float [][] b = new float[N][1];
		
		for (int i = 0; i < N-2; i++) {
			A[i][0] = x_values[N-1]-x_values[i];
			A[i][1] = y_values[N-1]-y_values[i];
			
			b[i][0] = (distances[i]*distances[i]-distances[N-1]*distances[N-1])-
					(x_values[i]*x_values[i]-x_values[N-1]*x_values[N-1])-
					(y_values[i]*y_values[i]-y_values[N-1]*y_values[N-1]);
		}		
	}
}
