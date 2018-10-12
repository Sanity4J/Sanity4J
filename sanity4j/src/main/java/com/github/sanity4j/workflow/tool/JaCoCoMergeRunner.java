package com.github.sanity4j.workflow.tool;

import com.github.sanity4j.util.Tool;

/**
 * JaCoCoMergeRunner - a work unit that merges JaCoco data files.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.8.0
 */
public class JaCoCoMergeRunner extends AbstractToolRunner
{
    /**
     * Creates a JaCoCoMergeRunner.
     */
    public JaCoCoMergeRunner()
    {
        super(Tool.JACOCO_MERGE);
    }

    /**
     * Produces the Merged JaCoco data file.
     * 
     * @param commandLine the Merge JaCoCo data file command line.
     */
    @Override
    public void runTool(final String commandLine)
    {
        // TODO: Does nothing
    }

    /**
     * @return the file path where the tool should place it's output.
     */
    @Override
    protected String getToolResultFile()
    {
        return null;
    }

    /**
     * @return the description of this WorkUnit.
     */
    @Override
    public String getDescription()
    {
        return "Running JaCoCo Data File Merge";
    }
}
