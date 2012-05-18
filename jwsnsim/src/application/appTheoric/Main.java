package application.appTheoric;

public class Main {
	
	public static void main(String[] args) {
		try{
			new application.appTheoric.GradientApp(20,"gradient_line.txt",GradientApp.LINE);
			//new application.appTheoric.GradientApp(20,"gradient_ring.txt",GradientApp.RING);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}		
}
