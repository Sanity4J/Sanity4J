package net.sf.sanity4j.report; 

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;

import net.sf.sanity4j.model.summary.PackageSummary;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;


/** 
 * ChartFactory - factory for creating charts.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public final class ChartFactory
{
    /** The width of the generated chart, in pixels. */
    private static final int IMAGE_WIDTH = 600;
    
    /** The height of the generated chart, in pixels. */
    private static final int IMAGE_HEIGHT = 400;
    
    /** The maximum number of data points before data point markers are turned off. */
    private static final int MAX_RUNS_FOR_SHAPES = 20;
    
    /** The inset. */
    private static final double INSET = 5.0;
    
    /** One hundred. */
    private static final double HUNDRED = 100.0;
    
    /** ChartFactory should never be instantiated. */
    private ChartFactory()
    {
    }
    
    /**
     * Creates the chart data set.
     * 
     * @param summaries the PackageSummaries for the current and historical runs 
     * @return the chart Dataset
     */
    private static XYDataset createDataset(final PackageSummary[] summaries)
    {
        TimeSeries lineCoverage = new TimeSeries("Line coverage", Second.class);
        TimeSeries branchCoverage = new TimeSeries("Branch coverage", Second.class);
        TimeSeries quality = new TimeSeries("Quality", Second.class);

        for (int i = 0; i < summaries.length; i++)
        {
            Second second = new Second(summaries[i].getRunDate());
            
            lineCoverage.addOrUpdate(second, HUNDRED * summaries[i].getLineCoverage());
            branchCoverage.addOrUpdate(second, HUNDRED * summaries[i].getBranchCoverage());
            
            double qualityValue = ReportUtil.evaluateMetric("quality", summaries[i]);
            quality.addOrUpdate(second, HUNDRED * qualityValue);
        }
        
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(lineCoverage);
        dataset.addSeries(branchCoverage);
        dataset.addSeries(quality);
        
        return dataset;
    }

    /**
     * Creates a chart for the given package and run summaries.
     * 
     * @param summaries the PackageSummaries for the current and historical runs 
     * @param packageName the package name
     * 
     * @return A chart.
     */
    private static JFreeChart createChart(final PackageSummary[] summaries, 
                                          final String packageName)
    {
        XYDataset dataset = createDataset(summaries);
        String title = packageName.length() == 0 ? "Summary for all packages"
                                                 : "Summary for " + packageName + '*';
        
        JFreeChart chart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
          title,	  // title
          "Date",     // x-axis label
          "%",   	  // y-axis label
          dataset,    // data
          true,       // create legend?
          false,      // generate tooltips?
          false       // generate URLs?
        );

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(INSET, INSET, INSET, INSET));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        XYItemRenderer renderer = plot.getRenderer();
        
        if (renderer instanceof XYLineAndShapeRenderer)
        {
            XYLineAndShapeRenderer shapeRenderer = (XYLineAndShapeRenderer) renderer;
            
            // If there are too many runs, don't draw shapes on the lines as it clutters up the graph
            boolean showShapes = dataset.getSeriesCount() > 0 && dataset.getItemCount(0) <= MAX_RUNS_FOR_SHAPES; 
            shapeRenderer.setBaseShapesFilled(true);
            shapeRenderer.setBaseShapesVisible(showShapes);
            
            if (!showShapes)
            {
                renderer.setBaseStroke(new BasicStroke(2f, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_BEVEL));
            }
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM/yy"));
        
        plot.getRangeAxis().setAutoRange(false);
        plot.getRangeAxis().setLowerBound(0.0);
        plot.getRangeAxis().setUpperBound(HUNDRED);

        return chart;
    }
    
    /**
     * Creates a chart image.
     * 
     * @param summaries the PackageSummaries for the current and historical runs 
     * @param packageName the package name
     * 
     * @return an Image showing the summary for the given package
     */
    public static BufferedImage createImage(final PackageSummary[] summaries, final String packageName)
    {
        JFreeChart chart = createChart(summaries, packageName);
        return chart.createBufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT);
    }
}
