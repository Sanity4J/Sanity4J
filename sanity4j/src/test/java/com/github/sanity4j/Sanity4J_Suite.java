package com.github.sanity4j;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.github.sanity4j.model.coverage.ClassCoverage_Test;
import com.github.sanity4j.model.coverage.Coverage_Test;
import com.github.sanity4j.model.coverage.PackageCoverage_Test;
import com.github.sanity4j.model.diagnostic.DiagnosticCategory_Test;
import com.github.sanity4j.model.diagnostic.DiagnosticSet_Test;
import com.github.sanity4j.model.diagnostic.Diagnostic_Test;
import com.github.sanity4j.model.summary.PackageSummary_Test;
import com.github.sanity4j.model.summary.SummaryCsvMarshaller_Test;
import com.github.sanity4j.report.ChartFactory_Test;
import com.github.sanity4j.report.ReportUtil_Test;
import com.github.sanity4j.util.ExternalProcessRunner_Test;
import com.github.sanity4j.util.ExtractStats_Test;
import com.github.sanity4j.util.FileUtil_Test;
import com.github.sanity4j.util.JaxbMarshaller_Test;
import com.github.sanity4j.util.PipeInputThread_Test;
import com.github.sanity4j.util.StringUtil_Test;

/**
 * This class is the <a href="http://www.junit.org">JUnit</a> TestSuite for the classes within
 * {@link com.github.sanity4j} package within the <b>Sanity4J</b> project.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses
({
   Coverage_Test.class,
   ClassCoverage_Test.class,
   PackageCoverage_Test.class,
   
   Diagnostic_Test.class,
   DiagnosticCategory_Test.class,
   DiagnosticSet_Test.class,
   
   ChartFactory_Test.class,
   ReportUtil_Test.class,
   
   SummaryCsvMarshaller_Test.class,
   PackageSummary_Test.class,
   
   ExternalProcessRunner_Test.class,
   ExtractStats_Test.class,
   FileUtil_Test.class,
   PipeInputThread_Test.class,
   JaxbMarshaller_Test.class,
   StringUtil_Test.class
})
public class Sanity4J_Suite
{
}
