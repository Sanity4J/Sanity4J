package net.sf.sanity4j.util; 

import java.io.OutputStream;


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
    
    /** ExternalProcessRunner should not be instantiated. */
    private ExternalProcessRunner() 
    {        
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
    public static int runProcess(final String[] cmdLine, final OutputStream out, 
            final OutputStream err)
    {    
        StringBuffer cmdLineStr = new StringBuffer();                   
        
        for (int i = 0; i < cmdLine.length; i++)
        {
            if (cmdLine[i].indexOf(' ') == -1)
            {
                cmdLineStr.append(cmdLine[i]);
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
    public static int runProcess(final String cmdLine, final OutputStream out, 
                           final OutputStream err)
    {
        QaLogger.getInstance().info(cmdLine);     
            
        try
        {
            Process process = Runtime.getRuntime().exec(cmdLine);

            // Pipe process output to stdout/stderr
            PipeInputThread stdout = new PipeInputThread(process.getInputStream(), out);
            PipeInputThread stderr = new PipeInputThread(process.getErrorStream(), err);
            
            stdout.start();
            stderr.start();
            
            int result = process.waitFor();
            
            // Allow some more time for all output to be written
            for (int i = 0; i < 10 && (stdout.isRunning() || stderr.isRunning()); i++)
            {
                Thread.sleep(SLEEP_TIME);
            }
            
            return result;
        }
        catch (Exception e)
        {
            throw new QAException("Failed to run external process", e);
        }
    }
}
