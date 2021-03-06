package com.github.sanity4j.util; 

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/** 
 * PipeInputStream_Test - unit test for PipeInputStream.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class PipeInputThread_Test
{
    /** Should complete in far less than 1 second. */
    private static final int SLEEP_TIME = 100;
    
    /** Retry count. */
    private static final int RETRY_COUNT = 10;

    @Test
    public void testPipeInput()
    {
        // Test with a fair amount of data that would 
        // just overflow most typical buffer lengths 
        byte[] dataIn = new byte[65536 + 1];
        new Random().nextBytes(dataIn);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(dataIn);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        PipeInputThread pipe = new PipeInputThread(bais, baos);
        pipe.start();
        
        // Should complete in far less than a second...
        for (int count = 1; count < RETRY_COUNT; count++)
        {
            try
            {
                Thread.sleep(SLEEP_TIME);
                
                if (!pipe.isRunning())
                {
                    break;
                }
            }
            catch (InterruptedException e)
            {
                Assert.fail("Interrupted");
            }
        }
        
        byte[] dataOut = baos.toByteArray();
        
        Assert.assertEquals("Incorrect amount of data read", dataIn.length, dataOut.length);
        Assert.assertTrue("Incorrect data read", Arrays.equals(dataIn, dataOut));
    }
}
