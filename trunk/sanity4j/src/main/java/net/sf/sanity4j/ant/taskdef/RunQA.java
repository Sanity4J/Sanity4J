package net.sf.sanity4j.ant.taskdef;

import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaLoggerAntImpl;
import net.sf.sanity4j.workflow.QAProcessor;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * This class is an Apache ANT task that runs Sanity4J against a project's source code and classes.
 * <p>
 * This task simply configures up a {@link QAProcessor} and then runs it in the {@link #execute()} method.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class RunQA extends Task
{
    /** The QAProcessor that runs the QA process. */
    private final QAProcessor qaProcessor = new QAProcessor();

    /**
     * An ant type that stores a tool name / tool version / and the 'location' of a configuration file for the given
     * tool.
     */
    public static class Configuration
    {
        /** The ANT project in which this Configuration was created. */
    	private final Project project;
    	
    	/** The name of the tool to be configured. */
        private String tool;

        /** The version of the tool to be configured. */
        private String version;

        /** The additional class path used by the custom configuration. */
        private Path classpath;

        /** The 'location' of the configuration file for the given tool. */
        private String config;
        
        /**
         * Creates a {@link #Configuration} associated with the given ANT {@link Project}.
         * 
         * @param project The ant project which created this {@link #Configuration}.
         */
        public Configuration(final Project project)
        {
        	this.project = project;
        }
        
        /**
         * @return the {@link #tool}.
         */
        public String getTool()
        {
            return tool;
        }

        /**
         * @return the {@link #version}
         */
        public String getVersion()
        {
            return version;
        }

        /**
         * @return the {@link #config}.
         */
        public String getConfig()
        {
            return config;
        }

        /**
         * @return the {@link #classpath}.
         */
        public String getClasspath()
        {
        	if (classpath == null)
        	{
        		return null;
        	}
        	else
        	{
        		return classpath.toString();
        	}
        }
        
        /**
         * @param tool the value to which to set {@link #tool}.
         */
        public void setTool(final String tool)
        {
            this.tool = tool;
        }

        /**
         * @param version the value to which to set {@link #version}.
         */
        public void setVersion(final String version)
        {
            this.version = version;
        }

        /**
         * @param config the value to which to set {@link #config}.
         */
        public void setConfig(final String config)
        {
            this.config = config;
        }

        /**
         * @return the created classpath from the project.
         */
        public Path createClasspath()
        {
        	if (classpath == null)
        	{
        		classpath = new Path(project); 
        	}
        	
        	return classpath.createPath();
        }
        
        /**
         * @param classpath the value to which to set {@link #classpath}.
         */
        public void setClasspath(final Path classpath)
        {
        	if (this.classpath == null)
        	{
        		this.classpath = classpath;
        	}
        	else
        	{
        		this.classpath.append(classpath);
        	}
        }
        
        /**
         * Set the {@link #classpath} from a reference to another classpath.
         * 
         * @param reference The reference from which the value of {@link #classpath} is to be derived. 
         */
        public void setClasspathRef(final Reference reference)
        {
        	createClasspath().setRefid(reference);
        }
    }

    /**
     * Adds the given path to the list of sources.
     * 
     * @param path the source path to add.
     */
    public void addConfiguredSourcepath(final Path path)
    {
        for (String pathElement : path.list())
        {
            qaProcessor.getConfig().addSourcePath(pathElement);
        }
    }

    /**
     * Adds the given path to the list of classes.
     * 
     * @param path the class path to add.
     */
    public void addConfiguredClasspath(final Path path)
    {
        for (String pathElement : path.list())
        {
            qaProcessor.getConfig().addClassPath(pathElement);
        }
    }

    /**
     * Adds the given path to the list of libraries.
     * 
     * @param path the library path to add.
     */
    public void addConfiguredLibraryPath(final Path path)
    {
        for (String pathElement : path.list())
        {
            qaProcessor.getConfig().addLibraryPath(pathElement);
        }
    }

    /**
     * @return a newly created {@link Configuration}.
     */
    public Configuration createConfiguration()
    {
        return new Configuration(getProject());
    }

    /**
     * @param configuration The tool configuration to be added to the QA Configuration.
     */
    public void addConfiguredConfiguration(final Configuration configuration)
    {
    	if (configuration.getConfig() != null)
    	{
	        qaProcessor.getConfig().setToolConfig(
	        	configuration.getTool(), configuration.getVersion(),
	            configuration.getConfig(), configuration.getClasspath());
	
	        if (configuration.getVersion() == null)
	        {
	            String version = qaProcessor.getConfig().getToolVersion(configuration.getTool());
	
	            qaProcessor.getConfig().setToolConfig(
	            	configuration.getTool(), version, 
	            	configuration.getConfig(), configuration.getClasspath());
	        }
    	}
    }

    /**
     * @param javaRuntime The javaRuntime to set.
     */
    public void setJavaRuntime(final String javaRuntime)
    {
        qaProcessor.getConfig().setJavaRuntime(javaRuntime);
    }
    
    /**
     * @param javaArgs The javaArgs to set.
     */
    public void setJavaArgs(final String javaArgs)
    {
        qaProcessor.getConfig().setJavaArgs(javaArgs);
    }

    /**
     * @param productsDir The productsDir to set.
     */
    public void setProductsDir(final String productsDir)
    {
        qaProcessor.getConfig().setProductsDir(productsDir);
    }

    /**
     * @param reportDir The reportDir to set.
     */
    public void setReportDir(final String reportDir)
    {
        qaProcessor.getConfig().setReportDir(reportDir);
    }

    /**
     * @param coverageDataFile The coverageDataFile to set.
     */
    public void setCoverageDataFile(final String coverageDataFile)
    {
        qaProcessor.getConfig().setCoverageDataFile(coverageDataFile);
    }

    /**
     * @param path The coverageDataFile to add to the list.
     */
    public void addConfiguredCoverageDataFiles(final Path path)
    {
        for (String pathElement : path.list())
        {
            qaProcessor.getConfig().addCoverageDataFile(pathElement);
        }
    }

    /**
     * @param coverageMergeDataFile The coverageMergeDataFile to set.
     */
    public void setCoverageMergeDataFile(final String coverageMergeDataFile)
    {
        qaProcessor.getConfig().setCoverageMergeDataFile(coverageMergeDataFile);
    }

    /**
     * @param externalPropertiesPath The externalPropertiesPath to set.
     */
    public void setExternalPropertiesPath(final String externalPropertiesPath)
    {
        qaProcessor.getConfig().setExternalPropertiesPath(externalPropertiesPath);
    }

    /**
     * @param summaryDataFile The summaryDataFile to set.
     */
    public void setSummaryDataFile(final String summaryDataFile)
    {
        qaProcessor.getConfig().setSummaryDataFile(summaryDataFile);
    }

    /**
     * @param includeToolOutput The includeToolOutput to set.
     */
    public void setIncludeToolOutput(final String includeToolOutput)
    {
        qaProcessor.getConfig().setIncludeToolOutput("true".equalsIgnoreCase(includeToolOutput));
    }

    /**
     * Sets the maximum number of threads to use (default is 1).
     * 
     * @param numThreads the maximum number of threads to use.
     */
    public void setNumThreads(final int numThreads)
    {
        qaProcessor.getConfig().setNumThreads(numThreads);
    }

    /**
     * Executes this task, invoking the {@link QAProcessor} which has already been configured by Ant using the setters
     * in this Task.
     * 
     * @throws BuildException if there is an error executing the task.
     */
    public void execute() throws BuildException
    {
        QaLogger.getInstance().debug("Coverage Data File:       [" + qaProcessor.getConfig().getCoverageDataFile() + "]");
        QaLogger.getInstance().debug("Coverage Merge Data File: [" + qaProcessor.getConfig().getCoverageMergeDataFile() + "]");
        QaLogger.getInstance().debug("External Properties Path: [" + qaProcessor.getConfig().getExternalPropertiesPath() + "]");
        QaLogger.getInstance().debug("Java Runtime:             [" + qaProcessor.getConfig().getJavaRuntime() + "]");
        QaLogger.getInstance().debug("Java Args:                [" + qaProcessor.getConfig().getJavaArgs() + "]");
        QaLogger.getInstance().debug("Number of Threads:        [" + qaProcessor.getConfig().getNumThreads() + "]");
        QaLogger.getInstance().debug("Products Directory:       [" + qaProcessor.getConfig().getProductsDir() + "]");
        QaLogger.getInstance().debug("Report Directory:         [" + qaProcessor.getConfig().getReportDir() + "]");
        QaLogger.getInstance().debug("Summary DataFile:         [" + qaProcessor.getConfig().getSummaryDataFile() + "]");
        
        try
        {
            // TODO: HACK! Get around Stax using the context classloader rather than this class's.
            // Need to write a custom class loader which combines the two.
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            qaProcessor.run();
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
        catch (QAException ex)
        {
            throw new BuildException(ex.getMessage(), ex);
        }
    }

    /**
     * Override setProject to initialise the logger. No logging methods should be called before this method.
     * 
     * @param project the project to set.
     */
    public void setProject(final Project project)
    {
        super.setProject(project);
        QaLogger.setLogger(new QaLoggerAntImpl(this));
    }
}
