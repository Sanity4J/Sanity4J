package net.sf.sanity4j.workflow;

import java.io.File;
import java.io.IOException;

import net.sf.sanity4j.report.ReportWriter;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;

/**
 * A {@link WorkUnit} that produces the report for the current run.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class ReportProducer implements WorkUnit
{
    /** The QA configuration for the current run. */
    private final QAConfig config;
    
    /** The stats the stats/diagnostics for the current run. */
    private final ExtractStats stats;
    
    /**
     * Creates a ReportProducer.
     * 
     * @param config the configuration for the current run.
     * @param stats the stats/diagnostics for the current run.
     */
    public ReportProducer(final QAConfig config, final ExtractStats stats)
    {
        this.config = config;
        this.stats = stats;
    }

    /**
     * Produces the report.
     */
    public void run()
    {
        long start = System.currentTimeMillis();
        FileUtil.createDir(config.getReportDir());
        
        try
        {
            File reportDirFile = new File(config.getReportDir());
            ReportWriter reportWriter = new ReportWriter(stats, reportDirFile);
            reportWriter.produceReport();
        }
        catch (IOException e)
        {
            throw new QAException("Failed to write combined report", e);
        }
        
        long elapsed = System.currentTimeMillis() - start;
        QaLogger.getInstance().info("Report generated in " + (elapsed / 1000) + "s.");        
    }
    
    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Producing combined report";
    }
}
