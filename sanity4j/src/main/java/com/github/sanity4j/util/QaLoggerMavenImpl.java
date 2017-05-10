package com.github.sanity4j.util; 

import com.github.sanity4j.maven.plugin.RunQAMojo;

/**
 * This class acts as wrapper for a Maven QA Mojo, so that the bulk 
 * of the classes can avoid a direct dependence on Maven. 
 * 
 * @author Darian Bridge
 * @since Sanity4J 1.0
 */
public class QaLoggerMavenImpl extends QaLogger
{
    /** The current Maven QA mojo, whose project is used for the logging. */
    private final RunQAMojo qaMojo;

    /** 
     * Creates a QaLoggerMavenImpl. The constructor is hidden, as
     * instances must be obtained using getInstance.
     *
     * @param qaMojo the current Maven QA mojo
     */
    public QaLoggerMavenImpl(final RunQAMojo qaMojo)
    {
        this.qaMojo = qaMojo;
    }
    
    /**
     * Write a message to the log with the log level of DEBUG .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void debug(final String message)
    {
        qaMojo.getLog().debug(message);
    }
    
    /**
     * Write a message to the log with the log level of DEBUG .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void debug(final String message, final Throwable throwable)
    {
        //String messageWithStrackTrace = stackTraceToString(message, throwable);        
        qaMojo.getLog().debug(message, throwable);
    }

    /**
     * Write a message to the log with the log level of INFO .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void info(final String message)
    {
        qaMojo.getLog().info(message);
    }

    /**
     * Write a message to the log with the log level of WARN .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void warn(final String message)
    {
        qaMojo.getLog().warn(message);
    }
    
    /**
     * Write a message to the log with the log level of WARN .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void warn(final String message, final Throwable throwable)
    {
        //String messageWithStrackTrace = stackTraceToString(message, throwable);        
        qaMojo.getLog().warn(message, throwable);
    }

    /**
     * Write a message to the log with the log level of ERROR .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void error(final String message)
    {
        qaMojo.getLog().error(message);
    }
    
    /**
     * Write a message to the log with the log level of ERROR .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void error(final String message, final Throwable throwable)
    {
        //String messageWithStrackTrace = stackTraceToString(message, throwable);        
        qaMojo.getLog().error(message, throwable);
    }
}
