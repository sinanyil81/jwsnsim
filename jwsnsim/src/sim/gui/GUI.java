package sim.gui;

import javax.swing.JFrame;

public class GUI {
	protected static GUI instance = null;
	protected static NodeFrame frame = null;
	
	protected GUI(){
		frame = new NodeFrame();
	}
	
	public static void start(){
		GUI.getInstance();
	}
	
	public static void stop(){
		if(frame!=null){
			frame.setVisible(false); //you can't see me!
			frame.dispose(); 
		}
		instance = null;
	}
	
	public static GUI getInstance(){
		if(instance == null)
			instance = new GUI();
		
		return instance;		
	}
}
