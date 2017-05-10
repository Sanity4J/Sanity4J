package com.github.sanity4j.workflow;

import java.io.File;
import java.io.IOException;

import com.github.sanity4j.report.ReportWriter;
import com.github.sanity4j.util.ExtractStats;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;

/**
 * A {@link WorkUnit} that produces the report for the current run.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class ReportProducer implements WorkUnit
{
    /** The number of milli-seconds in a second. */
    private static final int MILLISECONDS_PER_SECOND = 1000;
    
    /** The QA configuration for the current run. */
    private final QAConfig config;
    
    /** The statistics/diagnostics for the current run. */
    private final ExtractStats stats;
    
    /**
     * Creates a ReportProducer.
     * 
     * @param config the configuration for the current run.
     * @param stats the statistics/diagnostics for the current run.
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
            boolean diagnosticsFirst = config.getDiagnosticsFirst();
            ReportWriter reportWriter = new ReportWriter(stats, diagnosticsFirst, reportDirFile);
            reportWriter.produceReport(config);
        }
        catch (IOException e)
        {
            throw new QAException("Failed to write combined report", e);
        }
        
        long elapsed = System.currentTimeMillis() - start;
        QaLogger.getInstance().info("Report generated in " + (elapsed / MILLISECONDS_PER_SECOND) + "s.");        
    }
    
    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Producing combined report";
    }
}
