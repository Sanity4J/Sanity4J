package net.sf.sanity4j.workflow.tool;

import java.io.File;

import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.Tool;
import net.sf.sanity4j.workflow.FileCopier;
import net.sf.sanity4j.workflow.QAConfig;
import net.sf.sanity4j.workflow.WorkUnitGroup;

/**
 * ToolRunnerGroup is responsible for running the tools to be used in the analysis.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class ToolRunnerGroup extends WorkUnitGroup
{
    /** The configuration for the current run. */
    private final QAConfig config;
    
    /** The stats to store the results in. */
    private final ExtractStats stats;

    /**
     * Creates a ToolRunnerGroup.
     * 
     * @param config the configuration for the current run.
     * @param stats the stats to store the results in.
     */
    public ToolRunnerGroup(final QAConfig config, final ExtractStats stats)
    {
        super(config.getNumThreads() > 1, "Running tools");
        this.config = config;
        this.stats = stats;

        String[] toolsToRun = config.getToolsToRun();

        for (String toolId : toolsToRun)
        {
            Tool tool = Tool.get(toolId.trim());
            
            // Special case for coverage targets - only run them if a coverage data file has been provided
            // TODO: Generalise this to properties file to include cases where source and/or classes are not available
            // ie. CheckStyle, PMD & PMD-CPD require source, FindBugs requires classes & libs.
            if (Tool.COBERTURA.equals(tool) && config.getCoverageDataFile() == null)
            {
                String message = tool.getName() + " included in tools to run, but no coverage file available - skipping.";
                QaLogger.getInstance().warn(message);
                continue;
            }
            else if (Tool.COBERTURA_MERGE.equals(tool) && config.getCoverageDataFileCount() <= 1)
            {
                String message = tool.getName() + " not required.";
                QaLogger.getInstance().info(message);
                continue;
            }

            String version = config.getToolVersion(tool.getId());
            String runnerClassName = config.getToolRunner(tool.getId(), version);
            AbstractToolRunner runner = createRunner(runnerClassName);
            runner.setToolVersion(version);
            
            add(runner);

            if (runner.getToolResultFile() != null)
            {
                if (config.isIncludeToolOutput())
                {
                    File resultFile = new File(runner.getToolResultFile());
                    add(new FileCopier(resultFile, new File(config.getReportDir(), resultFile.getName())));
                }
    
                String readerClassName = config.getToolReader(tool.getId(), version);
                ResultReader reader = createReader(readerClassName, runner.getToolResultFile());
                add(reader);
            }
        }
    }

    /**
     * Instantiates and configures a runner of the given class.
     * @param className the runner class name.
     * @return an instance of the runner class.
     */
    private AbstractToolRunner createRunner(final String className)
    {
        AbstractToolRunner runner = null;

        try
        {
            Class<?> runnerClass = Class.forName(className);

            if (!AbstractToolRunner.class.isAssignableFrom(runnerClass))
            {
                throw new QAException("Invalid runner class - runners must extend AbstractToolRunner");
            }

            runner = (AbstractToolRunner) runnerClass.newInstance();
            runner.setConfig(config);
        }
        catch (Exception e)
        {
            throw new QAException("Error instantiating runner " + className, e);
        }

        return runner;
    }

    /**
     * Instantiates and configures a reader of the given class.
     * @param className the reader class name.
     * @param toolResultFile the result file to be read.
     * @return an instance of the reader class.
     */
    private ResultReader createReader(final String className, final String toolResultFile)
    {
        ResultReader reader = null;

        try
        {
            Class<?> readerClass = Class.forName(className);

            if (!ResultReader.class.isAssignableFrom(readerClass))
            {
                throw new QAException("Invalid reader class - readers must implement ResultReader");
            }

            reader = (ResultReader) readerClass.newInstance();
            reader.setProperties(config.getToolProperties());
            reader.setStats(stats);
        }
        catch (Exception e)
        {
            throw new QAException("Error instantiating reader " + className, e);
        }

        reader.setResultFile(new File(toolResultFile));
        return reader;
    }

}
