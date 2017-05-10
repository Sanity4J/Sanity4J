package com.github.sanity4j.maven.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Site;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.utils.resolvers.ArtifactsResolver;
import org.apache.maven.plugin.dependency.utils.resolvers.DefaultArtifactsResolver;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;

import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaLoggerMavenImpl;
import com.github.sanity4j.util.Tool;
import com.github.sanity4j.workflow.QAConfig;
import com.github.sanity4j.workflow.QAProcessor;

/**
 * <p>
 * This class is a Maven plugin that runs Sanity4J against a project's source
 * code and classes.
 * </p>
 *
 * <p>
 * This task simply configures up a {@link QAProcessor} and then runs it in the
 * {@link QAProcessor#run()} method.
 * </p>
 *
 * @author Darian Bridge
 * @since Sanity4J 1.0
 */
@Mojo(name = "sanity4j", requiresProject = false, requiresDependencyResolution = ResolutionScope.TEST, requiresDependencyCollection = ResolutionScope.TEST)
public class RunQAMojo extends AbstractMavenReport
{
    /** Mojo and goal to download historical coverage stats. */
    private static final String DOWNLOAD_SINGLE_MOJO = "org.codehaus.mojo:wagon-maven-plugin:1.0-beta-5:download-single";

    /**
     * The Maven Project to interact with.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The projects in the reactor for aggregation report.
     */
    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)    
    private List<MavenProject> reactorProjects;

    /**
     * Helper for locating Dependencies.
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;
    
    /**
     * Helper for resolving Artifacts against the Repository.
     */
    @Component
    private ArtifactResolver resolver;
    
    /**
     * Helper for creating a Maven Project.
     */
    @Component(role = ProjectBuilder.class)
    private ProjectBuilder projectBuilder;
    
    /**
     * Can be one of the below.
     * <pre> 
     * org.sonatype.aether.RepositorySystemSession
     * org.eclipse.aether.RepositorySystemSession
     * </pre>
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private Object session;

    /**
     * The Local Repository.
     */
    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository localRepo;

    /**
     * The Remote Repositories.
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    private List<ArtifactRepository> remoteRepos;    

    /**
     * Flag to indicate that this mojo should run it in a multi module way.
     */
    @Parameter(defaultValue = "false")
    private boolean aggregate;

    /**
     * <i>Maven Internal</i>: The Doxia Site Renderer.
     */
    @Component
    private Renderer siteRenderer;

    /**
     * The source file paths for analysis.
     */
    @Parameter    
    private String[] sources;

    /**
     * The test source file paths for analysis.
     */
    @Parameter    
    private String[] testSources;

    /**
     * The class file paths for analysis.
     */
    @Parameter    
    private String[] classes;

    /**
     * The test class file paths for analysis.
     */
    @Parameter    
    private String[] testClasses;

    /**
     * The library file paths for analysis.
     */
    @Parameter    
    private String[] libraries;

    /**
     * The location of the directory containing the various tools.
     */
    @Parameter
    private String productsDir;

    /**
     * The directory in which to place report output.
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/sanity4j")  
    private String reportDir;

    /**
     * The file containing the jUnit coverage data.
     */
    @Parameter(defaultValue = "target/cobertura/cobertura.ser")    
    private String coverageDataFile;

    /**
     * The file containing the merged jUnit coverage data.
     */
    @Parameter(defaultValue = "${project.build.directory}/cobertura/cobertura-merged.ser")    
    private String coverageMergeDataFile;

    /**
     * The summary data file, if used.
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/${project.name}_analyse_summary.csv")    
    private String summaryDataFile;

    /**
     * The path location to the external properties file (sanity4j.properties).
     */
    @Parameter(defaultValue = "")    
    private String externalPropertiesPath;

    /**
     * The configuration passed to the checkStyle ${sanity4j.tool.checkstyle.command} as ${sanity4j.tool.checkstyle.config}.
     */
    @Parameter    
    private String checkStyleConfig;

    /**
     * The class path used by the configuration passed to the checkStyle ${sanity4j.tool.checkstyle.command} as ${sanity4j.tool.checkstyle.config}.
     */
    @Parameter    
    private String checkStyleConfigClasspath;

    /**
     * The configuration passed to the FindBugs ${sanity4j.tool.findbugs.command} as ${sanity4j.tool.findbugs.config}.
     */
    @Parameter    
    private String findBugsConfig;

    /**
     * The class path used by the configuration passed to the FindBugs ${sanity4j.tool.findbugs.command} as ${sanity4j.tool.findbugs.config}.
     */
    @Parameter    
    private String findBugsConfigClasspath;

    /**
     * The configuration passed to the PMD ${sanity4j.tool.pmd.command} as ${sanity4j.tool.pmd.config}.
     */
    @Parameter    
    private String pmdConfig;

    /**
     * Any additional properties to be passed to the tools.
     */
    @Parameter    
    private String additionalProperties;
    
    /**
     * The class path used by the configuration passed to the PMD ${sanity4j.tool.pmd.command} as ${sanity4j.tool.pmd.config}.
     */
    @Parameter    
    private String pmdConfigClasspath;

    /**
     * The temporary directory.
     */
    @Parameter(defaultValue = "${project.build.directory}/sanity4jAnalysis")    
    private File tempDir;

    /**
     * The java runtime to use when running external tasks.
     */
    @Parameter(defaultValue = QAProcessor.DEFAULT_JAVA_RUNTIME)    
    private String javaRuntime;
    
    /**
     * The java args to use when running external tasks.
     */
    @Parameter(defaultValue = "${env.MAVEN_OPTS}")    
    private String javaArgs;

    /**
     * If true, the raw tool output is included in the report directory.
     */
    @Parameter(defaultValue = "false")
    private boolean includeToolOutput;

    /**
     * The number of threads to use to run the tools and produce the report
     * output.
     */
    @Parameter(defaultValue = "1")
    private int numThreads; 
    // TODO: Add support for concurrent tasks
    // (Note: some tasks can not be run in parallel).

    /**
     * Whether to use historical statistics when generating sanity4j reports.
     */
    @Parameter(defaultValue = "true")    
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
     * been configured by Maven using either the annotations or the POM (through
     * reflection).
     *
     * @param locale The locale
     * @throws MavenReportException Thrown if a problem occurs generating the report.
     */
    @Override
    public void executeReport(final Locale locale) throws MavenReportException
    {
        if (isAggregate() && !getProject().isExecutionRoot())
        {
            getLog().info(getProject().getName() + " aggregation is on, but this is not the execution root. Skipping 'sanity4j' check.");

            // Write out an small index.html file so that Maven
            // doesn't complain about empty files when deploying the site.
            File index = new File(getReportDir(), "index.html");
            writeTextFile(index, " ");

            return;
        }

        FileUtil.createDir(getReportDir());
        
        boolean aggregating = isAggregate() && getProject().isExecutionRoot();

        QAProcessor sanity4j = new QAProcessor();
        QAConfig qaConfig = sanity4j.getConfig();
        qaConfig.setRunQAMojo(this);
        
        if (aggregating)
        {
            for (MavenProject reactorProject : getReactorProjects())
            {
                if (getSources() == null)
                {
                    qaConfig.addSourcePath(
                        new File(reactorProject.getBasedir(), "src/main").getPath());
                }
                else
                {
                    for (String source : getSources())
                    {
                        qaConfig.addSourcePath(
                            new File(reactorProject.getBasedir(), source).getPath());
                    }
                }
            }
        }
        else if (getSources() == null)
        {
            qaConfig.addSourcePath(
                new File(getProject().getBasedir(), "src/main").getPath());
        }
        else
        {
            for (String source : getSources())
            {
                qaConfig.addSourcePath(
                     new File(getProject().getBasedir(), source).getPath());
            }
        }

        if (aggregating)
        {
            for (MavenProject reactorProject : getReactorProjects())
            {
                if (getTestSources() == null)
                {
                    qaConfig.addSourcePath(
                        new File(reactorProject.getBasedir(), "src/test").getPath());
                }
                else
                {
                    for (String testSource : getTestSources())
                    {
                        qaConfig.addSourcePath(
                            new File(reactorProject.getBasedir(), testSource).getPath());
                    }
                }
            }
        }
        else if (getTestSources() == null)
        {
            qaConfig.addSourcePath(
                new File(getProject().getBasedir(), "src/test").getPath());
        }
        else
        {
            for (String testSource : getTestSources())
            {
                qaConfig.addSourcePath(
                    new File(getProject().getBasedir(), testSource).getPath());
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
            for (MavenProject reactorProject : getReactorProjects())
            {
                if (getClasses() == null)
                {
                qaConfig.addClassPath(
                        new File(reactorProject.getBasedir(), "target/classes").getPath());
                }
                else
                {
                    for (String clazz : getClasses())
                    {
                        qaConfig.addClassPath(
                            new File(reactorProject.getBasedir(), clazz).getPath());
                    }
                }
            }
        }
        else if (getClasses() == null)
        {
            qaConfig.addClassPath(
                new File(getProject().getBasedir(), "target/classes").getPath());
        }
        else
        {
            for (String clazz : getClasses())
            {
                qaConfig.addClassPath(
                    new File(getProject().getBasedir(), clazz).getPath());
            }
        }
        
        if (aggregating)
        {
            for (MavenProject reactorProject : getReactorProjects())
            {
                if (getTestClasses() == null)
                {
                    qaConfig.addClassPath(
                        new File(reactorProject.getBasedir(), "target/test-classes").getPath());
                }
                else
                {
                    for (String testClazz : getTestClasses())
                    {
                        qaConfig.addClassPath(
                            new File(reactorProject.getBasedir(), testClazz).getPath());
                    }
                }
            }
        }
        else if (getTestClasses() == null)
        {
            qaConfig.addClassPath(
                new File(getProject().getBasedir(), "target/test-classes").getPath());
        }
        else
        {
            for (String testClazz : getTestClasses())
            {
                qaConfig.addClassPath(
                    new File(getProject().getBasedir(), testClazz).getPath());
            }
        }

        if (aggregating)
        {
            if (getLibraries() == null)
            {
                for (MavenProject reactorProject : getReactorProjects())
            {
                try
                {
                    for (Artifact artifact : getTransitiveDependencies(reactorProject))
                    {
                        qaConfig.addLibraryPath(artifact.getFile().getPath());
                    }
                }
                catch (Exception e)
                {
                    // QA task will not be as accurate, log a warning
                    getLog().warn("Unable to resolve library dependencies, analysis may not be as accurate.", e);
                }
            }
        }
            else
            {
                for (String library : getLibraries())
                {
                    qaConfig.addLibraryPath(library);
                }
            }
        }
        else if (getLibraries() == null)
        {
            try
            {
                for (Artifact artifact : getTransitiveDependencies(getProject()))
                {
                    qaConfig.addLibraryPath(artifact.getFile().getPath());
                }
            }
            catch (Exception e)
            {
                // QA task will not be as accurate, log a warning
                getLog().warn("Unable to resolve library dependencies, analysis may not be as accurate.", e);
            }
        }
        else
        {
            for (String library : getLibraries())
            {
                qaConfig.addLibraryPath(library);
            }
        }

        if (aggregating)
        {
            for (MavenProject reactorProject : getReactorProjects())
            {
                qaConfig.addCoverageDataFile(
                    new File(reactorProject.getBasedir(), getCoverageDataFile()).getPath());
            }
        }
        else
        {
            qaConfig.setCoverageDataFile(
                new File(getProject().getBasedir(), getCoverageDataFile()).getPath());
        }

        qaConfig.setCoverageMergeDataFile(getCoverageMergeDataFile());
        qaConfig.setIncludeToolOutput(isIncludeToolOutput());
        qaConfig.setJavaRuntime(getJavaRuntime());
        qaConfig.setJavaArgs(getJavaArgs());
        qaConfig.setNumThreads(getNumThreads());
        qaConfig.setProductsDir(getProductsDir());
        qaConfig.setReportDir(getReportDir());
        qaConfig.setSummaryDataFile(getSummaryDataFile().replaceAll(" ", ""));
        qaConfig.setTempDir(getTempDir());
        qaConfig.setExternalPropertiesPath(getExternalPropertiesPath(), getAdditionalProperties());

        if (isUseHistory())
        {
            retrieveSanity4jStats();
        }

        if (getCheckStyleConfig() != null)
        {
            String version = qaConfig.getToolVersion(Tool.CHECKSTYLE.getId());
            qaConfig.setToolConfig(Tool.CHECKSTYLE.getId(), null, getCheckStyleConfig(), getCheckStyleConfigClasspath());
            qaConfig.setToolConfig(Tool.CHECKSTYLE.getId(), version, getCheckStyleConfig(), getCheckStyleConfigClasspath());
        }

        if (getFindBugsConfig() != null)
        {
            String version = qaConfig.getToolVersion(Tool.FINDBUGS.getId());
            qaConfig.setToolConfig(Tool.FINDBUGS.getId(), null, getFindBugsConfig(), getFindBugsConfigClasspath());
            qaConfig.setToolConfig(Tool.FINDBUGS.getId(), version, getFindBugsConfig(), getFindBugsConfigClasspath());
        }

        if (getPmdConfig() != null)
        {
            String version = qaConfig.getToolVersion(Tool.PMD.getId());
            qaConfig.setToolConfig(Tool.PMD.getId(), null, getPmdConfig(), getPmdConfigClasspath());
            qaConfig.setToolConfig(Tool.PMD.getId(), version, getPmdConfig(), getPmdConfigClasspath());
        }
       
        // TODO: HACK! Get around Stax using the context classloader rather than this class's.
        // Need to write a custom class loader which combines the two.
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        sanity4j.run();
        Thread.currentThread().setContextClassLoader(oldLoader);
    }
    
    /**
     * Resolves the list of Transitive Dependencies for the specified artifact.
     * 
     * @param artifact The artifact for which to resolve the transitive dependencies.
     * @param jars The list for which to populate the resolved file paths for all the transitive dependencies.
     */
    public void getTransitiveDependencies(final String artifact, final List<String> jars) 
    {
        try 
        {
            for (Artifact artifactItem : getTransitiveDependencies(artifact))
            {
                jars.add(artifactItem.getFile().getPath());
            }
        } 
        catch (Exception ex) 
        {
            throw new QAException(ex.getClass().getName() + ": ", ex);
        }
    }
    
    /**
     * Resolves the list of Transitive Dependencies for the specified artifact.
     * 
     * @param artifact The artifact for which to resolve the transitive dependencies.
     * @return The set of resolved transitive dependencies.
     * 
     * @throws DependencyGraphBuilderException Thrown if a problem occurs.
     * @throws MojoExecutionException Thrown if a problem occurs.
     * @throws CloneNotSupportedException Thrown if a problem occurs.
     * @throws InvalidVersionSpecificationException Thrown if a problem occurs.
     * @throws ProjectBuildingException Thrown if a problem occurs.
     * @throws IllegalAccessException Thrown if a problem occurs.
     * @throws InvocationTargetException Thrown if a problem occurs.
     * @throws NoSuchMethodException Thrown if a problem occurs.
     */
    private Set<Artifact> getTransitiveDependencies(final String artifact) 
        throws DependencyGraphBuilderException, MojoExecutionException, CloneNotSupportedException, InvalidVersionSpecificationException, ProjectBuildingException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        Coordinate coordinate = new Coordinate(artifact);
        Artifact mavenArtifact = getArtifact(coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getVersion(), coordinate.getScope(), coordinate.getPackaging(), coordinate.getClassifier());
        return getTransitiveDependencies(mavenArtifact);
    }

    /**
     * Assembles a Maven Artifact object.
     * 
     * @param groupId The groupId.
     * @param artifactId The artifactId.
     * @param version The version.
     * @param scope The scope.
     * @param type The type.
     * @param classifier The classifier.
     * @return The assembled Maven Artifact object.
     * @throws InvalidVersionSpecificationException Thrown if the version could not be idientified.
     */
    private Artifact getArtifact(final String groupId, final String artifactId, final String version, final String scope, final String type, final String classifier) 
        throws InvalidVersionSpecificationException
    {
        return new DefaultArtifact(
            groupId, artifactId, VersionRange.createFromVersionSpec(version), scope, type, classifier, new DefaultArtifactHandler(type));
    }
    
    /**
     * Resolves the list of Transitive Dependencies for the specified artifact.
     * 
     * @param artifact The artifact for which to resolve the transitive dependencies.
     * @return The set of resolved transitive dependencies.
     * 
     * @throws DependencyGraphBuilderException Thrown if a problem occurs.
     * @throws MojoExecutionException Thrown if a problem occurs.
     * @throws CloneNotSupportedException Thrown if a problem occurs.
     * @throws InvalidVersionSpecificationException Thrown if a problem occurs.
     * @throws ProjectBuildingException Thrown if a problem occurs.
     * @throws IllegalAccessException Thrown if a problem occurs.
     * @throws InvocationTargetException Thrown if a problem occurs.
     * @throws NoSuchMethodException Thrown if a problem occurs.
     */
    private Set<Artifact> getTransitiveDependencies(final Artifact artifact) 
        throws DependencyGraphBuilderException, MojoExecutionException, CloneNotSupportedException, InvalidVersionSpecificationException, ProjectBuildingException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
        //request.setUserProperties(getProject().getProperties());
        request.setSystemProperties(System.getProperties());
        request.setLocalRepository(getLocalRepo());
        request.setRemoteRepositories(getRemoteRepos());
        
        // Support for Maven 3.0 / 3.1+ (Move from Sonatype-Aether to Eclipse-Aether).
        //request.setRepositorySession(session);
        invoke(request, "setRepositorySession", getSession());

        MavenProject project = getProjectBuilder().build(artifact, request).getProject();
        
        return getTransitiveDependencies(project);
    }
    
    /**
     * Uses reflection to invoke a method on an object, passing in the arguments as parameters.
     * Attempts to determine the method signature by using the first Interface of each argument's Class. 
     * 
     * Note, we should probably try to make a better attempt at identifying the method signature rather 
     * than simply using the first Interface of the argument's Class. However for the moment it is enough 
     * for it to support the use by it's only caller. 
     * 
     * @param object The Object on which to invoke the method.
     * @param method The name of the method to invoke on the Object.
     * @param args The parameters to pass to the method.
     * @return The returned object of the method invoked.
     * 
     * @throws IllegalAccessException Thrown if the method cannot be invoked.
     * @throws InvocationTargetException Thrown if the invoked method throws an Exception. 
     * @throws NoSuchMethodException Thrown if the method cannot be identified.
     */
    private Object invoke(final Object object, final String method, final Object... args)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        Class<?>[] types = new Class<?>[args.length];
        
        for (int i = 0; i < args.length; i++)
        {
            types[i] = args[i].getClass().getInterfaces()[0];
        }
        
        return object.getClass().getMethod(method, types).invoke(object, args);
    }
    
    /**
     * Resolves the list of Transitive Dependencies for the specified Maven Project.
     * 
     * @param project The Maven Project for which to resolve the transitive dependencies.
     * @return The set of resolved transitive dependencies.
     * 
     * @throws DependencyGraphBuilderException Thrown if a problem occurs.
     * @throws MojoExecutionException Thrown if a problem occurs.
     */
    private Set<Artifact> getTransitiveDependencies(final MavenProject project) 
        throws DependencyGraphBuilderException, MojoExecutionException
    {
        DependencyNode rootNode = getDependencyGraphBuilder().buildDependencyGraph(project, new ArtifactFilter()
        {
            public boolean include(final Artifact artifact) 
            {
                return !Artifact.SCOPE_SYSTEM.equals(artifact.getScope());
            }
        });
            
        Set<Artifact> sortedArtifacts = new LinkedHashSet<Artifact>();
        
        sortedArtifacts.add(rootNode.getArtifact());
        flattenChildren(sortedArtifacts, rootNode);
        
        ArtifactsResolver artifactsResolver = new DefaultArtifactsResolver(getResolver(), getLocalRepo(), getRemoteRepos(), true);
        
        Set<Artifact> resolvedArtifacts = artifactsResolver.resolve(sortedArtifacts, getLog());
        
        Set<Artifact> orderedArtifacts = new LinkedHashSet<Artifact>();
        
        for (Artifact sortedArtifact : sortedArtifacts)
        {
            for (Artifact resolvedArtifact : resolvedArtifacts)
            {
                if (sortedArtifact.compareTo(resolvedArtifact) == 0)
                {
                    orderedArtifacts.add(resolvedArtifact);
                }
            }
        }
        
        return orderedArtifacts;
    }
    
    /**
     * Resolves a single Artifact.
     * 
     * @param artifact The Artifact for which to resolve.
     * @return The File referencing the resolved Artifact.
     */
    public File resolveArtifact(final String artifact) 
    {
        try
        {
            Coordinate coordinate = new Coordinate(artifact);
            
        Set<Artifact> artifacts = new HashSet<Artifact>();
            artifacts.add(getArtifact(coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getVersion(), coordinate.getScope(), coordinate.getPackaging(), coordinate.getClassifier()));
            
            ArtifactsResolver artifactsResolver = new DefaultArtifactsResolver(getResolver(), getLocalRepo(), getRemoteRepos(), true);
            
            artifacts = artifactsResolver.resolve(artifacts, getLog());
            
            return artifacts.toArray(new Artifact[1])[0].getFile();
            
        } 
        catch (Exception ex) 
        {
            throw new QAException(ex.getClass().getName() + ": ", ex);
        }
    }
    
    /**
     * Recursively traverses the hierarchy of the dependency tree flattening it's contents into a Set of Artifacts.
     * 
     * @param artifacts The Set for which to populate the Artifacts. 
     * @param rootNode The start or currently positioned node within the dependency tree.
     * @return The same Set of Artifacts as passed in, with the Artifacts populated.
     */
    private Set<Artifact> flattenChildren(final Set<Artifact> artifacts, final DependencyNode rootNode)
    {
        if (rootNode.getChildren() != null)
        {
            // Add all the child artifacts first.
            for (DependencyNode node : rootNode.getChildren())
            {
                artifacts.add(node.getArtifact());
            }

            // Then add all the grand children second.
            for (DependencyNode node : rootNode.getChildren())
            {
                flattenChildren(artifacts, node);
            }
        }
        
        return artifacts;
    }

    /**
     * If available, retrieves sanity4j historical results from 'site' repository.
     */
    private void retrieveSanity4jStats()
    {
        try
        {
            DistributionManagement distributionManagement = getProject().getDistributionManagement();
            if (distributionManagement == null)
            {
                getLog().info("'Site' distribution management not defined. No build history will be graphed. Continuing 'sanity4j' checks.");
            }
            else
            {
                Site site = distributionManagement.getSite();

                if (site == null)
                {
                    getLog().info("'Site' distribution management not defined. No build history will be graphed. Continuing 'sanity4j' checks.");
                }
                else
                {
                    InvocationRequest request = new DefaultInvocationRequest();
                    request.setGoals(Collections.singletonList(DOWNLOAD_SINGLE_MOJO));
                    Properties props = new Properties();

                    String siteUrl = site.getUrl();
                    props.put("wagon.url", siteUrl);
                    String dataFileName = getSummaryDataFile().replaceAll(" ", "");
                    props.put("wagon.fromFile", new File(dataFileName).getName());
                    props.put("wagon.toDir", getProject().getReporting().getOutputDirectory());
                    request.setProperties(props);

                    getLog().info("Retrieveing 'sanity4j' statistics from URL: " + props);
                    Invoker invoker = new DefaultInvoker();
                    invoker.execute(request);
                }
            }
        }
        catch (Exception e)
        {
            getLog().error("Unable to retrieve extisting report statistics. Continuing 'sanity4j' checks.", e);
        }
    }

    /**
     * Recursive check for existence of .java file.
     *
     * @param srcFile the <code>File</code> to start .java check from
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
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     *
     * @param locale the locale
     * @return the description.
     */
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
    @Override
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
        return ResourceBundle.getBundle("sanity4j-maven-report", locale,
            getClass().getClassLoader());
    }

    /**
     * @return The Reactor Projects.
     */
    public List<MavenProject> getReactorProjects() 
    {
        return reactorProjects;
    }

    /**
     * @return The Dependency Graph Builder. 
     */
    public DependencyGraphBuilder getDependencyGraphBuilder() 
    {
        return dependencyGraphBuilder;
    }

    /**
     * @return The Artifact Resolver.
     */
    public ArtifactResolver getResolver() 
    {
        return resolver;
    }

    /**
     * @return The Project Builder.
     */
    public ProjectBuilder getProjectBuilder() 
    {
        return projectBuilder;
    }

    /**
     * Can be one of the below.
     * <pre> 
     * org.sonatype.aether.RepositorySystemSession
     * org.eclipse.aether.RepositorySystemSession
     * </pre>
     * 
     * @return The Repository System Session.
     */
    public Object getSession() 
    {
        return session;
    }

    /**
     * @return The Local Artifact Repsoitory.
     */
    public ArtifactRepository getLocalRepo() 
    {
        return localRepo;
    }

    /**
     * @return The List of Remote Artifact Repositories.  
     */
    public List<ArtifactRepository> getRemoteRepos() 
    {
        return remoteRepos;
    }

    /**
     * @return Is Aggregate.
     */
    public boolean isAggregate() 
    {
        return aggregate;
    }

    /**
     * @return The Sources.
     */
    public String[] getSources() 
    {
        return sources;
    }

    /**
     * @return The Test Sources.
     */
    public String[] getTestSources() 
    {
        return testSources;
    }

    /**
     * @return The Classes.
     */
    public String[] getClasses() 
    {
        return classes;
    }

    /**
     * @return The Test Classes.
     */
    public String[] getTestClasses() 
    {
        return testClasses;
    }

    /**
     * @return The Libraries.
     */
    public String[] getLibraries() 
    {
        return libraries;
    }

    /**
     * @return The Products Dir.
     */
    public String getProductsDir() 
    {
        return productsDir;
    }

    /**
     * @return The Report Dir.
     */
    public String getReportDir() 
    {
        return reportDir;
    }

    /**
     * @return The Coverage Data File.
     */
    public String getCoverageDataFile() 
    {
        return coverageDataFile;
    }

    /**
     * @return The Coverage Merge Data File.
     */
    public String getCoverageMergeDataFile() 
    {
        return coverageMergeDataFile;
    }

    /**
     * @return The Summary Data File.
     */
    public String getSummaryDataFile() 
    {
        return summaryDataFile;
    }

    /**
     * @return The External Properties Path.
     */
    public String getExternalPropertiesPath() 
    {
        return externalPropertiesPath;
    }

    /**
     * @return The Check Style Config.
     */
    public String getCheckStyleConfig() 
    {
        return checkStyleConfig;
    }

    /**
     * @return The Check Style Config Classpath.
     */
    public String getCheckStyleConfigClasspath() 
    {
        return checkStyleConfigClasspath;
    }

    /**
     * @return The Fins Bugs Config.
     */
    public String getFindBugsConfig() 
    {
        return findBugsConfig;
    }

    /**
     * @return The Find Bugs Config Classpath. 
     */
    public String getFindBugsConfigClasspath() 
    {
        return findBugsConfigClasspath;
    }

    /**
     * @return The Pmd Config.
     */
    public String getPmdConfig() 
    {
        return pmdConfig;
    }

    /**
     * @return The Additional Properties.
     */
    public String getAdditionalProperties() 
    {
        return additionalProperties;
    }

    /**
     * @return The Pmd Config Classpath.
     */
    public String getPmdConfigClasspath() 
    {
        return pmdConfigClasspath;
    }

    /**
     * @return The Temp Dir.
     */
    public File getTempDir() 
    {
        return tempDir;
    }

    /**
     * @return The Java Runtime. 
     */
    public String getJavaRuntime() 
    {
        return javaRuntime;
    }

    /**
     * @return The Java Args.
     */
    public String getJavaArgs() 
    {
        return javaArgs;
    }

    /**
     * @return Is Include Tool Output.
     */
    public boolean isIncludeToolOutput() 
    {
        return includeToolOutput;
    }

    /**
     * @return The Num Threads.
     */
    public int getNumThreads() 
    {
        return numThreads;
    }

    /**
     * @return Is Use History.
     */
    public boolean isUseHistory() 
    {
        return useHistory;
    }
}
