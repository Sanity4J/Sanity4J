package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.sanity4j.util.ExternalProcessRunner;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.Tool;

/**
 * PmdRunner - a work unit that runs PMD.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class PmdRunner extends AbstractToolRunner
{
    /**
     * Creates a PmdRunner.
     */
    public PmdRunner()
    {
        super(Tool.PMD);
    }

    /**
     * Runs PMD.
     * @param commandLine the PMD command line.
     */
    public void runTool(final String commandLine)
    {
        File resultFile = getConfig().getToolResultFile(Tool.PMD);
        FileUtil.createDir(resultFile.getParentFile().getPath());

        try
        {
            // Run the process
            // PMD sends its output to standard out, so we need to intercept it and write it to a file ourselves
            FileOutputStream fos = new FileOutputStream(resultFile);
            int result = ExternalProcessRunner.runProcess(commandLine, fos, System.err);

            if (result != 0)
            {
                throw new QAException("PMD returned error code " + result);
            }

            fos.close();
        }
        catch (IOException e)
        {
            throw new QAException("Error writing PMD output to " + resultFile, e);
        }
    }

    /**
     * @return the description of this WorkUnit
     */
    public String getDescription()
    {
        return "Running PMD";
    }
}
