package com.github.sanity4j.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticCategory;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.util.ExtractStats;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.StringUtil;
import com.github.sanity4j.workflow.QAProcessor;


/**
 * CategoryWriter writes diagnostics by category.
 *  
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class CategoryWriter 
{
    /** The diagnostics that we are writing. */
    private final ExtractStats stats;
    
    /** The destination directory. */
    private final File reportDir;
    
    /**
     * Creates a category writer for the given stats.
     * 
     * @param stats the stats utility containing the results
     * @param reportDir the base directory for the report
     */
    public CategoryWriter(final ExtractStats stats, final File reportDir)
    {
        this.stats = stats;
        this.reportDir = reportDir;
    }

    /**
     * Writes the output for a package to e.g. "com/foobar/mypackage/package-by-category.xml"
     * 
     * @param packageName the package name
     * @throws IOException if there is an error writing to the file
     */
    public void writeCategories(final String packageName) throws IOException
    {
        String fileName = "package-by-category.xml";
        
        if (packageName != null)
        {
        	StringBuilder buf = new StringBuilder();
        	buf.append(packageName.replace('.', File.separatorChar))
        		.append(File.separatorChar)
        		.append(fileName);
        	
            fileName = buf.toString();  
        }

        String summary = generateCategoryPage(packageName);       
        FileUtil.writeToFile(summary, new File(reportDir, fileName));
    }       
    
    /**
     * Generates the category XML.
     * 
     * @param packageName the package name, or null to create a summary for all packages.
     * @return the generated XML
     */
    private String generateCategoryPage(final String packageName)
    {
        // For the package name com.bar.foo, we need "../../../"
        String pathToRoot = StringUtil.empty(packageName) 
                          ? "" 
                          : (packageName.replaceAll("[^\\.]", "").replaceAll("\\.", "../") + "../");
        
        StringBuilder xml = new StringBuilder();

        // Write top-level package summary info        
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(pathToRoot).append("xslt/package-by-category.xsl\"?>\n")        
           .append("<packageByCategory packageName=\"").append(packageName == null ? "" : packageName).append("\" pathToRoot=\"")
           .append(pathToRoot).append("\" qaVersion=\"").append(QAProcessor.QA_VERSION)
           .append("\" runDate=\"").append(new Date()).append("\">\n");

        // Get diagnostics for the given package
        DiagnosticSet diags = stats.getDiagnostics();

        if (!StringUtil.empty(packageName))
        {
            diags = diags.getDiagnosticsForPackage(packageName);
        }
        
        // Build up categories & generate XML
        DiagnosticCategory category = new DiagnosticCategory();
        
        for (Iterator<Diagnostic> i = diags.iterator(); i.hasNext();)
        {
            category.addDiagnostic(i.next());
        }               
        
        generateCategoryDetails(category, xml);
        
        xml.append("</packageByCategory>\n"); 

        return xml.toString();
    }
    
    /**
     * Generates category details XML.
     * 
     * @param category the Diagnostic category
     * @param xml the string buffer to append the XML output to
     */
    private void generateCategoryDetails(final DiagnosticCategory category, 
                                          final StringBuilder xml)
    {
        xml.append("<category name=\"").append(category.getName()).append("\">\n");
                
        // Output diagnostics (if any)
        Map<String, List<Diagnostic>> diagnosticsByRuleName = new HashMap<String, List<Diagnostic>>();
        
        for (Diagnostic diagnostic : category.getDiagnostics())
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
            
            xml.append("<rule name=\"").append(ruleName)
               .append("\" tool=\"").append(firstDiag.getSourceDescription())
               .append("\" severity=\"").append(firstDiag.getSeverity())
               .append("\">\n");
            
            Map<String, List<Diagnostic>> diagnosticsByClassName = ReportUtil.mapDiagnosticsByClassName(diagnosticsForRule); 
            List<String> classNames = new ArrayList<String>(diagnosticsByClassName.keySet());
            Collections.sort(classNames);
            
            for (String className : classNames)
            {
                xml.append("<class name=\"").append(className).append("\">\n");
                
                for (Diagnostic diag : diagnosticsByClassName.get(className))
                {
                    xml.append("<diag id=\"").append(diag.getId()).append("\"/>\n");
                }
                
                xml.append("</class>\n");
            }
            
            xml.append("</rule>\n");
        }       
        
        // Output subcategories (if any)
        for (Iterator<DiagnosticCategory> i = category.subCategoriesIterator(); i.hasNext();)
        {
            DiagnosticCategory subCategory = i.next();
            generateCategoryDetails(subCategory, xml);
        }
        
        xml.append("</category>\n");
    }       
}
