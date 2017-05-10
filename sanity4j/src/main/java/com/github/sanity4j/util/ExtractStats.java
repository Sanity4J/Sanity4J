package com.github.sanity4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.sanity4j.model.coverage.Coverage;
import com.github.sanity4j.model.coverage.PackageCoverage;
import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.model.summary.PackageSummary;
import com.github.sanity4j.model.summary.SummaryCsvMarshaller;

/**
 * Utility class for extracting statistics from the various tool's XML outputs.
 * Since many of the tools use different versions of paths (e.g. short
 * file names on win32), the canonical path must be used when referring
 * to any path.
 *  
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class ExtractStats
{
    /** The directory containing the source code. */
    private final String sourceDirectory;
    
    /** The set of diagnostics for the current run. */
    private final DiagnosticSet diagnostics = new DiagnosticSet();
    
    /** Top-level Unit test coverage. */
    private final Coverage coverage = new Coverage();
    
    /** Lines of code, by package name. */
    private final Map<String, Integer> lineCountByPackage = new HashMap<String, Integer>();
    
    /** Lines of code, by class name. */
    private final Map<String, Integer> lineCountByClass = new HashMap<String, Integer>();
    
    /** Number of classes per package, by package name. */
    private final Map<String, Integer> classCountByPackage = new HashMap<String, Integer>();
    
    /** Run summaries, by package name. */
    private final Map<String, List<PackageSummary>> summaryByPackage = new HashMap<String, List<PackageSummary>>();
    
    /** Summarised data for the current run. */
    private PackageSummary[] currentRunSummary;
    
    /** A default size for a buffer. */
    private static final int BUF_SIZE = 4096;
	
	/**
     * Creates an ExtractStats.
     * 
     * @param sourceDirectory the source directory
     * @throws IOException if there is an error determining the canonical path of the source directory
     */
    public ExtractStats(final String sourceDirectory) throws IOException
    {
        this.sourceDirectory = getCanonicalPath(sourceDirectory);
    }
	
	/**
     * @return the source directory
     */
    public String getSourceDirectory()
    {
        return sourceDirectory;
    }

    /** @return the set of Diagnostics */
    public DiagnosticSet getDiagnostics()
    {
        return diagnostics;
    }

    /** @return the coverage */
    public Coverage getCoverage()
    {
        return coverage;
    }
	
	/**
	 * Extracts source file line counts for all source files .
     * @throws IOException if there is an error reading from a file
	 */
	public void extractLineCounts() throws IOException
	{
	    extractLineCounts(new File(sourceDirectory));
	}

	/**
     * Extract source file line counts.
     * 
     * @param file the file to count lines for
     * @throws IOException if there is an error reading from a file
     */
    private void extractLineCounts(final File file) throws IOException
    {
        if (file.isDirectory())
        {
            File[] children = file.listFiles();

            for (int i = 0; i < children.length; i++)
            {
                extractLineCounts(children[i]);
            }
        }
        else
        {
            int lineCount = countLines(file);
            String path = file.getCanonicalPath();
            String className = getClassNameForSourcePath(path);
            String packageName = getPackageName(path);

            if ("".equals(packageName))
            {
                packageName = "default";
            }

            lineCountByClass.put(className, lineCount);

            Integer packageLineCount = lineCountByPackage.get(packageName);

            if (packageLineCount == null)
            {
                lineCountByPackage.put(packageName, lineCount);
            }
            else
            {
                packageLineCount = packageLineCount.intValue() + lineCount;
                lineCountByPackage.put(packageName, packageLineCount);
            }

            Integer packageClassCount = classCountByPackage.get(packageName);

            if (packageClassCount == null)
            {
                classCountByPackage.put(packageName, 1);
            }
            else
            {
                packageClassCount = packageClassCount.intValue() + 1;
                classCountByPackage.put(packageName, packageClassCount);
            }
        }
    }
	
	/**
     * Counts the number of lines in a file.
     * 
     * @param file the text file to count lines for
     * @return the number of lines in the file
     * @throws IOException if there is an error reading from the file
     */
    private int countLines(final File file) throws IOException
    {
        int count = file.length() == 0 ? 0 : 1;

        if (file.length() > 0)
        {
            byte[] buf = new byte[BUF_SIZE];
            FileInputStream fis = null;

            try
            {
                fis = new FileInputStream(file);

                for (int len = fis.read(buf); len != -1; len = fis.read(buf))
                {
                    for (int i = 0; i < len; i++)
                    {
                        if (buf[i] == '\n')
                        {
                            count++;
                        }
                    }
                }
            }
            finally
            {
                QaUtil.safeClose(fis);
            }
        }

        return count;
    }
	
	/**
     * Returns the canonical (unique) version of the given path. See File.getCanonicalPath().
     * 
     * @param path the path
     * @return the canonical version of the path
     * @throws IOException if there is a problem retrieving the path
     */
    public String getCanonicalPath(final String path) throws IOException
    {
        File file = new File(path);

        // May be relative to the source directory
        if (!file.exists())
        {
            File relativeFile = new File(sourceDirectory, path);

            if (relativeFile.exists())
            {
                file = relativeFile;
            }
        }

        // The file still may not exist, for example, generated classes,
        // with debugging info, where the source was deleted afterwards

        return file.exists() ? file.getCanonicalPath() : null;
    }
	
	/**
     * Returns the class name for the given source path.
     * 
     * @param sourcePath the source path
     * @return the class name, or "unknown" if not a class
     */
    public String getClassNameForSourcePath(final String sourcePath)
    {
        String className = "unknown";

        if (sourcePath != null && sourcePath.toLowerCase().endsWith(".java"))
        {
            int basePathLength = getSourceDirectory().length() + 1;
            String relativeSourcePath = sourcePath.substring(basePathLength);

            // Strip off the file extension (if any)
            int dotIndex = relativeSourcePath.lastIndexOf('.');
            int lastPathIndex = relativeSourcePath.lastIndexOf(File.separatorChar);

            if (dotIndex > lastPathIndex)
            {
                relativeSourcePath = relativeSourcePath.substring(0, dotIndex);
            }

            className = relativeSourcePath.replace(File.separatorChar, '.');
        }

        return className;
    }
	
	/**
     * Determines the "correct" package name for a java source file, given its 
     * full path. The path is assumed to be inside getSourceDirectory() .
     * 
     * @param sourceFilePath the full path to the source file
     * @return the package name for the given source file.
     */
    public String getPackageName(final String sourceFilePath)
    {
    	// Find the directory containing for the given sourceFilePath.
    	String sourceDir = sourceFilePath;
        File sourceFile = new File(sourceFilePath);

        if (!sourceFile.isDirectory())
        {
            sourceDir = sourceFile.getParent();
        }

        // If we are in the default package return "";
        int sourceDirectoryLength = getSourceDirectory().length() + 1;

        if (sourceDir.equals(getSourceDirectory()) || sourceDir.length() < sourceDirectoryLength)
        {
            return "";
        }

        String relativePath = sourceDir.substring(sourceDirectoryLength);
        String packageName = relativePath.replace(File.separatorChar, '.');
        
        return packageName;
    }

    /**
     * @return the total line count
     */
    public int getLineCount()
    {
        int count = 0;

        for (Integer packageCount : lineCountByPackage.values())
        {
            count += packageCount;
        }

        return count;
    }

    /**
     * @return the total class count
     */
    public int getClassCount()
    {
        int count = 0;

        for (Integer classCount : classCountByPackage.values())
        {
            count += classCount;
        }

        return count;
    }
	
	/**
     * Retrieves the line count for the given package.
     * 
     * @param packageName the package name
     * @return the line count for the given package
     */
    public int getPackageLineCount(final String packageName)
    {
        Integer count = lineCountByPackage.get(packageName);
        return count == null ? 0 : count.intValue();
    }

    /**
     * Retrieves the class count for the given package.
     * 
     * @param packageName the package name
     * @return the class count for the given package
     */
    public int getPackageClassCount(final String packageName)
    {
        Integer count = classCountByPackage.get(packageName);
        return count == null ? 0 : count.intValue();
    }
	
	/**
	 * Retrieves the line count for the given class.
	 * 
	 * @param className the class name
	 * @return the line count for the given class
	 */
	public int getClassLineCount(final String className)
	{
	    Integer count = lineCountByClass.get(className);	    
	    return count == null ? 0 : count.intValue();
	}
	
	/**
     * Reads the historical summary information.
     * 
     * @param summaryFile the file to read from
     * @throws IOException if there is an error reading from the summary file 
     */
	public void extractHistoricalSummary(final File summaryFile) throws IOException
	{
	    // Retrieve the summaries
	    SummaryCsvMarshaller marshaller = new SummaryCsvMarshaller();
	    PackageSummary[] summaries = marshaller.read(summaryFile);
	    
	    // Hash them by package name for efficiency
	    addSummariesToSummaryMap(summaries);
	}
	
	/**
     * Retrieves the summary for the given package.
     * 
     * @param packageName the package name
	 * @return a summary of the package quality over time, may be empty
	 */
	public PackageSummary[] getPackageSummary(final String packageName)
	{
	    if (currentRunSummary == null)
	    {
	        summariseCurrentRun();
	    }
	    
	    List<PackageSummary> summaries = summaryByPackage.get(packageName);

	    if (summaries == null)
	    {
	        return new PackageSummary[0];
	    }
	    else
	    {
	        return summaries.toArray(new PackageSummary[summaries.size()]); 
	    }
	}
	
	/**
	 * @return a summary of the quality for this run
	 */
	public PackageSummary[] getRunSummary()
	{
	    if (currentRunSummary == null)
	    {
	        summariseCurrentRun();
	    }	
	    
	    return currentRunSummary;
	}
	
	/**
	 * Appends the current run into the package summary map.
	 */
	private void summariseCurrentRun()
	{
        List<PackageSummary> entries = new ArrayList<PackageSummary>();
        Set<String> packageNames = classCountByPackage.keySet();
        Date currentDate = new Date();
        
        // Summary for all packages
	    PackageSummary rootEntry = new PackageSummary();
	    rootEntry.setPackageName("");
	    rootEntry.setRunDate(currentDate);
	    rootEntry.setLineCoverage(coverage.getLineCoverage());
	    rootEntry.setBranchCoverage(coverage.getBranchCoverage());
	    rootEntry.setInfoCount(diagnostics.getCountForSeverity(Diagnostic.SEVERITY_INFO));
        rootEntry.setLowCount(diagnostics.getCountForSeverity(Diagnostic.SEVERITY_LOW));
        rootEntry.setModerateCount(diagnostics.getCountForSeverity(Diagnostic.SEVERITY_MODERATE));
        rootEntry.setSignificantCount(diagnostics.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT));
        rootEntry.setHighCount(diagnostics.getCountForSeverity(Diagnostic.SEVERITY_HIGH));
        rootEntry.setLineCount(getLineCount());
        entries.add(rootEntry);
        
        // For each package
    	for (String packageName : packageNames)
    	{
    	    PackageSummary entry = new PackageSummary();
            entry.setPackageName(packageName);
    	    entry.setRunDate(currentDate);
            
            PackageCoverage packageCoverage = coverage.getPackageCoverage(packageName);
            
            if (packageCoverage != null)
            {
	            entry.setLineCoverage(packageCoverage.getLineCoverage());
	            entry.setBranchCoverage(packageCoverage.getBranchCoverage());
            }
            
            // Diagnostics & line-counts for this package and sub-packages
            DiagnosticSet diagsForPackage = diagnostics.getDiagnosticsForPackage(packageName);
            int lineCountForPackage = (lineCountByPackage.get(packageName)).intValue();
            String packageNamePlusDot = packageName + '.';
            
            for (String otherPackageName : lineCountByPackage.keySet())
            {
                if (otherPackageName.startsWith(packageNamePlusDot))
                {
                    Integer lineCount = (lineCountByPackage.get(otherPackageName));
                    lineCountForPackage += lineCount.intValue();
                }
            }
            
            entry.setInfoCount(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_INFO));
            entry.setLowCount(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_LOW));
            entry.setModerateCount(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_MODERATE));
            entry.setSignificantCount(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT));
            entry.setHighCount(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_HIGH));
            entry.setLineCount(lineCountForPackage);
            
            entries.add(entry);
    	}        

    	currentRunSummary = entries.toArray(new PackageSummary[entries.size()]);
    	
    	// Now add them to the summary map
    	addSummariesToSummaryMap(currentRunSummary);
	}
	
	/**
	 * Adds the given package summaries to the summary by package map.
	 * @param summaries the summaries to add
	 */
	private void addSummariesToSummaryMap(final PackageSummary[] summaries)
	{
	    for (int i = 0; i < summaries.length; i++)
	    {
	        PackageSummary summary = summaries[i];
	        List<PackageSummary> summariesForPackage = summaryByPackage.get(summary.getPackageName());
	        
	        if (summariesForPackage == null)
	        {
	            summariesForPackage = new ArrayList<PackageSummary>();
	            summaryByPackage.put(summary.getPackageName(), summariesForPackage);	            
	        }
	        
	        summariesForPackage.add(summary);
	    }	    
	}	
}
