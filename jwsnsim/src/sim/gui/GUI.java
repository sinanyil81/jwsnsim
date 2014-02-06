package sim.gui;

public class GUI {
	protected MainFrame frame = null;
	protected static GUI instance = null;
	
	protected GUI(){
		frame = new MainFrame();
	}
	
	public static void start(){
		GUI.getInstance();
	}
	
	public static GUI getInstance(){
		if(instance == null)
			instance = new GUI();
		
		return instance;		
	}
	
	public static void refresh(){
		getInstance().frame.repaint();
	}
}
