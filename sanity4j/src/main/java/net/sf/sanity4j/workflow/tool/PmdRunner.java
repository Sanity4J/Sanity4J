package net.sf.sanity4j.workflow.tool;

import net.sf.sanity4j.util.QaLogger;

/**
 * PmdRunner - a work unit that runs PMD.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 * @deprecated use Pmd4Runner
 */
@Deprecated
public class PmdRunner extends Pmd4Runner
{
    /**
     * Creates a PmdRunner.
     */
    public PmdRunner()
    {
        QaLogger.getInstance().warn("PmdRunner is deprecated, use Pmd4Runner for PMD 4.x or Pmd5Runner for PMD 5.x");
    }
}
