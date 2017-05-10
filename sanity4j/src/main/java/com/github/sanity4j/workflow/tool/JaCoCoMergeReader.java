package com.github.sanity4j.workflow.tool; 

import java.io.File;
import java.util.Properties;

import com.github.sanity4j.util.ExtractStats;

/**
 * JaCoCoMergeReader - The result of the JaCoco Merge doesn't require reading,
 * therefore, this class does nothing.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.8.0
 */
public class JaCoCoMergeReader implements ResultReader
{
	/** The properties used to configure this {@link ResultReader}. */
	private final Properties properties = new Properties();
	
	/** {@inheritDoc} */
	@Override
   public void setProperties(final Properties properties) 
	{
		this.properties.putAll(properties);
	}

    /**
     * Do nothing.
     * @param resultFile the result file.
     */
    @Override
   public void setResultFile(final File resultFile)
    {
        // Do nothing.
    }

    /**
     * Do nothing.
     * @param stats the extract stats.
     */
    @Override
   public void setStats(final ExtractStats stats)
    {
        // Do nothing.
    }

    /**
     * @return null
     */
    @Override
   public String getDescription()
    {
        return null;
    }

    /**
     * Do nothing.
     */
    @Override
   public void run()
    {
        // Do nothing.
    }
}
