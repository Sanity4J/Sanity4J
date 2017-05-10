package com.github.sanity4j.workflow.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.sanity4j.util.ExternalProcessRunner;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaUtil;
import com.github.sanity4j.util.Tool;

/**
 * PmdCpd4Runner - a work unit that runs PMD CPD.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class PmdCpd4Runner extends AbstractToolRunner
{
    /**
     * Creates a PmdCpdRunner.
     */
    public PmdCpd4Runner()
    {
        super(Tool.PMD_CPD);
    }

    /**
     * Runs PMD-CPD.
     * @param commandLine the PMD-CPD command line.
     */
    @Override
    public void runTool(final String commandLine)
    {
        File pmdCpdXmlFile = getConfig().getToolResultFile(Tool.PMD_CPD);
        FileUtil.createDir(pmdCpdXmlFile.getParentFile().getPath());
        
        FileOutputStream stdout = null;
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        
        try
        {
            // Run the process
            // PMD CPD sends its output to standard out, so we need to intercept it and write it to a file ourselves
            stdout = new FileOutputStream(pmdCpdXmlFile);
            
            int result = ExternalProcessRunner.runProcess(commandLine, stdout, stderr);

            if (result != 0)
            {
                throw new QAException("PMD CPD returned error code " + result);
            }

            String stderrString = new String(stderr.toByteArray());

            if (FileUtil.hasValue(stderrString))
            {
                QaLogger.getInstance().error(stderrString);
            }
        }
        catch (IOException e)
        {
            String err = new String(stderr.toByteArray());
            throw new QAException("PMD CPD Command [" + commandLine + "] failed : [" + err + "]", e);
        }
        finally
        {
            QaUtil.safeClose(stdout);
            QaUtil.safeClose(stderr);
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
