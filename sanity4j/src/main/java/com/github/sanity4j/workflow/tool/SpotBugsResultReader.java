package com.github.sanity4j.workflow.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.github.sanity4j.gen.spotbugs_3_1_6.BugCollection;
import com.github.sanity4j.gen.spotbugs_3_1_6.BugCollection.BugInstance;
import com.github.sanity4j.gen.spotbugs_3_1_6.SourceLine;
import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticFactory;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.util.ExtractStats;
import com.github.sanity4j.util.JaxbMarshaller;
import com.github.sanity4j.util.QAException;

/**
 * SpotBugsResultReader - Translates SpotBugs results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class SpotBugsResultReader implements ResultReader
{
    /** The properties used to configure this {@link ResultReader}. */
    private final Properties properties = new Properties();
    
    /** The ExtractStats to add the results to. */
    private ExtractStats stats;
    
    /** The SpotBugs XML result file to read from. */
    private File spotBugsResultFile;

    /** {@inheritDoc} */
    @Override
    public void setProperties(final Properties properties) 
    {
        this.properties.putAll(properties);
    }

    /** {@inheritDoc} */
    @Override
    public void setResultFile(final File resultFile)
    {
        this.spotBugsResultFile = resultFile;
    }

    /** {@inheritDoc} */
    @Override
    public void setStats(final ExtractStats stats)
    {
        this.stats = stats;
    }

    /**
     * Extracts SpotBugs statistics from spotBugsResultFile.
     */
    @Override
    public void run()
    {
        DiagnosticFactory diagnosticFactory = DiagnosticFactory.getInstance(properties);
        DiagnosticSet diagnostics = stats.getDiagnostics();

        BugCollection result = (BugCollection)
        JaxbMarshaller.unmarshal(spotBugsResultFile, "com.github.sanity4j.gen.spotbugs_3_1_6", "http://com.github.sanity4j/namespace/spotbugs-3.1.6");

        List<BugInstance> bugs = result.getBugInstance();

        for (BugInstance bug : bugs)
        {
            Diagnostic diagnostic = diagnosticFactory.getDiagnostic();
            diagnostic.setSource(Diagnostic.SOURCE_SPOTBUGS);
            diagnostic.setRuleName(bug.getType());
            diagnostic.calcSeverity();

            // Unfortunately, there is a whole set of information that may or
            // may not be present. We have to iterate through the whole list
            // and pick out the summary information that we're interested in.
            List<Object> details = bug.getClazzOrTypeOrMethod();

            for (Object detail : details)
            {
                if (detail instanceof BugInstance.Class)
                {
                    BugInstance.Class clazz = (BugInstance.Class) detail;

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
            }

            if (bug.getLongMessage() != null)
            {
               diagnostic.setMessage(bug.getLongMessage());
            }
            else
            {
               diagnostic.setMessage(bug.getShortMessage());
            }
            
            diagnostics.add(diagnostic);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription()
    {
        return "Reading SpotBugs results";
    }
}
