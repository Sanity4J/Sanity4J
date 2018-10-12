package com.github.sanity4j.model.coverage; 

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** 
 * PackageCoverage_Test - unit tests for {@link PackageCoverage}. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class PackageCoverage_Test
{
    /** Dummy Class Name 1. */
    private static final String CLASS_NAME_1 = "DummyClass";
    
    /** Dummy Class Name 2. */
    private static final String CLASS_NAME_2 = "AnotherDummyClass";
    
    /** The PackageCoverage to be tested. */
    private PackageCoverage packageCoverage;

    @Before
    public void setUp()
    {
        packageCoverage = new PackageCoverage("");
    
        ClassCoverage classCoverage = new ClassCoverage(CLASS_NAME_1);
        classCoverage.addLineCoverage(121, 0, false);
        classCoverage.addLineCoverage(122, 0, true);
        packageCoverage.addClass(classCoverage);
        
        classCoverage = new ClassCoverage(CLASS_NAME_2);
        classCoverage.addLineCoverage(123, 1, true);
        classCoverage.addLineCoverage(124, 2, false);
        packageCoverage.addClass(classCoverage);
    }

    @Test
    public void testConstructor()
    {
        String packageName = "mydomain.qatest"; 
        
        Assert.assertEquals("incorrect package name", 
                     packageName, new PackageCoverage(packageName).getPackageName());
    }
    
    @Test
    public void testBranchCoverageAccessors()
    {
        double branchCoverage = 12.3;
        
        PackageCoverage coverage = new PackageCoverage("dummy");
        coverage.setBranchCoverage(branchCoverage);        
        Assert.assertEquals("branchCoverage accessor incorrect", branchCoverage, coverage.getBranchCoverage(), 0.0);
    }
    
    @Test
    public void testLineCoverageAccessors()
    {
        double lineCoverage = 12.3;
        
        PackageCoverage coverage = new PackageCoverage("dummy");
        coverage.setLineCoverage(lineCoverage);        
        Assert.assertEquals("lineCoverage accessor incorrect", lineCoverage, coverage.getLineCoverage(), 0.0);
    }
    
    @Test
    public void testGetLineCount()
    {
        Assert.assertEquals("Incorrect line count returned for empty coverage", 
                     0, new PackageCoverage("").getLineCount());
        
        Assert.assertEquals("Incorrect line count returned", 
                     4, packageCoverage.getLineCount());
    }
    
    @Test
    public void testGetCoveredLineCount()
    {
        Assert.assertEquals("Incorrect covered line count returned for empty coverage", 
                     0, new PackageCoverage("").getCoveredLineCount());
        
        Assert.assertEquals("Incorrect covered line count returned", 
                     2, packageCoverage.getCoveredLineCount());
    }
    
    @Test
    public void testGetBranchCount()
    {
        Assert.assertEquals("Incorrect branch count returned for empty coverage", 
                     0, new PackageCoverage("").getBranchCount());
        
        Assert.assertEquals("Incorrect branch count returned", 
                     2, packageCoverage.getBranchCount());
    }
    
    @Test
    public void testGetCoveredBranchCount()
    {
        Assert.assertEquals("Incorrect covered branch count returned for empty coverage", 
                     0, new PackageCoverage("").getBranchCount());
                     
        Assert.assertEquals("Incorrect covered branch count returned", 
                     1, packageCoverage.getCoveredBranchCount());
    }
    
    @Test
    public void testGetClassCount()
    {
        Assert.assertEquals("Incorrect class count returned for empty coverage", 
                     0, new PackageCoverage("").getClassCount());
                     
        Assert.assertEquals("Incorrect class count returned", 
                     2, packageCoverage.getClassCount());
    }
    
    @Test
    public void testGetClassCoverage()
    {
        Assert.assertNull("Class coverage returned for empty coverage", 
                   new PackageCoverage("").getClassCoverage(CLASS_NAME_1));
        
        Assert.assertNotNull("Class coverage for class 1 missing", 
                      packageCoverage.getClassCoverage(CLASS_NAME_1));
        
        Assert.assertNotNull("Class coverage for class 2 missing", 
                      packageCoverage.getClassCoverage(CLASS_NAME_2));
        
        Assert.assertNull("Class coverage returned for incorrect class name", 
                   packageCoverage.getClassCoverage(CLASS_NAME_1 + CLASS_NAME_2));
    }
}
