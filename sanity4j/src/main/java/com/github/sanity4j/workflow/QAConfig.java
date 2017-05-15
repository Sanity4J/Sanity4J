package com.github.sanity4j.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import com.github.sanity4j.maven.plugin.RunQAMojo;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaUtil;
import com.github.sanity4j.util.StringUtil;
import com.github.sanity4j.util.Tool;

/**
 * This class holds all the configuration attributes for the QA process.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class QAConfig
{
    /** The location of the "tools" configuration properties file. */
    private static final String TOOL_PROPERTIES = "/com/github/sanity4j/workflow/tool/tools.properties";

    /** The property key for the tools to be run. */
    private static final String TOOLS_TO_RUN_PROPERTY = "sanity4j.toolsToRun";

    /** Prefix used within properties for QA tool properties. */
    private static final String QA_TOOL_PREFIX = "sanity4j.tool.";

    /**
     * A list of source file paths.
     */
    private final List<String> sources = new ArrayList<String>();

    /**
     * A list of class file paths.
     */
    private final List<String> classes = new ArrayList<String>();

    /**
     * A list of library file paths.
     */
    private final List<String> libraries = new ArrayList<String>();

    /**
     * The location of the directory containing the various tools.
     */
    private String productsDir;
    
    /**
     * The RunQAMojo.
     */
    private RunQAMojo runQAMojo;

    /**
     * A flag indicating whether diagnostics should be displayed first.
     */
    private boolean diagnosticsFirst;

    /**
     * The directory in which to place report output.
     */
    private String reportDir;

    /**
     * The file containing the merged jUnit coverage data.
     */
    private String coverageMergeDataFile;

    /**
     * The list of files containing the jUnit coverage data.
     */
    private final List<String> coverageDataFiles = new ArrayList<String>();

    /**
     * The summary data file, if used.
     */
    private String summaryDataFile;

    /**
     * The temporary directory.
     */
    private File tempDir = new File(System.getProperty("java.io.tmpdir"), "sanity4j-temp-" + System.currentTimeMillis());
    
    /**
     * The java runtime to use when running external tasks.
     */
    private String javaRuntime = QAProcessor.DEFAULT_JAVA_RUNTIME;
    
    /**
     * The java arguments to use when running external tasks.
     */
    private String javaArgs = QAProcessor.JAVA_RUNTIME_MAX_MEMORY;

    /**
     * A Map from a tool / version to a configuration file.
     */
    private final Map<String, String> toolConfig = new HashMap<String, String>();

    /**
     * If true, the raw tool output is included in the report directory.
     */
    private boolean includeToolOutput = false;

    /**
     * The number of threads to use to run the tools and produce the report output.
     */
    private int numThreads = 1; // TODO: Add support for concurrent tasks (Note: some tasks can not be run in parallel).

    /**
     * The configuration properties. This is a combination of the internal defaults {@link #TOOL_PROPERTIES}
     * and the {@link #externalPropertiesPath}.
     */
    private Properties properties = QaUtil.getProperties(TOOL_PROPERTIES);

    /**
     * Adds a source path to the list of source paths to analyse.
     *
     * @param sourcePath the source path to add.
     */
    public void addSourcePath(final String sourcePath)
    {
        if (!sources.contains(sourcePath) && new File(sourcePath).exists())
        {
            sources.add(sourcePath);
        }
    }

    /**
     * Adds a class path to the list of class paths to analyse.
     *
     * @param classPath the class path to add.
     */
    public void addClassPath(final String classPath)
    {
        if (!classes.contains(classPath) && new File(classPath).exists())
        {
            classes.add(classPath);
        }
    }

    /**
     * Adds a library path to the list of library paths.
     *
     * @param libraryPath the library path to add.
     */
    public void addLibraryPath(final String libraryPath)
    {
        if (!libraries.contains(libraryPath) && new File(libraryPath).exists())
        {
            libraries.add(libraryPath);
        }
    }

    /**
     * @return the list of source directories to analyse.
     */
    public List<String> getSourceDirs()
    {
        return sources;
    }

    /**
     * @return the list of class directories to analyse.
     */
    public List<String> getClassDirs()
    {
        return classes;
    }

    /**
     * @return the list of library directories that are necessary for analysis.
     */
    public List<String> getLibraryDirs()
    {
        return libraries;
    }

    /**
     * @return Returns the java runtime.
     */
    public String getJavaRuntime()
    {
        return javaRuntime;
    }
    
    /**
     * @param javaRuntime The java runtime to set.
     */
    public void setJavaRuntime(final String javaRuntime)
    {
        if (javaRuntime == null || javaRuntime.length() == 0)
        {
            this.javaRuntime = QAProcessor.DEFAULT_JAVA_RUNTIME;
        }
        else
        {
            this.javaRuntime = javaRuntime;
        }
    }
    
    /**
     * @return Returns the java arguments.
     */
    public String getJavaArgs()
    {
        return javaArgs;
    }
    
    /**
     * @param javaArgs The java args to set.
     */
    public void setJavaArgs(final String javaArgs)
    {
        if (javaArgs == null || javaArgs.length() == 0)
        {
            this.javaArgs = QAProcessor.JAVA_RUNTIME_MAX_MEMORY;
        }
        else
        {
            this.javaArgs = javaArgs;
        }
    }

    /**
     * @return Returns the productsDir.
     */
    public String getProductsDir()
    {
        return productsDir;
    }

    /**
     * @param productsDir The productsDir to set.
     */
    public void setProductsDir(final String productsDir)
    {
        this.productsDir = productsDir;
    }

    /**
     * @return Returns the RunQAMojo.
     */
    public RunQAMojo getRunQAMojo()
    {
        return runQAMojo;
    }

    /**
     * @param runQAMojo The RunQAMojo to set.
     */
    public void setRunQAMojo(final RunQAMojo runQAMojo)
    {
        this.runQAMojo = runQAMojo;
    }    
    /**
     * @return Returns the reportDir.
     */
    public String getReportDir()
    {
        return reportDir;
    }

    /**
     * @param reportDir The reportDir to set.
     */
    public void setReportDir(final String reportDir)
    {
        this.reportDir = reportDir;
    }

    /**
     * @param coverageDataFile The coverage data file to set.
     */
    public void setCoverageDataFile(final String coverageDataFile)
    {
        addCoverageDataFile(coverageDataFile);
    }

    /**
     * @param coverageDataFile The coverage data file to add to the list.
     */
    public void addCoverageDataFile(final String coverageDataFile)
    {
        if (coverageDataFile != null && coverageDataFile.length() > 0)
        {
            File file = new File(coverageDataFile);

            if (file.exists() && file.isFile())
            {
                if (coverageDataFiles.contains(coverageDataFile))
                {
                    QaLogger.getInstance().debug("Coverage data file is already in the list: " + coverageDataFile);
                }
                else
                {
                    coverageDataFiles.add(coverageDataFile);
                    QaLogger.getInstance().debug("Added coverage data file to list: " + coverageDataFile);
                }
            }
            else
            {
                QaLogger.getInstance().warn("Unable to locate coverage data file: " + coverageDataFile);
            }
        }
        else
        {
            QaLogger.getInstance().debug("Invalid coverage data file name: " + coverageDataFile);
        }
    }

    /**
     * @return Returns the coverage data file.
     */
    public String getCoverageDataFile()
    {
        if (coverageDataFiles.isEmpty())
        {
            return null;
        }
        else if (coverageDataFiles.size() > 1)
        {
            return getCoverageMergeDataFile();
        }
        else
        {
            return coverageDataFiles.get(0);
        }
    }

    /**
     * @return Returns the coverage data file.
     */
    public String getCoverageMergeDataFiles()
    {
        if (coverageDataFiles.isEmpty())
        {
            return null;
        }
        else if (coverageDataFiles.size() > 1)
        {
            StringBuilder builder = new StringBuilder();

            int index = 0;
            for (String coverageDataFile : coverageDataFiles)
            {
                builder.append(coverageDataFile);

                if (++index < coverageDataFiles.size())
                {
                    builder.append(' ');
                }
            }

            return builder.toString();
        }
        else
        {
            return coverageDataFiles.get(0);
        }
    }
    
    /**
     * @return Returns the coverage data files as a list.
     */
    public List<String> getCoverageMergeDataFileList()
    {
    	return coverageDataFiles;
    }

    /**
     * @return The number of coverage data files.
     */
    public int getCoverageDataFileCount()
    {
        return coverageDataFiles.size();
    }

    /**
     * @param summaryDataFile The summaryDataFile to set.
     */
    public void setSummaryDataFile(final String summaryDataFile)
    {
        this.summaryDataFile = summaryDataFile;
    }

    /**
     * @return Returns the summaryDataFile.
     */
    public String getSummaryDataFile()
    {
        return summaryDataFile;
    }

    /**
     * @return Returns the includeToolOutput.
     */
    public boolean isIncludeToolOutput()
    {
        return includeToolOutput;
    }

    /**
     * @param includeToolOutput The includeToolOutput to set.
     */
    public void setIncludeToolOutput(final boolean includeToolOutput)
    {
        this.includeToolOutput = includeToolOutput;
    }

    /**
     * Sets the maximum number of WorkUnits which can be run concurrently.
     *
     * @param numThreads the number of simultaneous WorkUnits to allow
     */
    public void setNumThreads(final int numThreads)
    {
        this.numThreads = numThreads;
    }

    /**
     * @return the number of simultaneous WorkUnits to allow
     */
    public int getNumThreads()
    {
        return numThreads;
    }

    /**
     * @return the temporary directory used during analysis.
     */
    public File getTempDir()
    {
        return tempDir;
    }

    /**
     * Sets the temporary directory used during analysis.
     *
     * @param tempDir the temporary directory.
     */
    public void setTempDir(final File tempDir)
    {
        if (tempDir == null)
        {
            this.tempDir = new File(System.getProperty("java.io.tmpdir"), "sanity4j-temp-" + System.currentTimeMillis());            
        }
        
        this.tempDir = tempDir;
    }

    /**
     * @return the location of the combined sources directory
     */
    public File getCombinedSourceDir()
    {
        return new File(getTempDir(), "source");
    }

    /**
     * @return the location of the combined classes directory
     */
    public File getCombinedClassDir()
    {
        return new File(getTempDir(), "classes");
    }

    /**
     * @return the location of the combined library directory
     */
    public File getCombinedLibraryDir()
    {
        return new File(getTempDir(), "lib");
    }

    /**
     * Returns a map view of this configuration to be used in e.g. performing variable substitution. Some additional
     * parameters are added to the map for convenience.
     *
     * @return a view of this configuration as a map
     */
    public Map<String, String> asParameterMap()
    {
        Map<String, String> params = new HashMap<String, String>();

        for (Map.Entry<Object, Object> entry : properties.entrySet())
        {
            params.put((String) entry.getKey(), (String) entry.getValue());
        }

        params.put("java", getJavaRuntime());
        params.put("javaArgs", getJavaArgs());
        params.put("products", getProductsDir());
        params.put("source", getCombinedSourceDir().getPath());
        params.put("classes", getCombinedClassDir().getPath());
        params.put("libs", getCombinedLibraryDir().getPath());
        params.put("coverageDataFile", getCoverageDataFile());
        params.put("coverageMergeDataFiles", getCoverageMergeDataFiles());
        params.put("tempDir", getTempDir().getPath());
        params.put("File.separatorChar", File.separator);
        params.put("File.pathSeparator", File.pathSeparator);

        for (Entry<String, String> toolConfig : this.toolConfig.entrySet())
        {
            String tool = toolConfig.getKey();
            String config = toolConfig.getValue();

            params.put(QA_TOOL_PREFIX + tool + ".config", config);
        }

        return params;
    }

    /**
     * The path location to the external properties file (sanity4j.properties).
     *
     * @param externalPropertiesPath the path location to the external properties file.
     * @param additionalProperties a String representing additional properties values (if any). 
     */
    public void setExternalPropertiesPath(final String externalPropertiesPath, final String additionalProperties)
    {
        QaUtil.setExternalPropertiesPath(externalPropertiesPath);
        properties = QaUtil.getProperties(TOOL_PROPERTIES, additionalProperties);
    }

    /**
     * The path location to the external properties file (sanity4j.properties).
     *
     * @param externalPropertiesPath the path location to the external properties file.
     */
    public void setExternalPropertiesPath(final String externalPropertiesPath)
    {
        setExternalPropertiesPath(externalPropertiesPath, null);
    }
    
    /**
     * Returns the path location to the external properties file (sanity4j.properties).
     *
     * @return the path location to the external properties file.
     */
    public String getExternalPropertiesPath()
    {
        return QaUtil.getExternalPropertiesPath();
    }

    /**
     * @return <b>true</b> if the diagnostics should be displayed first, <b>false</b> otherwise.
     */
    public boolean getDiagnosticsFirst()
    {
        return this.diagnosticsFirst;
    }

    /**
     * @param diagnosticsFirst <b>true</b> if the diagnostics should be displayed first, <b>false</b> otherwise.
     */
    public void setDiagnosticsFirst(final boolean diagnosticsFirst)
    {
        this.diagnosticsFirst = diagnosticsFirst;
    }

    /**
     * Set the file containing the merged jUnit coverage data.
     *
     * @param coverageMergeDataFile the file containing the merged jUnit coverage data.
     */
    public void setCoverageMergeDataFile(final String coverageMergeDataFile)
    {
        this.coverageMergeDataFile = coverageMergeDataFile;
    }

    /**
     * Get the file containing the merged jUnit coverage data.
     *
     * @return the file containing the merged jUnit coverage data.
     */
    public String getCoverageMergeDataFile()
    {
        return coverageMergeDataFile;
    }

    /**
     * Retrieve the properties for the QA tools.
     *
     * @return The Properties for the QA tools.
     */
    public Properties getToolProperties()
    {
        return properties;
    }

    /**
     * Set the value of a internal tool property.
     *
     * @param key the name of the internal tool property.
     * @param value the value of the internal tool property.
     */
    public void setToolProperty(final String key, final String value)
    {
        if (key != null)
        {
            if (value == null)
            {
                properties.remove(key);
            }
            else
            {
                properties.setProperty(key, value);
            }
        }
    }

    /**
     * Finds the names of the set of tool which should be run by the QA tool. A {@link QAException} will be thrown if
     * the {@link #TOOLS_TO_RUN_PROPERTY} is not set.
     *
     * @return the latest version number for the given Tool.
     */
    public String[] getToolsToRun()
    {
        if (!properties.containsKey(TOOLS_TO_RUN_PROPERTY))
        {
            String message = "Missing tools to run, please set property " + TOOLS_TO_RUN_PROPERTY
                             + "in external sanity4j.properties";

            throw new QAException(message);
        }

        String[] toolsToRun = properties.getProperty(TOOLS_TO_RUN_PROPERTY).split(",");

        return toolsToRun;
    }

    /**
     * Finds the versions of the tool which are defined within the Sanity4J properties file.
     *
     * @param tool the Tool for which to find version definitions.
     * @return An array of string representing the version definitions for the given <em>tool</em>.
     */
    public String[] getToolVersions(final String tool)
    {
        String versions = properties.getProperty(QA_TOOL_PREFIX + tool + ".versions");

        if (versions == null)
        {
            return new String[0];
        }

        List<String> versionsList = new ArrayList<String>();

        for (StringTokenizer versionTokenizer = new StringTokenizer(versions, ", "); versionTokenizer.hasMoreTokens();)
        {
            String version = versionTokenizer.nextToken();
            versionsList.add(version);
        }

        return versionsList.toArray(new String[versionsList.size()]);
    }

    /**
     * Finds the latest version of the tool which is available to be run. i.e. the directory for it can be found on the
     * file system. A {@link QAException} will be thrown if the tool properties are incorrect or the tool can not be
     * found.
     *
     * @param tool the Tool to find.
     * @return the latest version number for the given <em>tool</em>.
     */
    public String getToolVersion(final String tool)
    {
        String availableVersions = properties.getProperty(QA_TOOL_PREFIX + tool + ".versions");

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

            if (productsDir != null)
            {
                String homeKey = QA_TOOL_PREFIX + tool + '.' + version + ".home";
    
                if (!properties.containsKey(homeKey))
                {
                    String message = "Missing tool home for " + tool + ' ' + version 
                        + ". Please set parameter: " + homeKey
                        + " in external sanity4j.properties";
    
                    throw new QAException(message);
                }
    
                String versionHome = properties.getProperty(homeKey);
                versionHome = QaUtil.replaceTokens(versionHome, asParameterMap(), this, null);
    
                if (new File(versionHome).exists())
                {
                    if (!version.equals(firstVersion))
                    {
                        String msg = "WARNING: Running an out-dated version [" + version + "] of tool [" + tool
                             + "]. The current version is [" + firstVersion + "]";
    
                        QaLogger.getInstance().warn(msg);
                    }
    
                    return version;
                }
                else
                {
                    String msg = "WARNING: Could not find [" + versionHome + "] directory";
                    QaLogger.getInstance().warn(msg);
                }
            }
            else if (runQAMojo != null)
            {
                String mavenKey = QA_TOOL_PREFIX + tool + '.' + version + ".maven";
                
                if (!properties.containsKey(mavenKey))
                {
                    String message = "Missing Maven Coordinate for " + tool + ' ' + version 
                        + ". Please set parameter: " + mavenKey
                        + " in external sanity4j.properties";
    
                    throw new QAException(message);
                }
    
                if (!version.equals(firstVersion))
                {
                    String msg = "WARNING: Running an out-dated version [" + version + "] of tool [" + tool
                         + "]. The current version is [" + firstVersion + "]";

                    QaLogger.getInstance().warn(msg);
                }
    
                return version;
            }
            else
            {
                throw new QAException("Parameter 'productsDir' must be set.");
            }
        }

        QaLogger.getInstance().warn("Unable to find home directory for any version of tool [" + tool + "]");

        return firstVersion;
    }

    /**
     * This method retrieve the home directory for a given tool version.
     *
     * @param tool The tool for which the home directory is to be retrieved.
     * @param version The version number of the given <em>tool</em> for which the home directory is to be retrieved.
     * @return The home directory for the given <em>tool</em> / <em> version</em>.
     */
    public String getToolHome(final String tool, final String version)
    {
        if (productsDir != null)
        {
            String homeKey = QA_TOOL_PREFIX + tool + '.' + version + ".home";
            String toolHome = properties.getProperty(homeKey);
            toolHome = QaUtil.replaceTokens(toolHome, asParameterMap(), this, null);
            
            QaLogger.getInstance().debug("Tool Home: " + toolHome);
            return toolHome;
        }
        else if (runQAMojo != null)
        {
            String mavenKey = QA_TOOL_PREFIX + tool + '.' + version + ".maven";
            String artifact = properties.getProperty(mavenKey);
            File toolFile = runQAMojo.resolveArtifact(artifact);
            
            String toolHome = toolFile.isDirectory() ? toolFile.getPath() : toolFile.getParent();
            
            QaLogger.getInstance().debug("Tool Home: " + toolHome);
            return toolHome;
        }
        else
        {
            throw new QAException("Parameter 'productsDir' must be set.");
        }
    }
    
    /**
     * This method retrieve the home directory for a given tool Maven artifact.
     *
     * @param tool The tool for which the artifact is to be retrieved.
     * @param version The version number of the given <em>tool</em> for which the artifact is to be retrieved.
     * @return The artifact for the given <em>tool</em> / <em> version</em>.
     */
    public String getToolArtifact(final String tool, final String version)
    {
        if (runQAMojo != null)
        {
            String homeKey = QA_TOOL_PREFIX + tool + '.' + version + ".maven";
            String toolArtifact = properties.getProperty(homeKey);
            toolArtifact = QaUtil.replaceTokens(toolArtifact, asParameterMap(), this, null);
            
            QaLogger.getInstance().debug("Tool Artifact: " + toolArtifact);
            return toolArtifact;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * This method returns the name of the configuration parameter that specifies the configuration for a given tool.
     *
     * @param tool The name of the tool for which the configuration parameter is to be retrieved.
     * @param version The version of the tool for which the configuration parameter is to be retrieved.
     * @return the <b>name</b> of the tool "configuration" parameter.
     */
    public String getToolConfigParam(final String tool, final String version)
    {
        return getToolParam(tool, version, "config");
    }
    
    /**
     * This method returns the name of a parameter that specifies the configuration for a given tool.
     *
     * @param tool The name of the tool for which the parameter is to be retrieved.
     * @param version The version of the tool for which the parameter is to be retrieved.
     * @param key the name of the key for the parameter.
     * @return the name of the tool parameter.
     */
    private String getToolParam(final String tool, final String version, final String key)
    {
        StringBuffer configBuf = new StringBuffer();
        
        configBuf.append(QA_TOOL_PREFIX).append(tool).append('.');
        
        if (version != null)
        {
            configBuf.append(version).append('.');
        }
        
        configBuf.append(key);
        
        String configKey = configBuf.toString();
        
        return configKey;
    }
    
    /**
     * This method returns the externally specified configuration for a given tool.
     *
     * @param tool The tool for which the configuration is to be retrieved.
     * @param version the version of the tool for which the configuration is to be retrieved.
     * @return the <b>value</b> of the tool "configuration" parameter.
     */
    public String getToolConfig(final String tool, final String version)
    {
        String configKey = getToolConfigParam(tool, version);
        String toolConfig = properties.getProperty(configKey);
        toolConfig = QaUtil.replaceTokens(toolConfig, asParameterMap(), this, null);

        return toolConfig;
    }

    /**
     * This method sets the configuration used for a given <em>tool</em> / <em>version</em>.
     *
     * @param tool The tool for which the configuration is to be set.
     * @param version the version of the tool for which the configuration is to be set.
     * @param config the value of the tool "configuration" parameter.
     * @param classpath the classpath used by the "configuration".
     */
    public void setToolConfig(final String tool, final String version, final String config, final String classpath)
    {
        String configKey = getToolConfigParam(tool, version);
        String toolConfig = QaUtil.replaceTokens(config, asParameterMap(), this, null);
        properties.setProperty(configKey, toolConfig);

        QaLogger.getInstance().debug("Config set: " + configKey + " = " + toolConfig);
        
        if (classpath != null)
        {
            String configClasspathKey = getToolConfigClasspathParam(tool, version);
            String configClasspath = QaUtil.replaceTokens(classpath, asParameterMap(), this, null);

            properties.setProperty(configClasspathKey, configClasspath);
        }
    }
    
    /**
     * This method returns the name of the configuration parameter that specifies the configuration classpath for a
     * given tool.
     *
     * @param tool The name of the tool for which the configuration classpath parameter is to be retrieved.
     * @param version The version of the tool for which the configuration classpath parameter is to be retrieved. If the
     *            configuration is valid for "all" versions of a given tool, then this parameter may be null.
     * @return the name of the tool "configuration" parameter.
     */
    public String getToolConfigClasspathParam(final String tool, final String version)
    {
        return getToolParam(tool, version, "classpath");
    }

    /**
     * This method returns the externally specified configuration for a given tool.
     *
     * @param tool The tool for which the configuration is to be retrieved.
     * @param version the version of the tool for which the configuration is to be retrieved.
     * @return the <b>value</b> of the tool "configuration" parameter.
     */
    public String getToolConfigClasspath(final String tool, final String version)
    {
        String configClasspathKey = getToolConfigClasspathParam(tool, version);
        String toolConfigClasspath = properties.getProperty(configClasspathKey);
        toolConfigClasspath = QaUtil.replaceTokens(toolConfigClasspath, asParameterMap(), this, null);

        return toolConfigClasspath;
    }
    
    /**
     * This method retrieve the "Runner" class used to run a given tool.
     *
     * @param tool The tool to be run.
     * @param version The version of the tool to be run.
     * @return A String representing the fully qualified name of the "runner" class.
     */
    public String getToolRunner(final String tool, final String version)
    {
        String property = getToolParam(tool, version, "runner");
        
        String runnerClassName = properties.getProperty(property);

        if (StringUtil.empty(runnerClassName))
        {
            String message = "Missing runner for " + tool + ' ' + version + ", please set property '" + property
                             + "' in external sanity4j.properties";

            throw new QAException(message);
        }

        return runnerClassName;
    }

    /**
     * This method retrieve the "Reader" class used to parse the output from a given tool.
     *
     * @param tool The tool to be run.
     * @param version The version of the tool to be run.
     * @return A String representing the fully qualified name of the "reader" class.
     */
    public String getToolReader(final String tool, final String version)
    {
        String property = getToolParam(tool, version, "reader");
        
        String readerClassName = properties.getProperty(property);

        if (StringUtil.empty(readerClassName))
        {
            String message = "Missing reader for " + tool + ' ' + version + ", " + "please set property '"
                             + property + "' " + "in external sanity4j.properties";

            throw new QAException(message);
        }

        return readerClassName;
    }

    /**
     * Returns the result file for the given tool.
     *
     * @param tool the tool.
     * @return the result file for the given tool.
     */
    public File getToolResultFile(final Tool tool)
    {
        return new File(getTempDir(), tool.getId() + "_result.xml");
    }

    /**
     * Returns the command line used to run a given tool.
     *
     * @param tool The tool to be run.
     * @param version The version of the tool to be run.
     * @return A command line that can be run from a shell to invoke the given tool.
     */
    public String getToolCommandLine(final String tool, final String version)
    {
        String commandKey = getToolParam(tool, version, "command");

        if (!properties.containsKey(commandKey))
        {
            String message = "Missing tool command for " + tool + ' ' + version + ". Please set parameter: "
                             + commandKey + " in external sanity4j.properties";

            throw new QAException(message);
        }

        String toolCommandLine = properties.getProperty(commandKey);

        return toolCommandLine;
    }
}
