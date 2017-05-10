package com.github.sanity4j.workflow.tool;

import java.io.File;

/**
 * PmdCpdResultReader - Translates PMD CPD 5.x results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.1
 */
public final class PmdCpd5ResultReader extends PmdCpd4ResultReader
{
    /** {@inheritDoc} */
    @Override
    public void run()
    {
        File pmdCpdResultFile = getResultFile();
        
        if (pmdCpdResultFile.exists() && pmdCpdResultFile.length() == 0)
        {
            // PMD-CPD 5.x doesn't output the empty <pmd-cpd> element if there are no results
            return;
        }
        
        super.run();
    }
}
