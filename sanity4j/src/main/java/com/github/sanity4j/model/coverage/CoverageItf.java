package com.github.sanity4j.model.coverage;

/** 
 * CoverageItf - coverage interface. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public interface CoverageItf
{
    /**
     * @return Returns the branchCoverage.
     */
    double getBranchCoverage();

    /**
     * @return Returns the lineCoverage.
     */
    double getLineCoverage();

    /**
     * @return the number of executable lines in this package.
     */
    int getLineCount();

    /**
     * @return the number of covered lines in this package.
     */
    int getCoveredLineCount();

    /**
     * @return the number of branches in this package.
     */
    int getBranchCount();

    /**
     * @return the number of covered branches in this package.
     */
    int getCoveredBranchCount();
}
