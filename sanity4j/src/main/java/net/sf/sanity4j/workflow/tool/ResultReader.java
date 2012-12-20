package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.util.Properties;

import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.workflow.WorkUnit;

/**
 * ResultReader provides a common interface which tool result readers must implement.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public interface ResultReader extends WorkUnit
{
    /**
     * Set the properties used to configure the {@link ResultReader}.
     * 
     * @param properties The properties used to configure the {@link ResulReader}.
     */
    void setProperties(Properties properties);

    /**
     * Sets the stats to store the results in.
     * 
     * @param stats the stats to set
     */
    void setStats(ExtractStats stats);

    /**
     * Sets the file to read the results from.
     * 
     * @param resultFile the resultFile to set
     */
    void setResultFile(File resultFile);
}
