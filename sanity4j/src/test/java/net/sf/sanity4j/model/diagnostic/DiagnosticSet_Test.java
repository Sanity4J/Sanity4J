package net.sf.sanity4j.model.diagnostic; 

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * DiagnosticSet_Test - unit tests for {@link DiagnosticSet}. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class DiagnosticSet_Test extends TestCase
{
	public void testIsEmpty()
    {
        DiagnosticSet diagnosticSet = new DiagnosticSet();
        assertTrue("isEmpty() returned false for empty DiagnosticSet", diagnosticSet.isEmpty());
        
        diagnosticSet.add(new Diagnostic());
        assertFalse("isEmpty() returned true for non-empty DiagnosticSet", diagnosticSet.isEmpty());
        
        diagnosticSet.add(new Diagnostic());
        assertFalse("isEmpty() returned true for non-empty DiagnosticSet", diagnosticSet.isEmpty());
    }
    
	public void testSize()
    {
        DiagnosticSet diagnosticSet = new DiagnosticSet();
        assertEquals("size() returned incorrect size", 0, diagnosticSet.size());
        
        diagnosticSet.add(new Diagnostic());
        assertEquals("size() returned incorrect size", 1, diagnosticSet.size());
        
        diagnosticSet.add(new Diagnostic());
        assertEquals("size() returned incorrect size", 2, diagnosticSet.size());
    }
    
    public void testGetDiagnosticsByFileName()
    {
        String fileName1 = "package/subpackage1/FileName.java"; 
        String fileName2 = "package/subpackage2/FileName.java";
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for file 1, and two for file 2 
        Diagnostic file1Diag1 = new Diagnostic();
        file1Diag1.setFileName(fileName1);
        diagnosticSet.add(file1Diag1);
        
        Diagnostic file1Diag2 = new Diagnostic();
        file1Diag2.setFileName(fileName1);
        diagnosticSet.add(file1Diag2);
        
        Diagnostic file1Diag3 = new Diagnostic();
        file1Diag3.setFileName(fileName1);
        diagnosticSet.add(file1Diag3);

        Diagnostic file2Diag1 = new Diagnostic();
        file2Diag1.setFileName(fileName2);
        diagnosticSet.add(file2Diag1);

        Diagnostic file2Diag2 = new Diagnostic();
        file2Diag2.setFileName(fileName2);
        diagnosticSet.add(file2Diag2);
        
        Map<String, List<Diagnostic>> diagsByName = diagnosticSet.getDiagnosticsByFileName();
        assertEquals("Incorrect number of files", 2, diagsByName.size());
        assertTrue("Diagnostics by file missing fileName1", diagsByName.containsKey(fileName1));
        assertTrue("Diagnostics by file missing fileName2", diagsByName.containsKey(fileName2));

        // Check diags for first file 
        List<Diagnostic> diags1 = diagsByName.get(fileName1);
        assertEquals("Incorrect number of diagnostics for file 1", 3, diags1.size());
        assertTrue("Diagnostics for file 1 missing diagnostic 1", diags1.contains(file1Diag1));
        assertTrue("Diagnostics for file 1 missing diagnostic 2", diags1.contains(file1Diag2));
        assertTrue("Diagnostics for file 1 missing diagnostic 3", diags1.contains(file1Diag3));
        
        // Check diags for 2nd file 
        List<Diagnostic> diags2 = diagsByName.get(fileName2);
        assertEquals("Incorrect number of diagnostics for file 2", 2, diags2.size());
        assertTrue("Diagnostics for file 2 missing diagnostic 1", diags2.contains(file2Diag1));
        assertTrue("Diagnostics for file 2 missing diagnostic 2", diags2.contains(file2Diag2));
    }
    
    public void testGetDiagnosticsByClassName()
    {
        String className1 = "package.subpackage1.ClassName"; 
        String className2 = "package.subpackage2.ClassName";
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for class 1, and two for class 2 
        Diagnostic class1Diag1 = new Diagnostic();
        class1Diag1.setClassName(className1);
        diagnosticSet.add(class1Diag1);
        
        Diagnostic class1Diag2 = new Diagnostic();
        class1Diag2.setClassName(className1);
        diagnosticSet.add(class1Diag2);
        
        Diagnostic class1Diag3 = new Diagnostic();
        class1Diag3.setClassName(className1);
        diagnosticSet.add(class1Diag3);

        Diagnostic class2Diag1 = new Diagnostic();
        class2Diag1.setClassName(className2);
        diagnosticSet.add(class2Diag1);

        Diagnostic class2Diag2 = new Diagnostic();
        class2Diag2.setClassName(className2);
        diagnosticSet.add(class2Diag2);
        
        Map<String, List<Diagnostic>> diagsByName = diagnosticSet.getDiagnosticsByClassName();
        assertEquals("Incorrect number of classes", 2, diagsByName.size());
        assertTrue("Diagnostics by class missing className1", diagsByName.containsKey(className1));
        assertTrue("Diagnostics by class missing className2", diagsByName.containsKey(className2));

        // Check diags for first Class 
        List<Diagnostic> diags1 = diagsByName.get(className1);
        assertEquals("Incorrect number of diagnostics for class 1", 3, diags1.size());
        assertTrue("Diagnostics for class 1 missing diagnostic 1", diags1.contains(class1Diag1));
        assertTrue("Diagnostics for class 1 missing diagnostic 2", diags1.contains(class1Diag2));
        assertTrue("Diagnostics for class 1 missing diagnostic 3", diags1.contains(class1Diag3));
        
        // Check diags for 2nd Class 
        List<Diagnostic> diags2 = diagsByName.get(className2);
        assertEquals("Incorrect number of diagnostics for class 2", 2, diags2.size());
        assertTrue("Diagnostics for class 2 missing diagnostic 1", diags2.contains(class2Diag1));
        assertTrue("Diagnostics for class 2 missing diagnostic 2", diags2.contains(class2Diag2));
    }
    
    public void testGetDiagnosticsByPackageName()
    {
        String topPackage = "package";
        String packageName1 = topPackage + ".subpackage1"; 
        String className1 = packageName1 + ".ClassName"; 
        String packageName2 = topPackage + ".subpackage2"; 
        String className2 = packageName2 + ".ClassName"; 
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for class 1, and two for class 2 
        Diagnostic class1Diag1 = new Diagnostic();
        class1Diag1.setClassName(className1);
        diagnosticSet.add(class1Diag1);
        
        Diagnostic class1Diag2 = new Diagnostic();
        class1Diag2.setClassName(className1);
        diagnosticSet.add(class1Diag2);
        
        Diagnostic class1Diag3 = new Diagnostic();
        class1Diag3.setClassName(className1);
        diagnosticSet.add(class1Diag3);

        Diagnostic class2Diag1 = new Diagnostic();
        class2Diag1.setClassName(className2);
        diagnosticSet.add(class2Diag1);

        Diagnostic class2Diag2 = new Diagnostic();
        class2Diag2.setClassName(className2);
        diagnosticSet.add(class2Diag2);
        
        Map<String, List<Diagnostic>> diagsByName = diagnosticSet.getDiagnosticsByPackageName();
        assertEquals("Incorrect number of packages", 3, diagsByName.size());
        assertTrue("Diagnostics by package missing top package", diagsByName.containsKey(topPackage));
        assertTrue("Diagnostics by package missing package 1", diagsByName.containsKey(packageName1));
        assertTrue("Diagnostics by package missing package 2", diagsByName.containsKey(packageName2));

        // Check diags for first package 
        List<Diagnostic> diags1 = diagsByName.get(packageName1);
        assertEquals("Incorrect number of diagnostics for package 1", 3, diags1.size());
        assertTrue("Diagnostics for package 1 missing diagnostic 1", diags1.contains(class1Diag1));
        assertTrue("Diagnostics for package 1 missing diagnostic 2", diags1.contains(class1Diag2));
        assertTrue("Diagnostics for package 1 missing diagnostic 3", diags1.contains(class1Diag3));
        
        // Check diags for 2nd package 
        List<Diagnostic> diags2 = diagsByName.get(packageName2);
        assertEquals("Incorrect number of diagnostics for package 2", 2, diags2.size());
        assertTrue("Diagnostics for package 1 missing diagnostic 1", diags2.contains(class2Diag1));
        assertTrue("Diagnostics for package 2 missing diagnostic 2", diags2.contains(class2Diag2));
        
        // Check that diags for top package contains all diags
        List<Diagnostic> topDiags = diagsByName.get(topPackage);
        assertEquals("Incorrect number of diagnostics for top package", 5, topDiags.size());
        assertTrue("Diagnostics for top package missing diagnostic 1-1", topDiags.contains(class1Diag1));
        assertTrue("Diagnostics for top package missing diagnostic 1-2", topDiags.contains(class1Diag2));
        assertTrue("Diagnostics for top package missing diagnostic 1-3", topDiags.contains(class1Diag3));
        assertTrue("Diagnostics for top package missing diagnostic 2-1", topDiags.contains(class2Diag1));
        assertTrue("Diagnostics for top package missing diagnostic 2-2", topDiags.contains(class2Diag2));
    }
    
    public void testGetDiagnosticsBySeverity()
    {
        int severity1 = Diagnostic.SEVERITY_MODERATE; 
        int severity2 = Diagnostic.SEVERITY_HIGH;
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for file 1, and two for file 2 
        Diagnostic severity1Diag1 = new Diagnostic();
        severity1Diag1.setSeverity(severity1);
        diagnosticSet.add(severity1Diag1);
        
        Diagnostic severity1Diag2 = new Diagnostic();
        severity1Diag2.setSeverity(severity1);
        diagnosticSet.add(severity1Diag2);
        
        Diagnostic severity1Diag3 = new Diagnostic();
        severity1Diag3.setSeverity(severity1);
        diagnosticSet.add(severity1Diag3);

        Diagnostic severity2Diag1 = new Diagnostic();
        severity2Diag1.setSeverity(severity2);
        diagnosticSet.add(severity2Diag1);

        Diagnostic severity2Diag2 = new Diagnostic();
        severity2Diag2.setSeverity(severity2);
        diagnosticSet.add(severity2Diag2);
        
        Map<String, List<Diagnostic>> diagsBySeverity = diagnosticSet.getDiagnosticsBySeverity();
        assertEquals("Incorrect number of severities", 2, diagsBySeverity.size());
        assertTrue("Diagnostics by severity missing severity 1", diagsBySeverity.containsKey(String.valueOf(severity1)));
        assertTrue("Diagnostics by severity missing severity 2", diagsBySeverity.containsKey(String.valueOf(severity2)));

        // Check diags for first file 
        List<Diagnostic> diags1 = diagsBySeverity.get(String.valueOf(severity1));
        assertEquals("Incorrect number of diagnostics for severity 1", 3, diags1.size());
        assertTrue("Diagnostics for severity 1 missing diagnostic 1", diags1.contains(severity1Diag1));
        assertTrue("Diagnostics for severity 1 missing diagnostic 2", diags1.contains(severity1Diag2));
        assertTrue("Diagnostics for severity 1 missing diagnostic 3", diags1.contains(severity1Diag3));
        
        // Check diags for 2nd file 
        List<Diagnostic> diags2 = diagsBySeverity.get(String.valueOf(severity2));
        assertEquals("Incorrect number of diagnostics for severity 2", 2, diags2.size());
        assertTrue("Diagnostics for severity 2 missing diagnostic 1", diags2.contains(severity2Diag1));
        assertTrue("Diagnostics for severity 2 missing diagnostic 2", diags2.contains(severity2Diag2));
    }
    
    public void testGetDiagnosticsByTool()
    {
        int tool1 = Diagnostic.SOURCE_PMD; 
        int tool2 = Diagnostic.SOURCE_FINDBUGS;
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for tool 1, and two for tool 2 
        Diagnostic tool1Diag1 = new Diagnostic();
        tool1Diag1.setSource(tool1);
        diagnosticSet.add(tool1Diag1);
        
        Diagnostic tool1Diag2 = new Diagnostic();
        tool1Diag2.setSource(tool1);
        diagnosticSet.add(tool1Diag2);
        
        Diagnostic tool1Diag3 = new Diagnostic();
        tool1Diag3.setSource(tool1);
        diagnosticSet.add(tool1Diag3);

        Diagnostic tool2Diag1 = new Diagnostic();
        tool2Diag1.setSource(tool2);
        diagnosticSet.add(tool2Diag1);

        Diagnostic tool2Diag2 = new Diagnostic();
        tool2Diag2.setSource(tool2);
        diagnosticSet.add(tool2Diag2);
        
        Map<String, List<Diagnostic>> diagsByTool = diagnosticSet.getDiagnosticsByTool();
        assertEquals("Incorrect number of tools", 2, diagsByTool.size());
        assertTrue("Diagnostics by tool missing tool 1", diagsByTool.containsKey(String.valueOf(tool1)));
        assertTrue("Diagnostics by tool missing tool 2", diagsByTool.containsKey(String.valueOf(tool2)));

        // Check diags for first file 
        List<Diagnostic> diags1 = diagsByTool.get(String.valueOf(tool1));
        assertEquals("Incorrect number of diagnostics for tool 1", 3, diags1.size());
        assertTrue("Diagnostics for tool 1 missing diagnostic 1", diags1.contains(tool1Diag1));
        assertTrue("Diagnostics for tool 1 missing diagnostic 2", diags1.contains(tool1Diag2));
        assertTrue("Diagnostics for tool 1 missing diagnostic 3", diags1.contains(tool1Diag3));
        
        // Check diags for 2nd file 
        List<Diagnostic> diags2 = diagsByTool.get(String.valueOf(tool2));
        assertEquals("Incorrect number of diagnostics for tool 2", 2, diags2.size());
        assertTrue("Diagnostics for tool 2 missing diagnostic 1", diags2.contains(tool2Diag1));
        assertTrue("Diagnostics for tool 2 missing diagnostic 2", diags2.contains(tool2Diag2));
    }
    
    public void testGetDiagnosticsForTool()
    {
        int tool1 = Diagnostic.SOURCE_CHECKSTYLE; 
        int tool2 = Diagnostic.SOURCE_PMD_CPD;
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for tool 1, and two for tool 2 
        Diagnostic tool1Diag1 = new Diagnostic();
        tool1Diag1.setSource(tool1);
        diagnosticSet.add(tool1Diag1);
        
        Diagnostic tool1Diag2 = new Diagnostic();
        tool1Diag2.setSource(tool1);
        diagnosticSet.add(tool1Diag2);
        
        Diagnostic tool1Diag3 = new Diagnostic();
        tool1Diag3.setSource(tool1);
        diagnosticSet.add(tool1Diag3);

        Diagnostic tool2Diag1 = new Diagnostic();
        tool2Diag1.setSource(tool2);
        diagnosticSet.add(tool2Diag1);

        Diagnostic tool2Diag2 = new Diagnostic();
        tool2Diag2.setSource(tool2);
        diagnosticSet.add(tool2Diag2);

        // Check diagnostics for tool 1
        DiagnosticSet diagnosticsForTool = diagnosticSet.getDiagnosticsForTool(tool1);
        assertEquals("Incorrect number of diagnostics for tool 1", 3, diagnosticsForTool.size());
        
        List<Diagnostic> diagsFromIter = new ArrayList<Diagnostic>(3);
        
        for (Iterator<Diagnostic> i = diagnosticsForTool.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for tool 1", 3, diagsFromIter.size());
        assertTrue("Diagnostics for tool 1 missing diagnostic 1", diagsFromIter.contains(tool1Diag1));
        assertTrue("Diagnostics for tool 1 missing diagnostic 2", diagsFromIter.contains(tool1Diag2));
        assertTrue("Diagnostics for tool 1 missing diagnostic 3", diagsFromIter.contains(tool1Diag3));
        
        // Check diagnostics for tool 2
        diagnosticsForTool = diagnosticSet.getDiagnosticsForTool(tool2);
        assertEquals("Incorrect number of diagnostics for tool 2", 2, diagnosticsForTool.size());
        
        diagsFromIter = new ArrayList<Diagnostic>(2);
        
        for (Iterator<Diagnostic> i = diagnosticsForTool.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for tool 2", 2, diagsFromIter.size());
        assertTrue("Diagnostics for tool 2 missing diagnostic 1", diagsFromIter.contains(tool2Diag1));
        assertTrue("Diagnostics for tool 2 missing diagnostic 2", diagsFromIter.contains(tool2Diag2));
        
        // Check diagnostics for all tools        
        diagnosticsForTool = diagnosticSet.getDiagnosticsForTool(Diagnostic.SOURCE_ALL);
        assertEquals("Incorrect number of diagnostics for all tools", 5, diagnosticsForTool.size());
        
        diagsFromIter = new ArrayList<Diagnostic>(5);
        
        for (Iterator<Diagnostic> i = diagnosticsForTool.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for all tools", 5, diagsFromIter.size());
        assertTrue("Diagnostics for all tools missing diagnostic 1-1", diagsFromIter.contains(tool1Diag1));
        assertTrue("Diagnostics for all tools missing diagnostic 1-2", diagsFromIter.contains(tool1Diag2));
        assertTrue("Diagnostics for all tools missing diagnostic 1-3", diagsFromIter.contains(tool1Diag3));
        assertTrue("Diagnostics for all tools missing diagnostic 2-1", diagsFromIter.contains(tool2Diag1));
        assertTrue("Diagnostics for all tools missing diagnostic 2-2", diagsFromIter.contains(tool2Diag2));
    }
    
    public void testGetDiagnosticsForSeverity()
    {
        int severity1 = Diagnostic.SEVERITY_MODERATE; 
        int severity2 = Diagnostic.SEVERITY_HIGH;
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for severity 1, and two for severity 2 
        Diagnostic severity1Diag1 = new Diagnostic();
        severity1Diag1.setSeverity(severity1);
        diagnosticSet.add(severity1Diag1);
        
        Diagnostic severity1Diag2 = new Diagnostic();
        severity1Diag2.setSeverity(severity1);
        diagnosticSet.add(severity1Diag2);
        
        Diagnostic severity1Diag3 = new Diagnostic();
        severity1Diag3.setSeverity(severity1);
        diagnosticSet.add(severity1Diag3);

        Diagnostic severity2Diag1 = new Diagnostic();
        severity2Diag1.setSeverity(severity2);
        diagnosticSet.add(severity2Diag1);

        Diagnostic severity2Diag2 = new Diagnostic();
        severity2Diag2.setSeverity(severity2);
        diagnosticSet.add(severity2Diag2);

        // Check diagnostics for severity 1
        DiagnosticSet diagnosticsForSev = diagnosticSet.getDiagnosticsForSeverity(severity1);
        assertEquals("Incorrect number of diagnostics for severity 1", 3, diagnosticsForSev.size());
        
        List<Diagnostic> diagsFromIter = new ArrayList<Diagnostic>(3);
        
        for (Iterator<Diagnostic> i = diagnosticsForSev.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for severity 1", 3, diagsFromIter.size());
        assertTrue("Diagnostics for severity 1 missing diagnostic 1", diagsFromIter.contains(severity1Diag1));
        assertTrue("Diagnostics for severity 1 missing diagnostic 2", diagsFromIter.contains(severity1Diag2));
        assertTrue("Diagnostics for severity 1 missing diagnostic 3", diagsFromIter.contains(severity1Diag3));
        
        // Check diagnostics for severity 2
        diagnosticsForSev = diagnosticSet.getDiagnosticsForSeverity(severity2);
        assertEquals("Incorrect number of diagnostics for severity 2", 2, diagnosticsForSev.size());
        
        diagsFromIter = new ArrayList<Diagnostic>(2);
        
        for (Iterator<Diagnostic> i = diagnosticsForSev.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for severity 2", 2, diagsFromIter.size());
        assertTrue("Diagnostics for severity 2 missing diagnostic 1", diagsFromIter.contains(severity2Diag1));
        assertTrue("Diagnostics for severity 2 missing diagnostic 2", diagsFromIter.contains(severity2Diag2));
        
        // Check diagnostics for all severities        
        diagnosticsForSev = diagnosticSet.getDiagnosticsForSeverity(Diagnostic.SEVERITY_ALL);
        assertEquals("Incorrect number of diagnostics for all severities", 5, diagnosticsForSev.size());
        
        diagsFromIter = new ArrayList<Diagnostic>(5);
        
        for (Iterator<Diagnostic> i = diagnosticsForSev.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for all severities", 5, diagsFromIter.size());
        assertTrue("Diagnostics for all severities missing diagnostic 1-1", diagsFromIter.contains(severity1Diag1));
        assertTrue("Diagnostics for all severities missing diagnostic 1-2", diagsFromIter.contains(severity1Diag2));
        assertTrue("Diagnostics for all severities missing diagnostic 1-3", diagsFromIter.contains(severity1Diag3));
        assertTrue("Diagnostics for all severities missing diagnostic 2-1", diagsFromIter.contains(severity2Diag1));
        assertTrue("Diagnostics for all severities missing diagnostic 2-2", diagsFromIter.contains(severity2Diag2));
    }
    
    public void testGetDiagnosticsForPackage()
    {
        String topPackage = "package";
        String packageName1 = topPackage + ".subpackage1"; 
        String className1 = packageName1 + ".ClassName"; 
        String packageName2 = topPackage + ".subpackage2"; 
        String className2 = packageName2 + ".ClassName"; 
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for class 1, and two for class 2 
        Diagnostic class1Diag1 = new Diagnostic();
        class1Diag1.setClassName(className1);
        diagnosticSet.add(class1Diag1);
        
        Diagnostic class1Diag2 = new Diagnostic();
        class1Diag2.setClassName(className1);
        diagnosticSet.add(class1Diag2);
        
        Diagnostic class1Diag3 = new Diagnostic();
        class1Diag3.setClassName(className1);
        diagnosticSet.add(class1Diag3);

        Diagnostic class2Diag1 = new Diagnostic();
        class2Diag1.setClassName(className2);
        diagnosticSet.add(class2Diag1);

        Diagnostic class2Diag2 = new Diagnostic();
        class2Diag2.setClassName(className2);
        diagnosticSet.add(class2Diag2);
        
        // Check diagnostics for package 1
        DiagnosticSet diagnosticsForPackage = diagnosticSet.getDiagnosticsForPackage(packageName1);
        assertEquals("Incorrect number of diagnostics for package 1", 3, diagnosticsForPackage.size());
        
        List<Diagnostic> diagsFromIter = new ArrayList<Diagnostic>(3);
        
        for (Iterator<Diagnostic> i = diagnosticsForPackage.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for package 1", 3, diagsFromIter.size());
        assertTrue("Diagnostics for package 1 missing diagnostic 1", diagsFromIter.contains(class1Diag1));
        assertTrue("Diagnostics for package 1 missing diagnostic 2", diagsFromIter.contains(class1Diag2));
        assertTrue("Diagnostics for package 1 missing diagnostic 3", diagsFromIter.contains(class1Diag3));
        
        // Check diagnostics for package 2
        diagnosticsForPackage = diagnosticSet.getDiagnosticsForPackage(packageName2);
        assertEquals("Incorrect number of diagnostics for package 2", 2, diagnosticsForPackage.size());
        
        diagsFromIter = new ArrayList<Diagnostic>(2);
        
        for (Iterator<Diagnostic> i = diagnosticsForPackage.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for package 2", 2, diagsFromIter.size());
        assertTrue("Diagnostics for package 2 missing diagnostic 1", diagsFromIter.contains(class2Diag1));
        assertTrue("Diagnostics for package 2 missing diagnostic 2", diagsFromIter.contains(class2Diag2));
        
        // Check diagnostics for top package - subpackages excluded
        diagnosticsForPackage = diagnosticSet.getDiagnosticsForPackage(topPackage, false);
        assertTrue("Top package should not have any diagnostics", diagnosticsForPackage.isEmpty());
        
        // Check diagnostics for top package - subpackages included
        diagnosticsForPackage = diagnosticSet.getDiagnosticsForPackage(topPackage, true);
        assertEquals("Incorrect number of diagnostics for all packages", 5, diagnosticsForPackage.size());
        
        diagsFromIter = new ArrayList<Diagnostic>(2);
        
        for (Iterator<Diagnostic> i = diagnosticsForPackage.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for top package", 5, diagsFromIter.size());
        assertTrue("Diagnostics for top package missing diagnostic 1-1", diagsFromIter.contains(class1Diag1));
        assertTrue("Diagnostics for top package missing diagnostic 1-2", diagsFromIter.contains(class1Diag2));
        assertTrue("Diagnostics for top package missing diagnostic 1-3", diagsFromIter.contains(class1Diag3));
        assertTrue("Diagnostics for top package missing diagnostic 2-1", diagsFromIter.contains(class2Diag1));
        assertTrue("Diagnostics for top package missing diagnostic 2-2", diagsFromIter.contains(class2Diag2));
    }
    
    public void testGetDiagnosticsForFile()
    {
        String fileName1 = "package/subpackage1/FileName.java"; 
        String fileName2 = "package/subpackage2/FileName.java";
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();

        // Add three diagnostics for file 1, and two for file 2 
        Diagnostic file1Diag1 = new Diagnostic();
        file1Diag1.setFileName(fileName1);
        diagnosticSet.add(file1Diag1);
        
        Diagnostic file1Diag2 = new Diagnostic();
        file1Diag2.setFileName(fileName1);
        diagnosticSet.add(file1Diag2);
        
        Diagnostic file1Diag3 = new Diagnostic();
        file1Diag3.setFileName(fileName1);
        diagnosticSet.add(file1Diag3);

        Diagnostic file2Diag1 = new Diagnostic();
        file2Diag1.setFileName(fileName2);
        diagnosticSet.add(file2Diag1);

        Diagnostic file2Diag2 = new Diagnostic();
        file2Diag2.setFileName(fileName2);
        diagnosticSet.add(file2Diag2);
        
        // Check diagnostics for file 1
        DiagnosticSet diagnosticsForFile = diagnosticSet.getDiagnosticsForFile(fileName1);
        assertEquals("Incorrect number of diagnostics for file 1", 3, diagnosticsForFile.size());
        
        List<Diagnostic> diagsFromIter = new ArrayList<Diagnostic>(3);
        
        for (Iterator<Diagnostic> i = diagnosticsForFile.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for file 1", 3, diagsFromIter.size());
        assertTrue("Diagnostics for file 1 missing diagnostic 1", diagsFromIter.contains(file1Diag1));
        assertTrue("Diagnostics for file 1 missing diagnostic 2", diagsFromIter.contains(file1Diag2));
        assertTrue("Diagnostics for file 1 missing diagnostic 3", diagsFromIter.contains(file1Diag3));
        
        // Check diagnostics for file 2
        diagnosticsForFile = diagnosticSet.getDiagnosticsForFile(fileName2);
        assertEquals("Incorrect number of diagnostics for file 2", 2, diagnosticsForFile.size());
        
        diagsFromIter = new ArrayList<Diagnostic>(2);
        
        for (Iterator<Diagnostic> i = diagnosticsForFile.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Incorrect number of diagnostics for file 2", 2, diagsFromIter.size());
        assertTrue("Diagnostics for file 2 missing diagnostic 1", diagsFromIter.contains(file2Diag1));
        assertTrue("Diagnostics for file 2 missing diagnostic 2", diagsFromIter.contains(file2Diag2));
    }
    
    public void testGetCountForSeverity()
    {
        int severity1 = Diagnostic.SEVERITY_MODERATE; 
        int severity2 = Diagnostic.SEVERITY_HIGH;
        
        DiagnosticSet diagnosticSet = new DiagnosticSet();
        assertEquals("Count for severity returned non-zero for empty DiagnosticSet", 0, diagnosticSet.getCountForSeverity(Diagnostic.SEVERITY_ALL));

        // Add three diagnostics for severity 1, and two for severity 2 
        Diagnostic severity1Diag1 = new Diagnostic();
        severity1Diag1.setSeverity(severity1);
        diagnosticSet.add(severity1Diag1);
        
        Diagnostic severity1Diag2 = new Diagnostic();
        severity1Diag2.setSeverity(severity1);
        diagnosticSet.add(severity1Diag2);
        
        Diagnostic severity1Diag3 = new Diagnostic();
        severity1Diag3.setSeverity(severity1);
        diagnosticSet.add(severity1Diag3);

        Diagnostic severity2Diag1 = new Diagnostic();
        severity2Diag1.setSeverity(severity2);
        diagnosticSet.add(severity2Diag1);

        Diagnostic severity2Diag2 = new Diagnostic();
        severity2Diag2.setSeverity(severity2);
        diagnosticSet.add(severity2Diag2);

        assertEquals("Count for severity incorrect for severity 1", 3, diagnosticSet.getCountForSeverity(severity1));
        assertEquals("Count for severity incorrect for severity 2", 2, diagnosticSet.getCountForSeverity(severity2));
        assertEquals("Count for severity incorrect for all severities", 5, diagnosticSet.getCountForSeverity(Diagnostic.SEVERITY_ALL));
    }
    
    public void testIterator()
    {
        // Test that iterator returns all diagnostics
        Diagnostic diagnostic1 = new Diagnostic();
        Diagnostic diagnostic2 = new Diagnostic();
        Diagnostic diagnostic3 = new Diagnostic();
        DiagnosticSet diagnosticSet = new DiagnosticSet();
        
        diagnosticSet.add(diagnostic1);
        diagnosticSet.add(diagnostic2);
        diagnosticSet.add(diagnostic3);
        
        List<Diagnostic> diagsFromIter = new ArrayList<Diagnostic>(3);
        
        for (Iterator<Diagnostic> i = diagnosticSet.iterator(); i.hasNext();)
        {
            diagsFromIter.add(i.next());
        }
        
        assertEquals("Iterator returned incorrect number of elements", 3, diagsFromIter.size());
        assertTrue("Iterator missed diagnostic 1", diagsFromIter.contains(diagnostic1));
        assertTrue("Iterator missed diagnostic 2", diagsFromIter.contains(diagnostic2));
        assertTrue("Iterator missed diagnostic 3", diagsFromIter.contains(diagnostic3));
        
        // Test that iterator doesn't support remove
        try
        {
            Iterator<Diagnostic> iterator = diagnosticSet.iterator();
            iterator.next();
            iterator.remove();
            
            fail("Should have thrown an UnsupportedOperationException");
        }
        catch (UnsupportedOperationException expected)
        {
            assertEquals("Diagnostic should not have been removed", 3, diagnosticSet.size());
        }
        
        // Test that the iterator throws the correct exception 
        // when it runs out of Diagnostics
        Iterator<Diagnostic> iterator = diagnosticSet.iterator();
        iterator.next();
        iterator.next();
        iterator.next();

        try
        {
            iterator.next();
            fail("Should have thrown a NoSuchElementException");
        }
        catch (NoSuchElementException ex)
        {
            assertFalse("hasNext should still return false", iterator.hasNext());
        }
    }
}
