package com.github.sanity4j.model.coverage; 

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** 
 * ClassCoverage_Test - unit tests for ClassCoverage. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class ClassCoverage_Test
{
    /** The ClassCoverage to be tested. */
    private ClassCoverage coverage;
    
    /** A dummy string. */
    private static final String DUMMY_STRING = "dummy";
    
    @Before
    public void setUp()
    {
        coverage = new ClassCoverage(DUMMY_STRING);
        coverage.addLineCoverage(121, 0, false);
        coverage.addLineCoverage(122, 0, true);
        coverage.addLineCoverage(123, 1, true);
        coverage.addLineCoverage(124, 2, false);
    }
    
    @Test
    public void testConstructor()
    {
        String className = "ClassCoverage_Test"; 
        coverage = new ClassCoverage(className);
        
        Assert.assertEquals("incorrect class name", className, coverage.getClassName());
    }
    
    @Test
    public void testBranchCoverageAccessors()
    {
        double branchCoverage = 12.3;
        
        coverage.setBranchCoverage(branchCoverage);        
        Assert.assertEquals("branchCoverage accessor incorrect", branchCoverage, coverage.getBranchCoverage(), 0.0);
    }
    
    @Test
    public void testLineCoverageAccessors()
    {
        double lineCoverage = 12.3;
        
        coverage.setLineCoverage(lineCoverage);        
        Assert.assertEquals("lineCoverage accessor incorrect", lineCoverage, coverage.getLineCoverage(), 0.0);
    }
    
    @Test
    public void testGetInvocationsForLine()
    {
        Assert.assertEquals("Incorrect invocations returned for empty coverage", 
                     -1, new ClassCoverage(DUMMY_STRING).getInvocationsForLine(1));
        
        Assert.assertEquals("Incorrect invocations returned for line 121", 
                     0, coverage.getInvocationsForLine(121));
        
        Assert.assertEquals("Incorrect invocations returned for line 122", 
                     0, coverage.getInvocationsForLine(122));
        
        Assert.assertEquals("Incorrect invocations returned for line 123", 
                     1, coverage.getInvocationsForLine(123));
        
        Assert.assertEquals("Incorrect invocations returned for line 124", 
                     2, coverage.getInvocationsForLine(124));
        
        Assert.assertEquals("Incorrect invocations returned for line 125", 
                     -1, coverage.getInvocationsForLine(125));
    }
    
    @Test
    public void testGetLineCount()
    {
        Assert.assertEquals("Incorrect line count returned for empty coverage", 
                     0, new ClassCoverage(DUMMY_STRING).getLineCount());
        
        Assert.assertEquals("Incorrect line count returned", 
                     4, coverage.getLineCount());
    }
    
    @Test
    public void testGetCoveredLineCount()
    {
        Assert.assertEquals("Incorrect covered line count returned for empty coverage", 
                     0, new ClassCoverage(DUMMY_STRING).getCoveredLineCount());
        
        Assert.assertEquals("Incorrect covered line count returned", 
                     2, coverage.getCoveredLineCount());
    }
    
    @Test
    public void testGetBranchCount()
    {
        Assert.assertEquals("Incorrect branch count returned for empty coverage", 
                     0, new ClassCoverage(DUMMY_STRING).getBranchCount());
        
        Assert.assertEquals("Incorrect branch count returned", 2, coverage.getBranchCount());
    }
    
    @Test
    public void testGetCoveredBranchCount()
    {
        Assert.assertEquals("Incorrect covered branch count returned for empty coverage", 
                     0, new ClassCoverage(DUMMY_STRING).getCoveredBranchCount());
        
        Assert.assertEquals("Incorrect covered branch count returned", 
                     1, coverage.getCoveredBranchCount());
    }    
}
