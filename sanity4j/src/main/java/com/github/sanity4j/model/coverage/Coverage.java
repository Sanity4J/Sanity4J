package com.github.sanity4j.model.coverage; 

import java.util.HashMap;
import java.util.Map;

/** 
 * Coverage information for a project. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class Coverage extends AbstractCoverage
{
    /** PackageCoverages keyed by package name. */
    private final Map<String, PackageCoverage> packagesByName = new HashMap<String, PackageCoverage>();
    
    /**
     * Retrieves the coverage information for the given package.
     *  
     * @param packageName the package name.
     * @return the coverage for the given package, or null if there isn't any.
     */
    public PackageCoverage getPackageCoverage(final String packageName)
    {
        return packagesByName.get(packageName);
    }
    
    /**
     * Retrieves the coverage information for the given class.
     *  
     * @param className the fully qualified class name.
     * @return the coverage for the given class, or null if there isn't any.
     */
    public ClassCoverage getClassCoverage(final String className)
    {
        String packageName = null;
        int dotIndex = className.lastIndexOf('.');
        
        if (dotIndex != -1)
        {
           	packageName = className.substring(0, dotIndex);
        }
        
        PackageCoverage packageCoverage = getPackageCoverage(packageName);
        
        if (packageCoverage != null)
        {
            return packageCoverage.getClassCoverage(className);
        }
        
        return null;
    }
        
    /**
     * Adds package coverage.
     * @param coverage the package coverage to add.
     */
    public void addPackage(final PackageCoverage coverage)
    {
        packagesByName.put(coverage.getPackageName(), coverage);
    }
    
    /**
     * @return the number of executable lines in this package.
     */
    public int getLineCount()
    {
        int count = 0;
        
        for (PackageCoverage coverage : packagesByName.values())
        {
            count += coverage.getLineCount();
        }
        
        return count;
    }
    
    /**
     * @return the number of covered lines in this package.
     */
    public int getCoveredLineCount()
    {
        int count = 0;
        
        for (PackageCoverage coverage : packagesByName.values())
        {
            count += coverage.getCoveredLineCount();
        }
        
        return count;
    }
    
    /**
     * @return the number of branches in this package.
     */
    public int getBranchCount()
    {
        int count = 0;
        
        for (PackageCoverage coverage : packagesByName.values())
        {
            count += coverage.getBranchCount();
        }
        
        return count;
    }
    
    /**
     * @return the number of covered branches in this package.
     */
    public int getCoveredBranchCount()
    {
        int count = 0;
        
        for (PackageCoverage coverage : packagesByName.values())
        {
            count += coverage.getCoveredBranchCount();
        }
        
        return count;
    }    
}
