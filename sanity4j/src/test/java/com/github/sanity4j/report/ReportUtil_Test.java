package com.github.sanity4j.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.sanity4j.model.diagnostic.Diagnostic;

/**
 * TestReportUtil - unit tests for ReportUtil. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class ReportUtil_Test
{
    @Test
    public void testGetHtmlPathToRoot()
    {
        Assert.assertEquals("Incorrect path to root for foo.html", "", ReportUtil.getHtmlPathToRoot("foo.html"));
        Assert.assertEquals("Incorrect path to root for bar/foo.html", "../", ReportUtil.getHtmlPathToRoot("bar/foo.html"));
        Assert.assertEquals("Incorrect path to root for bar/bar/foo.html", "../../", ReportUtil.getHtmlPathToRoot("bar/bar/foo.html"));
    }
    
    @Test
    public void testHtmlEscape()
    {
        Assert.assertEquals("Incorrect html escape for null string", null, ReportUtil.htmlEscape(null));
        Assert.assertEquals("Incorrect html escape for empty string", "", ReportUtil.htmlEscape(""));
        Assert.assertEquals("Incorrect html escape for safe string", "abc", ReportUtil.htmlEscape("abc"));
        Assert.assertEquals("Incorrect html escape for ampersand", "&amp;", ReportUtil.htmlEscape("&"));
        Assert.assertEquals("Incorrect html escape for less than", "&lt;", ReportUtil.htmlEscape("<"));
        Assert.assertEquals("Incorrect html escape for greater than", "&gt;", ReportUtil.htmlEscape(">"));
        Assert.assertEquals("Incorrect html escape for double quote", "&quot;", ReportUtil.htmlEscape("\""));
        Assert.assertEquals("Incorrect html escape for mixed string", 
                     "&lt;hello &amp; world!&gt;", ReportUtil.htmlEscape("<hello & world!>"));
    }
    
    @Test
    public void testMapDiagnosticsByClassName()
    {
        Map<String, List<Diagnostic>> map = ReportUtil.mapDiagnosticsByClassName(new ArrayList<Diagnostic>(0));
        Assert.assertTrue("Map should be empty if there are no diagnostics", map.isEmpty());
        
        // Add three diagnostics for class 1, and two for class 2
        String className1 = "package.subpackage1.ClassName"; 
        String className2 = "package.subpackage2.ClassName";
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        
        Diagnostic class1Diag1 = new Diagnostic();
        class1Diag1.setClassName(className1);
        diagnostics.add(class1Diag1);
        
        Diagnostic class1Diag2 = new Diagnostic();
        class1Diag2.setClassName(className1);
        diagnostics.add(class1Diag2);
        
        Diagnostic class1Diag3 = new Diagnostic();
        class1Diag3.setClassName(className1);
        diagnostics.add(class1Diag3);

        Diagnostic class2Diag1 = new Diagnostic();
        class2Diag1.setClassName(className2);
        diagnostics.add(class2Diag1);

        Diagnostic class2Diag2 = new Diagnostic();
        class2Diag2.setClassName(className2);
        diagnostics.add(class2Diag2);
        
        map = ReportUtil.mapDiagnosticsByClassName(diagnostics);
        Assert.assertEquals("Map should have two entries", 2, map.size());
        
        List<Diagnostic> diagsForClass1 = map.get(className1);
        List<Diagnostic> diagsForClass2 = map.get(className2);
        
        Assert.assertEquals("Incorrect number of diagnostics for class 1", 3, diagsForClass1.size());
        Assert.assertEquals("Incorrect number of diagnostics for class 2", 2, diagsForClass2.size());
        
        Assert.assertTrue("Map for class one missing diagnostic 1", diagsForClass1.contains(class1Diag1));
        Assert.assertTrue("Map for class one missing diagnostic 2", diagsForClass1.contains(class1Diag2));
        Assert.assertTrue("Map for class one missing diagnostic 3", diagsForClass1.contains(class1Diag3));
        
        Assert.assertTrue("Map for class two missing diagnostic 1", diagsForClass2.contains(class2Diag1));
        Assert.assertTrue("Map for class two missing diagnostic 2", diagsForClass2.contains(class2Diag2));
    }
    
    @Test
    public void testGetRelativeSourcePath()
    {
        Assert.assertEquals("Incorrect relative path for src/foo.java", 
                     "foo.xml", ReportUtil.getRelativeSourcePath("src", "src/foo.java"));
        Assert.assertEquals("Incorrect path to root for src/bar/foo.java", 
                     "bar/foo.xml", ReportUtil.getRelativeSourcePath("src", "src/bar/foo.java"));
        
        try
        {
            ReportUtil.getRelativeSourcePath("src", "bar/bar/foo.html");
            Assert.fail("Should throw an IllegalArgumentException for files outside the source directory");
        }
        catch (IllegalArgumentException expected)
        {            
            Assert.assertNotNull("Thrown exception should have a message", expected.getMessage());
        }
    }
}
