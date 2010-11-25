package net.sf.sanity4j.model.coverage; 

import java.util.HashMap;
import java.util.Map;

/** 
 * ClassCoverage - coverage information for a class.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class ClassCoverage extends AbstractCoverage
{
    /** The class's name (enclosing class for inner classes). */
    private final String className;
    
    /** Invocation counts by line number. */
    private final Map<Integer, Integer> invocationsByLine = new HashMap<Integer, Integer>();

    /** Branch coverage by line number. */
    private final Map<Integer, Double> branchCoverageByLine = new HashMap<Integer, Double>();
    
    /** The number of covered lines in the class. */
    private int coveredLineCount;
    /** The number of branches in the class. */
    private int branchCount;
    /** The number of covered branches in the class. */
    private int coveredBranchCount;
    
    /**
     * Creates a ClassCoverage.
     * @param className the class name
     */
    public ClassCoverage(final String className)
    {
        this.className = className;
    }
    
    /**
     * @return the class's name
     */
    public String getClassName()
    {
        return className;
    }
    
    /**
     * Returns the number of invocations of the given line.
     * 
     * @param lineNumber the line number
     * 
     * @return the number of invocations of the given line, 
     *         or -1 if the line was not analysed
     */
    public int getInvocationsForLine(final int lineNumber)
    {
        Integer coverage = invocationsByLine.get(lineNumber);
        
        return coverage == null ? -1 : coverage.intValue();
    }
    
    /**
     * Returns the branch coverage percentage for the given line.
     * 
     * @param lineNumber the line number
     * 
     * @return the branch coverage for the given line 
     *         or -1.0 if the line was not analysed
     */
    public double getBranchCoverageForLine(final int lineNumber)
    {
        Double coverage = branchCoverageByLine.get(lineNumber);
        
        return coverage == null ? -1.0 : coverage.doubleValue();
    }
    
    /**
     * Adds coverage for a line.
     * 
     * @param lineNumber the line number
     * @param invocationCount the invocation count
     * @param isBranch true if the given line is a branch
     */
    public void addLineCoverage(final int lineNumber, final int invocationCount, 
                                final boolean isBranch)
    {
        invocationsByLine.put(lineNumber, invocationCount);

        if (isBranch)
        {
            branchCount++;
            
            if (invocationCount > 0)
            {
                coveredBranchCount++;
            }            
        }
        
        if (invocationCount > 0)
        {
            coveredLineCount++;
        }
    }

    /**
     * Adds branch coverage for a line.
     * 
     * @param lineNumber the line number
     * @param percentage the percentage of the branch that was covered
     */
    public void addBranchCoverage(final int lineNumber, final double percentage)
    {
        branchCoverageByLine.put(lineNumber, percentage);
    }
    
    /**
     * @return the number of executable lines in this class
     */
    public int getLineCount()
    {
        return invocationsByLine.size();
    }
    
    /**
     * @return the number of covered lines in this class
     */
    public int getCoveredLineCount()
    {
        return coveredLineCount;
    }
    
    /**
     * @return the number of branches in this class
     */
    public int getBranchCount()
    {
        return branchCount;
    }
    
    /**
     * @return the number of covered branches in this class
     */
    public int getCoveredBranchCount()
    {
        return coveredBranchCount;
    }    
}
