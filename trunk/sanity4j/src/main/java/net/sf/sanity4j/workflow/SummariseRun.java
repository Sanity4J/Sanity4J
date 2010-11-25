package net.sf.sanity4j.workflow; 

import java.io.File;
import java.io.IOException;

import net.sf.sanity4j.model.summary.SummaryCsvMarshaller;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;

/**
 * Summarises the current run, and reads in the old summary data (if available).
 * Should only be run once all results have been read in, line counts calculated etc. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class SummariseRun implements WorkUnit
{
    /** The ExtractStats which holds the summary data. */
    private final ExtractStats stats;
    
    /** The current QA run's configuration. */
    private final QAConfig config;
    
    /**
     * Creates a SummariseRun.
     * 
     * @param stats the stats which holds the summary data.
     * @param config the current QA run's configuration.
     */
    public SummariseRun(final ExtractStats stats, final QAConfig config)
    {
        this.stats = stats;
        this.config = config;
    }
    
    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Summarising run";
    }

    /**
     * Reads in the existing summary data file (if available),
     * and records the current run summary.
     */
    public void run()
    {
        String summaryDataFileName = config.getSummaryDataFile();
        
        // Read the existing summary
        File summaryDataFile = new File(summaryDataFileName);
        
        if (summaryDataFile.exists())
        {
            QaLogger.getInstance().debug("Reading previous run summaries");
            
            try
            {
                stats.extractHistoricalSummary(summaryDataFile);
            }
            catch (IOException e)
            {
                throw new QAException("Unable to read previous run summary", e);
            }
        }
        
        // Save the current run summary
        if (!summaryDataFile.exists() || summaryDataFile.canWrite())
        {
            String msg = "Saving run summary data to " + summaryDataFile;
            QaLogger.getInstance().info(msg);

            SummaryCsvMarshaller marshaller = new SummaryCsvMarshaller();
            marshaller.write(stats.getRunSummary(), summaryDataFile);
        }
        else
        {
            QaLogger.getInstance().warn("Unalbe to update run summary file: " + summaryDataFileName);
        }
    }
}
