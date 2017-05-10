package com.github.sanity4j.workflow.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticFactory;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.util.ExtractStats;
import com.github.sanity4j.util.JaxbMarshaller;
import com.github.sanity4j.util.QAException;

import com.github.sanity4j.gen.pmd_4_2_1.Pmd;
import com.github.sanity4j.gen.pmd_4_2_1.Suppressedviolation;
import com.github.sanity4j.gen.pmd_4_2_1.Violation;

/**
 * PmdResultReader - Translates PMD results into the common format used by the QA tool.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class PmdResultReader implements ResultReader
{
    /** The size of the internal copy buffer. */
    private static final int BUFFER_SIZE = 4096;
    
    /** The properties used to configure this {@link ResultReader}. */
    private final Properties properties = new Properties();

    /** The ExtractStats to add the results to. */
    private ExtractStats stats;

    /** The PMD XML result file to read from. */
    private File pmdResultFile;

    /**
     *  Retrieve the contents of a file as a String.  If an IOException is encountered, return the stack trace as the result.
     *  
     *  @param file The file whose contents are to be read.
     *  
     *  @return A String representing the contents of the given <em>file</em>
     */
    private static String fileToString(final File file)
    {
        String string = "";
        FileInputStream fis = null;
        
        try
        {
            byte[] buffer = new byte[BUFFER_SIZE];
            fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = fis.read(buffer);
            
            while (read > 0)
            {
                baos.write(buffer, 0, read);
                read = fis.read(buffer);
            }
        
            string = new String(baos.toByteArray());
        }
        catch (IOException ioe)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(baos);
            stream.flush();
            string = new String(baos.toByteArray());
        }
        finally 
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException ioe)
                {
                    // Ignore
                }
            }
        }
        
        return string;
    }
    
    /** {@inheritDoc} */
    public void setProperties(final Properties properties)
    {
        this.properties.putAll(properties);
    }

    /** {@inheritDoc} */
    public void setResultFile(final File resultFile)
    {
        this.pmdResultFile = resultFile;
    }

    /** {@inheritDoc} */
    public void setStats(final ExtractStats stats)
    {
        this.stats = stats;
    }

    /**
     * Extracts the PMD statistics from the pmdResultFile.
     */
    public void run()
    {
        DiagnosticFactory diagnosticFactory = DiagnosticFactory.getInstance(properties);
        DiagnosticSet diagnostics = stats.getDiagnostics();

        Pmd result = null;
        
        try
        {
            result = (Pmd) JaxbMarshaller.unmarshal(pmdResultFile, "com.github.sanity4j.gen.pmd_4_2_1",
                                                    "http://com.github.sanity4j/namespace/pmd-4.2.1");
        }
        catch (Exception e)
        {
            String content = fileToString(pmdResultFile);
            throw new QAException("Error parsing PMD Result file [" + pmdResultFile + "] [" + content + "]", e);
        }
        
        List<com.github.sanity4j.gen.pmd_4_2_1.File> files = result.getFile();

        for (com.github.sanity4j.gen.pmd_4_2_1.File file : files)
        {
            for (Violation violation : file.getViolation())
            {
                Diagnostic diagnostic = diagnosticFactory.getDiagnostic();
                diagnostic.setSource(Diagnostic.SOURCE_PMD);

                try
                {
                    diagnostic.setFileName(stats.getCanonicalPath(file.getName()));
                }
                catch (IOException e)
                {
                    throw new QAException("Failed to obtain canonical path for " + file.getName(), e);
                }

                diagnostic.setStartLine(violation.getBeginline().intValue());
                diagnostic.setEndLine(violation.getEndline().intValue());
                diagnostic.setStartColumn(violation.getBegincolumn().intValue());
                diagnostic.setEndColumn(violation.getEndcolumn().intValue());
                diagnostic.setRuleName(violation.getRule());
                diagnostic.setMessage(violation.getValue());
                diagnostic.calcSeverity();

                // Some diagnostics don't include the class name,
                // but we can determine it from the source name.
                if (violation.getClassName() == null)
                {
                    String className = stats.getClassNameForSourcePath(diagnostic.getFileName());
                    diagnostic.setClassName(className);
                }
                else
                {
                    diagnostic.setClassName(violation.getPackage() + '.' + violation.getClassName());
                }

                diagnostics.add(diagnostic);
            }
        }

        // Do not suppress violations
        for (Suppressedviolation violation : result.getSuppressedviolation())
        {
            Diagnostic diagnostic = diagnosticFactory.getDiagnostic();

            try
            {
                diagnostic.setFileName(stats.getCanonicalPath(violation.getFilename()));
            }
            catch (IOException e)
            {
                throw new QAException("Failed to obtain canonical path for " + violation.getFilename(), e);
            }

            diagnostic.setSource(Diagnostic.SOURCE_OTHER);
            diagnostic.setRuleName("DoNotSuppressWarnings");
            diagnostic.setMessage("Do not use the SuppressWarnings annotation");
            diagnostic.setClassName(stats.getClassNameForSourcePath(diagnostic.getFileName()));
            diagnostic.calcSeverity();
            diagnostic.setStartLine(1);
            diagnostic.setEndLine(1);
            diagnostics.add(diagnostic);
        }
    }

    /**
     * @return the description of this WorkUnit
     */
    public String getDescription()
    {
        return "Reading PMD results";
    }
}
