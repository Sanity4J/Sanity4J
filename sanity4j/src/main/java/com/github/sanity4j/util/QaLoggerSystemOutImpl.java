package com.github.sanity4j.util; 

import java.io.PrintStream;

/**
 * This logger implementation just uses System.out / System.err
 * to print message for running from a command-line, within eclipse etc. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class QaLoggerSystemOutImpl extends QaLogger
{
    /** Hide the constructor - instances must be obtained using getInstance. */
    protected QaLoggerSystemOutImpl()
    {
    	// Hide the constructor - instances must be obtained using getInstance.
    }
    
    /**
     * Write a message to the log with the log level of MSG_DEBUG .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void debug(final String message)
    {
        log(message, System.out);
    }
    
    /**
     * Write a message to the log with the log level of MSG_DEBUG .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void debug(final String message, final Throwable throwable)
    {
        String messageWithStrackTrace = stackTraceToString(message, throwable);        
        log(messageWithStrackTrace, System.out);
    }

    /**
     * Write a message to the log with the log level of MSG_INFO .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void info(final String message)
    {
        log(message, System.out);
    }

    /**
     * Write a message to the log with the log level of MSG_WARN .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void warn(final String message)
    {
        log(message, System.out);
    }
    
    /**
     * Write a message to the log with the log level of MSG_WARN .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void warn(final String message, final Throwable throwable)
    {
        String messageWithStrackTrace = stackTraceToString(message, throwable);        
        log(messageWithStrackTrace, System.out);
    }
    
    /**
     * Write a message to the log with the log level of MSG_ERR .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void error(final String message)
    {
        log(message, System.err);
    }
    
    /**
     * Write a message to the log with the log level of MSG_ERR .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void error(final String message, final Throwable throwable)
    {
        String messageWithStrackTrace = stackTraceToString(message, throwable);        
        log(messageWithStrackTrace, System.err);
    }
    
    /**
     * Logs a message to the underlying output stream.
     * @param message the message to log
     * @param stream the stream to write to
     */
    private void log(final String message, final PrintStream stream)
    {
        stream.println(message);
    }
}
