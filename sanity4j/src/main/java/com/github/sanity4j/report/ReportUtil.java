package com.github.sanity4j.report; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.model.summary.PackageSummary;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaUtil;
import com.github.sanity4j.util.math.InfixExpression;
import com.github.sanity4j.util.math.SyntaxException;


/** 
 * General utility methods used by the report classes. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public final class ReportUtil
{
    /** The reporting properties, such as metrics. */
    private static Properties properties = QaUtil.getProperties("/net/sf/sanity4j/report/report.properties");
    
    /** ReportUtil should not be instantiated. */
    private ReportUtil()
    {
    }
    
    /**
     * Return the path to the report "root" directory for the given path.
     * 
     * @param relativeFileName a path to a file, relative to the root dir
     * @return the relative path to the root directory
     */
    public static String getHtmlPathToRoot(final String relativeFileName)
    {
        StringBuffer pathToRoot = new StringBuffer();
        
        // For the path com/bar/foo/Foobar.html, we need "../../../"
        int depth = new StringTokenizer(relativeFileName, "/\\").countTokens();
        
        while (--depth > 0)
        {
            pathToRoot.append("../");
        }
    
        return pathToRoot.toString();
    }

    /**
     * Escapes (certain) special characters in HTML commonly found in source files.
     * 
     * @param text the String to escape
     * @return the escaped String
     */
    public static String htmlEscape(final String text)
    {
        if (text == null || text.length() == 0)
        {
            return text;
        }
        
        final int len = text.length();
        StringBuffer out = new StringBuffer(len);
        
        for (int i = 0; i < len; i++)
        {
            char chr = text.charAt(i);
            
            switch (chr)
            {
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '&':
                    out.append("&amp;");
                    break;
                
                default:
                    out.append(chr);
            }
        }
        
        return out.toString();
    }
    
    /**
     * Produces a Map of Diagnostic lists, keyed by class name.
     * 
     * @param diagnostics the diagnostics to map
     * @return a Map containing Lists of Diagnostics, keyed by class name
     */
    public static Map<String, List<Diagnostic>> mapDiagnosticsByClassName(final List<Diagnostic> diagnostics)
    {
        Map<String, List<Diagnostic>> diagnosticsByClassName = new HashMap<String, List<Diagnostic>>();
        
        for (Diagnostic diagnostic : diagnostics)
        {
            List<Diagnostic> list = diagnosticsByClassName.get(diagnostic.getClassName());
            
            if (list == null)
            {
                list = new ArrayList<Diagnostic>();
                diagnosticsByClassName.put(diagnostic.getClassName(), list);
            }
            
            list.add(diagnostic);
        }
        
        return diagnosticsByClassName;
    }    
        
    /**
     * Returns the path of a source file, relative to the source directory.
     * 
     * @param basePath the path of the source directory
     * @param sourcePath the absolute path to the source file
     * @return the path of the source file, relative to the source directory 
     */
    public static String getRelativeSourcePath(final String basePath, final String sourcePath)
    {
        if (!sourcePath.startsWith(basePath))
        {
            throw new IllegalArgumentException("File " + sourcePath + " is not within the source directory");
        }
        
        int basePathLength = basePath.length() + 1;
        return sourcePath.substring(basePathLength).replaceAll("\\.java\\z", ".xml");
    }
    
    /**
     * Evaluates the given metric for a package/class with the given set of diagnostics.
     * 
     * @param metric the metric name, as defined in report.properties
     * @param diagnostics the diagnostics for the package/class
     * @param linesOfCode the number of lines of code
     * @return the value of the given metric, or -1 on error.
     */
    public static double evaluateMetric(final String metric, final DiagnosticSet diagnostics, final int linesOfCode)
    {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("highCount", diagnostics.getCountForSeverity(Diagnostic.SEVERITY_HIGH));
        variables.put("significantCount", diagnostics.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT));        
        variables.put("moderateCount", diagnostics.getCountForSeverity(Diagnostic.SEVERITY_MODERATE));
        variables.put("lowCount", diagnostics.getCountForSeverity(Diagnostic.SEVERITY_LOW));
        variables.put("infoCount", diagnostics.getCountForSeverity(Diagnostic.SEVERITY_INFO));
        variables.put("linesOfCode", linesOfCode);

        return evaluateMetric(metric, variables);
    }
    
    /**
     * Evaluates the given metric for a package/class with the given set of diagnostics.
     * 
     * @param metric the metric name, as defined in report.properties
     * @param summary the summary for the package
     * @return the value of the given metric, or -1 on error.
     */
    public static double evaluateMetric(final String metric, final PackageSummary summary)
    {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("highCount", summary.getHighCount());
        variables.put("significantCount", summary.getSignificantCount());        
        variables.put("moderateCount", summary.getModerateCount());
        variables.put("lowCount", summary.getLowCount());
        variables.put("infoCount", summary.getInfoCount());
        variables.put("linesOfCode", summary.getLineCount());

        return evaluateMetric(metric, variables);
    }
    
    /**
     * Evaluates the given metric for a package/class with the given set of diagnostics.
     * This version is used by the other <code>evaluateMetric</code> methods in this class,
     * which build up the <code>variables</code> map depending on the data they have.
     * 
     * @param metric the metric name, as defined in report.properties.
     * @param variables the map of variables to substitute.
     * @return the value of the given metric, or -1 on error.
     */
    private static double evaluateMetric(final String metric, final Map<String, Object> variables)
    {
        String expression = (String) properties.get("sanity4j.report.metric." + metric + ".expression");
        
        try
        {
            return Math.max(0.0, new InfixExpression(expression).evaluate(variables));
        }
        catch (SyntaxException e)
        {
            QaLogger.getInstance().error("Syntax exception running: " + expression, e);
            return -1.0;
        }
    }
}
