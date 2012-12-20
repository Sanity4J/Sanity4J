package net.sf.sanity4j.maven.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaLoggerMavenImpl;
import net.sf.sanity4j.workflow.QAConfig;
import net.sf.sanity4j.workflow.QAProcessor;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Site;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;

/**
 * <p>
 * This class is a Maven plugin that runs Sanity4J against a project's source
 * code and classes.
 * </p>
 *
 * <p>
 * This task simply configures up a {@link QAProcessor} and then runs it in the
 * {@link #executeReport()} method.
 * </p>
 *
 * @goal sanity4j
 *
 * @author Darian Bridge
 * @since Sanity4J 1.0
 *
 */
public class RunQAMojo extends AbstractMavenReport
{
    /** Mojo and goal to download historical coverage stats. */
    private static final String DOWNLOAD_SINGLE_MOJO = "org.codehaus.mojo:wagon-maven-plugin:1.0-beta-3:download-single";
    
    /** The Pmd Tool. */
    private static final String PMD_TOOL = "pmd";
    
    /** The Find Bugs Tool. */
    private static final String FINDBUGS_TOOL = "findbugs";
    
    /** The Check Style Tool. */
    private static final String CHECKSTYLE_TOOL = "checkstyle";

    /**
     * <i>Maven Internal</i>: Project to interact with.
     *
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * The projects in the reactor for aggregation report.
     *
     * @parameter expression="${reactorProjects}"
     * @readonly
     */
    private List<MavenProject> reactorProjects;

    /**
     * Flag to indicate that this mojo should run it in a multi module way.
     *
     * @parameter
     */
    private boolean aggregate;

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
     */
    private String productsDir;

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;
    
    /**
     * The directory in which to place report output.
     *
     * @parameter default-value="${project.reporting.outputDirectory}/sanity4j"
     */
    private String reportDir;

    /**
     * The file containing the jUnit coverage data.
     *
     * @parameter
     *            default-value="${project.build.directory}/cobertura/cobertura.ser"
     */
    private String coverageDataFile;

    /**
     * The file containing the merged jUnit coverage data.
     *
     * @parameter
     *            default-value="${project.build.directory}/cobertura/cobertura-merged.ser"
     */
    private String coverageMergeDataFile;

    /**
     * The summary data file, if used.
     *
     * @parameter default-value=
     *            "${project.reporting.outputDirectory}/${project.name}_analyse_summary.csv"
     */
    private String summaryDataFile;

    /**
     * The path location to the external properties file (sanity4j.properties).
     *
     * @parameter default-value= ""
     */
    private String externalPropertiesPath;

    /**
     * The configuration passed to the checkStyle ${sanity4j.tool.checkstyle.command} as ${sanity4j.tool.checkstyle.config}.
     * @parameter
     */
    private String checkStyleConfig;

    /**
     * The class path used by the configuration passed to the checkStyle ${sanity4j.tool.checkstyle.command} as ${sanity4j.tool.checkstyle.config}.
     * @parameter
     */
    private String checkStyleConfigClasspath;

    /**
     * The configuration passed to the FindBugs ${sanity4j.tool.findbugs.command} as ${sanity4j.tool.findbugs.config}.
     *
     * @parameter
     */
    private String findBugsConfig;

    /**
     * The class path used by the configuration passed to the FindBugs ${sanity4j.tool.findbugs.command} as ${sanity4j.tool.findbugs.config}.
     *
     * @parameter
     */
    private String findBugsConfigClasspath;

    /**
     * The configuration passed to the PMD ${sanity4j.tool.pmd.command} as ${sanity4j.tool.pmd.config}.
     *
     * @parameter
     */
    private String pmdConfig;

    /**
     * Any additional properties to be passed to the tools.
     *
     * @parameter
     */
    private String additionalProperties;
    
    /**
     * The class path used by the configuration passed to the PMD ${sanity4j.tool.pmd.command} as ${sanity4j.tool.pmd.config}.
     *
     * @parameter
     */
    private String pmdConfigClasspath;

    /**
     * The temporary directory.
     *
     * @parameter default-value="${project.build.directory}/sanity4jAnalysis"
     */
    private File tempDir;

    /**
     * The java runtime to use when running external tasks.
     *
     * @parameter
     */
    // Please don't allow your IDE to automatically mark this field 
    // as static or final. 
    // The Maven Mojo API spec allows for an @parameter annotation 
    // which can be configured from the POM. The annotation uses 
    // reflection to set the field rather than using a java setter 
    // method.
    // Unfortunately, this also means that the below two rules get 
    // triggered on this field during static code analysis.
    //
    // Moderate Private field could be made final; it is only 
    //     initialized in the declaration or constructor. 
    // Moderate This final field could be made static 
    private String javaRuntime = QAProcessor.DEFAULT_JAVA_RUNTIME;

