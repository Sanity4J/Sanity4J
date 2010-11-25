package net.sf.sanity4j.report; 

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;

import net.sf.sanity4j.model.summary.PackageSummary;
import net.sf.sanity4j.model.summary.SummaryCsvMarshaller;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.StringUtil;

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
 * CrossProjectChartCreator - a command-line utility for 
 * creating cross-project comparison charts.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class CrossProjectChartCreator
{
    /** The width of the generated chart, in pixels. */
    private static final int IMAGE_WIDTH = 600;
    /** The height of the generated chart, in pixels. */
    private static final int IMAGE_HEIGHT = 400;
    /** The maximum number of data points before data point markers are turned off. */
    private static final int MAX_RUNS_FOR_SHAPES = 20;
    
    /** ChartFactory should never be instantiated. */
    private CrossProjectChartCreator()
    {
    }
    
    /**
     * Main entry point to the CrossProjectChartCreator application.
     * Expects each command-line argument to be the path to a QA summary CSV file for a project.
     * 
     * @param args the program command-line arguments.
     */
    public static void main(final String[] args)
    {
        PackageSummary[][] summaries = new PackageSummary[args.length][];

        for (int i = 0; i < summaries.length; i++)
        {
            summaries[i] = new SummaryCsvMarshaller().read(new File(args[i]));
        }

        XYDataset[] dataSets = createDatasets(args, summaries);

        BufferedImage lineCoverageChart = createImage(dataSets[0], "jUnit Line coverage");
        BufferedImage branchCoverageChart = createImage(dataSets[1], "jUnit Branch coverage");
        BufferedImage qualityChart = createImage(dataSets[2], "Code quality");

        try
        {
            ImageIO.write(lineCoverageChart, "PNG", new File("allProjectLineCoverage.png"));
            ImageIO.write(branchCoverageChart, "PNG", new File("allProjectBranchCoverage.png"));
            ImageIO.write(qualityChart, "PNG", new File("allProjectQuality.png"));
        }
        catch (IOException e)
        {
            QaLogger.getInstance().error("Failed to write cross project charts", e);
        }
    }
    
    /**
     * Creates the chart data sets.
     * 
     * @param fileNames the chart file name for each project.
     * @param summaries the PackageSummaries for the current and historical runs for several projects. 
     * @return the chart Datasets [line coverage, branch coverage, quality]
     */
    private static XYDataset[] createDatasets(final String[] fileNames, final PackageSummary[][] summaries)
    {
        TimeSeriesCollection lineCoverage = new TimeSeriesCollection();
        TimeSeriesCollection branchCoverage = new TimeSeriesCollection();
        TimeSeriesCollection quality = new TimeSeriesCollection();

        XYDataset[] dataSets = new XYDataset[] { lineCoverage, branchCoverage, quality };

        for (int i = 0; i < summaries.length; i++)
        {
            String projectName = new File(fileNames[i]).getName().replaceAll("\\.csv", "");

            TimeSeries lineCoverageForProject = new TimeSeries(projectName, Second.class);
            TimeSeries branchCoverageForProject = new TimeSeries(projectName, Second.class);
            TimeSeries qualityForProject = new TimeSeries(projectName, Second.class);

            for (int j = 0; j < summaries[i].length; j++)
            {
                // Only want top-level package summary.
                if (StringUtil.empty(summaries[i][j].getPackageName()))
                {
                    Second second = new Second(summaries[i][j].getRunDate());

                    lineCoverageForProject.add(second, 100.0 * summaries[i][j].getLineCoverage());
                    branchCoverageForProject.add(second, 100.0 * summaries[i][j].getBranchCoverage());
                    
                    double qualityValue = ReportUtil.evaluateMetric("quality", summaries[i][j]);
                    qualityForProject.add(second, 100.0 * qualityValue);
                }
            }

            lineCoverage.addSeries(lineCoverageForProject);
            branchCoverage.addSeries(branchCoverageForProject);
            quality.addSeries(qualityForProject);
        }

        return dataSets;
    }

    /**
     * Creates a chart for the given package and run summaries.
     * 
     * @param dataset the data set to plot on the char 
     * @param title the chart title.
     * 
     * @return A chart.
     */
    private static JFreeChart createChart(final XYDataset dataset, 
                                            final String title)
    {
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
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
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
        plot.getRangeAxis().setUpperBound(100.0);

        return chart;
    }
    
    /**
     * Creates a chart image.
     * 
     * @param dataset the data set to plot on the char 
     * @param title the chart title.
     * @return an Image containing the generated chart.
     */
    public static BufferedImage createImage(final XYDataset dataset, 
                                            final String title)
    {
        JFreeChart chart = createChart(dataset, title);
        return chart.createBufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT);
    }
}
