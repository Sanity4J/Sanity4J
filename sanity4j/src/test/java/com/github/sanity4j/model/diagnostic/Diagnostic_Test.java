package com.github.sanity4j.model.diagnostic; 

import java.util.HashSet;
import java.util.Set;

import com.github.sanity4j.model.diagnostic.Diagnostic;

import junit.framework.TestCase;

/**
 * Diagnostic_Test - unit tests for {@link Diagnostic}. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class Diagnostic_Test extends TestCase
{
    public void testSetClassName()
    {
        Diagnostic diagnostic = new Diagnostic();
        String packageName = "mydomain.qa";  
        String className = packageName + ".DummyClass";
        
        diagnostic.setClassName(className);
        assertEquals("className accessor incorrect", className, diagnostic.getClassName());
        assertEquals("packageName accessor incorrect", packageName, diagnostic.getPackageName());
    }
        
    public void testSetStartColumn()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setStartColumn(12345);
        assertEquals("startColumn accessor incorrect", 12345, diagnostic.getStartColumn());
        assertEquals("endColumn not equal to startColumn", diagnostic.getStartColumn(), diagnostic.getEndColumn());
        
        diagnostic.setStartColumn(123);
        assertEquals("startColumn accessor incorrect", 123, diagnostic.getStartColumn());
        assertEquals("endColumn set incorrectly", 12345, diagnostic.getEndColumn());
    }
    
    public void testSetEndColumn()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setEndColumn(12345);
        assertEquals("endColumn accessor incorrect", 12345, diagnostic.getEndColumn());
    }
    
    public void testSetStartLine()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setStartLine(12345);
        assertEquals("startLine accessor incorrect", 12345, diagnostic.getStartLine());
        assertEquals("endLine not equal to startLine", diagnostic.getStartLine(), diagnostic.getEndLine());
        
        diagnostic.setStartLine(123);
        assertEquals("startLine accessor incorrect", 123, diagnostic.getStartLine());
        assertEquals("endLine set incorrectly", 12345, diagnostic.getEndLine());
    }
    
    public void testSetEndLine()
    {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setEndLine(12345);
        assertEquals("endLine accessor incorrect", 12345, diagnostic.getEndLine());
    }
    
    public void testSetFileName()
    {
        Diagnostic diagnostic = new Diagnostic();
        String fileName = "dummy_file.name";
        
        diagnostic.setFileName(fileName);
        assertEquals("fileName accessor incorrect", fileName, diagnostic.getFileName());
    }
    
    public void testSetMessage()
    {
        Diagnostic diagnostic = new Diagnostic();
        String message = "Dummy message";
        
        diagnostic.setMessage(message);
        assertEquals("message accessor incorrect", message, diagnostic.getMessage());
    }
    
    public void testSetSeverity()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setSeverity(Diagnostic.SEVERITY_MODERATE);
        assertEquals("severity accessor incorrect", Diagnostic.SEVERITY_MODERATE, diagnostic.getSeverity());
    }
    
    public void testSetSource()
    {
        Diagnostic diagnostic = new Diagnostic();
        
        diagnostic.setSource(Diagnostic.SOURCE_FINDBUGS);
        assertEquals("source accessor incorrect", Diagnostic.SOURCE_FINDBUGS, diagnostic.getSource());
    }
    
    public void testSetRuleName()
    {
        Diagnostic diagnostic = new Diagnostic();
        String ruleName = "DummyRuleName";
        
        diagnostic.setRuleName(ruleName);
        assertEquals("ruleName accessor incorrect", ruleName, diagnostic.getRuleName());
    }
    
    public void testGetId()
    {
        assertNotSame("Diagnostics should have unique Ids",
                   new Diagnostic().getId(), new Diagnostic().getId()); 
    }
    
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
            assertNotNull("Severity description should not be null for severity " + diag.getSeverity(), 
                          description);
            assertFalse("Severity descriptions should be unique: " + description, 
                        descriptions.contains(description)); 
            descriptions.add(description);
        }
    }
    
    public void testGetSourceDescription()
    {
        int[] sources = 
        {
            Diagnostic.SOURCE_ALL,
            Diagnostic.SOURCE_OTHER,
            Diagnostic.SOURCE_FINDBUGS,
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
            assertNotNull("Source description should not be null for source " + diag.getSource(), 
                          description);
            assertFalse("Source descriptions should be unique: " + description, 
                        descriptions.contains(description)); 
            descriptions.add(description);
        }
        
        diag.setSource(Integer.MIN_VALUE);
        assertNotNull("Source description should not be null for source " + diag.getSource(), 
                      diag.getSourceDescription());
    }
}
