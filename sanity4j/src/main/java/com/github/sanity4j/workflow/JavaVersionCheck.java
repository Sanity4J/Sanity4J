package com.github.sanity4j.workflow; 

import java.io.ByteArrayOutputStream;

import com.github.sanity4j.util.ExternalProcessRunner;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;


/** 
 * Checks the java version that the tools will be run under.
 * The version is checked by running "java -version" and 
 * parsing the output. Currently, 1.3 through 1.5 VMs from
 * various vendors seem to have a common first line:
 *      java version "x.y.z" 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class JavaVersionCheck implements WorkUnit
{
    /** The java runtime to check. */
    private final String javaRuntime;
    
    /**
     * Creates a JavaVersionCheck.
     * @param config the current QA run's configuration.
     */
    public JavaVersionCheck(final QAConfig config)
    {
        this.javaRuntime = config.getJavaRuntime();
    }
    
    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Checking java version";
    }
    
    /**
     * Executes the java runtime to obtain the version number.
     */
    public void run()
    {
        String[] cmdLine = new String[]
        {
            javaRuntime,
            "-version"
        };
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ExternalProcessRunner.runProcess(cmdLine, System.out, baos);

        String[] output = baos.toString().split("[\n\r]");
        String javaVersion = output[0].replaceAll(".*\"(.*)\".*", "$1");

        QaLogger.getInstance().info("Java version: " + javaVersion);
        
        if ("1.5".compareTo(javaVersion) > 0)
        {
            String msg = "The QA tools require java 1.5 or later to Run, current java version is " + javaVersion;
            throw new QAException(msg);
        }
    }
}
