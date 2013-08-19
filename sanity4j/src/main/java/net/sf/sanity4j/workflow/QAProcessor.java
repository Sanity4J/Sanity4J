package net.sf.sanity4j.workflow;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.StringUtil;
import net.sf.sanity4j.workflow.tool.ToolRunnerGroup;

/**
 * QAProcessor handles the various tasks that need to be performed in the QA process.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class QAProcessor implements Runnable
{
    /** The current Sanity4J version number. This is the version number which is logged, embedded in reports, etc. */
    public static final String QA_VERSION = "1.1.0";

    /** The default Java runtime to use when running external tasks. */
    public static final String DEFAULT_JAVA_RUNTIME = "java";

    /** The maximum heap size to use when running external tasks. */
    public static final String JAVA_RUNTIME_MAX_MEMORY = "-Xmx768M";

    /** The number of milliseconds in a second. */
    private static final int MILLIS = 1000;
    
    /**
     * The QA configuration for the current run.
     */
    private final QAConfig config = new QAConfig();

    /**
     * The ExtractStats instance which will hold the results of the current run.
     */
    private ExtractStats stats;

    /** @return the QA configuration for the current run. */
    public QAConfig getConfig()
    {
        return config;
    }

    /**
     * Executes the QA process.
     */
    public void run()
    {
        try
        {
            validateConfig();
            doExecute();
        }
        finally
        {
            cleanUp();
        }
    }

    /**
     * Runs the given work.
     * @param workUnits a list of WorkUnits
     */
    protected void runWork(final List<WorkUnit> workUnits)
    {
        for (int i = 0; i < workUnits.size(); i++)
        {
            WorkUnit work = workUnits.get(i);
            QaLogger.getInstance().debug(work.getDescription());
            work.run();
        }
    }

    /**
     * <p>Validates the QA run configuration.
     * Will throw a QAException if the configuration is invalid.</p>
     *
     * <p>At least one source and class file entry must have been added
     * for this task to run. We allow no libaries to be specified,
     * as it's quite possible that the only dependencies are on
     * the java runtime.</p>
     */
    private void validateConfig()
    {
        if (config.getSourceDirs().isEmpty())
        {
            throw new QAException("No source files specified");
        }

        if (config.getClassDirs().isEmpty())
        {
            throw new QAException("No class files specified");
        }

        File systemTempDir = new File(System.getProperty("java.io.tmpdir"));

        File tempDir = new File(systemTempDir, "sanity4j-temp" + System.currentTimeMillis());

        if (!tempDir.mkdirs())
        {
            throw new QAException("Unable to create temp directory " + tempDir.getPath());
        }

        tempDir.deleteOnExit();

        config.setTempDir(tempDir);

        try
        {
            String combinedSourcePath = config.getCombinedSourceDir().getPath();
            FileUtil.createDir(combinedSourcePath);
            stats = new ExtractStats(combinedSourcePath);
        }
        catch (IOException e)
        {
            throw new QAException("Unable to determine canonical source path", e);
        }
    }

    /**
     * Cleans up after the task has executed.
     * Currently, only the temporary directory is deleted.
     */
    private void cleanUp()
    {
        File tempDir = config.getTempDir();

        if (tempDir != null && tempDir.exists())
        {
            try
            {
                FileUtil.delete(new File(tempDir.getPath()));
                tempDir = null;
            }
            catch (IOException e)
            {
                QaLogger.getInstance().warn("Failed to delete temp dir", e);
            }
        }
    }

    /**
     * Creates the work unit which runs the tools against the combined
     * source/class/library directories.
     *
     * @param stats the stats which will hold the analysis results.
     * @return a WorkUnit which will run the analysis tools.
     */
    private WorkUnit getToolsWork(final ExtractStats stats)
    {
        WorkUnitGroup tools = new WorkUnitGroup(true, "Performing analysis")
        {
            @Override
            public void run()
            {
                long start = System.currentTimeMillis();

                super.run();

                long elapsed = System.currentTimeMillis() - start;
                QaLogger.getInstance().info("Combined analysis completed in " + (elapsed / MILLIS) + "s.");
            }
        };

        tools.add(new ToolRunnerGroup(config, stats));
        return tools;
    }

    /**
     * Executes the main part of this task.
     * @throws QAException if an error occurs
     */
    private void doExecute() throws QAException
    {
        QaLogger.getInstance().info("Sanity4J version " + QA_VERSION);
        long start = System.currentTimeMillis();

        WorkUnitGroup work = new WorkUnitGroup("Performing analysis");

        // Check configuration
        work.add(new JavaVersionCheck(config));

        // Collect the various files necessary for analysis
        work.add(new SourceFileCollector(config));
        work.add(new ClassFileCollector(config));
        work.add(new LibraryFileCollector(config));

        // Run the various tools
        work.add(getToolsWork(stats));

        // Determine line counts (for quality metric).
        work.add(new WorkUnit()
        {
            public String getDescription()
            {
                return "Reading line counts";
            }

            public void run()
            {
                try
                {
                    stats.extractLineCounts();
                }
                catch (IOException e)
                {
                    throw new QAException("Unable to determine line counts", e);
                }
            }
        });

        // Summarise run (if applicable)
        if (!StringUtil.empty(config.getSummaryDataFile()))
        {
            work.add(new SummariseRun(stats, config));
        }

        // Produce the report
        work.add(new ReportProducer(config, stats));

        // RunWork is called rather than work.run so that we can indicate progress
        runWork(Arrays.asList(new WorkUnit[]{work}));

        long elapsed = System.currentTimeMillis() - start;
        QaLogger.getInstance().info("Analysis completed in " + (elapsed / MILLIS) + "s.");
        QaLogger.getInstance().info("Done!");
    }
}
