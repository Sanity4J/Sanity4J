package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.util.Properties;
import java.util.StringTokenizer;

import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.util.StringUtil;
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
    /** The property key for the tools to be run. */
    private static final String TOOLS_TO_RUN_PROPERTY = "sanity4j.toolsToRun";

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

        Properties props = QaUtil.getProperties("/net/sf/sanity4j/workflow/tool/tools.properties");

        if (!props.containsKey(TOOLS_TO_RUN_PROPERTY))
        {
            String message = "Missing tools to run, please set property " + TOOLS_TO_RUN_PROPERTY
                           + "in external sanity4j.properties";

            throw new QAException(message);
        }

        String[] toolsToRun = props.getProperty(TOOLS_TO_RUN_PROPERTY).split(",");

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

            String version = getToolVersion(tool);
            String propertyPrefix = "sanity4j.tool." + tool.getId() + '.' + version + '.';
            String runnerClassName = props.getProperty(propertyPrefix + "runner");
            String readerClassName = props.getProperty(propertyPrefix + "reader");

            if (StringUtil.empty(runnerClassName))
            {
                String message = "Missing runner for " + tool.getName() + ' ' + version + ", "
                               + "please set property '" + propertyPrefix + "runner' "
                               + "in external sanity4j.properties";

                throw new QAException(message);
            }

            if (StringUtil.empty(readerClassName))
            {
                String message = "Missing reader for " + tool.getName() + ' ' + version + ", "
                               + "please set property '" + propertyPrefix + "reader' "
                               + "in external sanity4j.properties";

                throw new QAException(message);
            }

            AbstractToolRunner runner = createRunner(runnerClassName);
            runner.setToolVersion(version);
            add(runner);

            if (config.isIncludeToolOutput())
            {
                File resultFile = new File(runner.getToolResultFile());
                add(new FileCopier(resultFile, new File(config.getReportDir(), resultFile.getName())));
            }

            ResultReader reader = createReader(readerClassName, runner.getToolResultFile());
            add(reader);
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
            reader.setStats(stats);
        }
        catch (Exception e)
        {
            throw new QAException("Error instantiating reader " + className, e);
        }

        reader.setResultFile(new File(toolResultFile));
        return reader;
    }

    /**
     * Finds the latest version of the tool which is available to be run.
     * ie. the directory for it can be found on the file system. A {@link QAException}
     * will be thrown if the tool properties are incorrect or the tool can not be found.
     * 
     * @param tool the Tool to find.
     * @return the latest version number for the given Tool.
     */
    private String getToolVersion(final Tool tool)
    {
       Properties props = QaUtil.getProperties("/net/sf/sanity4j/workflow/tool/tools.properties");
       String availableVersions = props.getProperty("sanity4j.tool." + tool + ".versions");

       if (availableVersions == null)
       {
          throw new QAException("Missing tool version information for " + tool);
       }

       String firstVersion = null;

       for (StringTokenizer tok = new StringTokenizer(availableVersions, ", "); tok.hasMoreTokens();)
       {
          String version = tok.nextToken();

          if (firstVersion == null)
          {
             firstVersion = version;
          }

          String homeKey = "sanity4j.tool." + tool.getId() + '.' + version + ".home";

          if (!props.containsKey(homeKey))
          {
              String message = "Missing tool home for " + tool.getName() + ' ' + version
                             + ". Please set parameter: " + homeKey + " in external sanity4j.properties";

              throw new QAException(message);
          }

          String versionHome = props.getProperty(homeKey);
          versionHome = QaUtil.replaceTokens(versionHome, config.asParameterMap());

          if (new File(versionHome).exists())
          {
             if (!version.equals(firstVersion))
             {
                 String msg = "WARNING: Running an out-dated version of "
                   + tool.getName() + ". The current version is " + firstVersion;

                 QaLogger.getInstance().warn(msg);
             }

             return version;
          }
       }

       throw new QAException("Unable to find tool home for " + tool.getName());
    }
}
