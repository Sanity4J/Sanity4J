package net.sf.sanity4j.maven.plugin;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;
import java.util.ResourceBundle;

import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaLoggerMavenImpl;
import net.sf.sanity4j.workflow.QAProcessor;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;

/**
 * <p>This class is a Maven plugin that runs Sanity4J
 * against a project's source code and classes.</p>
 * 
 * <p>This task simply configures up a {@link QAProcessor} 
 * and then runs it in the {@link #executeReport()} 
 * method.</p>
 *  
 * @goal sanity4j
 * 
 * @author Darian Bridge
 * @since Sanity4J 1.0
 */
public class RunQAMojo extends AbstractMavenReport
{
    /**
     * <i>Maven Internal</i>: Project to interact with.
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * <i>Maven Internal</i>: The Doxia Site Renderer.
     * 
     * @component
     */
    private Renderer siteRenderer;
    
    /** 
     * The source file paths for analysis.
     *  
     * @parameter
     */
    private String[] sources;

    /** 
     * The class file paths for analysis. 
     *  
     * @parameter
     */
    private String[] classes;

    /** 
     * The library file paths for analysis.
     *  
     * @parameter
     */
    private String[] libraries;

    /** 
     * The location of the directory containing the various tools. 
     *  
     * @parameter
     * @required
     */
    private String productsDir;

    /** 
     * The directory in which to place report output. 
     *  
     * @parameter default-value="${project.reporting.outputDirectory}/sanity4j"
     */
    private String reportDir;

    /** 
     * The file containing the jUnit coverage data. 
     *  
     * @parameter default-value="${project.build.directory}/cobertura/cobertura.ser"
     */
    private String coverageDataFile;

    /** 
     * The summary data file, if used. 
     *  
     * @parameter default-value="${project.reporting.outputDirectory}/${project.name}_analyse_summary.csv"
     */
    private String summaryDataFile;

    /** 
     * The temporary directory.
     *  
     * @parameter
     */
    private File tempDir;

    /** 
     * The java runtime to use when running external tasks. 
     *  
     * @parameter
     */
    private final String javaRuntime = QAProcessor.DEFAULT_JAVA_RUNTIME;

    /** 
     * If true, the raw tool output is included in the report directory. 
     *  
     * @parameter
     */
    private final boolean includeToolOutput = false;

    /** 
     * The number of threads to use to run the tools and produce the report output. 
     *  
     * @parameter
     */
    private final int numThreads = 1; // TODO: Add support for concurrent tasks (Note: some tasks can not be run in parallel).
    
    /**
     * Override the default constructor to initialise the logger.
     * No logging methods should be called before this method. 
     */
    public RunQAMojo()
    {
        QaLogger.setLogger(new QaLoggerMavenImpl(this));
    }

