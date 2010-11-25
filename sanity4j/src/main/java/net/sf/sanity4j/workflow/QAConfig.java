package net.sf.sanity4j.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.Tool;

/**
 * This class holds all the configuration attributes for the QA process.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class QAConfig
{
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
     * The directory in which to place report output. 
     */
    private String reportDir;

    /** 
     * The file containing the jUnit coverage data. 
     */
    private String coverageDataFile;

    /** 
     * The summary data file, if used. 
     */
    private String summaryDataFile;

    /** 
     * The temporary directory.
     */
    private File tempDir;

    /** 
     * The java runtime to use when running external tasks. 
     */
    private String javaRuntime = QAProcessor.DEFAULT_JAVA_RUNTIME;

    /** 
     * If true, the raw tool output is included in the report directory. 
     */
    private boolean includeToolOutput = false;

    /** 
     * The number of threads to use to run the tools and produce the report output. 
     */
    private int numThreads = 1; // TODO: Add support for concurrent tasks (Note: some tasks can not be run in parallel).

    /**
     * Adds a source path to the list of source paths to analyse.
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
    	if(coverageDataFile != null)
    	{
	    	File file = new File(coverageDataFile);
	    	
	    	if(file.exists())
	    	{
	            this.coverageDataFile = coverageDataFile;
	    	}
	    	else
	    	{
	    		QaLogger.getInstance().warn("Unabe to locate coverage data file: " + coverageDataFile);
	    	}
    	}
    }

    /**
     * @return Returns the coverage data file.
     */
    public String getCoverageDataFile()
    {
    	return coverageDataFile;
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
     * Returns a map view of this configurationt to be used in e.g.
     * performing variable substitution. Some additional parameters
     * are added to the map for convenience.
     *
     * @return a view of this configuration as a map
     */
    public Map<String, String> asParameterMap()
    {
       Map<String, String> params = new HashMap<String, String>();

       params.put("java", getJavaRuntime());
       params.put("javaArgs", getMaxMemSize());
       params.put("products", getProductsDir());
       params.put("source", getCombinedSourceDir().getPath());
       params.put("classes", getCombinedClassDir().getPath());
       params.put("libs", getCombinedLibraryDir().getPath());
       params.put("coverageDataFile", getCoverageDataFile());
       params.put("tempDir", getTempDir().getPath());
       params.put("File.separatorChar", File.separator);
       params.put("File.pathSeparator", File.pathSeparator);

       return params;
    }

    /**
     * Returns the result file for the given tool.
     * @param tool the tool.
     * @return the result file for the given tool.
     */
    public File getToolResultFile(final Tool tool)
    {
       return new File(getTempDir(), tool.getId() + "_result.xml");
    }
}
