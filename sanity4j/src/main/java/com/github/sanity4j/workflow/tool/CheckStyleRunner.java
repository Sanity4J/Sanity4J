package com.github.sanity4j.workflow.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;

import com.github.sanity4j.util.ExternalProcessRunner;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaUtil;
import com.github.sanity4j.util.Tool;

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
        ByteArrayOutputStream stdout = null; 
        ByteArrayOutputStream stderr = null; 

        try
        {
            stdout = new ByteArrayOutputStream();
            stderr = new ByteArrayOutputStream();
            
            ExternalProcessRunner.runProcess(commandLine, stdout, stderr);
            
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
        }
        finally
        {
            QaUtil.safeClose(stdout);
            QaUtil.safeClose(stderr);
        }

        // The checkstyle result is the number of errors found, so
        // the only way to determine if it succeeded is to check if
        // the output file has been created successfully
        if (!resultFile.exists() || resultFile.length() == 0)
        {
            String out = new String(stdout.toByteArray());
            String err = new String(stderr.toByteArray());
            throw new QAException("Checkstyle Command [" + commandLine + "] failed to generate output: [" + out  + "] [" + err + "]");
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