    /**
     * Executes this mojo, invoking the {@link QAProcessor}
     * which has already been configured by Maven using 
     * either the annotations or the pom (through reflection).
     * 
     * @param locale The locale
     */
    @Override
    public void executeReport(final Locale locale) 
    {
        QAProcessor sanity4j = new QAProcessor();

        if (sources == null)
        {
            sanity4j.getConfig().addSourcePath(getProject().getBasedir() + "/src");
        }
        else
        {
            for (String source : sources)
            {
                sanity4j.getConfig().addSourcePath(source);
            }
        }

        if (classes == null)
        {
            sanity4j.getConfig().addClassPath(getProject().getBasedir() + "/target/classes");
            sanity4j.getConfig().addClassPath(getProject().getBasedir() + "/target/test-classes");
        }
        else
        {
            for (String clazz : classes)
            {
                sanity4j.getConfig().addClassPath(clazz);
            }
        }
        
        if (libraries == null)
        {
            try
            {
                for (Object o : getProject().getTestClasspathElements())
                {
                    sanity4j.getConfig().addLibraryPath(o.toString());
                }
            }
            catch (DependencyResolutionRequiredException e)
            {
                // QA task will not be as accurate, log a warning
                getLog().warn("Unable to resolve library dependencies, analysis may not be as accurate", e);
            }
        }
        else
        {
            for (String library : libraries)
            {
                sanity4j.getConfig().addLibraryPath(library);
            }
        }
        
        sanity4j.getConfig().setCoverageDataFile(coverageDataFile);
        sanity4j.getConfig().setIncludeToolOutput(includeToolOutput);
        sanity4j.getConfig().setJavaRuntime(javaRuntime);
        sanity4j.getConfig().setNumThreads(numThreads);
        sanity4j.getConfig().setProductsDir(productsDir);
        sanity4j.getConfig().setReportDir(reportDir);
        sanity4j.getConfig().setSummaryDataFile(summaryDataFile);
        sanity4j.getConfig().setTempDir(tempDir);

        if (hasSource(sanity4j)) 
        {
            // TODO: HACK! Get around Stax using the context classloader rather than this class's.
            //       Need to write a custom class loader which combines the two.
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader(); 
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            sanity4j.run();
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
        else
        {
            getLog().warn(project.getName() + " contains no java source files. Skipping analysis.");
        }
    }
    
    /**
     * Indicates whether there are source files available to analyse.
     * @return true if there is at least one java source file, false otherwise.
     */
    private boolean hasSource(final QAProcessor sanity4j)
    {
        
        for (String sourceDir : sanity4j.getConfig().getSourceDirs())
        {
            File dir = new File(sourceDir);
            
            if (hasSource(dir))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Indicates whether the given directory (or a subdirectory) contains one or more java source files.
     * @param directory the directory to search.
     * @return true if there is at least one java source file, false otherwise.
     */
    private boolean hasSource(final File directory)
    {
        FileFilter srcFilter = new FileFilter() 
        {
            public boolean accept(final File file) 
            {
                return file.isDirectory() || file.getName().endsWith(".java"); 
            }
        };

        for (File file : directory.listFiles(srcFilter))
        {
            if (file.isDirectory())
            {
                // Search sub-dir
                if (hasSource(file))
                {
                    return true;
                }
            }
            else
            {
                // A non-directory was found, must be a source file
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     * 
     * @return the maven project.
     */
    @Override
    public MavenProject getProject()
    {
        return project;
    }

    /**
     * @param project the maven project.
     */
    public void setProject(final MavenProject project)
    {
        this.project = project;
    }
    
    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     * 
     * @return the output directory.
     */
    @Override
    protected String getOutputDirectory()
    {
        return reportDir;
    }
    
    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     * 
     * @return the site renderer.
     */
    @Override
    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }
    
    /**
     * @param siteRenderer the site renderer.
     */
    public void setSiteRenderer(final Renderer siteRenderer)
    {
        this.siteRenderer = siteRenderer;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     * 
     * @param locale the locale
     * @return the description.
     */
    //@Override
    public String getDescription(final Locale locale)
    {
        return getBundle(locale).getString("report.sanity4j.description");
    }
    
    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     * 
     * @param locale the locale
     * @return the name.
     */
    //@Override
    public String getName(final Locale locale)
    {
        return getBundle(locale).getString("report.sanity4j.name");
    }
    
    
    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     * 
     * @return the output name.
     */
    public String getOutputName()
    {
        return "sanity4j/index";
    }

    /**
     * Indicates that this report is external so that the generated 
     * index.html file is not overwritten by the default maven template. 
     * 
     * @see org.apache.maven.reporting.MavenReport#isExternalReport()
     * 
     * @return true to indicate that the report is external.
     */
    public boolean isExternalReport()
    {
        return true;
    }
    
    /**
     * Gets the resource bundle for the report text.
     * 
     * @param locale The locale for the report, must not be <code>null</code>.
     * @return The resource bundle for the requested locale.
     */
    private ResourceBundle getBundle(final Locale locale)
    {
        return ResourceBundle.getBundle("sanity4j-maven-report", locale, getClass().getClassLoader());
    }
}
