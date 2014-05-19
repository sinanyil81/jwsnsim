package graph;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.ApplicationFrame;

public class BarGraph {
	protected final JFreeChart chart; 
	public BarGraph(String title, String xTitle, String yTitle, CategoryDataset dataset){
			    
		chart = ChartFactory.createBarChart(title,xTitle, yTitle, dataset, PlotOrientation.VERTICAL, false,true, false);
	    
        CategoryPlot plot = chart.getCategoryPlot();


        plot.setDomainGridlinesVisible(true);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.1);
        renderer.setShadowVisible(false);	   
        
        final ChartPanel panel = new ChartPanel(chart);
	    plot.setBackgroundPaint(Color.WHITE);
	    plot.setDomainGridlinePaint(Color.DARK_GRAY);
		plot.setRangeGridlinePaint(Color.DARK_GRAY);
	    
	    panel.setPreferredSize(new java.awt.Dimension(500, 270));  
	    ApplicationFrame a1 = new ApplicationFrame("");    
	    a1.setContentPane(panel);	    
	    a1.pack();	    
	    a1.setVisible(true);
	}		
}
