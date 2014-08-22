package net.sf.sanity4j.workflow.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;

import net.sf.sanity4j.util.ExternalProcessRunner;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.util.Tool;

/**
 * PmdRunner - a work unit that runs PMD 5.x.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.1
 */
public class Pmd5Runner extends AbstractToolRunner
{
    /**
     * Creates a Pmd5Runner.
     */
    public Pmd5Runner()
    {
        super(Tool.PMD);
    }

    /**
     * Runs PMD.
     * @param commandLine the PMD command line.
     */
    @Override
    public void runTool(final String commandLine)
    {
        File resultFile = getConfig().getToolResultFile(Tool.PMD);
        FileUtil.createDir(resultFile.getParentFile().getPath());

        // Run the process
        ByteArrayOutputStream stdout = null;
        ByteArrayOutputStream stderr = null;

        try
        {
            stdout = new ByteArrayOutputStream();
            stderr = new ByteArrayOutputStream();

            // Run the process
            int result = ExternalProcessRunner.runProcess(commandLine, stdout, stderr);

            String stderrString = new String(stderr.toByteArray());
            
            if (FileUtil.hasValue(stderrString))
            {
                QaLogger.getInstance().error(stderrString);
            }

            if (result != 0)
            {
                String err = new String(stderr.toByteArray());
                throw new QAException("PMD Command [" + commandLine + "] failed to generate output: [" + err + "]");
            }
        }
        finally
        {
            QaUtil.safeClose(stdout);
            QaUtil.safeClose(stderr);
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