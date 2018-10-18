package com.github.sanity4j.workflow.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import com.github.sanity4j.util.ExternalProcessRunner;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaUtil;
import com.github.sanity4j.util.StringUtil;
import com.github.sanity4j.util.Tool;

/**
 * PmdCpdRunner - a work unit that runs PMD CPD 5.x.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.1
 */
public class PmdCpd5Runner extends AbstractToolRunner
{
    /**
     * Creates a PmdCpdRunner.
     */
    public PmdCpd5Runner()
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

            // Result code 4 is "DUPLICATE_CODE_FOUND", and is ok.
            if (result != 0 && result != 4)
            {
                throw new QAException("PMD CPD returned error code " + result);
            }

            String stderrString = new String(stderr.toByteArray(), Charset.defaultCharset());

            if (!StringUtil.empty(stderrString))
            {
                QaLogger.getInstance().error(stderrString);
            }
        }
        catch (IOException e)
        {
            String err = new String(stderr.toByteArray(), Charset.defaultCharset());
            throw new QAException("PMD CPD Command [" + commandLine + "] failed : [" + err + "]", e);
        }
        finally
        {
            QaUtil.safeClose(stdout);
            QaUtil.safeClose(stderr);
        }
    }

    /**
     * Subclasses may override this method to add any additional parameters specific to the tool.
     * PMD 5.0 through 5.0.4 require a canonicalized path for the source directory on Windows OS. 
     * See bugs 1068 &amp; 1081.
     * 
     * @return a map of parameters to use for replacing configuration tokens.
     */
    @Override
    protected Map<String, String> getParameterMap()
    {
        Map<String, String> params = super.getParameterMap();
        
        try
        {
            String srcDir = params.get("source");
            params.put("source", new File(srcDir).getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new QAException("Failed to get canonical path for src dir", e);
        }
        
        return params;
    }
    
    /**
     * @return the description of this WorkUnit.
     */
    @Override
    public String getDescription()
    {
        return "Running PMD CPD";
    }
}
