package com.github.sanity4j.util; 

import java.io.OutputStream;

import org.apache.commons.lang.SystemUtils;

/**
 * Runs an external process.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public final class ExternalProcessRunner
{
    /** Sleep time while waiting for piping of process output streams to complete. */ 
    private static final int SLEEP_TIME = 500; // 0.5 seconds
    
    /** Number of times to sleep. */
    private static final int SLEEP_COUNT = 10;
    
    /** ExternalProcessRunner should not be instantiated. */
    private ExternalProcessRunner() 
    {        
        // ExternalProcessRunner should not be instantiated.
    }

    /**
     * Runs a process using the given command line,
     * piping stdout/stderr to out/err.
     * 
     * @param cmdLine the command line to run
     * @param out where to pipe System.out to
     * @param err where to pipe System.err to
     * 
     * @return the return code of the process
     */
    public static int runProcess(final String[] cmdLine, 
        final OutputStream out, final OutputStream err)
    {    
        StringBuffer cmdLineStr = new StringBuffer();                   
        
        for (int i = 0; i < cmdLine.length; i++)
        {
            if (cmdLine[i].indexOf(' ') == -1)
            {
                cmdLineStr.append(cmdLine[i]);
            }
            else if (SystemUtils.IS_OS_UNIX)
            {
                // escape any spaces.
                cmdLineStr.append(cmdLine[i].replace(" ", "\\ "));
            }
            else
            {
                cmdLineStr.append('"');
                cmdLineStr.append(cmdLine[i]);
                cmdLineStr.append('"');
            }
            
            cmdLineStr.append(' ');
        }
        
        return runProcess(cmdLineStr.toString(), out, err);
    }
    
    /**
     * Runs a process using the given command line,
     * piping stdout/stderr to out/err.
     * 
     * @param cmdLine the command line to run
     * @param out where to pipe System.out to
     * @param err where to pipe System.err to
     * 
     * @return the return code of the process
     */
    public static int runProcess(final String cmdLine, 
        final OutputStream out, final OutputStream err)
    {
        QaLogger.getInstance().debug(cmdLine);     
            
        try
        {
            // http://stackoverflow.com/questions/5946471/splitting-at-space-if-not-between-quotes
            String[] cmdArray = cmdLine.split("[ ]+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

            StringBuffer cmdBuf = new StringBuffer();
            for (int index = 0; index < cmdArray.length; index++)
            {
                if (SystemUtils.IS_OS_UNIX)
                {
                    // remove quotes.
                    String cmdItem = cmdArray[index].replace("\"", "");
                    cmdArray[index] = cmdItem; 
                    
                    if (cmdArray[index].contains(" "))
                    {
                        // escape any spaces.
                        cmdItem = cmdArray[index].replace(" ", "\\ ");
                        cmdArray[index] = cmdItem;
                    }
                }
                else
                {
                    if (!cmdArray[index].startsWith("\"")
                        && !cmdArray[index].endsWith("\"")
                        && cmdArray[index].contains(" "))
                    {
                        // add quotes if none already, and only if the param contains a space.
                        String cmdItem = '"' + cmdArray[index] + '"';
                        cmdArray[index] = cmdItem;
                    }
                }
                
                cmdBuf.append(cmdArray[index]).append(' ');
                
                QaLogger.getInstance().debug("cmdArg[" + index + "]: " + cmdArray[index]);
            }
            QaLogger.getInstance().info(cmdBuf.toString());     
            
            Process process = Runtime.getRuntime().exec(cmdArray);

            // Pipe process output to stdout/stderr
            PipeInputThread stdout = new PipeInputThread(process.getInputStream(), out);
            PipeInputThread stderr = new PipeInputThread(process.getErrorStream(), err);
            
            stdout.start();
            stderr.start();
            
            int result = process.waitFor();
            
            // Allow some more time for all output to be written
            for (int i = 0; i < SLEEP_COUNT && (stdout.isRunning() || stderr.isRunning()); i++)
            {
                Thread.sleep(SLEEP_TIME);
            }
            
            return result;
        }
        catch (Exception ex)
        {
            throw new QAException("Failed to run external process", ex);
        }
    }
}
