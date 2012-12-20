package net.sf.sanity4j.model.coverage; 

import junit.framework.TestCase;

/** 
 * PackageCoverage_Test - unit tests for {@link PackageCoverage}. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class PackageCoverage_Test extends TestCase
{
	/** Dummy Class Name 1. */
    private static final String CLASS_NAME_1 = "DummyClass";
	/** Dummy Class Name 2. */
    private static final String CLASS_NAME_2 = "AnotherDummyClass";
    
    /** The PackageCoverage to be tested. */
	private PackageCoverage packageCoverage;
	
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

    public void testConstructor()
    {
        String packageName = "mydomain.qatest"; 
        
        assertEquals("incorrect package name", 
                     packageName, new PackageCoverage(packageName).getPackageName());
    }
    
    public void testBranchCoverageAccessors()
    {
        double branchCoverage = 12.3;
        
        PackageCoverage coverage = new PackageCoverage("dummy");
        coverage.setBranchCoverage(branchCoverage);        
        assertEquals("branchCoverage accessor incorrect", branchCoverage, coverage.getBranchCoverage(), 0.0);
    }
    
    public void testLineCoverageAccessors()
    {
        double lineCoverage = 12.3;
        
        PackageCoverage coverage = new PackageCoverage("dummy");
        coverage.setLineCoverage(lineCoverage);        
        assertEquals("lineCoverage accessor incorrect", lineCoverage, coverage.getLineCoverage(), 0.0);
    }
    
    public void testGetLineCount()
    {
        assertEquals("Incorrect line count returned for empty coverage", 
                     0, new PackageCoverage("").getLineCount());
        
        assertEquals("Incorrect line count returned", 
                     4, packageCoverage.getLineCount());
    }
    
    public void testGetCoveredLineCount()
    {
        assertEquals("Incorrect covered line count returned for empty coverage", 
                     0, new PackageCoverage("").getCoveredLineCount());
        
        assertEquals("Incorrect covered line count returned", 
                     2, packageCoverage.getCoveredLineCount());
    }
    
    public void testGetBranchCount()
    {
        assertEquals("Incorrect branch count returned for empty coverage", 
                     0, new PackageCoverage("").getBranchCount());
        
        assertEquals("Incorrect branch count returned", 
                     2, packageCoverage.getBranchCount());
    }
    
    public void testGetCoveredBranchCount()
    {
        assertEquals("Incorrect covered branch count returned for empty coverage", 
        			 0, new PackageCoverage("").getBranchCount());
        			 
        assertEquals("Incorrect covered branch count returned", 
                     1, packageCoverage.getCoveredBranchCount());
    }
    
    public void testGetClassCount()
    {
        assertEquals("Incorrect class count returned for empty coverage", 
        			 0, new PackageCoverage("").getClassCount());
        			 
        assertEquals("Incorrect class count returned", 
                     2, packageCoverage.getClassCount());
    }
    
    public void testGetClassCoverage()
    {
        assertNull("Class coverage returned for empty coverage", 
        		   new PackageCoverage("").getClassCoverage(CLASS_NAME_1));
        
        assertNotNull("Class coverage for class 1 missing", 
        		      packageCoverage.getClassCoverage(CLASS_NAME_1));
        
        assertNotNull("Class coverage for class 2 missing", 
        		      packageCoverage.getClassCoverage(CLASS_NAME_2));
        
        assertNull("Class coverage returned for incorrect class name", 
        		   packageCoverage.getClassCoverage(CLASS_NAME_1 + CLASS_NAME_2));
    }
}
