package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import net.sf.sanity4j.gen.checkstyle_4_4.Checkstyle;
import net.sf.sanity4j.gen.checkstyle_4_4.Error;
import net.sf.sanity4j.model.diagnostic.Diagnostic;
import net.sf.sanity4j.model.diagnostic.DiagnosticFactory;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.JaxbMarshaller;
import net.sf.sanity4j.util.QAException;

/**
 * CheckStyleResultReader - Translates CheckStyle results into the common format used by the QA tool.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class CheckStyleResultReader implements ResultReader
{
    /** The properties used to configure the {@link CheckStyleResultReader}. */
    private final Properties properties = new Properties();

    /** The ExtractStats to add the results to. */
    private ExtractStats stats;

    /** The CheckStyle XML result file to read from. */
    private File checkStyleResultFile;

    /** {@inheritDoc} */
    public void setProperties(final Properties properties)
    {
        this.properties.putAll(properties);
    }

    /** {@inheritDoc} */
    public void setResultFile(final File resultFile)
    {
        this.checkStyleResultFile = resultFile;
    }

    /** {@inheritDoc} */
    public void setStats(final ExtractStats stats)
    {
        this.stats = stats;
    }

    /**
     * Extracts CheckStyle statistics from the given file.
     */
    public void run()
    {
        DiagnosticFactory diagnosticFactory = DiagnosticFactory.getInstance(properties);
        DiagnosticSet diagnostics = stats.getDiagnostics();

        Checkstyle result = (Checkstyle) JaxbMarshaller.unmarshal(checkStyleResultFile,
                                                                  "net.sf.sanity4j.gen.checkstyle_4_4",
                                                                  "http://net.sf.sanity4j/namespace/checkstyle-4.4");

        List<net.sf.sanity4j.gen.checkstyle_4_4.File> files = result.getFile();

        for (net.sf.sanity4j.gen.checkstyle_4_4.File file : files)
        {
            List<Object> errors = file.getContent();

            for (Object obj : errors)
            {
                if (obj instanceof Error)
                {
                    Error error = (Error) obj;

                    // Work-around for Checkstyle bugs
                    if (error.getLine().intValue() == 0)
                    {
                        if (error.getMessage().startsWith("Got an exception - java.lang.RuntimeException:"))
                        {
                            // Checkstyle fails on methods that throw custom exception types
                            // http://sourceforge.net/tracker/index.php?func=detail&aid=1462282&group_id=29721&atid=397078
                            continue;
                        }
                        else
                        {
                            // Some findings are reported as line 0, so change this
                            // to line 1 so that they appear on reports
                            error.setLine(BigInteger.ONE);
                        }
                    }

                    Diagnostic diagnostic = diagnosticFactory.getDiagnostic();
                    diagnostic.setSource(Diagnostic.SOURCE_CHECKSTYLE);

                    try
                    {
                        diagnostic.setFileName(stats.getCanonicalPath(file.getName()));
                    }
                    catch (IOException e)
                    {
                        throw new QAException("Failed to get canonicalPath for " + file.getName(), e);
                    }

                    diagnostic.setStartLine(error.getLine().intValue());
                    diagnostic.setMessage(error.getMessage());

                    String ruleName = error.getSource();
                    ruleName = ruleName.substring(ruleName.lastIndexOf('.') + 1);
                    diagnostic.setRuleName(ruleName);
                    diagnostic.calcSeverity();

                    if (error.getColumn() != null)
                    {
                        diagnostic.setStartColumn(error.getColumn().intValue());
                    }

                    // Checkstyle is source file based, so guess the class name
                    diagnostic.setClassName(stats.getClassNameForSourcePath(diagnostic.getFileName()));

                    diagnostics.add(diagnostic);
                }
            }
        }
    }

    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Reading CheckStyle results";
    }

}
