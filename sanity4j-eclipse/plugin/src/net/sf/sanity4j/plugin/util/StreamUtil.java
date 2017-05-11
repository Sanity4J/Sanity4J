package com.github.sanity4j.plugin.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Static utility methods related to working with Streams.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class StreamUtil
{
    /** No instance methods here. */
    private StreamUtil()
    {
    }
    
    /**
     * Default value is 2048.
     */
    public static final int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * Copies information from the input stream to the output stream.
     * 
     * @param in the source stream.
     * @param out the destination stream.
     * @throws IOException if there is an error reading or writing to the streams.
     */
    public static void copy(final InputStream in, final OutputStream out)
        throws IOException
    {
        copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies information from the input stream to the output stream using
     * a specified buffer size.
     * 
     * @param in the source stream.
     * @param out the destination stream.
     * @param bufferSize the buffer size.
     * @throws IOException if there is an error reading or writing to the streams.
     */
    public static void copy(final InputStream in, final OutputStream out, final int bufferSize)
        throws IOException
    {
        final byte[] buf = new byte[bufferSize];
        int bytesRead = in.read(buf);
        
        while (bytesRead != -1)
        {
            out.write(buf, 0, bytesRead);
            bytesRead = in.read(buf);
        }
        
        out.flush();
    }

    /**
     * Returns a byte array containing all the information contained in the
     * given input stream.
     * 
     * @param in the stream to read from.
     * @return the stream contents as a byte array.
     * @throws IOException if there is an error reading from the stream.
     */
    public static byte[] getBytes(final InputStream in) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        copy(in, result);
        result.close();
        return result.toByteArray();
    }
}
