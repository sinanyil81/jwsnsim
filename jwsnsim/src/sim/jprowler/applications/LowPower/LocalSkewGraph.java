package sim.jprowler.applications.LowPower;

import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

public class LocalSkewGraph{
	
	public LocalSkewGraph(int id,XYSeries sync) {
	    
	    final JFreeChart chart = createChart("Node:"+id,sync);
	    final ChartPanel panel = new ChartPanel(chart);
	    

	    panel.setPreferredSize(new java.awt.Dimension(500, 270));	    
	    ApplicationFrame a1 = new ApplicationFrame("");	    
	    a1.setContentPane(panel);
	    
	    a1.pack();
	    
	    a1.setVisible(true);
	}
	
	private JFreeChart createChart(String title,XYSeries sync) {
	    			
	    final ValueAxis time = new NumberAxis("Time");
	    final ValueAxis dutyCycle = new NumberAxis("Synchronization Error");			
	    
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(sync);
        
        final XYPlot plot = new XYPlot(dataset, time, dutyCycle, new StandardXYItemRenderer());

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setOrientation(PlotOrientation.VERTICAL);
              
        return new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}		
}
