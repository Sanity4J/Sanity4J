package com.github.sanity4j.model.coverage; 

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** 
 * PackageCoverage - Coverage information for a package.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class PackageCoverage extends AbstractCoverage
{
    /** The package name. */
    private final String packageName;
    
    /** ClassCoverages in this package, keyed by name. */
    private final Map<String, ClassCoverage> classesByName = new HashMap<String, ClassCoverage>();
    
    /**
     * Creates a PackageCoverage.
     * @param packageName the package name.
     */
    public PackageCoverage(final String packageName)
    {
        this.packageName = packageName;
    }

    /**
     * @return the package name.
     */
    public String getPackageName()
    {
        return packageName;
    }
    
    /**
     * Retrieves the coverage information for the given class.
     *  
     * @param className the fully qualified class name.
     * @return the coverage for the given class, or null if there isn't any.
     */
    public ClassCoverage getClassCoverage(final String className)
    {
        return classesByName.get(className);
    }
    
    /**
     * Adds class coverage.
     * @param coverage the class coverage to add.
     */
    public void addClass(final ClassCoverage coverage)
    {
        classesByName.put(coverage.getClassName(), coverage);
    }    

    /**
     * @return the number of classes in this package.
     */
    public int getClassCount()
    {
        return classesByName.size();
    }
    
    /**
     * @return the number of executable lines in this package.
     */
    @Override
    public int getLineCount()
    {
        int count = 0;
        
        for (ClassCoverage coverage : classesByName.values())
        {
            count += coverage.getLineCount();
        }
        
        return count;
    }
    
    /**
     * @return the number of covered lines in this package.
     */
    @Override
    public int getCoveredLineCount()
    {
        int count = 0;
        
        for (ClassCoverage coverage : classesByName.values())
        {
            count += coverage.getCoveredLineCount();
        }
        
        return count;
    }
    
    /**
     * @return the number of branches in this package.
     */
    @Override
    public int getBranchCount()
    {
        int count = 0;
        
        for (ClassCoverage coverage : classesByName.values())
        {
            count += coverage.getBranchCount();
        }
        
        return count;
    }
    
    /**
     * @return the number of covered branches in this package.
     */
    @Override
    public int getCoveredBranchCount()
    {
        int count = 0;
        
        for (ClassCoverage coverage : classesByName.values())
        {
            count += coverage.getCoveredBranchCount();
        }
        
        return count;
    }
    
    /**
     * @return the covered classes within this package.
     */
    public Set<String> getClassNames()
    {
       return Collections.unmodifiableSet(classesByName.keySet());
    }    
}
