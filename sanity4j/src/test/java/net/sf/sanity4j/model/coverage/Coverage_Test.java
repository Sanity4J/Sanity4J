package net.sf.sanity4j.model.coverage; 

import junit.framework.TestCase;

/** 
 * Coverage_Test - unit tests for {@link Coverage}. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class Coverage_Test extends TestCase
{
	/** Dummy Package Name 1. */
    private static final String PACKAGE_NAME_1 = "package.one";
	/** Dummy Package Name 2. */
    private static final String PACKAGE_NAME_2 = "package.two";
	/** Dummy Class Name 1. */
    private static final String CLASS_NAME_1 = PACKAGE_NAME_1 + ".DummyClass";
	/** Dummy Class Name 2. */
    private static final String CLASS_NAME_2 = PACKAGE_NAME_2 + ".AnotherDummyClass";

    /** The Coverage to be tested. */
	private Coverage coverage;
	
	public void setUp()
    {
        coverage = new Coverage();

        PackageCoverage packageCoverage = new PackageCoverage(PACKAGE_NAME_1);
        ClassCoverage classCoverage = new ClassCoverage(CLASS_NAME_1);
        classCoverage.addLineCoverage(121, 0, false);
        classCoverage.addLineCoverage(122, 0, true);
        packageCoverage.addClass(classCoverage);
        coverage.addPackage(packageCoverage);

        packageCoverage = new PackageCoverage(PACKAGE_NAME_2);
        classCoverage = new ClassCoverage(CLASS_NAME_2);
        classCoverage.addLineCoverage(123, 1, true);
        classCoverage.addLineCoverage(124, 2, false);
        packageCoverage.addClass(classCoverage);
        coverage.addPackage(packageCoverage);
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
    
    public void testGetLineCount()
    {
        assertEquals("Incorrect line count returned for empty coverage", 
                     0, new Coverage().getLineCount());
        
        assertEquals("Incorrect line count returned", 
                     4, coverage.getLineCount());
    }
    
    public void testGetCoveredLineCount()
    {
        assertEquals("Incorrect covered line count returned for empty coverage", 
                     0, new Coverage().getCoveredLineCount());
        
        assertEquals("Incorrect covered line count returned", 
                     2, coverage.getCoveredLineCount());
    }
    
    public void testGetBranchCount()
    {
        assertEquals("Incorrect branch count returned for empty coverage", 
                     0, new Coverage().getBranchCount());
        
        assertEquals("Incorrect branch count returned", 
                     2, coverage.getBranchCount());
    }
    
    public void testGetCoveredBranchCount()
    {
        assertEquals("Incorrect covered branch count returned for empty coverage", 
        			 0, new Coverage().getBranchCount());
        			 
        assertEquals("Incorrect covered branch count returned", 
                     1, coverage.getCoveredBranchCount());
    }

    public void testGetPackageCoverage()
    {
        assertNull("Class coverage returned for empty coverage", 
                   new Coverage().getPackageCoverage(PACKAGE_NAME_1));
        
        assertNotNull("Class coverage for package 1 missing", 
                      coverage.getPackageCoverage(PACKAGE_NAME_1));
        
        assertNotNull("Class coverage for package 2 missing", 
                      coverage.getPackageCoverage(PACKAGE_NAME_2));
        
        assertNull("Class coverage returned for incorrect package name", 
                   coverage.getClassCoverage(PACKAGE_NAME_1 + PACKAGE_NAME_2));
    }
    
    public void testGetClassCoverage()
    {
        assertNull("Class coverage returned for empty coverage", 
        		   new Coverage().getClassCoverage(CLASS_NAME_1));
        
        assertNotNull("Class coverage for class 1 missing", 
        		      coverage.getClassCoverage(CLASS_NAME_1));
        
        assertNotNull("Class coverage for class 2 missing", 
        		      coverage.getClassCoverage(CLASS_NAME_2));
        
        assertNull("Class coverage returned for incorrect class name", 
        		   coverage.getClassCoverage(CLASS_NAME_1 + CLASS_NAME_2));
    }
}
