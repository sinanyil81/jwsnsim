package sim.gui;

import javax.swing.JFrame;

import sim.configuration.AreaConfiguration;

public class MainFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MainFrame(){
		NodePanel p = new NodePanel();
		add(p);
		setSize(AreaConfiguration.dimX,AreaConfiguration.dimY);
		setVisible(true);
	}
	

}
