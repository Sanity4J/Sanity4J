package com.github.sanity4j.workflow.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.github.sanity4j.gen.pmdcpd_4_2_1.Duplication;
import com.github.sanity4j.gen.pmdcpd_4_2_1.PmdCpd;
import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticFactory;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.util.ExtractStats;
import com.github.sanity4j.util.JaxbMarshaller;
import com.github.sanity4j.util.QAException;

/**
 * PmdCpdResultReader - Translates PMD CPD 5.x results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.1
 */
public final class PmdCpd5ResultReader implements ResultReader
{
   /** The rule name to use for Diagnostics, as PMD-CPD doesn't have rules. */
   private static final String PMD_CPD_RULE_NAME = "DoNotCopyAndPasteCode";

   /** The properties used to configure this {@link ResultReader}. */
   private final Properties properties = new Properties();
   
   /** The ExtractStats to add the results to. */
   private ExtractStats stats;
   
   /** The PMD CPD XML result file to read from. */
   private File pmdCpdResultFile;
   
   /** The threshold for severity. */
   private static final int SEVERITY_THRESHOLD = 100;

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
       this.pmdCpdResultFile = resultFile;
   }
   
   /** {@inheritDoc} */
   @Override
   public void setStats(final ExtractStats stats)
   {
       this.stats = stats;
   }

   /**
    * Extracts PMD statistics from the pmdCpdResultFile.
    */
   @Override
   public void run()
   {
       if (pmdCpdResultFile.exists() && pmdCpdResultFile.length() == 0)
       {
          // PMD-CPD 5.x doesn't output the empty <pmd-cpd> element if there are no results
          return;
       }

       if (pmdCpdResultFile.exists() && pmdCpdResultFile.length() < 60)
       {
          // PMD-CPD 5.6.x outputs the empty <pmd-cpd> element as self-closing, which blows up jaxb with:
          // SAXParseException pmd-cpd must be followed by either attribute specifications, ">" or "/>". 
          return;
       }
      
       DiagnosticFactory diagnosticFactory = DiagnosticFactory.getInstance(properties);
       DiagnosticSet diagnostics = stats.getDiagnostics();

       PmdCpd result = (PmdCpd)
           JaxbMarshaller.unmarshal(pmdCpdResultFile, "com.github.sanity4j.gen.pmdcpd_4_2_1", "http://com.github.sanity4j/namespace/pmdcpd-4.2.1");

       List<Duplication> duplications = result.getDuplication();

       for (Duplication duplication : duplications)
       {
           try
           {
               com.github.sanity4j.gen.pmdcpd_4_2_1.File file1 = duplication.getFile().get(0);
               com.github.sanity4j.gen.pmdcpd_4_2_1.File file2 = duplication.getFile().get(1);
               String fileName1 = stats.getCanonicalPath(file1.getPath()).substring(stats.getSourceDirectory().length() + 1);
               String fileName2 = stats.getCanonicalPath(file2.getPath()).substring(stats.getSourceDirectory().length() + 1);

               // severity is based on the size of the duplication
               int severity = duplication.getLines().intValue() < SEVERITY_THRESHOLD
                              ? Diagnostic.SEVERITY_LOW
                              : Diagnostic.SEVERITY_MODERATE;

               Diagnostic diagnostic = diagnosticFactory.getDiagnostic();
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

               diagnostic = diagnosticFactory.getDiagnostic();
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
   @Override
   public String getDescription()
   {
       return "Reading PMD CPD results";
   }
}
