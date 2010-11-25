package net.sf.sanity4j.report;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import net.sf.sanity4j.model.coverage.CoverageItf;
import net.sf.sanity4j.model.diagnostic.Diagnostic;
import net.sf.sanity4j.model.diagnostic.DiagnosticCategory;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet;
import net.sf.sanity4j.model.summary.PackageSummary;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.StringUtil;
import net.sf.sanity4j.workflow.QAProcessor;


/**
 * PackageWriter writes diagnostics by package.
 *  
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class PackageWriter 
{
    /** The ImageIO image format to use for the package summary graph. */
    private static final String IMAGE_FORMAT = "PNG";
    /** The file suffix for the package summary graph image file. */
    private static final String IMAGE_FORMAT_FILE_SUFFIX = ".png";
    
    /** Source file paths, keyed by package name. */
    private final Map<String, List<String>> sourcesByPackage;
    
    /** The diagnostics that we are writing. */
    private final ExtractStats stats;
    
    /** The destination directory. */
    private final File reportDir;

    /** The tools to include in the tool summary. */
    private static final int[] TOOLS = 
    {
        Diagnostic.SOURCE_FINDBUGS,
        Diagnostic.SOURCE_PMD,
        Diagnostic.SOURCE_PMD_CPD,
        Diagnostic.SOURCE_CHECKSTYLE,
        Diagnostic.SOURCE_OTHER
    };
    
    /**
     * Creates a PackageWriter.
     * 
     * @param stats the stats utility containing the results
     * @param reportDir the base directory for the report
     * @param sourcesByPackage a map of source files by package name
     */
    public PackageWriter(final ExtractStats stats, final File reportDir, 
                         final Map<String, List<String>> sourcesByPackage)
    {
        this.stats = stats;
        this.reportDir = reportDir;        
        this.sourcesByPackage = sourcesByPackage;
    }
    
    /**
     * Writes the output for a package to the given directory.
     * 
     * @param packageName the package name
     * @throws IOException if there is an error writing any file
     */
    public void writePackage(final String packageName) throws IOException
    {
        if (StringUtil.empty(packageName))
        {
            // Write package frame to : "allclasses-frame.xml"
            String fileName = "allclasses-frame.xml";                            
            String xml = generatePackageFrame("");
            FileUtil.writeToFile(xml, new File(reportDir, fileName));
            
            // Write package overview
            fileName = "overview-summary.xml";          
            xml = generateSummaryPageForPackage("", fileName);
            FileUtil.writeToFile(xml, new File(reportDir, fileName));
            
            // Write package by rule
            fileName = "package-by-rule.xml";          
            xml = generatePackageByRule("");
            FileUtil.writeToFile(xml, new File(reportDir, fileName));
        }
        else
        {
            // Write package frame to : "com/foobar/mypackage/package-frame.xml"
            String fileName = packageName.replace('.', File.separatorChar) + "/package-frame.xml";                            
            String xml = generatePackageFrame(packageName);
            FileUtil.writeToFile(xml, new File(reportDir, fileName));
            
            // Write package summary to "com/foobar/mypackage/package-summary.xml"
            fileName = packageName.replace('.', File.separatorChar) + "/package-summary.xml";           
            xml = generateSummaryPageForPackage(packageName, fileName);
            FileUtil.writeToFile(xml, new File(reportDir, fileName));
            
            // Write package by rule
            fileName = packageName.replace('.', File.separatorChar) + "/package-by-rule.xml";           
            xml = generatePackageByRule(packageName);
            FileUtil.writeToFile(xml, new File(reportDir, fileName));
        }       
        
        // Write categories
        CategoryWriter categoryWriter = new CategoryWriter(stats, reportDir);
        categoryWriter.writeCategories(packageName);
    }
    
    /**
     * Generates the package frame page, to be written to 
     * com/foobar/mypackage/package-frame.xml .
     *  
     * @param packageName the name of the package to create the frame for
     * @return the frame page XML
     */
    private String generatePackageFrame(final String packageName)
    {
        // For the package name com.bar.foo, we need "../../../"
        String pathToRoot = StringUtil.empty(packageName) 
                          ? "" 
                          : (packageName.replaceAll("[^\\.]", "").replaceAll("\\.", "../") + "../");
        
        StringBuffer html = new StringBuffer();

        // Write top-level package summary info        
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(pathToRoot).append("xslt/package-frame.xsl\"?>\n")        
            .append("<packageClasses packageName=\"").append(StringUtil.empty(packageName) ? "" : packageName).append("\" pathToRoot=\"")
            .append(pathToRoot).append("\" qaVersion=\"").append(QAProcessor.QA_VERSION)
            .append("\" runDate=\"").append(new Date()).append("\">\n");
        
        List<String> packageSources;
        
        if (StringUtil.empty(packageName))
        {
            // The default package sources should list everything
            packageSources = new ArrayList<String>();
            
            for (List<String> packageSource : sourcesByPackage.values())
            {
                packageSources.addAll(packageSource);
            }
            
            // Sort by the class name only - ignoring the package
            Collections.sort(packageSources, 
                new Comparator<String>()
                {
                    public int compare(final String obj1, final String obj2)
                    {
                        String name1 = obj1.substring(obj1.lastIndexOf(File.separatorChar) + 1);
                        String name2 = obj2.substring(obj2.lastIndexOf(File.separatorChar) + 1);
        
                        return name1.compareTo(name2);
                    }
                }
            );
        }
        else
        {
            packageSources = sourcesByPackage.get(packageName);
        }
        
        for (String sourcePath : packageSources)
        {
            String className = ReportUtil.getRelativeSourcePath(stats.getSourceDirectory(), sourcePath).replaceAll(".xml", "").replaceAll("[\\\\/]", ".");
            DiagnosticSet diagsForFile = stats.getDiagnostics().getDiagnosticsForFile(sourcePath);
            
            html.append("<class name=\"").append(className)
                .append("\" high=\"").append(diagsForFile.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
                .append("\" significant=\"").append(diagsForFile.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
                .append("\" moderate=\"").append(diagsForFile.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
                .append("\" low=\"").append(diagsForFile.getCountForSeverity(Diagnostic.SEVERITY_LOW))
                .append("\" info=\"").append(diagsForFile.getCountForSeverity(Diagnostic.SEVERITY_INFO))
                .append("\"/>\n");
        }
        
        html.append("</packageClasses>\n");
        
        return html.toString();
    }
    
    /**
     * Generates the package by rule file, to be written to 
     * com/foobar/mypackage/package-by-rule.xml .
     *  
     * @param packageName the name of the package to create the frame for
     * @return the export XML
     */
    private String generatePackageByRule(final String packageName) 
    {
        // For the package name com.bar.foo, we need "../../../"
        String pathToRoot = StringUtil.empty(packageName) 
                          ? "" 
                          : (packageName.replaceAll("[^\\.]", "").replaceAll("\\.", "../") + "../");
        
        DiagnosticSet diags = (StringUtil.empty(packageName))
                              ? stats.getDiagnostics()
                              : stats.getDiagnostics().getDiagnosticsForPackage(packageName);
        
        StringBuffer html = new StringBuffer();

        // Write top-level package summary info        
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(pathToRoot).append("xslt/package-by-rule.xsl\"?>\n")        
            .append("<packageByRule packageName=\"").append(StringUtil.empty(packageName) ? "" : packageName).append("\" pathToRoot=\"")
            .append(pathToRoot).append("\" qaVersion=\"").append(QAProcessor.QA_VERSION)
            .append("\" runDate=\"").append(new Date()).append("\">\n");

        // Hash the diagnostics by rule name, to condense the report
        Map<String, List<Diagnostic>> diagnosticsByRuleName = new HashMap<String, List<Diagnostic>>();
        
        for (Diagnostic diagnostic : diags)
        {
            String key = diagnostic.getRuleName();
            
            if (key == null)
            {
                key = "(none)";
            }
            
            List<Diagnostic> list = diagnosticsByRuleName.get(key);
            
            if (list == null)
            {
                list = new ArrayList<Diagnostic>();
                diagnosticsByRuleName.put(key, list);
            }
            
            list.add(diagnostic);
        }
        
        List<String> ruleNames = new ArrayList<String>(diagnosticsByRuleName.keySet());
        Collections.sort(ruleNames);

        // Output diagnostics (if any)
        for (String ruleName : ruleNames)
        {
            List<Diagnostic> diagnosticsForRule = diagnosticsByRuleName.get(ruleName);
            Diagnostic firstDiag = diagnosticsForRule.get(0);
            
            html.append("<rule name=\"").append(ruleName)
                .append("\" tool=\"").append(Diagnostic.getSourceDescription(firstDiag.getSource()))
                .append("\" severity=\"").append(firstDiag.getSeverity())
                .append("\">\n");
            
            Map<String, List<Diagnostic>> diagnosticsByClassName = ReportUtil.mapDiagnosticsByClassName(diagnosticsForRule); 
            List<String> classNames = new ArrayList<String>(diagnosticsByClassName.keySet());
            Collections.sort(classNames);
            
            for (String className : classNames)
            {
                html.append("<class name=\"").append(className).append("\">\n");
                
                for (Diagnostic diag : diagnosticsByClassName.get(className))
                {
                    html.append("<diag id=\"").append(diag.getId()).append("\"/>\n");
                }
                
                html.append("</class>\n");
            }
            
            html.append("</rule>\n");
        }        
        
        
        html.append("</packageByRule>\n");
        
        return html.toString();
    }
    
    /**
     * Generates the summary page for a package.
     * 
     * @param packageName the package name, or null to create a summary for all packages
     * @param relativeFileName the name of the file being written to, used to create relative hyperlinks 
     * @return the generated page in a String
     */
    private String generateSummaryPageForPackage(final String packageName, 
                                                 final String relativeFileName)
    {
        String pathToRoot = ReportUtil.getHtmlPathToRoot(relativeFileName);
        
        DiagnosticSet diags = (StringUtil.empty(packageName))
                              ? stats.getDiagnostics()
                              : stats.getDiagnostics().getDiagnosticsForPackage(packageName);

        StringBuffer html = new StringBuffer();
                              
        // Write top-level package summary info        
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(pathToRoot).append("xslt/package-summary.xsl\"?>\n")        
            .append("<packageSummary packageName=\"").append(StringUtil.empty(packageName) ? "" : packageName).append("\" pathToRoot=\"")
            .append(pathToRoot).append("\" qaVersion=\"").append(QAProcessor.QA_VERSION)
            .append("\" runDate=\"").append(new Date()).append("\">\n");
        
        html.append("<issueSummary high=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
            .append("\" significant=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
            .append("\" moderate=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
            .append("\" low=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_LOW))
            .append("\" info=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_INFO)).append("\">\n");
        
        // Output category information
        DiagnosticCategory categories = new DiagnosticCategory();
        
        for (Diagnostic diag : diags)
        {
            categories.addDiagnostic(diag);
        }
        
        outputCategory(categories, html);

        // Output tool summary information
        for (int i = 0; i < TOOLS.length; i++)
        {
            outputTool(TOOLS[i], diags, html);
        }
        
        html.append("</issueSummary>\n");
        
        // Quality summary
        html.append("<qualitySummary>\n");
        outputQualitySummary(packageName, html);
        html.append("</qualitySummary>\n");
        
        // Summary image
        html.append("<graphs>\n");
        appendSummaryImage(packageName, html);
        html.append("</graphs>\n");
        
        html.append("</packageSummary>\n");
        
        return html.toString();
    }
    
    /**
     * Outputs tool summary information.
     * 
     * @param tool the tool to output the information for.
     * @param diags the full list of diagnostics for the package being written.
     * @param html the StringBuffer to append XML output to.
     */
    private void outputTool(final int tool, final DiagnosticSet diags, final StringBuffer html)
    {
        DiagnosticSet toolDiags = diags.getDiagnosticsForTool(tool);
        
        if (!toolDiags.isEmpty())
        {
            String toolName = Diagnostic.getSourceDescription(tool);
            
            html.append("<tool name=\"").append(toolName)
                .append("\" high=\"").append(toolDiags.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
                .append("\" significant=\"").append(toolDiags.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
                .append("\" moderate=\"").append(toolDiags.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
                .append("\" low=\"").append(toolDiags.getCountForSeverity(Diagnostic.SEVERITY_LOW))
                .append("\" info=\"").append(toolDiags.getCountForSeverity(Diagnostic.SEVERITY_INFO)).append("\"/>\n");
        }
    }
    
    /**
     * Outputs category information, recursing for sub-categories.
     * 
     * @param category the DiagnosticCategory to output.
     * @param html the StringBuffer to append XML output to.
     */
    private void outputCategory(final DiagnosticCategory category, final StringBuffer html)
    {
        DiagnosticSet diags = new DiagnosticSet();
        
        for (Diagnostic diag : category.getDiagnostics())
        {
            diags.add(diag);
        }
        
        // Output category
        html.append("<category name=\"").append(category.getName());          
        
        html.append("\" high=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
            .append("\" significant=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
            .append("\" moderate=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
            .append("\" low=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_LOW))
            .append("\" info=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_INFO)).append('"');
 
        if (category.getSubCategories().isEmpty())
        {
            html.append("/>\n");
        }
        else
        {
            html.append(">\n");
            
            for (Iterator<DiagnosticCategory> i = category.subCategories(); i.hasNext();)
            {
                DiagnosticCategory subCategory = i.next();
                outputCategory(subCategory, html);
            }
            
            html.append("</category>\n");
        }        
    }
    
    /**
     * If there is sufficient data available, 
     * append a summary image to the given HTML StringBuffer.
     * 
     * @param packageName the package name
     * @param html the string buffer to append the image tag to
     */
    private void appendSummaryImage(final String packageName, final StringBuffer html)
    {
        String nonNullPackageName = StringUtil.empty(packageName) ? "" : packageName;
        PackageSummary[] summaries = stats.getPackageSummary(nonNullPackageName);
        
        // Only generate the image if there's more than one run
        if (summaries.length > 1)
        {
            BufferedImage image = ChartFactory.createImage(summaries, nonNullPackageName);
            
            try
            {
                String path = nonNullPackageName.replace('.', File.separatorChar) + File.separatorChar;             
                String fileName = "summary" + IMAGE_FORMAT_FILE_SUFFIX;             
                ImageIO.write(image, IMAGE_FORMAT, new File(reportDir, path + fileName));

                html.append("<graph path=\"").append(fileName)
                    .append("\" alt=\"History for ").append(nonNullPackageName) 
                    .append("\"/>\n");
            }
            catch (IOException e)
            {
                QaLogger.getInstance().error("Error writing summary image", e);
            }
        }       
    }
    
    /**
     * Generates the quality summary table HTML.
     * 
     * @param packageName the package name to create the tables for
     * @param html the StringBuffer to append XML output to.
     */
    private void outputQualitySummary(final String packageName, final StringBuffer html)
    {
        // Display package and subpackages first
        Set<String> packageNames = new TreeSet<String>(sourcesByPackage.keySet());
        
        // Output summary for all classes
        if (StringUtil.empty(packageName))
        {
            appendPackageQualityStatsRow("", "", html);
        }
        
        String packageNamePlusDot = packageName + '.';
        
        for (String otherPackageName : packageNames)
        {
            if (StringUtil.empty(packageName) || otherPackageName.equals(packageName) 
                || otherPackageName.startsWith(packageNamePlusDot))
            {
                appendPackageQualityStatsRow(StringUtil.empty(packageName) ? "" : packageName, otherPackageName, html);
            }
        }
        
        // Display classes in package
        List<String> packageSources = sourcesByPackage.get(packageName);
        
        if (packageSources != null)
        {
            for (String sourcePath : packageSources)
            {
                appendClassQualityStatsRow(sourcePath, html);
            }       
        }
        else
        {
            appendClassQualityStatsRow(null, html);
        }
    }
    
    /**
     * Appends a package quality statistics row to the given string buffer.
     * 
     * @param currentPackageName the current package name
     * @param packageName the package name to write the stats for, may be a sub-package
     * @param html the string buffer to append to
     */
    private void appendPackageQualityStatsRow(final String currentPackageName, 
                                              final String packageName, 
                                              final StringBuffer html)
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
            coveredLinePct = (int) (coverage.getLineCoverage() * 100.0);
            coveredBranchPct = (int) (coverage.getBranchCoverage() * 100.0);
            coveredLines = coverage.getCoveredLineCount();
            branchCount = coverage.getBranchCount();
            coveredBranchCount = coverage.getCoveredBranchCount();
            numExecutableLines = coverage.getLineCount();
        }

        html.append("<package name=\"").append(packageName);
        
        if (!allPackages && !packageName.equals(currentPackageName))
        {
            // Link to the other package
            int dotLength = currentPackageName.length() > 0 ? 1 : 0;
            String relativePath = packageName.substring(currentPackageName.length() + dotLength).replace('.', '/');

            html.append("\" path=\"").append(relativePath);
        }

        html.append("\" classes=\"").append(classCount)
            .append("\" lineCount=\"").append(numLines);
        
        // Quality
        int qualityPct = (int) (100 * ReportUtil.evaluateMetric("quality", diags, numLines));
        
        html.append("\" high=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
            .append("\" significant=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
            .append("\" moderate=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
            .append("\" low=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_LOW))
            .append("\" info=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_INFO))
            .append("\" quality=\"").append(qualityPct)
            .append('"');
        
        // Test coverage
        if (numExecutableLines == 0)
        {
            html.append("/>\n");
        }
        else
        {
            html.append(">\n");
            
            html.append("<testCoverage lineCoveragePct=\"").append(coveredLinePct)
                .append("\" branchCoveragePct=\"").append(coveredBranchPct)
                .append("\" lineCount=\"").append(numExecutableLines)
                .append("\" coveredLineCount=\"").append(coveredLines)
                .append("\" branchCount=\"").append(branchCount)
                .append("\" coveredBranchCount=\"").append(coveredBranchCount)
                .append("\"/>\n");
            
            html.append("</package>\n");
        }
    }
    
    /**
     * Appends a class quality statistics row to the given string buffer.
     * 
     * @param sourcePath the source file to write the stats for
     * @param html the string buffer to append to
     */
    private void appendClassQualityStatsRow(final String sourcePath, final StringBuffer html)
    {
        if (sourcePath != null)
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
                coveredLinePct = (int) (coverage.getLineCoverage() * 100.0);
                coveredBranchPct = (int) (coverage.getBranchCoverage() * 100.0);
            }
            
            
            // Quality
            int qualityPct = (int) (100 * ReportUtil.evaluateMetric("quality", diags, numLines));
            
            html.append("<class name=\"").append(className.substring(className.lastIndexOf('.') + 1))
                .append("\" high=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
                .append("\" significant=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
                .append("\" moderate=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
                .append("\" low=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_LOW))
                .append("\" info=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_INFO))
                .append("\" quality=\"").append(qualityPct)
                .append('"');
            
            // Test coverage
            if (numExecutableLines == 0)
            {
                html.append("/>\n");
            }
            else
            {
                html.append(">\n");
                
                html.append("<testCoverage lineCoveragePct=\"").append(coveredLinePct)
                    .append("\" branchCoveragePct=\"").append(coveredBranchPct)
                    .append("\" lineCount=\"").append(numExecutableLines)
                    .append("\" coveredLineCount=\"").append(coveredLines)
                    .append("\" branchCount=\"").append(branchCount)
                    .append("\" coveredBranchCount=\"").append(coveredBranchCount)
                    .append("\"/>\n");
                
                html.append("</class>\n");
            }
        }   
    }
}