    /**
     * If true, the raw tool output is included in the report directory.
     *
     * @parameter
     */
    // Please don't allow your IDE to automatically mark this field 
    // as static or final. 
    // The Maven Mojo API spec allows for an @parameter annotation 
    // which can be configured from the POM. The annotation uses 
    // reflection to set the field rather than using a java setter 
    // method.
    // Unfortunately, this also means that the below two rules get 
    // triggered on this field during static code analysis.
    //
    // Moderate Private field could be made final; it is only 
    //     initialized in the declaration or constructor. 
    // Moderate This final field could be made static 
    private boolean includeToolOutput = false;

    /**
     * The number of threads to use to run the tools and produce the report
     * output.
     *
     * @parameter
     */
    // Please don't allow your IDE to automatically mark this field 
    // as static or final. 
    // The Maven Mojo API spec allows for an @parameter annotation 
    // which can be configured from the POM. The annotation uses 
    // reflection to set the field rather than using a java setter 
    // method.
    // Unfortunately, this also means that the below two rules get 
    // triggered on this field during static code analysis.
    //
    // Moderate Private field could be made final; it is only 
    //     initialized in the declaration or constructor. 
    // Moderate This final field could be made static 
    private int numThreads = 1; 
    // TODO: Add support for concurrent tasks
    // (Note: some tasks can not be run in parallel).

    /**
     * Whether to use historical statistics when generating sanity4j reports.
     *
     * @parameter default-value="true"
     */
    private boolean useHistory;

    /**
     * Override the default constructor to initialise the logger. No logging
     * methods should be called before this method.
     */
    public RunQAMojo()
    {
        QaLogger.setLogger(new QaLoggerMavenImpl(this));
    }

