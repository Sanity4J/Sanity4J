package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.util.StringUtil;
import net.sf.sanity4j.util.Tool;
import net.sf.sanity4j.workflow.QAConfig;
import net.sf.sanity4j.workflow.WorkUnit;

/**
 * AbstractToolRunner provides a properties-driven way of running
 * external tools.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public abstract class AbstractToolRunner implements WorkUnit
{
    /** The current QA configuration. */
    private QAConfig config;

    /** The tool to run. */
    private final Tool tool;

    /** The tool version that was found. */
    private String toolVersion;

    /**
     * Creates an AbstractToolRunner.
     *
     * @param tool the tool to run.
     */
    protected AbstractToolRunner(final Tool tool)
    {
        this.tool = tool;
    }

    /**
     * @return the path to the tool home directory.
     */
    protected String getToolHome()
    {
        Properties props = QaUtil.getProperties("/net/sf/sanity4j/workflow/tool/tools.properties");
        String homeKey = "sanity4j.tool." + tool.getId() + '.' + toolVersion + ".home";

        String toolHome = props.getProperty(homeKey);
        toolHome = QaUtil.replaceTokens(toolHome, config.asParameterMap());
        return toolHome;
    }

    /**
     * The entry point for the work unit. This implemention looks up the command line necessary
     * to run the tool, then calls {@link #runTool(String)}.
     */
    public void run()
    {
        Properties props = QaUtil.getProperties("/net/sf/sanity4j/workflow/tool/tools.properties");
        String commandKey = "sanity4j.tool." + tool.getId() + '.' + toolVersion + ".command";

        if (!props.containsKey(commandKey))
        {
            String message = "Missing tool command for " + tool.getName() + ' ' + toolVersion
                    + ". Please set parameter: " + commandKey
                    + " in external sanity4j.properties";

            throw new QAException(message);
        }

        String toolCommandLine = props.getProperty(commandKey);
        toolCommandLine = QaUtil.replaceTokens(toolCommandLine, getParameterMap());
        runTool(toolCommandLine);
    }

    /**
     * Subclasses must implement this method to actually run the tool.
     * @param commandLine the tool command line.
     */
    protected abstract void runTool(String commandLine);

    /**
     * Subclasses may override this method to add any additional parameters
     * specific to the tool.
     *
     * @return a map of parameters to use for replacing configuration tokens.
     */
    protected Map<String, String> getParameterMap()
    {
        Map<String, String> paramMap = config.asParameterMap();
        paramMap.put("outputFile", getToolResultFile());
        paramMap.put("toolHome", getToolHome());

        List<String> toolJars = getToolJars();
        String classPath = StringUtil.concatList(toolJars, File.pathSeparator);
        paramMap.put("javaArgs", paramMap.get("javaArgs") + " -cp " + classPath);

        return paramMap;
    }

    /**
     * @return a list of paths to all the jars needed to run the tool.
     */
    protected List<String> getToolJars()
    {
        List<String> toolJars = new ArrayList<String>();
        FileUtil.findJars(new File(getToolHome()), toolJars);

        if (toolJars.isEmpty())
        {
            throw new QAException("Couldn't find " + tool.getName() + " jars in " + getToolHome());
        }

        return toolJars;
    }

    /**
     * @return the file path where the tool should place it's output.
     */
    protected String getToolResultFile()
    {
        return config.getTempDir().getPath() + File.separatorChar + tool + "_result.xml";
    }

    /** @return the QA configuration for the current run. */
    protected QAConfig getConfig()
    {
        return config;
    }

    /**
     * Sets the QA configuration for the current run.
     * @param config the QA configuration.
     */
    public void setConfig(final QAConfig config)
    {
        this.config = config;
    }

    /**
     * Sets the tool version to use.
     * @param toolVersion the tool version to use.
     */
    public void setToolVersion(final String toolVersion)
    {
        this.toolVersion = toolVersion;
    }
}
