package net.sf.sanity4j.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.sanity4j.model.coverage.CoverageItf;
import net.sf.sanity4j.model.diagnostic.Diagnostic;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.StringUtil;
import net.sf.sanity4j.workflow.QAProcessor;


/**
 * ExportWriter writes an export of the entire run to a single XML file. 
 * The output file is <code>export.xml</code>, in the report directory.
 *  
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class ExportWriter 
{
    /** Source file paths, keyed by package name. */
	private final Map<String, List<String>> sourcesByPackage;
    
    /** The diagnostics that we are writing. */
    private final ExtractStats stats;
    
    /** The destination directory. */
    private final File reportDir;
    
    /** One hundred. */
    private static final double HUNDRED = 100.0;

    /** One hundred. */
    private static final int ONE_HUNDRED = 100;
    
    /**
     * Creates a ExportWriter.
     * 
     * @param stats the stats utility containing the results
     * @param reportDir the base directory for the report
     * @param sourcesByPackage a map of source files by package name
     */
	public ExportWriter(final ExtractStats stats, final File reportDir, 
                         final Map<String, List<String>> sourcesByPackage)
	{
        this.stats = stats;
        this.reportDir = reportDir;        
		this.sourcesByPackage = sourcesByPackage;
	}
	
	/**
	 * Writes the export file to the report directory.
	 * @throws IOException if there is an error writing any file
	 */
	public void writeExport() throws IOException
	{
        StringBuffer xml = new StringBuffer();

        // Write top-level package summary info        
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<?xml-stylesheet type=\"text/xsl\" href=\"xslt/package-frame.xsl\"?>\n")        
            .append("<export qaVersion=\"").append(QAProcessor.QA_VERSION)
            .append("\" runDate=\"").append(new Date()).append("\">\n");

        // Recursively write all packages
        writePackage("", xml);
        
        xml.append("</export>\n");
        
        FileUtil.writeToFile(xml.toString(), new File(reportDir, "export.xml"));
	}
	
	/**
	 * Writes out the findings for a single package.
	 * 
	 * @param packageName the name of the package to write
	 * @param xml the StringBuffer to write the XML to.
	 */
	private void writePackage(final String packageName, final StringBuffer xml)
	{
        boolean allPackages = "".equals(packageName);

        DiagnosticSet diags = null;
        CoverageItf coverage = null;
        int numLines = 0;
        int numExecutableLines = 0;
        int coveredLines = 0;
        int coveredLinePct = 0;
        int coveredBranchPct = 0;
        int branchCount = 0;
        int coveredBranchCount = 0;
        int classCount = 0;

        if (allPackages)
        {
            coverage = stats.getCoverage();
            diags = stats.getDiagnostics();
            numLines = stats.getLineCount();
            classCount = stats.getClassCount();
        }
        else
        {
            coverage = stats.getCoverage().getPackageCoverage(packageName);
            diags = stats.getDiagnostics().getDiagnosticsForPackage(packageName, false);

            numLines = stats.getPackageLineCount(packageName);
            classCount = stats.getPackageClassCount(packageName);
        }

        if (coverage != null)
        {
            coveredLines = (int) Math.round(numLines * coverage.getLineCoverage());
            coveredLinePct = (int) (coverage.getLineCoverage() * HUNDRED);
            coveredBranchPct = (int) (coverage.getBranchCoverage() * HUNDRED);
            coveredLines = coverage.getCoveredLineCount();
            branchCount = coverage.getBranchCount();
            coveredBranchCount = coverage.getCoveredBranchCount();
            numExecutableLines = coverage.getLineCount();
        }

        xml.append("<package name=\"").append(packageName);
        xml.append("\" classes=\"").append(classCount)
           .append("\" lineCount=\"").append(numLines);
        
        // Quality
        int qualityPct = (int) (ONE_HUNDRED * ReportUtil.evaluateMetric("quality", diags, numLines));
        
        xml.append("\" high=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
            .append("\" significant=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
            .append("\" moderate=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
            .append("\" low=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_LOW))
            .append("\" info=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_INFO))
            .append("\" quality=\"").append(qualityPct)
            .append("\">\n");
        
        // Test coverage
        if (numExecutableLines != 0)
        {
            xml.append("<testCoverage lineCoveragePct=\"").append(coveredLinePct)
                .append("\" branchCoveragePct=\"").append(coveredBranchPct)
                .append("\" lineCount=\"").append(numExecutableLines)
                .append("\" coveredLineCount=\"").append(coveredLines)
                .append("\" branchCount=\"").append(branchCount)
                .append("\" coveredBranchCount=\"").append(coveredBranchCount)
                .append("\"/>\n");
        }
        
        // Classes
        
        if (sourcesByPackage.containsKey(packageName))
        {
            writePackageClasses(packageName, xml);        
        }

        // Sub-packages
        String packagePrefix = StringUtil.empty(packageName) ? "" : packageName + '.';
        List<String> allSubPackages = new ArrayList<String>();
        
        for (String otherPackageName : sourcesByPackage.keySet())
        {
            // Find all sub-packages.
            if (!otherPackageName.equals(packageName) && otherPackageName.startsWith(packagePrefix))
            {
                allSubPackages.add(otherPackageName);
            }
        }
        
        // Only write sub-packages without any intermediate packages between them and this package.
        for (String subPackage : allSubPackages)
        {
            boolean hasIntermediatePackage = false;
            
            for (String otherSubPackage : allSubPackages)
            {
                if (!subPackage.equals(otherSubPackage) && subPackage.startsWith(otherSubPackage))
                {
                    hasIntermediatePackage = true;
                    break;
                }   
            }
            
            if (!hasIntermediatePackage)
            {
                writePackage(subPackage, xml);
            }
        }
        
        xml.append("</package>\n");
	}
	
    /**
     * Writes out the findings for the classes in a single package.
     * 
     * @param packageName the name of the package to write
     * @param xml the StringBuffer to write the XML to.
     */
    private void writePackageClasses(final String packageName, final StringBuffer xml)
    {
        List<String> packageSources = sourcesByPackage.get(packageName);
        
        for (String sourcePath : packageSources)
        {
            DiagnosticSet diags = stats.getDiagnostics().getDiagnosticsForFile(sourcePath);
            String className = stats.getClassNameForSourcePath(sourcePath);     
            CoverageItf coverage = stats.getCoverage().getClassCoverage(className);
            
            int numLines = stats.getClassLineCount(className);
            int numExecutableLines = 0;
            int coveredLines = 0;
            int coveredLinePct = 0;
            int coveredBranchPct = 0;
            int branchCount = 0;
            int coveredBranchCount = 0;
            
            if (coverage != null)
            {
                numExecutableLines = coverage.getLineCount();
                coveredLines = coverage.getCoveredLineCount();
                branchCount = coverage.getBranchCount();
                coveredBranchCount = coverage.getCoveredBranchCount();
                coveredLinePct = (int) (coverage.getLineCoverage() * HUNDRED);
                coveredBranchPct = (int) (coverage.getBranchCoverage() * HUNDRED);
            }
            
            
            // Quality
            int qualityPct = (int) (ONE_HUNDRED * ReportUtil.evaluateMetric("quality", diags, numLines));
            
            xml.append("<class name=\"").append(className.substring(className.lastIndexOf('.') + 1))
                .append("\" lineCount=\"").append(numLines)
                .append("\" high=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
                .append("\" significant=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
                .append("\" moderate=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
                .append("\" low=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_LOW))
                .append("\" info=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_INFO))
                .append("\" quality=\"").append(qualityPct)
                .append("\">\n");
            
            // Test coverage
            if (numExecutableLines != 0)
            {
                xml.append("<testCoverage lineCoveragePct=\"").append(coveredLinePct)
                    .append("\" branchCoveragePct=\"").append(coveredBranchPct)
                    .append("\" lineCount=\"").append(numExecutableLines)
                    .append("\" coveredLineCount=\"").append(coveredLines)
                    .append("\" branchCount=\"").append(branchCount)
                    .append("\" coveredBranchCount=\"").append(coveredBranchCount)
                    .append("\"/>\n");
            }

            // Write diagnostics for class
            for (Diagnostic diagnostic : diags)
            {
                xml.append("<diag id=\"").append(diagnostic.getId())
                                          .append("\" sev=\"").append(diagnostic.getSeverity())
                                          .append("\" tool=\"").append(diagnostic.getSourceDescription())
                                          .append("\" rule=\"").append(diagnostic.getRuleName())
                                          .append("\">");

                if (diagnostic.getMessage() != null)
                {
                    xml.append(ReportUtil.htmlEscape(diagnostic.getMessage().trim()));
                }

                xml.append("</diag>\n");
            }
            
            xml.append("</class>\n");
        }
    }	
}
