package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBElement;

import net.sf.sanity4j.gen.findbugs_1_3_9.BugCollection;
import net.sf.sanity4j.gen.findbugs_1_3_9.BugInstance;
import net.sf.sanity4j.gen.findbugs_1_3_9.Clazz;
import net.sf.sanity4j.gen.findbugs_1_3_9.SourceLine;
import net.sf.sanity4j.model.diagnostic.Diagnostic;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.JaxbMarshaller;
import net.sf.sanity4j.util.QAException;

/**
 * FindBugsResultReader - Translates FindBugs results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class FindBugsResultReader implements ResultReader
{
    /** The ExtractStats to add the results to. */
    private ExtractStats stats;
    /** The FindBugs XML result file to read from. */
    private File findBugsResultFile;

    /** {@inheritDoc} */
    public void setResultFile(final File resultFile)
    {
        this.findBugsResultFile = resultFile;
    }

    /** {@inheritDoc} */
    public void setStats(final ExtractStats stats)
    {
        this.stats = stats;
    }

    /**
     * Extracts FindBugs statistics from findBugsResultFile.
     */
    public void run()
    {
        DiagnosticSet diagnostics = stats.getDiagnostics();

        BugCollection result = (BugCollection)
        JaxbMarshaller.unmarshal(findBugsResultFile, "net.sf.sanity4j.gen.findbugs_1_3_9", "http://net.sf.sanity4j/namespace/findbugs-1.3.9");

        List<BugInstance> bugs = result.getBugInstance();

        for (BugInstance bug : bugs)
        {
            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setSource(Diagnostic.SOURCE_FINDBUGS);
            diagnostic.setRuleName(bug.getType());
            diagnostic.calcSeverity();

            // Unfortunately, there is a whole set of information that may or
            // may not be present. We have to iterate through the whole list
            // and pick out the summary information that we're interested in.
            List<Object> details = bug.getShortMessageOrLongMessageOrClazz();

            for (Object detail : details)
            {
                if (detail instanceof Clazz)
                {
                    Clazz clazz = (Clazz) detail;

                    if (Boolean.TRUE.equals(clazz.getPrimary()))
                    {
                        diagnostic.setClassName(clazz.getClassname());

                        SourceLine sourceLine = clazz.getSourceLine();

                        if (sourceLine != null && sourceLine.getSourcepath() != null)
                        {
                            try
                            {
                                diagnostic.setFileName(stats.getCanonicalPath(sourceLine.getSourcepath()));
                            }
                            catch (IOException e)
                            {
                                throw new QAException("Failed to get canonicalPath for "
                                                         + sourceLine.getSourcepath(), e);
                            }

							if (sourceLine.getStart() != null && sourceLine.getEnd() != 0
								&& sourceLine.getStart() != 0 && sourceLine.getEnd() != 0)
							{
								diagnostic.setStartLine(sourceLine.getStart());
								diagnostic.setEndLine(sourceLine.getEnd());
							}
                        }
                    }
                }
                else if (detail instanceof SourceLine)
                {
                    // The SourceLine further narrows down where the problem is... in most cases
                    SourceLine sourceLine = (SourceLine) detail;

                    if (sourceLine.getStart() != null && sourceLine.getEnd() != 0
                        && sourceLine.getStart() != 0 && sourceLine.getEnd() != 0)
                    {
                        diagnostic.setStartLine(sourceLine.getStart());
                        diagnostic.setEndLine(sourceLine.getEnd());
                    }
                }
                else if (detail instanceof JAXBElement<?>)
                {
                    JAXBElement<?> message = (JAXBElement<?>) detail;
                    boolean shortMessage = message.getName().getLocalPart().equals("ShortMessage");
                    boolean longMessage = message.getName().getLocalPart().equals("LongMessage");

                    // We don't want to overwrite a long message
                    if (longMessage || (shortMessage && diagnostic.getMessage() == null))
                    {
                        diagnostic.setMessage(message.getValue().toString());
                    }
                }
            }

            diagnostics.add(diagnostic);
        }
    }

    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Reading FindBugs results";
    }
}
