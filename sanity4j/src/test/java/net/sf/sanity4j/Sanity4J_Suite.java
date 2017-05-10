package net.sf.sanity4j;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.sanity4j.model.coverage.ClassCoverage_Test;
import net.sf.sanity4j.model.coverage.Coverage_Test;
import net.sf.sanity4j.model.coverage.PackageCoverage_Test;
import net.sf.sanity4j.model.diagnostic.DiagnosticCategory_Test;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet_Test;
import net.sf.sanity4j.model.diagnostic.Diagnostic_Test;
import net.sf.sanity4j.model.summary.PackageSummary_Test;
import net.sf.sanity4j.model.summary.SummaryCsvMarshaller_Test;
import net.sf.sanity4j.report.ChartFactory_Test;
import net.sf.sanity4j.report.ReportUtil_Test;
import net.sf.sanity4j.util.ExternalProcessRunner_Test;
import net.sf.sanity4j.util.ExtractStats_Test;
import net.sf.sanity4j.util.FileUtil_Test;
import net.sf.sanity4j.util.JaxbMarshaller_Test;
import net.sf.sanity4j.util.PipeInputThread_Test;
import net.sf.sanity4j.util.StringUtil_Test;

/**
 * This class is the <a href="http://www.junit.org">JUnit</a> TestSuite for the classes within
 * {@link com.github.sanity4j} package within the <b>Sanity4J</b> project.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class Sanity4J_Suite extends TestSuite
{
    /**
     * Returns a JUnit TestSuite comprising of the tests for the {@link com.github.sanity4j} package within the <b>Sanity4J</b>
     * library.
     * 
     * @return A JUnit TestSuite.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.setName(Sanity4J_Suite.class.getName());

        suite.addTestSuite(Coverage_Test.class);
        suite.addTestSuite(ClassCoverage_Test.class);
        suite.addTestSuite(PackageCoverage_Test.class);
        
        suite.addTestSuite(Diagnostic_Test.class);
        suite.addTestSuite(DiagnosticCategory_Test.class);
        suite.addTestSuite(DiagnosticSet_Test.class);
        
        suite.addTestSuite(ChartFactory_Test.class);
        suite.addTestSuite(ReportUtil_Test.class);
        
        suite.addTestSuite(SummaryCsvMarshaller_Test.class);
        suite.addTestSuite(PackageSummary_Test.class);
        
        suite.addTestSuite(ExternalProcessRunner_Test.class);
        suite.addTestSuite(ExtractStats_Test.class);
        suite.addTestSuite(FileUtil_Test.class);
        suite.addTestSuite(PipeInputThread_Test.class);
        suite.addTestSuite(JaxbMarshaller_Test.class);
        suite.addTestSuite(StringUtil_Test.class);
        
        return suite;
    }
}
