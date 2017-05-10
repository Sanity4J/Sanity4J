package com.github.sanity4j.model.coverage; 

/** 
 * Abstract implementation of Coverage.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public abstract class AbstractCoverage implements CoverageItf
{
    /** Branch coverage ratio, from 0.0 (none) to 1.0 (all). */
    private double branchCoverage;
    
    /** Line coverage ratio, from 0.0 (none) to 1.0 (all). */
    private double lineCoverage;
    
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
}
