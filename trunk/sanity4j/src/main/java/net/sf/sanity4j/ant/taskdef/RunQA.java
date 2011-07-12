package net.sf.sanity4j.ant.taskdef;

import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaLoggerAntImpl;
import net.sf.sanity4j.workflow.QAProcessor;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;


/**
 * <p>This class is an Apache ANT task that runs Sanity4J
 * against a project's source code and classes.</p>
 * 
 * <p>This task simply configures up a {@link QAProcessor} 
 * and then runs it in the {@link #execute()} method.</p> 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class RunQA extends Task
{
    /** The QAProcessor that runs the QA process. */
    private final QAProcessor qaProcessor = new QAProcessor();
    
    /**
     * Adds the given path to the list of sources.
     * 
     *  @param path the source path to add
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
     *  @param path the class path to add
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
     *  @param path the library path to add
     */
    public void addConfiguredLibraryPath(final Path path)
    {
        for (String pathElement : path.list())
        {
            qaProcessor.getConfig().addLibraryPath(pathElement);
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
     * @param numThreads the maximum number of threads to use.
     */
    public void setNumThreads(final int numThreads)
    {
        qaProcessor.getConfig().setNumThreads(numThreads);
    }   

    /**
     * Executes this task, invoking the {@link QAProcessor}
     * which has already been configured by Ant using the 
     * setters in this Task.
     * 
     * @throws BuildException if there is an error executing the task.
     */
    public void execute() throws BuildException
    {
        try
        {
            // TODO: HACK! Get around Stax using the context classloader rather than this class's.
            //       Need to write a custom class loader which combines the two.
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
     * Override setProject to initialise the logger.
     * No logging methods should be called before this method. 
     * 
     * @param project the project to set.
     */
    public void setProject(final Project project)
    {
        super.setProject(project);
        QaLogger.setLogger(new QaLoggerAntImpl(this));
    }
}
