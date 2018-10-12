package com.github.sanity4j.model.summary; 

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/** 
 * PackageSummary_Test - jUnit tests for {@link PackageSummary}. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class PackageSummary_Test
{
    @Test
    public void testSetRunDate()
    {
        Date date = new Date(System.currentTimeMillis() - 123456);
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setRunDate(date);
        Assert.assertEquals("runDate accessor incorrect", date, packageSummary.getRunDate());
    }
    
    @Test
    public void testSetPackageName()
    {
        String packageName = "package.subpackage";
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setPackageName(packageName);
        Assert.assertEquals("packageName accessor incorrect", packageName, packageSummary.getPackageName());        
    }

    @Test
    public void testSetLineCoverage()
    {
        double coverage = 0.123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setLineCoverage(coverage);
        Assert.assertEquals("lineCoverage accessor incorrect", coverage, packageSummary.getLineCoverage(), 0.0);
    }

    @Test
    public void testSetBranchCoverage()
    {
        double coverage = 0.123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setBranchCoverage(coverage);
        Assert.assertEquals("branchCoverage accessor incorrect", coverage, packageSummary.getBranchCoverage(), 0.0);
    }

    @Test
    public void testSetInfoCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setInfoCount(count);
        Assert.assertEquals("InfoCount accessor incorrect", count, packageSummary.getInfoCount(), 0.0);
    }
    
    @Test
    public void testSetLowCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setLowCount(count);
        Assert.assertEquals("LowCount accessor incorrect", count, packageSummary.getLowCount(), 0.0);
    }
    
    @Test
    public void testSetModerateCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setModerateCount(count);
        Assert.assertEquals("ModerateCount accessor incorrect", count, packageSummary.getModerateCount(), 0.0);
    }

    @Test
    public void testSetSignificantCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setSignificantCount(count);
        Assert.assertEquals("SignificantCount accessor incorrect", count, packageSummary.getSignificantCount(), 0.0);
    }
    
    @Test
    public void testSetHighCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setHighCount(count);
        Assert.assertEquals("HighCount accessor incorrect", count, packageSummary.getHighCount(), 0.0);
    }
    
    @Test
    public void testSetLineCount()
    {
        int count = 123;
        
        PackageSummary packageSummary = new PackageSummary();
        packageSummary.setLineCount(count);
        Assert.assertEquals("LineCount accessor incorrect", count, packageSummary.getLineCount(), 0.0);
    }    
}
