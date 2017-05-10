package net.sf.sanity4j.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.report.ReportUtil;

import junit.framework.TestCase;

/**
 * TestReportUtil - unit tests for ReportUtil. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class ReportUtil_Test extends TestCase
{
    public void testGetHtmlPathToRoot()
    {
        assertEquals("Incorrect path to root for foo.html", "", ReportUtil.getHtmlPathToRoot("foo.html"));
        assertEquals("Incorrect path to root for bar/foo.html", "../", ReportUtil.getHtmlPathToRoot("bar/foo.html"));
        assertEquals("Incorrect path to root for bar/bar/foo.html", "../../", ReportUtil.getHtmlPathToRoot("bar/bar/foo.html"));
    }
    
    public void testHtmlEscape()
    {
        assertEquals("Incorrect html escape for null string", null, ReportUtil.htmlEscape(null));
        assertEquals("Incorrect html escape for empty string", "", ReportUtil.htmlEscape(""));
        assertEquals("Incorrect html escape for safe string", "abc", ReportUtil.htmlEscape("abc"));
        assertEquals("Incorrect html escape for ampersand", "&amp;", ReportUtil.htmlEscape("&"));
        assertEquals("Incorrect html escape for less than", "&lt;", ReportUtil.htmlEscape("<"));
        assertEquals("Incorrect html escape for greater than", "&gt;", ReportUtil.htmlEscape(">"));
        assertEquals("Incorrect html escape for double quote", "&quot;", ReportUtil.htmlEscape("\""));
        assertEquals("Incorrect html escape for mixed string", 
                     "&lt;hello &amp; world!&gt;", ReportUtil.htmlEscape("<hello & world!>"));
    }
    
    public void testMapDiagnosticsByClassName()
    {
        Map<String, List<Diagnostic>> map = ReportUtil.mapDiagnosticsByClassName(new ArrayList<Diagnostic>(0));
        assertTrue("Map should be empty if there are no diagnostics", map.isEmpty());
        
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
        assertEquals("Map should have two entries", 2, map.size());
        
        List<Diagnostic> diagsForClass1 = map.get(className1);
        List<Diagnostic> diagsForClass2 = map.get(className2);
        
        assertEquals("Incorrect number of diagnostics for class 1", 3, diagsForClass1.size());
        assertEquals("Incorrect number of diagnostics for class 2", 2, diagsForClass2.size());
        
        assertTrue("Map for class one missing diagnostic 1", diagsForClass1.contains(class1Diag1));
        assertTrue("Map for class one missing diagnostic 2", diagsForClass1.contains(class1Diag2));
        assertTrue("Map for class one missing diagnostic 3", diagsForClass1.contains(class1Diag3));
        
        assertTrue("Map for class two missing diagnostic 1", diagsForClass2.contains(class2Diag1));
        assertTrue("Map for class two missing diagnostic 2", diagsForClass2.contains(class2Diag2));
    }
    
    public void testGetRelativeSourcePath()
    {
        assertEquals("Incorrect relative path for src/foo.java", 
                     "foo.xml", ReportUtil.getRelativeSourcePath("src", "src/foo.java"));
        assertEquals("Incorrect path to root for src/bar/foo.java", 
                     "bar/foo.xml", ReportUtil.getRelativeSourcePath("src", "src/bar/foo.java"));
        
        try
        {
            ReportUtil.getRelativeSourcePath("src", "bar/bar/foo.html");
            fail("Should throw an IllegalArgumentException for files outside the source directory");
        }
        catch (IllegalArgumentException expected)
        {            
            assertNotNull("Thrown exception should have a message", expected.getMessage());
        }
    }
}
