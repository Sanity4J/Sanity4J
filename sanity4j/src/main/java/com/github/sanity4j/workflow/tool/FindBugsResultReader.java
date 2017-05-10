package com.github.sanity4j.workflow.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBElement;

import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticFactory;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.util.ExtractStats;
import com.github.sanity4j.util.JaxbMarshaller;
import com.github.sanity4j.util.QAException;

import com.github.sanity4j.gen.findbugs_1_3_9.BugCollection;
import com.github.sanity4j.gen.findbugs_1_3_9.BugInstance;
import com.github.sanity4j.gen.findbugs_1_3_9.Clazz;
import com.github.sanity4j.gen.findbugs_1_3_9.SourceLine;

/**
 * FindBugsResultReader - Translates FindBugs results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class FindBugsResultReader implements ResultReader
{
	/** The properties used to configure this {@link ResultReader}. */
	private final Properties properties = new Properties();
	
    /** The ExtractStats to add the results to. */
    private ExtractStats stats;
    
    /** The FindBugs XML result file to read from. */
    private File findBugsResultFile;

	/** {@inheritDoc} */
	public void setProperties(final Properties properties) 
	{
		this.properties.putAll(properties);
	}

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
        DiagnosticFactory diagnosticFactory = DiagnosticFactory.getInstance(properties);
    	DiagnosticSet diagnostics = stats.getDiagnostics();

        BugCollection result = (BugCollection)
        JaxbMarshaller.unmarshal(findBugsResultFile, "com.github.sanity4j.gen.findbugs_1_3_9", "http://com.github.sanity4j/namespace/findbugs-1.3.9");

        List<BugInstance> bugs = result.getBugInstance();

        for (BugInstance bug : bugs)
        {
            Diagnostic diagnostic = diagnosticFactory.getDiagnostic();
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
