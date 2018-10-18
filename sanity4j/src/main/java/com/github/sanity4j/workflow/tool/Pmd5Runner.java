package com.github.sanity4j.workflow.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;

import com.github.sanity4j.util.ExternalProcessRunner;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaUtil;
import com.github.sanity4j.util.StringUtil;
import com.github.sanity4j.util.Tool;

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

            String stderrString = new String(stderr.toByteArray(), Charset.defaultCharset());
            
            if (!StringUtil.empty(stderrString))
            {
                QaLogger.getInstance().error(stderrString);
            }

            if (result != 0)
            {
                String err = new String(stderr.toByteArray(), Charset.defaultCharset());
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
    @Override
    public String getDescription()
    {
        return "Running PMD";
    }
}
