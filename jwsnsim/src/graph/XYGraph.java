package graph;

import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;


public class XYGraph {
	protected JFreeChart chart;
	protected XYPlot plot;
	protected XYLineAndShapeRenderer renderer;
	
	protected ValueAxis xAxis, yAxis;

	public XYGraph(String title, ValueAxis xAxis, ValueAxis yAxis,
			XYSeriesCollection dataset) {
		
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		
		XYPlot plot = createPlot(xAxis, yAxis, dataset);

		chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

		final ChartPanel panel = new ChartPanel(chart);
		chart.setBackgroundPaint(Color.WHITE);

		createLegend(plot);

		panel.setPreferredSize(new java.awt.Dimension(500, 270));
		ApplicationFrame a1 = new ApplicationFrame("");
		a1.setContentPane(panel);
		a1.pack();
		a1.setVisible(true);
	}

	protected void createLegend(XYPlot plot) {
//		chart.removeLegend();
//		LegendTitle legend = new LegendTitle(plot);
//		legend.setItemFont(new Font(Font.SERIF, Font.BOLD, 12));
//		legend.setPosition(RectangleEdge.TOP);
//		XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.6, legend,
//				RectangleAnchor.BOTTOM_RIGHT);
//
//		ta.setMaxWidth(0.2);
//		plot.addAnnotation(ta);
	}

	protected XYPlot createPlot(ValueAxis xAxis, ValueAxis yAxis,XYSeriesCollection dataset) {

		renderer = new XYLineAndShapeRenderer();

		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			renderer.setSeriesLinesVisible(i, true);
		}

		plot = new XYPlot(dataset, xAxis, yAxis, renderer);

		plot.setDomainGridlinePaint(Color.DARK_GRAY);
		plot.setRangeGridlinePaint(Color.DARK_GRAY);
		
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.WHITE);

		return plot;
	}
	
	public void setPlotColor(Color[] colors){
		for (int j = 0; j < colors.length; j++) {
			renderer.setSeriesPaint(j, colors[j]);
		}
	}
	
	public void setPlotThickness(float[] thickness){
		for (int j = 0; j < thickness.length; j++) {
			renderer.setSeriesStroke(j, new BasicStroke(thickness[j]));
		}
	}
	
	public void setDataRange(double startX, double endX, double startY, double endY){
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.getDomainAxis().setRange(startX, endX);
		plot.getRangeAxis().setRange(startY, endY);
	}
	
	public XYPlot getPlot(){
		return plot;
	}
	
	public XYLineAndShapeRenderer getRenderer(){
		return renderer;
	}
}
