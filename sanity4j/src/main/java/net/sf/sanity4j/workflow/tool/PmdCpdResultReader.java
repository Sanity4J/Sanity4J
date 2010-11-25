package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sf.sanity4j.gen.pmdcpd_4_2_1.Duplication;
import net.sf.sanity4j.gen.pmdcpd_4_2_1.PmdCpd;
import net.sf.sanity4j.model.diagnostic.Diagnostic;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.JaxbMarshaller;
import net.sf.sanity4j.util.QAException;

/**
 * PmdCpdResultReader - Translates PMD CPD results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class PmdCpdResultReader implements ResultReader
{
    /** The rule name to use for Diagnostics, as PMD-CPD doesn't have rules. */
    private static final String PMD_CPD_RULE_NAME = "DoNotCopyAndPasteCode";

    /** The ExtractStats to add the results to. */
    private ExtractStats stats;
    /** The PMD CPD XML result file to read from. */
    private File pmdCpdResultFile;

    /** {@inheritDoc} */
    public void setResultFile(final File resultFile)
    {
        this.pmdCpdResultFile = resultFile;
    }

    /** {@inheritDoc} */
    public void setStats(final ExtractStats stats)
    {
        this.stats = stats;
    }

    /**
     * Extracts PMD statistics from the pmdCpdResultFile.
     */
    public void run()
    {
        DiagnosticSet diagnostics = stats.getDiagnostics();

        PmdCpd result = (PmdCpd)
            JaxbMarshaller.unmarshal(pmdCpdResultFile, "net.sf.sanity4j.gen.pmdcpd_4_2_1", "http://net.sf.sanity4j/namespace/pmdcpd-4.2.1");

        List<Duplication> duplications = result.getDuplication();

        for (Duplication duplication : duplications)
        {
            try
            {
                net.sf.sanity4j.gen.pmdcpd_4_2_1.File file1 = duplication.getFile().get(0);
                net.sf.sanity4j.gen.pmdcpd_4_2_1.File file2 = duplication.getFile().get(1);
                String fileName1 = stats.getCanonicalPath(file1.getPath()).substring(stats.getSourceDirectory().length() + 1);
                String fileName2 = stats.getCanonicalPath(file2.getPath()).substring(stats.getSourceDirectory().length() + 1);

                // severity is based on the size of the duplication
                int severity = duplication.getLines().intValue() < 100
                               ? Diagnostic.SEVERITY_LOW
                               : Diagnostic.SEVERITY_MODERATE;

                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setSource(Diagnostic.SOURCE_PMD_CPD);
                diagnostic.setRuleName(PMD_CPD_RULE_NAME);
                diagnostic.setSeverity(severity);
                diagnostic.setFileName(stats.getCanonicalPath(file1.getPath()));
                diagnostic.setStartLine(file1.getLine().intValue());
                diagnostic.setEndLine(file1.getLine().intValue() + duplication.getLines().intValue());
                diagnostic.setMessage("Duplicate of " + fileName2 + ":\n" + duplication.getCodefragment());

                // CPD is source file based, so guess the class name
                diagnostic.setClassName(stats.getClassNameForSourcePath(diagnostic.getFileName()));

                diagnostics.add(diagnostic);

                diagnostic = new Diagnostic();
                diagnostic.setSource(Diagnostic.SOURCE_PMD_CPD);
                diagnostic.setRuleName(PMD_CPD_RULE_NAME);
                diagnostic.setSeverity(severity);
                diagnostic.setFileName(stats.getCanonicalPath(file2.getPath()));
                diagnostic.setStartLine(file2.getLine().intValue());
                diagnostic.setEndLine(file2.getLine().intValue() + duplication.getLines().intValue());
                diagnostic.setMessage("Duplicate of " + fileName1 + ":\n" + duplication.getCodefragment());
                diagnostic.setClassName(stats.getClassNameForSourcePath(diagnostic.getFileName()));

                // CPD is source file based, so guess the class name
                diagnostic.setClassName(stats.getClassNameForSourcePath(diagnostic.getFileName()));

                diagnostics.add(diagnostic);
            }
            catch (IOException e)
            {
                throw new QAException("Failed to obtain canonical path", e);
            }
        }
    }

    /**
     * @return the description of this WorkUnit
     */
    public String getDescription()
    {
        return "Reading PMD CPD results";
    }
}
