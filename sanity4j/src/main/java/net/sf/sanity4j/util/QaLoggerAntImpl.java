package net.sf.sanity4j.util; 

import net.sf.sanity4j.ant.taskdef.RunQA;

import org.apache.tools.ant.Project;

/**
 * This class acts as wrapper for an Ant Project, so that the bulk 
 * of the classes can avoid a direct dependence on Ant. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class QaLoggerAntImpl extends QaLogger
{
    /** The current Ant QA task, whose project is used for the logging. */
    private final RunQA qaTask;
    
    /** 
     * Creates a QaLoggerAntImpl. The constructor is hidden, as
     * instances must be obtained using getInstance.
     *
     * @param qaTask the current Ant QA task
     */
    public QaLoggerAntImpl(final RunQA qaTask)
    {
        this.qaTask = qaTask;
    }
    
    /**
     * Write a message to the log with the log level of MSG_DEBUG .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void debug(final String message)
    {
        qaTask.getProject().log(qaTask, message, Project.MSG_DEBUG);
    }
    
    /**
     * Write a message to the log with the log level of MSG_DEBUG .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void debug(final String message, final Throwable throwable)
    {
        String messageWithStrackTrace = stackTraceToString(message, throwable);        
        qaTask.getProject().log(qaTask, messageWithStrackTrace, Project.MSG_DEBUG);
    }

    /**
     * Write a message to the log with the log level of MSG_INFO .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void info(final String message)
    {
        qaTask.getProject().log(qaTask, message, Project.MSG_INFO);
    }

    /**
     * Write a message to the log with the log level of MSG_WARN .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void warn(final String message)
    {
        qaTask.getProject().log(qaTask, message, Project.MSG_WARN);
    }
    
    /**
     * Write a message to the log with the log level of MSG_WARN .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void warn(final String message, final Throwable throwable)
    {
        String messageWithStrackTrace = stackTraceToString(message, throwable);        
        qaTask.getProject().log(qaTask, messageWithStrackTrace, Project.MSG_WARN);
    }

    /**
     * Write a message to the log with the log level of MSG_ERR .
     * @param message The text to log. Should not be <code>null</code>.
     */
    public void error(final String message)
    {
        qaTask.getProject().log(qaTask, message, Project.MSG_ERR);
    }
    
    /**
     * Write a message to the log with the log level of MSG_ERR .
     * @param message The text to log. Should not be <code>null</code>.
     * @param throwable the throwable, presumably caught in a catch block.
     */
    public void error(final String message, final Throwable throwable)
    {
        String messageWithStrackTrace = stackTraceToString(message, throwable);        
        qaTask.getProject().log(qaTask, messageWithStrackTrace, Project.MSG_ERR);
    }
}
