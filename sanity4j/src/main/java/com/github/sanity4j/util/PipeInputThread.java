package com.github.sanity4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * PipeInputThread - A thread to pipe data between an input and output stream.
 * 
 * This is used to e.g. redirect both standard out and standard 
 * error from a process launched via Runtime.exec(...).
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class PipeInputThread extends Thread
{
    /** The buffer size to use when reading. */
    private static final int BUFFER_SIZE = 4096;

    /** The stream to read from. */
    private final InputStream inStream;
    /** The stream to write to. */
    private final OutputStream outStream;
    /** A flag to indicate whether the pipe is still running. */
    private boolean running = false;

    /**
     * Creates a PipeInputThread.
     * 
     * @param inStream the input stream to read from
     * @param outStream the output stream to write to
     */
    public PipeInputThread(final InputStream inStream, final OutputStream outStream)
    {
        this.inStream = inStream;
        this.outStream = outStream;
    }
	
    /**
     * Runs the PipeInputThread.
     */
    public void run()
    {
        running = true;
        byte[] buf = new byte[BUFFER_SIZE];

        try
        {
            for (int len = inStream.read(buf); len != -1; len = inStream.read(buf))
            {
                outStream.write(buf, 0, len);
            }
        }
        catch (IOException ignored)
        {
            // One of the pipes has been closed unexpectedly,
            // so stop the thread.
        }
        finally
        {
            try
            {
                outStream.flush();
            }
            catch (IOException ignored)
            {
                // don't care 
            }
        }

        running = false;
    }

    /** @return whether the pipe is still running */
    public boolean isRunning()
    {
        return running;
    }
}
