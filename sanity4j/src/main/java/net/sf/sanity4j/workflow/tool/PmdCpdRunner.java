package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.sanity4j.util.ExternalProcessRunner;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.util.Tool;

/**
 * PmdCpdRunner - a work unit that runs PMD CPD.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class PmdCpdRunner extends AbstractToolRunner
{
    /**
     * Creates a PmdCpdRunner.
     */
    public PmdCpdRunner()
    {
        super(Tool.PMD_CPD);
    }

    /**
     * Runs PMD-CPD.
     * @param commandLine the PMD-CPD command line.
     */
    public void runTool(final String commandLine)
    {
        File pmdCpdXmlFile = getConfig().getToolResultFile(Tool.PMD_CPD);
        FileUtil.createDir(pmdCpdXmlFile.getParentFile().getPath());
        FileOutputStream fos = null;

        try
        {
            // Run the process
            // PMD CPD sends its output to standard out, so we need to intercept it and write it to a file ourselves
            fos = new FileOutputStream(pmdCpdXmlFile);
            int result = ExternalProcessRunner.runProcess(commandLine, fos, System.err);

            if (result != 0)
            {
                throw new QAException("PMD CPD returned error code " + result);
            }

            fos.close();
        }
        catch (IOException e)
        {
            throw new QAException("Error writing PMD CPD output to " + pmdCpdXmlFile, e);
        }
        finally
        {
            QaUtil.safeClose(fos);
        }
    }

    /**
     * @return the description of this WorkUnit.
     */
    public String getDescription()
    {
        return "Running PMD CPD";
    }
}
