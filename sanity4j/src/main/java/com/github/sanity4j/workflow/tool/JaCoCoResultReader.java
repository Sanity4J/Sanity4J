package com.github.sanity4j.workflow.tool;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;

import com.github.sanity4j.model.coverage.ClassCoverage;
import com.github.sanity4j.model.coverage.Coverage;
import com.github.sanity4j.model.coverage.PackageCoverage;
import com.github.sanity4j.util.ExtractStats;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaUtil;
import com.github.sanity4j.workflow.QAConfig;

/**
 * JaCoCoResultReader - Translates JaCoco results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.8
 */
public class JaCoCoResultReader implements ResultReader
{
    /** The config for the current run. */
    private static QAConfig config;

    /** The properties used to configure this {@link ResultReader}. */
    private final Properties properties = new Properties();
   
    /** The ExtractStats to add the results to. */
    private ExtractStats stats;

    /** The JaCoCo result file to read from. */
    private File jacocoResultFile;

    /** {@inheritDoc} */
    @Override
    public void setProperties(final Properties properties) 
    {
        this.properties.putAll(properties);
    }

    /** {@inheritDoc} */
    @Override
    public void setResultFile(final File resultFile)
    {
        this.jacocoResultFile = resultFile;
    }

    /** {@inheritDoc} */
    @Override
    public void setStats(final ExtractStats stats)
    {
        this.stats = stats;
    }

    /**
     * Extracts the Coverage statistics from the jacocoResultFile.
     */
    @Override
    public void run()
    {
       Coverage coverage = stats.getCoverage();
       
       // Read coverage data
       ExecutionDataStore executionData = new ExecutionDataStore();
       final Set<String> classes = new HashSet<String>();
       readExecutionFiles(executionData, classes);
       
       // Process individual classes
       for (String clazz : classes)
       {
          processClass(clazz, executionData, coverage);
       }
       
       // Produce summarise package info
       for (String packageName : coverage.getPackageNames())
       {
          int coveredLines = 0;
          int totalLines = 0;
          int coveredBranches = 0;
          int totalBranches = 0;
          
          PackageCoverage packageCoverage = coverage.getPackageCoverage(packageName);
          
          for (String className : packageCoverage.getClassNames())
          {
             ClassCoverage classCoverage = packageCoverage.getClassCoverage(className);
             
             coveredLines += classCoverage.getCoveredLineCount();
             totalLines += classCoverage.getLineCount();
             
             coveredBranches += classCoverage.getCoveredBranchCount();
             totalBranches += classCoverage.getBranchCount();
          }
          
          packageCoverage.setLineCoverage(coveredLines / (double) totalLines);
          packageCoverage.setBranchCoverage(coveredBranches / (double) totalBranches);
       }
    }
    
    private void processClass(final String clazz, final ExecutionDataStore executionData, final Coverage coverage)
    {
       FileInputStream in = null;
       
       try
       {
          File classFile = new File(config.getCombinedClassDir(), clazz.replace('.', '/') + ".class");
          
          if (classFile.exists())
          {
             in = new FileInputStream(classFile);
             final CoverageBuilder coverageBuilder = new CoverageBuilder();
             final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
             analyzer.analyzeClass(in, clazz);

             // Read class info
             for (final IClassCoverage cc : coverageBuilder.getClasses()) 
             {
                final String className = cc.getName().replace('/', '.');
                final String enclosingClass = className.replaceAll("\\$.*", "");
                final int dotIndex = enclosingClass.lastIndexOf('.');
                final String packageName = dotIndex == -1 ? "" : enclosingClass.substring(0, dotIndex);
                
                PackageCoverage packageCoverage = coverage.getPackageCoverage(packageName);
                
                if (packageCoverage == null)
                {
                   packageCoverage = new PackageCoverage(packageName);
                   coverage.addPackage(packageCoverage);
                }

                ClassCoverage classCoverage = packageCoverage.getClassCoverage(enclosingClass);

                if (classCoverage == null)
                {
                    classCoverage = new ClassCoverage(enclosingClass);
                    packageCoverage.addClass(classCoverage);
                }

                int coveredBranchCount = 0;
                int totalBranchCount = 0;
                int coveredLineCount = 0;
                int totalLineCount = 0;
                
                for (int lineNum = cc.getFirstLine(); lineNum <= cc.getLastLine(); lineNum++) 
                {
                   ILine line = cc.getLine(lineNum);
                   
                   if (line.getStatus() != ICounter.EMPTY)
                   {
                      totalLineCount++;
                      boolean isBranch = line.getBranchCounter().getTotalCount() + line.getBranchCounter().getMissedCount() > 1;
                      
                      switch (line.getStatus()) 
                      {
                         case ICounter.NOT_COVERED:
                            classCoverage.addLineCoverage(lineNum, 0, isBranch);
                            break;
                            
                         case ICounter.PARTLY_COVERED:
                         case ICounter.FULLY_COVERED:
                            classCoverage.addLineCoverage(lineNum, 1, isBranch);
                            coveredLineCount++;
                            break;
                      }
                      
                      if (isBranch)
                      {
                         totalBranchCount += line.getBranchCounter().getTotalCount();
                         coveredBranchCount += line.getBranchCounter().getCoveredCount();
                         classCoverage.addBranchCoverage(lineNum, line.getBranchCounter().getCoveredRatio());
                      }
                   }
                }
                
                // Summarise class info
                classCoverage.setLineCoverage(coveredLineCount / (double) totalLineCount);
                classCoverage.setBranchCoverage(coveredBranchCount / (double) totalBranchCount);
             }
          }
       }
       catch (Exception e)
       {
          throw new QAException("Failed to analyse class: " + clazz, e);
       }
       finally
       {
          QaUtil.safeClose(in);
       }
    }

    /**
     * Reads in the JaCoCo execution data files and adds them to the combined data store.
     * 
     * @param executionData the execution data.
     * @param classes the combined set of classes which were found in the data.
     */
    private static void readExecutionFiles(final ExecutionDataStore executionData, final Set<String> classes)
    {
       // TODO: This won't work for paths containing a space
       //        Add a new method to QAConfig to retrieve the list of files directly.
       String mergeFiles = config.getCoverageMergeDataFiles();
       String[] coverageDataFiles = mergeFiles == null ? null : mergeFiles.split(" ");
       
       for (String file : coverageDataFiles)
       {
          FileInputStream in = null;
          
          try
          {
             in = new FileInputStream(file);
             
             final ExecutionDataReader reader = new ExecutionDataReader(in);
             
             reader.setSessionInfoVisitor(new ISessionInfoVisitor()
             {
                 @Override
                 public void visitSessionInfo(final SessionInfo info)
                 { 
                     // Does nothing, but required by JaCoCo
                 }
             });
            
             reader.setExecutionDataVisitor(new IExecutionDataVisitor()
             {
                 @Override
                 public void visitClassExecution(final ExecutionData data)
                 {
                     classes.add(data.getName());
                     executionData.put(data);
                 }
             });
            
             reader.read();
             in.close();
          }
          catch (Exception e)
          {
             throw new QAException("Error reading JaCoco Result file [" + file + "]", e);
          }
          finally
          {
             QaUtil.safeClose(in);
          }
       }
   }

    /**
     * @return the description of this WorkUnit
     */
    @Override
    public String getDescription()
    {
        return "Reading JaCoco results";
    }

    /**
     * Sets the config used on this run.
     * @param config the config to set.
     */
    public static void setConfig(final QAConfig config)
    {
        JaCoCoResultReader.config = config;
    }
}
