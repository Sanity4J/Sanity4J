package net.sf.sanity4j.workflow.tool;

import java.io.ByteArrayOutputStream;

import net.sf.sanity4j.util.ExternalProcessRunner;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.util.Tool;

/**
 * CoberturaMergeRunner - a work unit that merges Cobertura data files.
 * 
 * @author Darian Bridge
 * @since Sanity4J 1.0.4
 */
public class CoberturaMergeRunner extends AbstractToolRunner
{
    /**
     * Creates a CoberturaReportRunner.
     */
    public CoberturaMergeRunner()
    {
        super(Tool.COBERTURA_MERGE);
    }

    /**
     * Produces the Merged Cobertura data file.
     * 
     * @param commandLine the Merge Cobertura data file command line.
     */
    public void runTool(final String commandLine)
    {
        // Run the process
        ByteArrayOutputStream stdout = null;
        ByteArrayOutputStream stderr = null;

        try
        {
            stdout = new ByteArrayOutputStream();
            stderr = new ByteArrayOutputStream();

            // Run the process
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
                throw new QAException("Cobertura Merge Command [" + commandLine + "] failed : [" + out  + "] [" + err + "]");
            }
        }
        finally
        {
            QaUtil.safeClose(stdout);
            QaUtil.safeClose(stderr);
        }
    }

    /**
     * @return the file path where the tool should place it's output.
     */
    protected String getToolResultFile()
    {
        return null;
    }

    /**
     * @return the description of this WorkUnit.
     */
    public String getDescription()
    {
        return "Running Cobertura Data File Merge";
    }
}
