package sim.gui;

public class GUI {
	protected static GUI instance = null;
	
	protected GUI(){
		new NodeFrame();
	}
	
	public static void start(){
		GUI.getInstance();
	}
	
	public static GUI getInstance(){
		if(instance == null)
			instance = new GUI();
		
		return instance;		
	}
}
