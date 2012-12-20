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
 * CoberturaReportRunner - a work unit that produces a Cobertura report.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class CoberturaReportRunner extends AbstractToolRunner
{
    /**
     * Creates a CoberturaReportRunner.
     */
    public CoberturaReportRunner()
    {
        super(Tool.COBERTURA);
    }

    /**
     * Produces the Cobertura report file.
     * 
     * @param commandLine the Cobertura command line.
     */
    public void runTool(final String commandLine)
    {
        FileUtil.createDir(getConfig().getToolResultFile(Tool.COBERTURA).getParent());

        // Run the process
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
                throw new QAException("Cobertura Command [" + commandLine + "] failed : [" + out  + "] [" + err + "]");
            }
        }
        finally
        {
            QaUtil.safeClose(stdout);
            QaUtil.safeClose(stderr);
        }
    }

    /**
     * This needs to be overridden as Cobertura doesn't support specifying a file name for the report, only a
     * destination directory.
     * 
     * @return the file path where cobertura places it's output.
     */
    protected String getToolResultFile()
    {
        return new File(getConfig().getTempDir().getPath(), "coverage.xml").getPath();
    }

    /**
     * @return the description of this WorkUnit
     */
    public String getDescription()
    {
        return "Running CoberturaReport";
    }
}
