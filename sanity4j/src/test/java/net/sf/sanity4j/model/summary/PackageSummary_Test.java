package net.sf.sanity4j.model.summary; 

import java.util.Date;

import junit.framework.TestCase;

/** 
 * PackageSummary_Test - jUnit tests for {@link PackageSummary}. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class PackageSummary_Test extends TestCase
{
    public void testSetRunDate()
    {
        Date date = new Date(System.currentTimeMillis() - 123456);
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setRunDate(date);
        assertEquals("runDate accessor incorrect", date, packageSummary.getRunDate());
    }
    
    public void testSetPackageName()
    {
        String packageName = "package.subpackage";
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setPackageName(packageName);
        assertEquals("packageName accessor incorrect", packageName, packageSummary.getPackageName());        
    }

    public void testSetLineCoverage()
    {
        double coverage = 0.123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setLineCoverage(coverage);
        assertEquals("lineCoverage accessor incorrect", coverage, packageSummary.getLineCoverage(), 0.0);
    }

    public void testSetBranchCoverage()
    {
        double coverage = 0.123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setBranchCoverage(coverage);
        assertEquals("branchCoverage accessor incorrect", coverage, packageSummary.getBranchCoverage(), 0.0);
    }

    public void testSetInfoCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setInfoCount(count);
        assertEquals("InfoCount accessor incorrect", count, packageSummary.getInfoCount(), 0.0);
    }
    
    public void testSetLowCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setLowCount(count);
        assertEquals("LowCount accessor incorrect", count, packageSummary.getLowCount(), 0.0);
    }
    
    public void testSetModerateCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setModerateCount(count);
        assertEquals("ModerateCount accessor incorrect", count, packageSummary.getModerateCount(), 0.0);
    }

    public void testSetSignificantCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setSignificantCount(count);
        assertEquals("SignificantCount accessor incorrect", count, packageSummary.getSignificantCount(), 0.0);
    }
    
    public void testSetHighCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setHighCount(count);
        assertEquals("HighCount accessor incorrect", count, packageSummary.getHighCount(), 0.0);
    }
    
    public void testSetLineCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setLineCount(count);
        assertEquals("LineCount accessor incorrect", count, packageSummary.getLineCount(), 0.0);
    }    
}
