package sim.gui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import sim.configuration.AreaConfiguration;

public class NodeFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NodeFrame(){
		setSize(AreaConfiguration.dimX+150, AreaConfiguration.dimY);
		setResizable(false);
			
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		
		getContentPane().add(new NodePanel(AreaConfiguration.dimX,AreaConfiguration.dimY));
		getContentPane().add(new InfoPanel(150,AreaConfiguration.dimY));

		
	
		setVisible(true);		
	}
}
