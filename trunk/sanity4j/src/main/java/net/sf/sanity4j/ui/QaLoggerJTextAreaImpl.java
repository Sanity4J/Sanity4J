package net.sf.sanity4j.ui; 

import javax.swing.JTextArea;

import net.sf.sanity4j.util.QaLogger;

/** 
 * A QaLogger implementation that logs to a JTextArea. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class QaLoggerJTextAreaImpl extends QaLogger
{
    /** The text area to log to. */
    private final JTextArea textArea;
    
    /** 
     * Creates a QaLoggerJTextAreaImpl.
     * @param textArea the text area to log to. 
     */
    public QaLoggerJTextAreaImpl(final JTextArea textArea)
    {
        this.textArea = textArea;
    }
    
    /** {@inheritDoc} */
    @Override
    public void debug(final String message, final Throwable throwable)
    {
        log("[DEBUG] " + stackTraceToString(message, throwable));
    }

    /** {@inheritDoc} */
    @Override
    public void debug(final String message)
    {
        log("[DEBUG] " + message);
    }

    /** {@inheritDoc} */
    @Override
    public void error(final String message, final Throwable throwable)
    {
        log("[ERROR] " + stackTraceToString(message, throwable));
    }

    /** {@inheritDoc} */
    @Override
    public void error(final String message)
    {
        log("[ERROR] " + message);
    }

    /** {@inheritDoc} */
    @Override
    public void info(final String message)
    {
        log("[INFO] " + message);
    }

    /** {@inheritDoc} */
    @Override
    public void warn(final String message, final Throwable throwable)
    {
        log("[WARN] " + stackTraceToString(message, throwable));
    }

    /** {@inheritDoc} */
    @Override
    public void warn(final String message)
    {
        log("[WARN] " + message);
    }
    
    /**
     * Appends a message to the text area and scrolls to the bottom.
     * @param message the message to append.
     */
    private void log(final String message)
    {
        textArea.append(message + '\n');
        textArea.setCaretPosition(textArea.getText().length());
    }
}
