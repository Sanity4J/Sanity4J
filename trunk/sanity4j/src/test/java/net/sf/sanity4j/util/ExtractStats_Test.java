package net.sf.sanity4j.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import net.sf.sanity4j.model.coverage.ClassCoverage;
import net.sf.sanity4j.model.coverage.Coverage;
import net.sf.sanity4j.model.coverage.PackageCoverage;
import net.sf.sanity4j.model.diagnostic.Diagnostic;
import net.sf.sanity4j.model.diagnostic.DiagnosticSet;
import net.sf.sanity4j.model.summary.PackageSummary;
import net.sf.sanity4j.workflow.QAConfig;
import net.sf.sanity4j.workflow.tool.CheckStyleResultReader;
import net.sf.sanity4j.workflow.tool.CoberturaResultReader;
import net.sf.sanity4j.workflow.tool.FindBugsResultReader;
import net.sf.sanity4j.workflow.tool.PmdCpdResultReader;
import net.sf.sanity4j.workflow.tool.PmdResultReader;

/**
 * ExtractStats_Test - unit test for {@link ExtractStats}.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class ExtractStats_Test extends TestCase
{
    /** A temporary directory used to hold test data. */
    private QAConfig config;

    /**
     * Since ExtractStats uses canonical paths, the test files
     * do actually need to exist on the file system. This sets
     * up the files.
     */
    public void setUp() throws IOException
    {
        config = new QAConfig();

        // Get a path to a temp dir
        File tempDir = File.createTempFile("ExtractStats_Test", "");
        tempDir.delete();

        config.setTempDir(tempDir);

        File sourceDir = config.getCombinedSourceDir();

        // Package dirs & files
        File subpackage1Dir = new File(sourceDir, "package/subpackage1");
        File subpackage2Dir = new File(sourceDir, "package/subpackage2");
        File file1 = new File(sourceDir, "package/ClassOne.java");
        File file2 = new File(sourceDir, "package/subpackage1/ClassTwo.java");
        File file3 = new File(sourceDir, "package/subpackage2/ClassThree.java");
        File file4 = new File(sourceDir, "package/subpackage2/ClassFour.java");

        if (!tempDir.mkdirs()
            || !subpackage1Dir.mkdirs() || !subpackage2Dir.mkdirs()
            || !file1.createNewFile() || !file2.createNewFile()
            || !file3.createNewFile() || !file4.createNewFile())
        {
            throw new IOException("Failed to create temp files");
        }
    }

    /**
     * Deletes the temporary dir.
     */
    public void tearDown() throws IOException
    {
        File tempDir = config.getTempDir();

        if (tempDir != null && tempDir.isDirectory())
        {
            FileUtil.delete(tempDir);
        }
    }

    public void testExtractCheckStyleStats() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        createResultFile("/resources/tool-xml/checkStyle.xml", config.getToolResultFile(Tool.CHECKSTYLE));
        CheckStyleResultReader reader = new CheckStyleResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.CHECKSTYLE));
        reader.setStats(stats);
        reader.run();
        assertFalse("No stats extracted", stats.getDiagnostics().isEmpty());
        checkDiagnostics(stats);
    }

    public void testExtractCoberturaCoverage() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        createResultFile("/resources/tool-xml/cobertura.xml", config.getToolResultFile(Tool.COBERTURA));
        CoberturaResultReader reader = new CoberturaResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.COBERTURA));
        reader.setStats(stats);
        reader.run();

        Coverage coverage = stats.getCoverage();
        assertNotNull("No coverage extracted", coverage);

        PackageCoverage packageCoverage = coverage.getPackageCoverage("package");
        assertEquals("Incorrect package line coverage extracted", 0.5, packageCoverage.getLineCoverage(), 0.0);
        assertEquals("Incorrect package branch coverage extracted", 0.5, packageCoverage.getBranchCoverage(), 0.0);

        ClassCoverage classCoverage = packageCoverage.getClassCoverage("package.ClassOne");
        assertEquals("Incorrect class line coverage extracted", 1.0, classCoverage.getCoveredLineCount(), 0.0);
        assertEquals("Incorrect classs branch coverage extracted", 0.0, classCoverage.getBranchCount(), 0.0);
        assertEquals("Incorrect class line count extracted", 1, classCoverage.getLineCount());
        assertEquals("Incorrect class branch count extracted", 0, classCoverage.getBranchCount());
    }

    public void testExtractFindbugsStats() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        createResultFile("/resources/tool-xml/findBugs.xml", config.getToolResultFile(Tool.FINDBUGS));
        FindBugsResultReader reader = new FindBugsResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.FINDBUGS));
        reader.setStats(stats);
        reader.run();

        assertFalse("No stats extracted", stats.getDiagnostics().isEmpty());
        checkDiagnostics(stats);
    }

    public void testExtractPmdCpdStats() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        createResultFile("/resources/tool-xml/pmdCpd.xml", config.getToolResultFile(Tool.PMD_CPD));
        PmdCpdResultReader reader = new PmdCpdResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.PMD_CPD));
        reader.setStats(stats);
        reader.run();

        assertFalse("No stats extracted", stats.getDiagnostics().isEmpty());
        checkDiagnostics(stats);
    }

    public void testExtractPmdStats() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        createResultFile("/resources/tool-xml/pmd.xml", config.getToolResultFile(Tool.PMD));
        PmdResultReader reader = new PmdResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.PMD));
        reader.setStats(stats);
        reader.run();

        assertFalse("No stats extracted", stats.getDiagnostics().isEmpty());
        checkDiagnostics(stats);
    }

    private File createResultFile(final String resourcePath, final File dest) throws IOException
    {
        File sourceDir = config.getCombinedSourceDir();
        dest.deleteOnExit();

        InputStream ris = getClass().getResourceAsStream(resourcePath);
        String xml = new String(getBytes(ris), "UTF-8");
        QaUtil.safeClose(ris);

        xml = replace(xml, "src\\package\\", sourceDir.getCanonicalPath() + "\\package\\");
        xml = replace(xml, "src/package/", sourceDir.getCanonicalPath() + "/package/");

        FileOutputStream fos = new FileOutputStream(dest);
        fos.write(xml.getBytes("UTF-8"));

        ris.close();
        fos.close();

        return dest;
    }

    /** Checks the diagnostics. */
    private void checkDiagnostics(final ExtractStats stats) throws IOException
    {
        stats.extractLineCounts();

        // Check summary stats
        assertEquals("Incorrect number of classes", 4, stats.getClassCount());
        assertEquals("Incorrect number of classes for package 'package'",
                     1, stats.getPackageClassCount("package"));
        assertEquals("Incorrect number of classes for package 'package.subpackage1'",
                     1, stats.getPackageClassCount("package.subpackage1"));
        assertEquals("Incorrect number of classes for package 'package.subpackage2'",
                     2, stats.getPackageClassCount("package.subpackage2"));

        PackageSummary[] summary = stats.getRunSummary();
        assertEquals("Incorrect number of package summaries", 4, summary.length); // 3, +1 for root package

        // Check diagnostic set
        DiagnosticSet diags = stats.getDiagnostics();
        assertEquals("Incorrect number of diagnostics", 6, diags.size());

        assertEquals("Incorrect diags for package 'package'",
                     1, diags.getDiagnosticsForPackage("package", false).size());
        assertEquals("Incorrect diags for package 'package.subpackage1'",
                     2, diags.getDiagnosticsForPackage("package.subpackage1", false).size());
        assertEquals("Incorrect diags for package 'package.subpackage2'",
                     3, diags.getDiagnosticsForPackage("package.subpackage2", false).size());

        // Check the individual diagnostics
        for (Diagnostic diag : diags)
        {
            // We switch on the start line number, as it's the only thing
            // That all the tools have in common
            switch (diag.getStartLine())
            {
                case 11:
                {
                    assertEquals("Incorrect class name",
                                 "package.ClassOne", diag.getClassName());

                    break;
                }
                case 22:
                {
                    assertEquals("Incorrect class name",
                                 "package.subpackage1.ClassTwo", diag.getClassName());
                    break;
                }
                case 33:
                {
                    assertEquals("Incorrect class name",
                                 "package.subpackage1.ClassTwo", diag.getClassName());

                    break;
                }
                case 44:
                {
                    assertEquals("Incorrect class name",
                                 "package.subpackage2.ClassThree", diag.getClassName());

                    break;
                }
                case 55:
                {
                    assertEquals("Incorrect class name",
                                 "package.subpackage2.ClassThree", diag.getClassName());

                    break;
                }
                case 66:
                {
                    assertEquals("Incorrect class name",
                                 "package.subpackage2.ClassFour", diag.getClassName());

                    break;
                }
                default:
                {
                    fail("Extracted incorrect start line: " + diag.getStartLine());
                }
            }

            int testNum = diag.getStartLine() / 11;

            if (diag.getSource() != Diagnostic.SOURCE_PMD_CPD)
            {
                assertEquals("Incorrect rule name", "rule" + testNum, diag.getRuleName());

                assertEquals("Incorrect message text",
                             "message text" + testNum, diag.getMessage().trim());
            }
        }
    }

    /**
     * Rather than using regexps and having to escape the (dynamic)
     * replacement String, this has been shamelessly copied from
     * Apache Ant's StringUtils.
     */
    public static String replace(final String data, final String search, final String replace)
    {
        StringBuffer buf = new StringBuffer(data.length());
        int lastPos = 0;

        for (int pos = data.indexOf(search, 0); pos != -1; pos = data.indexOf(search, lastPos))
        {
            buf.append(data.substring(lastPos, pos)).append(replace);
            lastPos = pos + search.length();
        }

        buf.append(data.substring(lastPos));
        return buf.toString();
    }

    /**
     * Returns a byte array containing all the information contained in the
     * given input stream.
     *
     * @param in the stream to read from.
     * @return the stream contents as a byte array.
     * @throws IOException if there is an error reading from the stream.
     */
    private byte[] getBytes(final InputStream in) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        final byte[] buf = new byte[4096];
        int bytesRead = in.read(buf);

        while (bytesRead != -1)
        {
            result.write(buf, 0, bytesRead);
            bytesRead = in.read(buf);
        }

        result.flush();
        result.close();

        return result.toByteArray();
    }
}
