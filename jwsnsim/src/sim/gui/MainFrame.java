package sim.gui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import sim.configuration.AreaConfiguration;

public class MainFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MainFrame(){
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		getContentPane().add(new NodePanel(AreaConfiguration.dimX,AreaConfiguration.dimY));
		getContentPane().add(new InfoPanel(100,100));
		setSize(AreaConfiguration.dimX+100,AreaConfiguration.dimY);
		setVisible(true);
		
	}
}
