package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.util.StringUtil;
import net.sf.sanity4j.util.Tool;
import net.sf.sanity4j.workflow.QAConfig;
import net.sf.sanity4j.workflow.WorkUnit;

/**
 * AbstractToolRunner provides a properties-driven way of running external tools.
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
        return config.getToolHome(tool.getId(), toolVersion);
    }
    
    /**
     * @return the tool Maven artifact.
     */
    protected String getToolArtifact()
    {
        return config.getToolArtifact(tool.getId(), toolVersion);
    }

    /**
     * @return the additional configuration classpath for the tool.
     */
    protected String getToolConfigClasspath()
    {
        return config.getToolConfigClasspath(tool.getId(), toolVersion);
    }
    
    /**
     * @return a list of paths to all the jars needed to run the tool.
     */
    protected List<String> getToolJars()
    {
        List<String> toolJars = new ArrayList<String>();
        
        if (config.getProductsDir() != null)
        {
            FileUtil.findJars(new File(getToolHome()), toolJars);
        }
        else if (config.getRunQAMojo() != null)
        {
            config.getRunQAMojo().getTransitiveDependencies(getToolArtifact(), toolJars);
        }
        
        if (toolJars.isEmpty())
        {
            throw new QAException("Couldn't find " + tool.getName() + " jars in " + getToolHome());
        }

        String configClasspath = getToolConfigClasspath();
        
        if (configClasspath != null)
        {
            String separator = System.getProperty("path.separator");
            String[] paths = configClasspath.split(separator);
            
            for (String path : paths)
            {
                toolJars.add(path);
            }
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
     * Subclasses may override this method to add any additional parameters specific to the tool.
     * 
     * @return a map of parameters to use for replacing configuration tokens.
     */
    protected Map<String, String> getParameterMap()
    {
        Map<String, String> paramMap = config.asParameterMap();

        String outputFile = getToolResultFile();
        String toolHome = getToolHome();

        // Retrieve the configuration parameters for this tool.
        String toolConfigParam = config.getToolConfigParam(tool.getId(), toolVersion);
        String toolAuxClasspath = config.getToolConfigClasspath(tool.getId(), toolVersion);

        // Retrieve the configuration parameter values for this tool.
        String toolConfig = null;

        if (toolConfigParam != null)
        {
            toolConfig = paramMap.get(toolConfigParam);
        }

        if (FileUtil.hasValue(toolConfig))
        {
            InputStream stream = this.getClass().getResourceAsStream("/" + toolConfig);

            if (stream != null)
            {
                FileOutputStream fos = null;

                try
                {
                    File tempFile = File.createTempFile(tool.getId() + "-", "", config.getTempDir());
                    fos = new FileOutputStream(tempFile);
                    QaUtil.copy(stream, fos);
                    toolConfig = tempFile.getCanonicalPath();
                }
                catch (IOException ioe)
                {
                    String message = "Error creating temporary configuration file for tool [" + tool.getId() + "]";
                    throw new QAException(message, ioe);
                }
                finally
                {
                    QaUtil.safeClose(stream);
                    QaUtil.safeClose(fos);
                }
            }
        }
        
        // Add the class path for this tool to the javaArgs.
        StringBuffer javaArgsBuf = new StringBuffer();
        javaArgsBuf.append(paramMap.get("javaArgs"));
        List<String> toolJars = getToolJars();
        String classPath = StringUtil.concatList(toolJars, File.pathSeparator);
        javaArgsBuf.append(" -cp ").append(classPath);

        // Put the "extra" parameters to the parameter map.
        paramMap.put("outputFile", outputFile);
        paramMap.put("toolHome", toolHome);

        toolAuxClasspath = QaUtil.replaceTokens(toolAuxClasspath, paramMap, config, null);
        toolConfig = QaUtil.replaceTokens(toolConfig, paramMap, config, toolAuxClasspath);
        paramMap.put(toolConfigParam, toolConfig);

        paramMap.put("javaArgs", javaArgsBuf.toString());
        paramMap.put("auxClasspath", toolAuxClasspath);

        return paramMap;
    }

    /**
     * Subclasses must implement this method to actually run the tool.
     * 
     * @param commandLine the tool command line.
     */
    protected abstract void runTool(String commandLine);

    /**
     * The entry point for the work unit. This implementation looks up the command line necessary to run the tool, then
     * calls {@link #runTool(String)}.
     */
    public void run()
    {
        Map<String, String> params = getParameterMap();
        String auxClasspath = params.get("auxClasspath");
        String toolCommandLine = config.getToolCommandLine(tool.getId(), toolVersion);
        toolCommandLine = QaUtil.replaceTokens(toolCommandLine, getParameterMap(), config, auxClasspath);
        runTool(toolCommandLine);
    }

    /**
     * Sets the QA configuration for the current run.
     * 
     * @param config the QA configuration.
     */
    public void setConfig(final QAConfig config)
    {
        this.config = config;
    }

    /**
     * Sets the tool version to use.
     * 
     * @param toolVersion the tool version to use.
     */
    public void setToolVersion(final String toolVersion)
    {
        this.toolVersion = toolVersion;
    }
}
