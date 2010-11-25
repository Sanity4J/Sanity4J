package net.sf.sanity4j.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.sf.sanity4j.model.coverage.ClassCoverage;
import net.sf.sanity4j.model.diagnostic.Diagnostic;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.util.StringUtil;
import net.sf.sanity4j.workflow.QAProcessor;

/**
 * JavaSourceWriter writes out java source.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class JavaSourceWriter
{   
    /** The diagnostics that we are writing. */
    private final ExtractStats stats;
    
    /** The destination directory. */
    private final File reportDir;
    
    /**
     * Creates a JavaSourceWriter.
     * 
     * @param stats the stats utility containing the results
     * @param reportDir the base directory for the report
     */
    public JavaSourceWriter(final ExtractStats stats, final File reportDir)
    {
        this.stats = stats;
        this.reportDir = reportDir;
    }
    
    /**
     * Annotates the given source file and places the output in the given directory.
     * 
     * @param sourcePath the path to the source file
     * @throws IOException if there is an error reading/writing
     */
    public void writeSourceFile(final String sourcePath) throws IOException
    {
        // Source and destination files
        File sourceFile = new File(sourcePath);
        String relativeDestPath = ReportUtil.getRelativeSourcePath(stats.getSourceDirectory(), sourcePath);
        String pathToRoot = ReportUtil.getHtmlPathToRoot(relativeDestPath);
        File destFile = new File(reportDir, relativeDestPath);

        // Buffer and diagnostics for this source file
        StringBuffer html = new StringBuffer((int) sourceFile.length());
        DiagnosticSet diags = stats.getDiagnostics().getDiagnosticsForFile(sourcePath);
        List<Diagnostic> orderedDiags = sortDiags(diags);

        // We always use the enclosing class's name for coverage
        String className = stats.getClassNameForSourcePath(sourcePath);
        ClassCoverage coverage = stats.getCoverage().getClassCoverage(className);

        int lineCount = stats.getClassLineCount(className);

        // Write Header        
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        html.append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(pathToRoot).append("xslt/source-code.xsl\"?>\n");        
        html.append("<classDetails className=\"").append(className).append("\" pathToRoot=\"")
            .append(pathToRoot).append("\" qaVersion=\"").append(QAProcessor.QA_VERSION)
            .append("\" runDate=\"").append(new Date()).append("\">\n");
        
        html.append("<summary high=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
            .append("\" significant=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
            .append("\" moderate=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
            .append("\" low=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_LOW))
            .append("\" info=\"").append(diags.getCountForSeverity(Diagnostic.SEVERITY_INFO));
        
        if (coverage != null)
        {
            html.append("\" lineCoverage=\"").append((int) (100 * coverage.getLineCoverage()))
                .append("\" branchCoverage=\"").append((int) (100 * coverage.getBranchCoverage()));
        }
        
        html.append("\" quality=\"").append((int) (ReportUtil.evaluateMetric("quality", diags, lineCount) * 100.0))
            .append("\"/>\n");
        
        writeSourceLines(sourceFile, diags, orderedDiags, coverage, html);
        writeErrorsSummary(orderedDiags, html);

        html.append("</classDetails>\n");
        
        // Write to file "com/foo/FooBar.xml"
        FileUtil.writeToFile(html.toString(), destFile);
    }

    /**
     * Writes the source code information.
     * 
     * @param sourceFile the source file
     * @param diags the set of diagnostics for the file
     * @param orderedDiags the diagnostics for the file, sorted by line number
     * @param coverage the coverage statistics for the file
     * @param html the buffer to write the output to.
     * 
     * @throws IOException if there is an error reading from the source file
     */
    private void writeSourceLines(final File sourceFile, final DiagnosticSet diags, final List<Diagnostic> orderedDiags, final ClassCoverage coverage, final StringBuffer html) throws IOException
    {
        FileInputStream fis = new FileInputStream(sourceFile);
        
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            int lineNum = 1;
            int orderedDiagPos = 0;
            
            html.append("<source>\n");
    
            // Output source code, with line numbers + errors
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                // Max severity for this line
                int maxSev = -1;
    
                for (Diagnostic diagnostic : diags)
                {
                    if (diagnostic.getStartLine() <= lineNum && diagnostic.getEndLine() >= lineNum)
                    {
                        maxSev = Math.max(maxSev, diagnostic.getSeverity());
                    }
                }
                
                html.append("<line");
                
                if (maxSev != -1)
                {
                    html.append(" sev=\"").append(maxSev).append('"');
                }
                
                if (coverage != null && coverage.getInvocationsForLine(lineNum) >= 0)
                {
                    int invocations = coverage.getInvocationsForLine(lineNum);
                    double branchCoverage = coverage.getBranchCoverageForLine(lineNum);
                    
                    if (invocations == 0)
                    {
                        // Not covered at all
                        html.append(" covered=\"no\"");
                    }
                    else if (branchCoverage == -1.0 || branchCoverage == 1.0)
                    {
                        // Not a branch, or branch covered 100%
                        html.append(" covered=\"yes\"");
                    }
                    else
                    {
                        // Partially covered
                        html.append(" covered=\"partial\"");
                    }
                }
                
                boolean hasContent = false;
                
                for (int i = orderedDiagPos; i < orderedDiags.size(); i++)
                {
                    Diagnostic diagnostic = orderedDiags.get(i);
    
                    if (lineNum == diagnostic.getStartLine())
                    {
                        if (!hasContent)
                        {
                            html.append('>');
                            hasContent = true;
                        }
                        
                        html.append("<diag id=\"").append(diagnostic.getId()).append("\"/>");
                        orderedDiagPos++;
                    }
                    else
                    {
                        // Since the list is ordered, the next diagnostic won't match either
                        break;
                    }
                }
                
                if (!StringUtil.empty(line))
                {
                    if (!hasContent)
                    {
                        html.append('>');
                        hasContent = true;
                    }
                    
                    html.append(ReportUtil.htmlEscape(line));
                }
                
                html.append(hasContent ? "</line>\n" : "/>\n");
                lineNum++;
            }
            
            html.append("</source>\n");
        }
        finally
        {
            QaUtil.safeClose(fis);
        }
    }
    
    /**
     * Writes a diagnostic summary block at the bottom of the source page.
     * 
     * @param orderedDiags a list of diagnostics for the file, in line number order.
     * @param html the buffer to write the html output to.
     */
    private void writeErrorsSummary(final List<Diagnostic> orderedDiags, final StringBuffer html)
    {
        if (orderedDiags != null && !orderedDiags.isEmpty())
        {
            html.append("<diags>\n");
            
            for (Diagnostic diagnostic : orderedDiags)
            {
                html.append("<diag id=\"").append(diagnostic.getId())
                                          .append("\" sev=\"").append(diagnostic.getSeverity())
                                          .append("\" tool=\"").append(diagnostic.getSourceDescription())
                                          .append("\" rule=\"").append(diagnostic.getRuleName())
                                          .append("\">");

                html.append(ReportUtil.htmlEscape(diagnostic.getMessage()));

                html.append("</diag>\n");
            }
            
            html.append("</diags>\n");
        }
    }
    
    /**
     * Sorts the diagnostics by starting line number.
     * 
     * @param diags the diagnostics to sort
     * @return a list containing the diagnostics, sorted by ascending start line
     */
    private List<Diagnostic> sortDiags(final DiagnosticSet diags)
    {
        List<Diagnostic> sortedDiags = new ArrayList<Diagnostic>(diags.size());
        
        for (Diagnostic diag : diags)
        {
            sortedDiags.add(diag);
        }
        
        Collections.sort(sortedDiags, new DiagnosticStartLineComparator());
        return sortedDiags;
    }

    /**
     * A comparator to sort Diagnostics based on their start line.
     */
    private static final class DiagnosticStartLineComparator implements Comparator<Diagnostic>, Serializable 
    {
        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         *
         * @param diag1 the first object to be compared.
         * @param diag2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second. 
         */
        public int compare(final Diagnostic diag1, final Diagnostic diag2)
        {
            if (diag1 == null)
            {
                return diag2 == null ? 0 : -1;
            }
            
            return diag1.getStartLine() - diag2.getStartLine();
        }
    }
}
