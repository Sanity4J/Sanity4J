package com.github.sanity4j.workflow.tool;

import com.github.sanity4j.util.QaLogger;

/**
 * PmdCpdRunner - a work unit that runs PMD CPD.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 * @deprecated use PmdCpd4Runner
 */
@Deprecated
public class PmdCpdRunner extends PmdCpd4Runner
{
    /**
     * Creates a PmdCpdRunner.
     */
    public PmdCpdRunner()
    {
        QaLogger.getInstance().warn("PmdCpdRunner is deprecated, use PmdCpd4Runner for PMD 4.x or PmdCpd5Runner for PMD 5.x");
    }
}
