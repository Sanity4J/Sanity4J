package net.sf.sanity4j.model.summary; 

import java.util.Date;

/** 
 * PackageSummary - a quality summary entry. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class PackageSummary
{
    /** The date that the analysis was performed. */
    private Date runDate;
    
    /** The java package name that this summary is for. */
    private String packageName;
    
    /** jUnit test line coverage [0.0 .. 1.0]. */
    private double lineCoverage;
    
    /** jUnit test branch coverage [0.0 .. 1.0]. */
    private double branchCoverage;

    /** The number of 'info' level diagnostics. */
    private int infoCount;
    
    /** The number of 'low' level diagnostics. */
    private int lowCount;
    
    /** The number of 'moderate' level diagnostics. */
    private int moderateCount;
    
    /** The number of 'significant' level diagnostics. */
    private int significantCount;
    
    /** The number of 'high' level diagnostics. */
    private int highCount;
    
    /** The number of lines of code in the package. */
    private int lineCount;
    
    /**
     * @return Returns the branchCoverage.
     */
    public double getBranchCoverage()
    {
        return branchCoverage;
    }
    
    /**
     * @param branchCoverage The branchCoverage to set.
     */
    public void setBranchCoverage(final double branchCoverage)
    {
        this.branchCoverage = branchCoverage;
    }
    
    /**
     * @return Returns the lineCoverage.
     */
    public double getLineCoverage()
    {
        return lineCoverage;
    }
    
    /**
     * @param lineCoverage The lineCoverage to set.
     */
    public void setLineCoverage(final double lineCoverage)
    {
        this.lineCoverage = lineCoverage;
    }
    
    /**
     * @return Returns the packageName.
     */
    public String getPackageName()
    {
        return packageName;
    }
    
    /**
     * @param packageName The packageName to set.
     */
    public void setPackageName(final String packageName)
    {
        this.packageName = packageName;
    }
    
    /**
     * @return the infoCount
     */
    public int getInfoCount()
    {
        return infoCount;
    }

    /**
     * @param infoCount the infoCount to set
     */
    public void setInfoCount(final int infoCount)
    {
        this.infoCount = infoCount;
    }

    /**
     * @return the lowCount
     */
    public int getLowCount()
    {
        return lowCount;
    }

    /**
     * @param lowCount the lowCount to set
     */
    public void setLowCount(final int lowCount)
    {
        this.lowCount = lowCount;
    }

    /**
     * @return the moderateCount
     */
    public int getModerateCount()
    {
        return moderateCount;
    }

    /**
     * @param moderateCount the moderateCount to set
     */
    public void setModerateCount(final int moderateCount)
    {
        this.moderateCount = moderateCount;
    }

    /**
     * @return the significantCount
     */
    public int getSignificantCount()
    {
        return significantCount;
    }

    /**
     * @param significantCount the significantCount to set
     */
    public void setSignificantCount(final int significantCount)
    {
        this.significantCount = significantCount;
    }

    /**
     * @return the highCount
     */
    public int getHighCount()
    {
        return highCount;
    }

    /**
     * @param highCount the highCount to set
     */
    public void setHighCount(final int highCount)
    {
        this.highCount = highCount;
    }

    /**
     * @return Returns the runDate.
     */
    public Date getRunDate()
    {
        return runDate;
    }
    
    /**
     * @param runDate The runDate to set.
     */
    public void setRunDate(final Date runDate)
    {
        this.runDate = runDate;
    }
    
    /**
     * @return the lineCount
     */
    public int getLineCount()
    {
        return lineCount;
    }

    /**
     * @param lineCount the lineCount to set
     */
    public void setLineCount(final int lineCount)
    {
        this.lineCount = lineCount;
    }
}
