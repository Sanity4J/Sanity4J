package com.github.sanity4j.report; 

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.sanity4j.model.summary.PackageSummary;

/**
 * <p>ChartFactory_Test - unit tests for ChartFactory.</p>
 * 
 * <p>Because it's hard to actually interpret the resultant image, we just look at
 * the image data coming back and ensure that it is identical or different 
 * (somewhere) where it should be.</p>
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class ChartFactory_Test
{
    /** An empty set of package summaries, to ensure that there are no NPE, OOB etc. errors. */
    private final PackageSummary[] noSummary = new PackageSummary[0];
    
    /** A set of two package summaries, for producing chart data. */
    private PackageSummary[] twoSummary;

    @Before
    public void setUp()
    {
        Date runDate1 = new Date();
        Date runDate2 = new Date(runDate1.getTime() + 1000 * 60);
        
        PackageSummary summary1 = new PackageSummary();
        summary1.setPackageName(null);
        summary1.setBranchCoverage(0.10);
        summary1.setLineCoverage(0.13);
        summary1.setHighCount(10);
        summary1.setRunDate(runDate1);
        PackageSummary summary2 = new PackageSummary();
        summary2.setPackageName(null);
        summary2.setBranchCoverage(0.2);
        summary2.setLineCoverage(0.23);
        summary1.setHighCount(20);
        summary2.setRunDate(runDate2);
                           
        twoSummary = new PackageSummary[] { summary1, summary2 };
    }
    
    @Test
    public void testCreateImageNoSummaryIdentical()
    {        
        int[] noSummaryAllPackages1 = createImage(noSummary, "");
        int[] noSummaryAllPackages2 = createImage(noSummary, "");
        
        Assert.assertTrue("Images with identical data should be identical", 
                   Arrays.equals(noSummaryAllPackages1, noSummaryAllPackages2));
    }
    
    @Test
    public void testCreateImageNoSummaryDifferentPackages()
    {
        int[] noSummaryAllPackages = createImage(noSummary, "");
        int[] noSummaryAuPackage = createImage(noSummary, "au");
        
        Assert.assertFalse("Images for different packages should never be identical", 
                   Arrays.equals(noSummaryAuPackage, noSummaryAllPackages));
    }
    
    @Test
    public void testCreateImmageTwoSummaryIdentical()
    {
        int[] twoSummaryAllPackages1 = createImage(twoSummary, "");
        int[] twoSummaryAllPackages2 = createImage(twoSummary, "");
        
        Assert.assertTrue("Images with identical data should be identical", 
                   Arrays.equals(twoSummaryAllPackages1, twoSummaryAllPackages2));
    }
    
    @Test
    public void testCreateImageTwoSummaryDifferentData()
    {
        // No data vs some data
        int[] noSummaryAllPackages = createImage(noSummary, "");
        int[] twoSummaryAllPackages = createImage(twoSummary, "");
        
        Assert.assertFalse("Images with different data should not be identical", 
                    Arrays.equals(noSummaryAllPackages, twoSummaryAllPackages));
        
        // Data vs changed data
        twoSummary[1].setHighCount(twoSummary[1].getHighCount() + 100);
        int[] twoSummaryAllPackages2 = createImage(twoSummary, "");
        
        Assert.assertFalse("Images with different data should not be identical", 
                    Arrays.equals(twoSummaryAllPackages, twoSummaryAllPackages2));        
    }
    
    private int[] createImage(final PackageSummary[] summaries, final String packageName)
    {
        BufferedImage image = ChartFactory.createImage(summaries, packageName);
        image.flush();
        
        Assert.assertNotNull("Image should not be null", image);
        return imageToBytes(image);
    }
    
    /**
     * Converts a BufferedImage into an ARGB int array.
     * 
     * @param image the image to convert
     * @return the image data as an ARGB array
     */
    private int[] imageToBytes(final BufferedImage image)
    {
        final int width = image.getWidth();
        final int height = image.getHeight();
        
        int[] rgba = new int[image.getWidth() * image.getHeight()];
        
        try
        {
            PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, width, height, rgba, 0, width);
            pixelGrabber.grabPixels();
        }
        catch (InterruptedException e)
        {
            Assert.fail("Test interrupted whilst grabbing pixels");
        }
        
        return rgba;
    }
}
