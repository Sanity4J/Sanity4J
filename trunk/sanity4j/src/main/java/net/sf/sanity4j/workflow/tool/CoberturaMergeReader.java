package net.sf.sanity4j.workflow.tool; 

import java.io.File;

import net.sf.sanity4j.util.ExtractStats;

/**
 * CoberturaMergeReader - The result of the Cobertura Merge doesn't require reading,
 * therefore, this class does nothing.
 *
 * @author Darian Bridge
 * @since Sanity4J 1.0.4
 */
public class CoberturaMergeReader implements ResultReader
{
    /**
     * Do nothing.
     * @param resultFile the result file.
     */
    public void setResultFile(final File resultFile)
    {
        // Do nothing.
    }

    /**
     * Do nothing.
     * @param stats the extract stats.
     */
    public void setStats(final ExtractStats stats)
    {
        // Do nothing.
    }

    /**
     * @return null
     */
    public String getDescription()
    {
        return null;
    }

    /**
     * Do nothing.
     */
    public void run()
    {
        // Do nothing.
    }
}
