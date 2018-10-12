package com.github.sanity4j.model.diagnostic; 

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Diagnostic_Test - unit tests for {@link Diagnostic}. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class Diagnostic_Test
{
    @Test
    public void testSetClassName()
    {
        Diagnostic diagnostic = new Diagnostic();
        String packageName = "mydomain.qa";  
        String className = packageName + ".DummyClass";
        
        diagnostic.setClassName(className);
        Assert.assertEquals("className accessor incorrect", className, diagnostic.getClassName());
        Assert.assertEquals("packageName accessor incorrect", packageName, diagnostic.getPackageName());
    }
        
    @Test
    public void testSetStartColumn()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setStartColumn(12345);
        Assert.assertEquals("startColumn accessor incorrect", 12345, diagnostic.getStartColumn());
        Assert.assertEquals("endColumn not equal to startColumn", diagnostic.getStartColumn(), diagnostic.getEndColumn());
        
        diagnostic.setStartColumn(123);
        Assert.assertEquals("startColumn accessor incorrect", 123, diagnostic.getStartColumn());
        Assert.assertEquals("endColumn set incorrectly", 12345, diagnostic.getEndColumn());
    }
    
    @Test
    public void testSetEndColumn()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setEndColumn(12345);
        Assert.assertEquals("endColumn accessor incorrect", 12345, diagnostic.getEndColumn());
    }
    
    @Test
    public void testSetStartLine()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setStartLine(12345);
        Assert.assertEquals("startLine accessor incorrect", 12345, diagnostic.getStartLine());
        Assert.assertEquals("endLine not equal to startLine", diagnostic.getStartLine(), diagnostic.getEndLine());
        
        diagnostic.setStartLine(123);
        Assert.assertEquals("startLine accessor incorrect", 123, diagnostic.getStartLine());
        Assert.assertEquals("endLine set incorrectly", 12345, diagnostic.getEndLine());
    }
    
    @Test
    public void testSetEndLine()
    {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setEndLine(12345);
        Assert.assertEquals("endLine accessor incorrect", 12345, diagnostic.getEndLine());
    }
    
    @Test
    public void testSetFileName()
    {
        Diagnostic diagnostic = new Diagnostic();
        String fileName = "dummy_file.name";
        
        diagnostic.setFileName(fileName);
        Assert.assertEquals("fileName accessor incorrect", fileName, diagnostic.getFileName());
    }
    
    @Test
    public void testSetMessage()
    {
        Diagnostic diagnostic = new Diagnostic();
        String message = "Dummy message";
        
        diagnostic.setMessage(message);
        Assert.assertEquals("message accessor incorrect", message, diagnostic.getMessage());
    }
    
    @Test
    public void testSetSeverity()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setSeverity(Diagnostic.SEVERITY_MODERATE);
        Assert.assertEquals("severity accessor incorrect", Diagnostic.SEVERITY_MODERATE, diagnostic.getSeverity());
    }
    
    @Test
    public void testSetSource()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setSource(Diagnostic.SOURCE_SPOTBUGS);
        Assert.assertEquals("source accessor incorrect", Diagnostic.SOURCE_SPOTBUGS, diagnostic.getSource());
    }
    
    @Test
    public void testSetRuleName()
    {
        Diagnostic diagnostic = new Diagnostic();
        String ruleName = "DummyRuleName";
        
        diagnostic.setRuleName(ruleName);
        Assert.assertEquals("ruleName accessor incorrect", ruleName, diagnostic.getRuleName());
    }
    
    @Test
    public void testGetId()
    {
        Assert.assertNotSame("Diagnostics should have unique Ids",
                   new Diagnostic().getId(), new Diagnostic().getId()); 
    }
    
    @Test
    public void testGetSeverityDescription()
    {
        int[] severities = 
        {
            Diagnostic.SEVERITY_ALL,
            Diagnostic.SEVERITY_INFO,
            Diagnostic.SEVERITY_LOW,
            Diagnostic.SEVERITY_MODERATE,
            Diagnostic.SEVERITY_SIGNIFICANT,
            Diagnostic.SEVERITY_HIGH,
            Integer.MIN_VALUE // Should not result in an error
        };
        
        Diagnostic diag = new Diagnostic();
        Set<String> descriptions = new HashSet<String>(); 
        
        for (int i = 0; i < severities.length; i++)
        {
            diag.setSeverity(severities[i]);
            String description = diag.getSeverityDescription();
            Assert.assertNotNull("Severity description should not be null for severity " + diag.getSeverity(), 
                          description);
            Assert.assertFalse("Severity descriptions should be unique: " + description, 
                        descriptions.contains(description)); 
            descriptions.add(description);
        }
    }
    
    @Test
    public void testGetSourceDescription()
    {
        int[] sources = 
        {
            Diagnostic.SOURCE_ALL,
            Diagnostic.SOURCE_OTHER,
            Diagnostic.SOURCE_SPOTBUGS,
            Diagnostic.SOURCE_PMD,
            Diagnostic.SOURCE_PMD_CPD,
            Diagnostic.SOURCE_CHECKSTYLE,  
        };
        
        Diagnostic diag = new Diagnostic();
        Set<String> descriptions = new HashSet<String>(); 
        
        for (int i = 0; i < sources.length; i++)
        {
            diag.setSource(sources[i]);
            String description = diag.getSourceDescription();
            Assert.assertNotNull("Source description should not be null for source " + diag.getSource(), 
                          description);
            Assert.assertFalse("Source descriptions should be unique: " + description, 
                        descriptions.contains(description)); 
            descriptions.add(description);
        }
        
        diag.setSource(Integer.MIN_VALUE);
        Assert.assertNotNull("Source description should not be null for source " + diag.getSource(), 
                      diag.getSourceDescription());
    }
}
