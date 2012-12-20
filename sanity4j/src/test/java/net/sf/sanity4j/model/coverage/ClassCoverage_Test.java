package net.sf.sanity4j.model.coverage; 

import junit.framework.TestCase;

/** 
 * ClassCoverage_Test - unit tests for ClassCoverage. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class ClassCoverage_Test extends TestCase
{
    /** The ClassCoverage to be tested. */
    private ClassCoverage coverage;
    
    /** A dummy string. */
    private static final String DUMMY_STRING = "dummy";
    
    public void setUp()
    {
        coverage = new ClassCoverage(DUMMY_STRING);
        coverage.addLineCoverage(121, 0, false);
        coverage.addLineCoverage(122, 0, true);
        coverage.addLineCoverage(123, 1, true);
        coverage.addLineCoverage(124, 2, false);
    }
    
    public void testConstructor()
    {
        String className = "ClassCoverage_Test"; 
        coverage = new ClassCoverage(className);
        
        assertEquals("incorrect class name", className, coverage.getClassName());
    }
    
    public void testBranchCoverageAccessors()
    {
        double branchCoverage = 12.3;
        
        coverage.setBranchCoverage(branchCoverage);        
        assertEquals("branchCoverage accessor incorrect", branchCoverage, coverage.getBranchCoverage(), 0.0);
    }
    
    public void testLineCoverageAccessors()
    {
        double lineCoverage = 12.3;
        
        coverage.setLineCoverage(lineCoverage);        
        assertEquals("lineCoverage accessor incorrect", lineCoverage, coverage.getLineCoverage(), 0.0);
    }
    
    public void testGetInvocationsForLine()
    {
        assertEquals("Incorrect invocations returned for empty coverage", 
                     -1, new ClassCoverage(DUMMY_STRING).getInvocationsForLine(1));
        
        assertEquals("Incorrect invocations returned for line 121", 
                     0, coverage.getInvocationsForLine(121));
        
        assertEquals("Incorrect invocations returned for line 122", 
                     0, coverage.getInvocationsForLine(122));
        
        assertEquals("Incorrect invocations returned for line 123", 
                     1, coverage.getInvocationsForLine(123));
        
        assertEquals("Incorrect invocations returned for line 124", 
                     2, coverage.getInvocationsForLine(124));
        
        assertEquals("Incorrect invocations returned for line 125", 
                     -1, coverage.getInvocationsForLine(125));
    }
    
    public void testGetLineCount()
    {
        assertEquals("Incorrect line count returned for empty coverage", 
                     0, new ClassCoverage(DUMMY_STRING).getLineCount());
        
        assertEquals("Incorrect line count returned", 
                     4, coverage.getLineCount());
    }
    
    public void testGetCoveredLineCount()
    {
        assertEquals("Incorrect covered line count returned for empty coverage", 
                     0, new ClassCoverage(DUMMY_STRING).getCoveredLineCount());
        
        assertEquals("Incorrect covered line count returned", 
                     2, coverage.getCoveredLineCount());
    }
    
    public void testGetBranchCount()
    {
        assertEquals("Incorrect branch count returned for empty coverage", 
                     0, new ClassCoverage(DUMMY_STRING).getBranchCount());
        
        assertEquals("Incorrect branch count returned", 2, coverage.getBranchCount());
    }
    
    public void testGetCoveredBranchCount()
    {
        assertEquals("Incorrect covered branch count returned for empty coverage", 
                     0, new ClassCoverage(DUMMY_STRING).getCoveredBranchCount());
        
        assertEquals("Incorrect covered branch count returned", 
                     1, coverage.getCoveredBranchCount());
    }    
}
