package application.appTheoric;

public class Main {
	
	public static void main(String[] args) {
		try{
			new application.appTheoric.GradientApp(20,"gradient.txt",GradientApp.LINE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}		
}