    /**
     * Executes this mojo, invoking the {@link QAProcessor} which has already
     * been configured by Maven using either the annotations or the pom (through
     * reflection).
     *
     * @param locale
     *            The locale
     */
    public void executeReport(final Locale locale) throws MavenReportException
    {
        if (aggregate && !getProject().isExecutionRoot())
        {
            getLog().info(getProject().getName() + " aggregation is on, but this is not the execution root. Skipping 'sanity4j' check.");

            // Write out an small index.html file so that Maven
            // doesn't complain about empty files when deploying the site.
            File index = new File(reportDir, "index.html");
            writeTextFile(index, " ");

            return;
        }

        boolean aggregating = aggregate && getProject().isExecutionRoot();

        QAProcessor sanity4j = new QAProcessor();
        QAConfig qaConfig = sanity4j.getConfig();
        
        if (aggregating)
        {
            for (MavenProject reactorProject : reactorProjects)
            {
                qaConfig.addSourcePath(
                    reactorProject.getBasedir() + "/src");
            }
        }
        else if (sources == null)
        {
            qaConfig.addSourcePath(
                getProject().getBasedir() + "/src");
        }
        else
        {
            for (String source : sources)
            {
                qaConfig.addSourcePath(source);
            }
        }

        boolean sourceFileDetected = false;
        for (String srcDir : qaConfig.getSourceDirs())
        {
            if (checkForSource(new File(srcDir)))
            {
                sourceFileDetected = true;
                break;
            }
        }
        if (!sourceFileDetected)
        {
            getLog().info(getProject().getName() + " contains no source code. Skipping 'sanity4j' check.");
            return;
        }

        if (aggregating)
        {
            for (MavenProject reactorProject : reactorProjects)
            {
                qaConfig.addClassPath(
                    reactorProject.getBasedir() + "/target/classes");
                qaConfig.addClassPath(
                    reactorProject.getBasedir() + "/target/test-classes");
            }
        }
        else if (classes == null)
        {
            qaConfig.addClassPath(
                getProject().getBasedir() + "/target/classes");
            qaConfig.addClassPath(
                getProject().getBasedir() + "/target/test-classes");
        }
        else
        {
            for (String clazz : classes)
            {
                qaConfig.addClassPath(clazz);
            }
        }

        if (aggregating)
        {
            for (MavenProject reactorProject : reactorProjects)
            {
                try
                {
                    for (Object o : reactorProject.getTestClasspathElements())
                    {
                        qaConfig.addLibraryPath(o.toString());
                    }
                }
                catch (DependencyResolutionRequiredException e)
                {
                    // QA task will not be as accurate, log a warning
                    getLog().warn(
                        "Unable to resolve library dependencies, analysis may not be as accurate.",
                        e);
                }
            }
        }
        else if (libraries == null)
        {
            try
            {
                for (Object o : getProject().getTestClasspathElements())
                {
                    qaConfig.addLibraryPath(o.toString());
                }
            }
            catch (DependencyResolutionRequiredException e)
            {
                // QA task will not be as accurate, log a warning
                getLog().warn(
                    "Unable to resolve library dependencies, analysis may not be as accurate.",
                    e);
            }
        }
        else
        {
            for (String library : libraries)
            {
                qaConfig.addLibraryPath(library);
            }
        }

        if (aggregating)
        {
            for (MavenProject reactorProject : reactorProjects)
            {
                qaConfig.addCoverageDataFile(
                    reactorProject.getBasedir() + "/target/cobertura/cobertura.ser");
            }
        }
        else
        {
            qaConfig.setCoverageDataFile(coverageDataFile);
        }

        QADependency qaDependency = getDependencies("net.sf.sanity4j", "sanity4j");
        if (qaDependency != null)
        {
            for (QADependency child : qaDependency.getDependencies())
            {
                QADependency qaChildDep = getDependencies(child.getGroupId(), child.getArtifactId());
                if (qaChildDep != null)
                {
                    for (QADependency grandChild : qaChildDep.getDependencies())
                    {
                        child.addDependency(grandChild);
                    }
                }
            }
        }
        
        if (getLog().isDebugEnabled() && qaDependency != null)
        {
            getLog().debug("Effective dependency tree - ");
            getLog().debug("  " + qaDependency.getGroupId() + ":" + qaDependency.getArtifactId() + ":" + qaDependency.getVersion());
            for (QADependency child : qaDependency.getDependencies())
            {
                getLog().debug("    " + child.getGroupId() + ":" + child.getArtifactId() + ":" + child.getVersion());
                
                for (QADependency grandChild : child.getDependencies())
                {
                    getLog().debug("      " + grandChild.getGroupId() + ":" + grandChild.getArtifactId() + ":" + grandChild.getVersion());
                }
            }
        }
        
        qaConfig.setCoverageMergeDataFile(coverageMergeDataFile);
        qaConfig.setIncludeToolOutput(includeToolOutput);
        qaConfig.setJavaRuntime(javaRuntime);
        qaConfig.setNumThreads(numThreads);
        qaConfig.setProductsDir(productsDir);
        qaConfig.setMavenLocalRepository(localRepository.getBasedir());
        qaConfig.setQADependency(qaDependency);
        qaConfig.setReportDir(reportDir);
        qaConfig.setSummaryDataFile(summaryDataFile.replaceAll(" ", ""));
        qaConfig.setTempDir(tempDir);
        qaConfig.setExternalPropertiesPath(externalPropertiesPath, additionalProperties);

        if (useHistory)
        {
            retrieveSanity4jStats();
        }

        if (checkStyleConfig != null)
        {
            String version = qaConfig.getToolVersion(CHECKSTYLE_TOOL);
            qaConfig.setToolConfig(CHECKSTYLE_TOOL, null, checkStyleConfig, checkStyleConfigClasspath);
            qaConfig.setToolConfig(CHECKSTYLE_TOOL, version, checkStyleConfig, checkStyleConfigClasspath);
        }

        if (findBugsConfig != null)
        {
            String version = qaConfig.getToolVersion(FINDBUGS_TOOL);
            qaConfig.setToolConfig(FINDBUGS_TOOL, null, findBugsConfig, findBugsConfigClasspath);
            qaConfig.setToolConfig(FINDBUGS_TOOL, version, findBugsConfig, findBugsConfigClasspath);
        }

        if (pmdConfig != null)
        {
            String version = qaConfig.getToolVersion(PMD_TOOL);
            qaConfig.setToolConfig(PMD_TOOL, null, pmdConfig, pmdConfigClasspath);
            qaConfig.setToolConfig(PMD_TOOL, version, pmdConfig, pmdConfigClasspath);
        }
       
        // TODO: HACK! Get around Stax using the context classloader rather than
        // this class's.
        // Need to write a custom class loader which combines the two.
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
            getClass().getClassLoader());
        sanity4j.run();
        Thread.currentThread().setContextClassLoader(oldLoader);
    }

    /**
     * Retrieve the dependencies from the Maven Build Plugins declaration. 
     * 
     * @param groupId The group id to search for.
     * @param artifactId the artifact id to search for.
     * @return The dependencies for the matching group id and artifact id, or null if not declared.
     */
    private QADependency getDependencies(final String groupId, final String artifactId)
    {
        getLog().debug("Looking for dependency - " + groupId + ":" + artifactId);
        
        for (Object objPlugin : project.getBuildPlugins())
        {
            if (objPlugin instanceof Plugin)
            {
                Plugin plugin = (Plugin) objPlugin;
                
                if (groupId.equals(plugin.getGroupId()) && artifactId.equals(plugin.getArtifactId()))
                {
                    getLog().debug("  " + plugin.getGroupId() + ":" + plugin.getArtifactId() + ":" + plugin.getVersion());
                    
                    QADependency dependency = new QADependency();
                    dependency.setGroupId(plugin.getGroupId());
                    dependency.setArtifactId(plugin.getArtifactId());
                    dependency.setVersion(plugin.getVersion());
                
                    List/*<Dependency>*/ dependencies = plugin.getDependencies();
    
                    for (Object objDependency : dependencies)
                    {
                        if (objDependency instanceof Dependency)
                        {
                            Dependency dep = (Dependency) objDependency;
                            
                            getLog().debug("    " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
                            
                            QADependency childDependency = new QADependency();
                            childDependency.setGroupId(dep.getGroupId());
                            childDependency.setArtifactId(dep.getArtifactId());
                            childDependency.setVersion(dep.getVersion());
                            
                            dependency.addDependency(childDependency);
                        }
                    }
                    
                    return dependency;
                }
            }
        }
        
        return null;
    }
    
    /**
     * If available, retrieves sanity4j historical results from 'site' repository.
     */
    private void retrieveSanity4jStats()
    {
        try
        {
            DistributionManagement distributionManagement = project.getDistributionManagement();
            if (distributionManagement == null)
            {
                getLog().info(
                    "'Site' distibution management not defined. No build history will be graphed. Continuing 'sanity4j' checks.");
            }
            else
            {
                Site site = distributionManagement.getSite();

                if (site == null)
                {
                    getLog().info(
                        "'Site' distibution management not defined. No build history will be graphed. Continuing 'sanity4j' checks.");
                }
                else
                {
                    InvocationRequest request = new DefaultInvocationRequest();
                    request.setGoals(Collections.singletonList(DOWNLOAD_SINGLE_MOJO));
                    Properties props = new Properties();

                    String siteUrl = site.getUrl();
                    props.put("wagon.url", siteUrl);
                    String dataFileName= summaryDataFile.replaceAll(" ", "");
                    props.put("wagon.fromFile", new File(dataFileName).getName());
                    props.put("wagon.toDir", project.getReporting().getOutputDirectory());
                    request.setProperties(props);

                    getLog().info("Retrieveing 'sanity4j' statistics from URL: " + props);
                    Invoker invoker = new DefaultInvoker();
                    invoker.execute(request);
                }
            }
        }
        catch (Exception e)
        {
            getLog().error(
                "Unable to retrieve extisting report statistics. Continuing 'sanity4j' checks.", e);
        }
    }

    /**
     * Recursive check for existence of .java file.
     *
     * @param srcFile
     *            the <code>File</code> to start .java check from
     * @return true if .java file exists; otherwise false
     */
    private boolean checkForSource(final File srcFile)
    {
        if (getLog().isDebugEnabled())
        {
            getLog().debug("checking if '.java' file: Location: " + srcFile.getAbsolutePath());
        }
        boolean srcFileMatch = false;
        if (srcFile.isDirectory())
        {
            File[] srcFiles = srcFile.listFiles();
            for (File files : srcFiles)
            {
                if (checkForSource(files))
                {
                    srcFileMatch = true;
                    break;
                }
            }
        }
        if (srcFile.getName().endsWith(".java"))
        {
            srcFileMatch = true;
            getLog().debug("Found '.java' file. Remaining recursive call will be skipped");
        }
        return srcFileMatch;
    }

    /**
     * Write out some small text to a file.
     *
     * @param file The file to write to.
     * @param text The text to write to that file.
     */
    private void writeTextFile(final File file, final String text)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
            writer.close();
        }
        catch (IOException ex)
        {
            getLog().warn("Unable to write file: " + file.getName());
        }
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
     * @param project
     *            the maven project.
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
     * @param siteRenderer
     *            the site renderer.
     */
    public void setSiteRenderer(final Renderer siteRenderer)
    {
        this.siteRenderer = siteRenderer;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     *
     * @param locale
     *            the locale
     * @return the description.
     */
    public String getDescription(final Locale locale)
    {
        return getBundle(locale).getString("report.sanity4j.description");
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     *
     * @param locale
     *            the locale
     * @return the name.
     */
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
     * Indicates that this report is external so that the generated index.html
     * file is not overwritten by the default maven template.
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
     * @param locale
     *            The locale for the report, must not be <code>null</code>.
     * @return The resource bundle for the requested locale.
     */
    private ResourceBundle getBundle(final Locale locale)
    {
        return ResourceBundle.getBundle("sanity4j-maven-report", locale,
            getClass().getClassLoader());
    }
}
