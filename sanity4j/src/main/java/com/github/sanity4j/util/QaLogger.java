package com.github.sanity4j.util; 

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Since the QA task can utilise several logging libs, it needs to 
 * provide its own logging facade. The logger defaults to System.out
 * until another logger is initialised.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public abstract class QaLogger
{
    /** The logger instance to use. Defaults to a logger which logs to <code>System.out</code>. */
    private static QaLogger instance = new QaLoggerSystemOutImpl();    
    
    /** 
     * Sets the logger to use. This logger will be the one returned by {@link #getInstance()}.
     * @param logger the logger to set.
     */
    public static void setLogger(final QaLogger logger)
    {
        instance = logger;
    }
    
    /**
     * @return an instance of the logger
     */
    public static QaLogger getInstance()
    {
        return instance;
    }
    
    /**
     * Write a message to the log with the log level of MSG_DEBUG .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public abstract void debug(String message);
    
    /**
     * Write a message to the log with the log level of MSG_DEBUG .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public abstract void debug(String message, Throwable throwable);

    /**
     * Write a message to the log with the log level of MSG_INFO .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public abstract void info(String message);

    /**
     * Write a message to the log with the log level of MSG_WARN .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public abstract void warn(String message);
    
    /**
     * Write a message to the log with the log level of MSG_WARN .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public abstract void warn(String message, Throwable throwable);

    /**
     * Write a message to the log with the log level of MSG_ERR .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public abstract void error(String message);
    
    /**
     * Write a message to the log with the log level of MSG_ERR .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public abstract void error(String message, Throwable throwable);
    
    /**
     * Converts the message and throwable to a format suitable for logging.
     * 
     * @param message the message
     * @param throwable the throwable
     * @return a String suitable for logging
     */
    protected String stackTraceToString(final String message, final Throwable throwable)
    {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));        
        String stackTrace = writer.toString();
        
        StringBuffer msgBuffer = new StringBuffer();        
        msgBuffer.append(message);
        msgBuffer.append('\n');
        msgBuffer.append(throwable.getMessage());
        msgBuffer.append(":\n");
        msgBuffer.append(stackTrace);

        return msgBuffer.toString();
    }
}
