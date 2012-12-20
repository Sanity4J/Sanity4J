package net.sf.sanity4j.workflow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import net.sf.sanity4j.maven.plugin.QADependency;
import net.sf.sanity4j.report.ExtractStaticContent;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.util.Resources;
import net.sf.sanity4j.util.StringUtil;
import net.sf.sanity4j.util.Tool;

/**
 * This class holds all the configuration attributes for the QA process.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class QAConfig
{
    /** The location of the "tools" configuration properties file. */
    private static final String TOOL_PROPERTIES = "/net/sf/sanity4j/workflow/tool/tools.properties";

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
     * The location of the local Maven repository.
     */
    private String mavenLocalRepository;
    
    /**
     * The extracted Maven dependencies from the Maven project.
     */
    private QADependency qaDependency;
    
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
    private File tempDir = new File(System.getProperty("java.io.tmpdir"));

    /**
     * The java runtime to use when running external tasks.
     */
    private String javaRuntime = QAProcessor.DEFAULT_JAVA_RUNTIME;

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
     * The configuration properties. This is a combination of the internal defaults {@link Resources#TOOL_PROPERTIES}
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
        if (!sources.contains(sourcePath))
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
        if (!classes.contains(classPath))
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
        if (!libraries.contains(libraryPath))
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
     * @param javaRuntime The javaRuntime to set.
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
     * @return Returns the mavenLocalRepository.
     */
    public String getMavenLocalRepository()
    {
        return mavenLocalRepository;
    }

    /**
     * @param mavenLocalRepository The mavenLocalRepository to set.
     */
    public void setMavenLocalRepository(final String mavenLocalRepository)
    {
        this.mavenLocalRepository = mavenLocalRepository;
    }
    
    /**
     * @return The tool dependency tree.
     */
    public QADependency getQADependency() 
    {
        return this.qaDependency;
    }
    
    /**
     * @param qaDependency The tool dependency tree to set.
     */
    public void setQADependency(final QADependency qaDependency) 
    {
        this.qaDependency = qaDependency;
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
                QaLogger.getInstance().warn("Unabe to locate coverage data file: " + coverageDataFile);
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
        if (coverageDataFiles.size() < 1)
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
        if (coverageDataFiles.size() < 1)
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
                    builder.append(" ");
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
        this.tempDir = tempDir;
    }

    /**
     * @return the max heap size to use when running java tools
     */
    public String getMaxMemSize()
    {
        return QAProcessor.JAVA_RUNTIME_MAX_MEMORY;
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
        params.put("javaArgs", getMaxMemSize());
        params.put("products", getProductsDir());
        params.put("mavenLocalRepository", getMavenLocalRepository());
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
                versionHome = QaUtil.replaceTokens(versionHome, asParameterMap());
    
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
            else if (mavenLocalRepository != null)
            {
                String mavenKey = QA_TOOL_PREFIX + tool + '.' + version + ".maven";
                
                if (!properties.containsKey(mavenKey))
                {
                    String message = "Missing Maven GAV for " + tool + ' ' + version + ". Please set parameter: " + mavenKey
                                     + " in external sanity4j.properties";
    
                    throw new QAException(message);
                }
                
                QADependency qaDependency = getQaDependency(tool, version);
                String mavenGav = properties.getProperty(mavenKey);
                
                if (qaDependency == null)
                {
                    String message = "Maven POM does not contain build plugin for " + tool + ' ' + version 
                        + ". Please define a Maven dependency for build plugin: " + mavenGav
                        + " in the Maven POM.";

                   throw new QAException(message);
                }
                
                String toolHome = getQaDependencyLocation(qaDependency);

                
                if (new File(toolHome).exists())
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
                    String msg = "WARNING: Could not find [" + toolHome + "] directory";
                    QaLogger.getInstance().warn(msg);
                }
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
            toolHome = QaUtil.replaceTokens(toolHome, asParameterMap());
            
            QaLogger.getInstance().debug("Tool Home: " + toolHome);
            return toolHome;
        }
        else if (mavenLocalRepository != null)
        {
            QADependency qaDependency = getQaDependency(tool, version);
            String toolHome = getQaDependencyLocation(qaDependency);

            QaLogger.getInstance().debug("Tool Home: " + toolHome);
            return toolHome;
        }
        else
        {
            throw new QAException("Parameter 'productsDir' must be set.");
        }
    }
    
    /**
     * Lookup the tool and version in the sanity4j properties, then confirm it's
     * existence in the Maven Project Build declaraion, then return the actual
     * dependency. 
     * 
     * @param tool the tool to look up.
     * @param version the version of the tool.
     * @return the dependency if found, or null otherwise.
     */
    public QADependency getQaDependency(final String tool, final String version)
    {
        if (this.qaDependency == null)
        {
            return null;
        }
        
        QaLogger.getInstance().debug("Tool: " + tool + ":" + version);
        
        String mavenKey = QA_TOOL_PREFIX + tool + '.' + version + ".maven";
        String mavenGav = properties.getProperty(mavenKey);
        
        QaLogger.getInstance().debug("Maven Gav: " + mavenGav);
        
        String[] mavenFields = mavenGav.split(":");
        
        String mavenGroupId = mavenFields[0];
        String mavenArtifactId = mavenFields[1];
        String mavenVersion = mavenFields[2];
        
        for (QADependency dependency : this.qaDependency.getDependencies())
        {
            if (mavenGroupId.equals(dependency.getGroupId())
                && mavenArtifactId.equals(dependency.getArtifactId())
                && mavenVersion.equals(dependency.getVersion()))
            {
                return dependency;
            }
        }
        
        return null;
    }
    
    /**
     * Return a list of jars for the dependency tool.
     * 
     * @param toolId the tool id.
     * @param version the tool version.
     * @return a list of jars for the dependency tool.
     */
    public List<String> getQaDependencyJarList(final String toolId, final String version) 
    {
        QADependency toolDependency = getQaDependency(toolId, version);
        if (toolDependency != null)
        {
	        List<QADependency> dependencies = toolDependency.getDependencies();
	        if (dependencies != null)
	        {
	        	List<String> toolJars = new ArrayList<String>();
	        	
		        for (QADependency dependency : dependencies)
		        {
			        String location = getQaDependencyLocation(dependency);
		            FileUtil.findJars(new File(location), toolJars);
		        }
		        
		        return toolJars;
	        }
        }

        return null;
    }

    /**
     * Format a dependency as a file path location.
     * 
     * @param qaDependency the dependency to format.
     * @return the dependency formatted as a file path location.
     */
    public String getQaDependencyLocation(final QADependency qaDependency)
    {
        String location = mavenLocalRepository + File.separator 
            + qaDependency.getGroupId().replace(".", File.separator) + File.separator
            + qaDependency.getArtifactId() + File.separator
            + qaDependency.getVersion();
        
        return location;
    }
    
    /**
     * This method returns the name of the configuration parameter that specifies the configuration for a given tool.
     *
     * @param tool The name of the tool for which the configuration parameter is to be retrieved.
     * @param version The version of the tool for which the configuration parameter is to be retrieved. If the
     *            configuration is valid for "all" versions of a given tool, then this parameter may be null.
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
     * @param version The version of the tool for which the parameter is to be retrieved. If the
     *            configuration is valid for "all" versions of a given tool, then this parameter may be null.
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
        toolConfig = QaUtil.replaceTokens(toolConfig, asParameterMap());

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
        String toolConfig = QaUtil.replaceTokens(config, asParameterMap());
        properties.setProperty(configKey, toolConfig);

        QaLogger.getInstance().debug("Config set: " + configKey + " = " + toolConfig);
        
        if (classpath != null)
        {
            String configClasspathKey = getToolConfigClasspathParam(tool, version);
            String configClasspath = QaUtil.replaceTokens(classpath, asParameterMap());

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
        toolConfigClasspath = QaUtil.replaceTokens(toolConfigClasspath, asParameterMap());

        return toolConfigClasspath;
    }

    /**
     * This method returns the name of the configuration parameter that specifies the extraction properties for a given tool.
     *
     * @param tool The name of the tool for which the configuration parameter is to be retrieved.
     * @param version The version of the tool for which the configuration parameter is to be retrieved. If the
     *            configuration is valid for "all" versions of a given tool, then this parameter may be null.
     * @return the <b>name</b> of the tool "configuration" parameter.
     */
    public String getToolExtractListParam(final String tool, final String version)
    {
        return getToolParam(tool, version, "extractList");
    }
    
    /**
     * This method returns the extraction properties configuration for a given tool.
     *
     * @param tool The tool for which the configuration is to be retrieved.
     * @param version the version of the tool for which the configuration is to be retrieved.
     * @return the <b>value</b> of the tool "configuration" parameter.
     */
    public String getToolExtractList(final String tool, final String version)
    {
        String configKey = getToolExtractListParam(tool, version);
        String toolConfig = properties.getProperty(configKey);
        toolConfig = QaUtil.replaceTokens(toolConfig, asParameterMap());

        return toolConfig;
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
    
    /**
     * Extract any tool configuration properties (if required).
     * 
     * @param tool The tool to extract configuration properties for.
     * @param toolVersion the version of the tool.
     */
    public void extractConfig(final Tool tool, final String toolVersion)
    {
        String toolExtractList = getToolExtractList(tool.getId(), toolVersion);
        if (toolExtractList != null)
        {
            QaLogger.getInstance().debug("Tool Extract List: " + toolExtractList);
            
            try 
            {
                ExtractStaticContent.extractContent(this, getTempDir(), toolExtractList);
            } 
            catch (IOException ex) 
            {
                String message = "Error extracting configuration file for tool [" + tool.getId() + "]";
                throw new QAException(message, ex);
            }
        }
    }
    
    /**
     * Extract any tool resources.
     * 
     * @param tool The tool to extract resources for.
     * @param toolVersion the version of the tool.
     */
    public void expandResource(final Tool tool, final String toolVersion)
    {
        String toolConfig = getToolConfig(tool.getId(), toolVersion);
        if (toolConfig != null)
        {
            QaLogger.getInstance().debug("Expanding Tool Config: " + toolConfig);
            
            try 
            {
                ExtractStaticContent.expandResource(this, getTempDir(), toolConfig, tool, toolVersion);
            } 
            catch (IOException ex) 
            {
                String message = "Error extracting resource for tool [" + tool.getId() + "]";
                throw new QAException(message, ex);
            }
        }
    }
    
}
