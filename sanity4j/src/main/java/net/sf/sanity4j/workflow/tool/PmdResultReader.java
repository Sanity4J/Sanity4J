package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sf.sanity4j.gen.pmd_4_2_1.Pmd;
import net.sf.sanity4j.gen.pmd_4_2_1.Suppressedviolation;
import net.sf.sanity4j.gen.pmd_4_2_1.Violation;
import net.sf.sanity4j.model.diagnostic.Diagnostic;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.JaxbMarshaller;
import net.sf.sanity4j.util.QAException;

/**
 * PmdResultReader - Translates PMD results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class PmdResultReader implements ResultReader
{
    /** The ExtractStats to add the results to. */
    private ExtractStats stats;
    /** The PMD XML result file to read from. */
    private File pmdResultFile;

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
        DiagnosticSet diagnostics = stats.getDiagnostics();

        Pmd result = (Pmd) JaxbMarshaller.unmarshal(pmdResultFile, "net.sf.sanity4j.gen.pmd_4_2_1", "http://net.sf.sanity4j/namespace/pmd-4.2.1");

        List<net.sf.sanity4j.gen.pmd_4_2_1.File> files = result.getFile();

        for (net.sf.sanity4j.gen.pmd_4_2_1.File file : files)
        {
            for (Violation violation : file.getViolation())
            {
                Diagnostic diagnostic = new Diagnostic();
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
                    diagnostic.setClassName(violation.getPackage()
                                            + '.' + violation.getClassName());
                }

                diagnostics.add(diagnostic);
            }
        }

        // Do not suppress violations
        for (Suppressedviolation violation : result.getSuppressedviolation())
        {
            Diagnostic diagnostic = new Diagnostic();

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
