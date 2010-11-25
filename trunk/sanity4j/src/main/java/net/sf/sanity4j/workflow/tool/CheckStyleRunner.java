package net.sf.sanity4j.workflow.tool;

import java.io.File;

import net.sf.sanity4j.util.ExternalProcessRunner;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.Tool;

/**
 * CheckStyleRunner - a work unit that runs CheckStyle.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class CheckStyleRunner extends AbstractToolRunner
{
    /**
     * Creates a CheckStyleRunner.
     */
    public CheckStyleRunner()
    {
        super(Tool.CHECKSTYLE);
    }

    /**
     * Runs CheckStyle.
     * @param commandLine the CheckStyle command line.
     */
    protected void runTool(final String commandLine)
    {
        File resultFile = getConfig().getToolResultFile(Tool.CHECKSTYLE);
        FileUtil.createDir(resultFile.getParentFile().getPath());

        // Run the process
        ExternalProcessRunner.runProcess(commandLine, System.out, System.err);

        // The checkstyle result is the number of errors found, so
        // the only way to determine if it succeeded is to check if
        // the output file has been created successfully
        if (!resultFile.exists() || resultFile.length() == 0)
        {
            throw new QAException("Checkstyle failed to generate output");
        }
    }

    /**
     * @return the description of this WorkUnit
     */
    public String getDescription()
    {
        return "Running CheckStyle";
    }
}
