package com.github.sanity4j.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.util.ExtractStats;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.workflow.QAConfig;
import com.github.sanity4j.workflow.QAProcessor;


/**
 * ReportWriter - utility class to write the combined report.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class ReportWriter
{
    /** A list of package names that need to be written. */
    private final List<String> packages = new ArrayList<String>();
    
    /** A list of source file paths that need to be written. */
    private final List<String> sources = new ArrayList<String>();
    
    /** Source file paths, keyed by their package name. */
    private final Map<String, List<String>> sourcesByPackage = new HashMap<String, List<String>>();

    /** The diagnostics that we are writing. */
    private final ExtractStats stats;

    /** Whether or not diagnostics should be printed first or last. */
    private final boolean diagnosticsFirst;
    
    /** The destination directory. */
    private final File reportDir;

    /**
     * Creates a ReportWriter.
     * 
     * @param stats the stats utility containing the results
     * @param diagnosticsFirst flag for diagnostics first
     * @param reportDir the base directory for the report
     */
    public ReportWriter(final ExtractStats stats, final boolean diagnosticsFirst, final File reportDir)
    {
        this.stats = stats;
        this.diagnosticsFirst = diagnosticsFirst;
        this.reportDir = reportDir;
    }
    
    /**
     * Produces the combined report in the given directory.
     * 
     * @param config The configuration object for the Sanity4J tool.
     * @throws IOException if there is an error writing any file.
     */
    public void produceReport(final QAConfig config) throws IOException
    {
        findSourceFiles(new File(stats.getSourceDirectory()));

        Collections.sort(packages);
        Collections.sort(sources);

        if (!reportDir.exists() && !reportDir.mkdirs())
        {
            throw new IOException("Failed to create report directory: " + reportDir);
        }

        // Write export
        QaLogger.getInstance().debug("Writing export");
        ExportWriter writer = new ExportWriter(stats, reportDir, sourcesByPackage);
        writer.writeExport();
        
        // Set up the default frames
        QaLogger.getInstance().debug("Extracting static content");
        ExtractStaticContent.extractContent(config, reportDir, "ExtractStaticContent.properties");

        writePackagesFrame();
        
        // Write rule catalogue
        QaLogger.getInstance().debug("Writing rule catalogue");
        RuleCatalogueWriter ruleCatalogueWriter = new RuleCatalogueWriter(reportDir);
        ruleCatalogueWriter.writeRuleCatalogue();

        // Write categories
        QaLogger.getInstance().debug("Writing top-level categories");
        CategoryWriter categoryWriter = new CategoryWriter(stats, reportDir);
        categoryWriter.writeCategories(null);

        // Write packages
        QaLogger.getInstance().debug("Writing top-level package summary");
        PackageWriter packageWriter = new PackageWriter(stats, reportDir, sourcesByPackage);
        packageWriter.writePackage(""); // write top-level package

        for (String packageName : packages)
        {
            QaLogger.getInstance().debug("Writing package " + packageName);
            packageWriter.writePackage(packageName);
        }
        
        // Write sources
        JavaSourceWriter sourceWriter = new JavaSourceWriter(stats, diagnosticsFirst, reportDir);

        for (String sourceName : sources)
        {
            QaLogger.getInstance().debug("Writing source for " + sourceName);
            sourceWriter.writeSourceFile(sourceName);
        }
        
        QaLogger.getInstance().info("Report generated successfully in " + reportDir);
    }

    /** 
     * Build up the list of packages, source files and 
     * source files by package.
     * 
     * @param base the directory to search
     */
    private void findSourceFiles(final File base)
    {
        // Determine package name
        String packageName = stats.getPackageName(base.getPath());
        boolean packageAdded = false;

        List<String> sourcesForPackage = null;

        // Iterate for all files and sub-dirs
        File[] files = base.listFiles();

        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    findSourceFiles(files[i]);
                }
                else if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".java"))
                {
                    String filePath = null;

                    try
                    {
                        filePath = files[i].getCanonicalPath();
                    }
                    catch (IOException e)
                    {
                        throw new QAException("Failed to get canonical path for " + files[i].getPath(), e);
                    }

                    sources.add(filePath);

                    if (!packageAdded)
                    {
                        packages.add(packageName);
                        packageAdded = true;

                        sourcesForPackage = new ArrayList<String>();
                        sourcesByPackage.put(packageName, sourcesForPackage);
                    }

                    sourcesForPackage.add(filePath);
                }
            }
        }

        if (sourcesForPackage != null)
        {
            Collections.sort(sourcesForPackage);
        }
    }

    /**
     * Writes the packages frame.
     * 
     * @throws IOException if there is an error writing any of the package frame files
     */
    private void writePackagesFrame() throws IOException
    {
        StringBuffer html = new StringBuffer();

        // Write top-level package summary info        
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                  + "<?xml-stylesheet type=\"text/xsl\" href=\"xslt/overview-frame.xsl\"?>\n"        
                  + "<packages qaVersion=\"").append(QAProcessor.QA_VERSION)
            .append("\" runDate=\"").append(new Date()).append("\">\n");
        
        for (String packageName : packages)
        {
            DiagnosticSet diagsForPackage = stats.getDiagnostics().getDiagnosticsForPackage(packageName);

            html.append("<package name=\"").append(packageName)
                .append("\" high=\"").append(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_HIGH))
                .append("\" significant=\"").append(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_SIGNIFICANT))
                .append("\" moderate=\"").append(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_MODERATE))
                .append("\" low=\"").append(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_LOW))
                .append("\" info=\"").append(diagsForPackage.getCountForSeverity(Diagnostic.SEVERITY_INFO))
                .append("\"/>\n");
        }
        
        html.append("</packages>\n");
        
        File destFile = new File(reportDir, "overview-frame.xml");
        FileUtil.writeToFile(html.toString(), destFile);

    }
}
