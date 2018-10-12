package com.github.sanity4j.util; 

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/** 
 * ExternalProcessRunner_Test - unit tests for ExternalProcessRunner.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class ExternalProcessRunner_Test
{
    @Test
    public void testRunProcess()
    {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        String javaExecutable = System.getProperty("java.home") 
                              + File.separatorChar + "bin"
                              + File.separatorChar + "java"; 
        
        // Test with a dodgy command-line
        try
        {
            String[] args = new String[]{"ExternalProcessRunner_Test.fail"};           
            ExternalProcessRunner.runProcess(args, stdout, stderr);
            
            Assert.fail("Should have thrown a QAException");
        }
        catch (QAException expected)
        {
            Assert.assertNotNull("Thrown exception should have a message", expected.getMessage());
        }
        
        // Test with response code == 0
        String[] args = new String[]{javaExecutable, "-version"};
        
        int result = ExternalProcessRunner.runProcess(args, stdout, stderr);        
        Assert.assertEquals("Result code should be zero (ok)", 0, result);
        
        // Test with response code != 0
        args = new String[]{javaExecutable, "-ExternalProcessRunner.fail"};
        
        result = ExternalProcessRunner.runProcess(args, stdout, stderr);
        Assert.assertFalse("Result code should be non-zero (failure)", result == 0);        
    }
}
