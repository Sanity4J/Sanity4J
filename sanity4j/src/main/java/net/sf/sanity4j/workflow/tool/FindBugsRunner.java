package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.sanity4j.util.ExternalProcessRunner;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.Tool;
import net.sf.sanity4j.workflow.QAConfig;

/**
 * FindBugsRunner - a work unit that runs FindBugs.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class FindBugsRunner extends AbstractToolRunner
{
    /** The generated FindBugs project file. */
    private File projectFile = null;

    /**
     * Creates a FindBugsRunner.
     */
    public FindBugsRunner()
    {
        super(Tool.FINDBUGS);
    }

    /**
     * Runs FindBugs.
     * @param commandLine the FindBugs command line.
     */
    public void runTool(final String commandLine)
    {
        QAConfig config = getConfig();
        
        File resultFile = config.getToolResultFile(Tool.FINDBUGS);
        FileUtil.createDir(resultFile.getParentFile().getPath());

        int result = ExternalProcessRunner.runProcess(commandLine, System.out, System.err);

        if (result != 0)
        {
            throw new QAException("FindBugs returned error code " + result);
        }
    }

    /**
     * Creates the FindBugs project file, which controls the FindBugs run.
     */
    private void createFindBugsProjectFile()
    {
        QAConfig config = getConfig();
        List<String> sourcePaths = Arrays.asList(new String[]{config.getCombinedSourceDir().getPath()});

        List<String> classPaths = new ArrayList<String>();
        classPaths.add(config.getCombinedClassDir().getPath());
        FileUtil.findJars(config.getCombinedClassDir(), classPaths);

        List<String> libraryPaths = new ArrayList<String>();
        libraryPaths.add(config.getCombinedLibraryDir().getPath());
        FileUtil.findJars(config.getCombinedLibraryDir(), libraryPaths);

        String xml = generateFindBugsProjectXml(sourcePaths, classPaths, libraryPaths);

        try
        {
            projectFile = File.createTempFile("findbugsproject", ".fbp");
            FileUtil.writeToFile(xml, projectFile);
            projectFile.deleteOnExit();
        }
        catch (IOException e)
        {
            throw new QAException("Failed to create FindBugs project file", e);
        }
    }

    /**
     * Overridden to add FindBugs specific parameters to the map.
     * @return a map of parameters to use for replacing configuration tokens.
     */
    protected Map<String, String> getParameterMap()
    {
        Map<String, String> map = super.getParameterMap();
        
        if (projectFile == null)
        {
            createFindBugsProjectFile();
        }

        map.put("findBugsProjectFile", projectFile.getPath());
        
        return map;
    }

    /**
     * Overriden to remove the GUI jar from the class path, as we want to run the text UI.
     * @return a list of paths to all the jars needed to run FindBugs.
     */
    protected List<String> getToolJars()
    {
        List<String> findbugsJars = super.getToolJars();

        for (Iterator<String> i = findbugsJars.iterator(); i.hasNext();)
        {
            if (i.next().endsWith("findbugsGUI.jar"))
            {
                i.remove();
            }
        }

        return findbugsJars;
    }

    /**
     * @return the description of this WorkUnit
     */
    public String getDescription()
    {
        return "Running FindBugs";
    }

    /**
     * Build the findbugs project file, as there doesn't seem to be a nicer way to run it.
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
     * @return the FindBugs project xml for the given data
     */
    private String generateFindBugsProjectXml(final List<String> sourcePaths,
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

        buf.append
        (
              "   <SuppressionFilter>\n"
            + "       <LastVersion value=\"-1\" relOp=\"NEQ\"/>\n"
            + "    </SuppressionFilter>\n"
            + "</Project>\n"
        );

        return buf.toString();
    }
}
