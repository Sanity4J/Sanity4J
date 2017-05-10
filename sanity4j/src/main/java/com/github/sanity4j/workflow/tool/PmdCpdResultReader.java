package com.github.sanity4j.workflow.tool;

import com.github.sanity4j.util.QaLogger;

/**
 * PmdCpdResultReader - Translates PMD CPD results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
* @deprecated use PmdCpd4ResultReader
*/
@Deprecated
public final class PmdCpdResultReader extends PmdCpd4ResultReader
{
   /**
    * Creates a PmdCpdResultReader.
    */
   public PmdCpdResultReader()
   {
       QaLogger.getInstance().warn("PmdCpdResultReader is deprecated, use PmdCpd4ResultReader for PMD 4.x or PmdCpd5ResultReader for PMD 5.x");
   }
}
