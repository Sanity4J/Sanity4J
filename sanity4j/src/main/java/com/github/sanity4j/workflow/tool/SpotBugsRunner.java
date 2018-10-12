package com.github.sanity4j.workflow.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.sanity4j.util.ExternalProcessRunner;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaUtil;
import com.github.sanity4j.util.Tool;
import com.github.sanity4j.workflow.QAConfig;

/**
 * SpotBugsRunner - a work unit that runs SpotBugs.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class SpotBugsRunner extends AbstractToolRunner
{
    /** The generated SpotBugs project file. */
    private File projectFile = null;

    /**
     * Creates a SpotBugsRunner.
     */
    public SpotBugsRunner()
    {
        super(Tool.SPOTBUGS);
    }

    /**
     * Runs SpotBugs.
     * @param commandLine the SpotBugs command line.
     */
    @Override
   public void runTool(final String commandLine)
    {
        QAConfig config = getConfig();
        
        File resultFile = config.getToolResultFile(Tool.SPOTBUGS);
        FileUtil.createDir(resultFile.getParentFile().getPath());

        ByteArrayOutputStream stdout = null; 
        ByteArrayOutputStream stderr = null; 

        try
        {
            stdout = new ByteArrayOutputStream();
            stderr = new ByteArrayOutputStream();
            
            int result = ExternalProcessRunner.runProcess(commandLine, stdout, stderr);

            String stdoutString = new String(stdout.toByteArray());

            if (FileUtil.hasValue(stdoutString))
            {
                QaLogger.getInstance().info(stdoutString);
            }
            
            String stderrString = new String(stderr.toByteArray());

            if (FileUtil.hasValue(stderrString))
            {
                QaLogger.getInstance().error(stderrString);
            }

            if (result != 0)
            {
                String out = new String(stdout.toByteArray());
                String err = new String(stderr.toByteArray());
                throw new QAException("SpotBugs Command [" + commandLine + "] failed : [" + out  + "] [" + err + "]");
            }
        }
        finally
        {
            QaUtil.safeClose(stdout);
            QaUtil.safeClose(stderr);
        }
    }

    /**
     * Creates the SpotBugs project file, which controls the SpotBugs run.
     */
    private void createSpotBugsProjectFile()
    {
        QAConfig config = getConfig();
        List<String> sourcePaths = Arrays.asList(new String[]{config.getCombinedSourceDir().getPath()});

        List<String> classPaths = new ArrayList<String>();
        classPaths.add(config.getCombinedClassDir().getPath());
        FileUtil.findJars(config.getCombinedClassDir(), classPaths);

        List<String> libraryPaths = new ArrayList<String>();
        libraryPaths.add(config.getCombinedLibraryDir().getPath());
        FileUtil.findJars(config.getCombinedLibraryDir(), libraryPaths);

        String xml = generateSpotBugsProjectXml(sourcePaths, classPaths, libraryPaths);

        try
        {
            projectFile = File.createTempFile("spotbugsproject", ".fbp");
            FileUtil.writeToFile(xml, projectFile);
            projectFile.deleteOnExit();
        }
        catch (IOException e)
        {
            throw new QAException("Failed to create SpotBugs project file", e);
        }
    }

    /**
     * Overridden to add SpotBugs specific parameters to the map.
     * @return a map of parameters to use for replacing configuration tokens.
     */
    @Override
   protected Map<String, String> getParameterMap()
    {
        Map<String, String> map = super.getParameterMap();
        
        if (projectFile == null)
        {
            createSpotBugsProjectFile();
        }

        map.put("spotBugsProjectFile", projectFile.getPath());
        
        return map;
    }

    /**
     * Overriden to remove the GUI jar from the class path, as we want to run the text UI.
     * @return a list of paths to all the jars needed to run SpotBugs.
     */
    @Override
   protected List<String> getToolJars()
    {
        List<String> spotbugsJars = super.getToolJars();

        for (Iterator<String> i = spotbugsJars.iterator(); i.hasNext();)
        {
            if (i.next().endsWith("spotbugsGUI.jar"))
            {
                i.remove();
            }
        }

        return spotbugsJars;
    }

    /**
     * @return the description of this WorkUnit
     */
    @Override
   public String getDescription()
    {
        return "Running SpotBugs";
    }

    /**
     * Build the spotbugs project file, as there doesn't seem to be a nicer way to run it.
     * The final XML will look like the following:
     * <pre>
     *   &lt;Project filename="unnamed project" projectName="unnamed project"&gt;
     *      &lt;Jar&gt;...path/project.jar&lt;/Jar&gt;
     *      ...
     *      &lt;AuxClasspathEntry&gt;...path/library.jar&lt;/AuxClasspathEntry&gt;
     *      ...
     *      &lt;SrcDir&gt;...path/src/java&lt;/SrcDir&gt;
     *      ...
     *      &lt;SuppressionFilter&gt;
     *         &lt;LastVersion value="-1" relOp="NEQ"/&gt;
     *      &lt;/SuppressionFilter&gt;
     *   &lt;/Project&gt;
     * </pre>
     *
     * @param sourcePaths the source paths
     * @param classPaths the class paths
     * @param libraryPaths the library paths
     *
     * @return the SpotBugs project xml for the given data
     */
    private String generateSpotBugsProjectXml(final List<String> sourcePaths,
                                              final List<String> classPaths, final List<String> libraryPaths)
    {
        StringBuffer buf = new StringBuffer("<Project filename=\"unnamed project\" projectName=\"unnamed project\">\n");

        for (int i = 0; i < classPaths.size(); i++)
        {
            buf.append("   <Jar>");
            buf.append(classPaths.get(i));
            buf.append("</Jar>\n");
        }

        for (int i = 0; i < libraryPaths.size(); i++)
        {
            buf.append("   <AuxClasspathEntry>");
            buf.append(libraryPaths.get(i));
            buf.append("</AuxClasspathEntry>\n");
        }

        for (int i = 0; i < sourcePaths.size(); i++)
        {
            buf.append("   <SrcDir>");
            buf.append(sourcePaths.get(i));
            buf.append("</SrcDir>\n");
        }

        buf.append(
              "   <SuppressionFilter>\n"
            + "       <LastVersion value=\"-1\" relOp=\"NEQ\"/>\n"
            + "    </SuppressionFilter>\n"
            + "</Project>\n"
        );

        return buf.toString();
    }
}
