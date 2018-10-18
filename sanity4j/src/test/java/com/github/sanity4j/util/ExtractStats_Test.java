package com.github.sanity4j.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.sanity4j.model.coverage.ClassCoverage;
import com.github.sanity4j.model.coverage.Coverage;
import com.github.sanity4j.model.coverage.PackageCoverage;
import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticSet;
import com.github.sanity4j.model.summary.PackageSummary;
import com.github.sanity4j.workflow.QAConfig;
import com.github.sanity4j.workflow.tool.CheckStyleResultReader;
import com.github.sanity4j.workflow.tool.JaCoCoResultReader;
import com.github.sanity4j.workflow.tool.PmdCpd5ResultReader;
import com.github.sanity4j.workflow.tool.PmdResultReader;
import com.github.sanity4j.workflow.tool.SpotBugsResultReader;

/**
 * ExtractStats_Test - unit test for {@link ExtractStats}.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class ExtractStats_Test
{
    /** Incorrect classname message. */
    private static final String INCORRECT_CLASSNAME_MSG = "Incorrect class name";

    /** Incorrect rulename message. */
    private static final String INCORRECT_RULENAME_MSG = "Incorrect rule name";
    
    /** Incorrect message text. */
    private static final String INCORRECT_MESSAGE_TEXT = "Incorrect message text";
    
    /** No stats extracted message. */
    private static final String NO_STATS_EXTRACTED_MSG = "No stats extracted";

    /** A temporary directory used to hold test data. */
    private QAConfig config;
    
    /** The temporary directory containing the result files. */
    private static File tempDir;

    /**
     * Extracts the test project into a temporary directory.
     * This avoid the classes/sources being analysed during a build.
     */
    @BeforeClass
    public static void extractFiles() throws IOException
    {
        tempDir = File.createTempFile("ExtractStats_Test", "");
        tempDir.delete();
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        byte[] buf = new byte[1024];
        
        try (ZipInputStream zis = new ZipInputStream(ExtractStats.class.getResourceAsStream("/test-project.zip")))
        {
            for (ZipEntry ze = zis.getNextEntry() ; ze != null ; ze = zis.getNextEntry())
            {
                String fileName = ze.getName();
                File newFile = new File(tempDir, fileName);

                // We create the dirs individually rather than using mkDirs() so that they are deleted on exit 
                Stack<File> dirsToCreate = new Stack<File>();
                
                for (File parentDir = newFile.getParentFile() ; !parentDir.exists() ; parentDir = parentDir.getParentFile())
                {
                    dirsToCreate.push(parentDir);
                }
                
                while (!dirsToCreate.isEmpty())
                {
                    File dir = dirsToCreate.pop();
                    
                    if (!dir.mkdir())
                    {
                        throw new IOException("Failed to create dir: " + dir);
                    }
                    
                    dir.deleteOnExit();
                }

                newFile.deleteOnExit();

                if (ze.isDirectory())
                {
                    if (!newFile.exists() && !newFile.mkdir())
                    {
                        throw new IOException("Failed to create dir: " + newFile);
                    }
                }
                else
                {
                    try (FileOutputStream fos = new FileOutputStream(newFile))
                    {
                        int len;
                        
                        while ((len = zis.read(buf)) > 0)
                        {
                            fos.write(buf, 0, len);
                        }
                    }
                }
                
                zis.closeEntry();
            }
        }
    }
    
    /**
     * Since ExtractStats uses canonical paths, the test files
     * do actually need to exist on the file system. This sets
     * up the files.
     */
    @Before
    public void setUp() throws IOException
    {
        config = new QAConfig();
        config.setTempDir(tempDir);
        config.setCoverageDataFile(new File(tempDir, "jacoco.exec").getPath());
    }

    @Test
    public void testExtractCheckStyleStats() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        CheckStyleResultReader reader = new CheckStyleResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.CHECKSTYLE));
        reader.setStats(stats);
        reader.run();
        Assert.assertFalse(NO_STATS_EXTRACTED_MSG, stats.getDiagnostics().isEmpty());
        checkDiagnostics(stats);
    }

    @Test
    public void testExtractJaCoCoCoverage() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        
        JaCoCoResultReader reader = new JaCoCoResultReader();
        JaCoCoResultReader.setConfig(config);
        reader.setResultFile(config.getToolResultFile(Tool.JACOCO));
        reader.setStats(stats);
        reader.run();

        Coverage coverage = stats.getCoverage();
        Assert.assertNotNull("No coverage extracted", coverage);

        PackageCoverage packageCoverage = coverage.getPackageCoverage("packagg");
        Assert.assertEquals("Incorrect package line coverage extracted", 0.57, packageCoverage.getLineCoverage(), 0.05);
        Assert.assertEquals("Incorrect package branch coverage extracted", 0.5, packageCoverage.getBranchCoverage(), 0.0);

        ClassCoverage classCoverage = packageCoverage.getClassCoverage("packagg.ClassOne");
        Assert.assertEquals("Incorrect class line coverage extracted", 4, classCoverage.getCoveredLineCount(), 0.0);
        Assert.assertEquals("Incorrect class branch coverage extracted", 0.25, classCoverage.getBranchCoverage(), 0.0);
        Assert.assertEquals("Incorrect class line count extracted", 7, classCoverage.getLineCount());
        Assert.assertEquals("Incorrect class branch count extracted", 2, classCoverage.getBranchCount());
    }

    @Test
    public void testExtractSpotBugsStats() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        SpotBugsResultReader reader = new SpotBugsResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.SPOTBUGS));
        reader.setStats(stats);
        reader.run();

        Assert.assertFalse(NO_STATS_EXTRACTED_MSG, stats.getDiagnostics().isEmpty());
        checkDiagnostics(stats);
    }

    @Test
    public void testExtractPmdCpdStats() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        PmdCpd5ResultReader reader = new PmdCpd5ResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.PMD_CPD));
        reader.setStats(stats);
        reader.run();

        Assert.assertFalse(NO_STATS_EXTRACTED_MSG, stats.getDiagnostics().isEmpty());
        checkDiagnostics(stats);
    }

    @Test
    public void testExtractPmdStats() throws Exception
    {
        ExtractStats stats = new ExtractStats(config.getCombinedSourceDir().getCanonicalPath());
        PmdResultReader reader = new PmdResultReader();
        reader.setResultFile(config.getToolResultFile(Tool.PMD));
        reader.setStats(stats);
        reader.run();

        Assert.assertFalse(NO_STATS_EXTRACTED_MSG, stats.getDiagnostics().isEmpty());
        checkDiagnostics(stats);
    }

    /** Checks the diagnostics. */
    private void checkDiagnostics(final ExtractStats stats) throws IOException
    {
        stats.extractLineCounts();

        // Check summary stats
        Assert.assertEquals("Incorrect number of classes", 4, stats.getClassCount());
        Assert.assertEquals("Incorrect number of classes for package 'packagg'",
                     1, stats.getPackageClassCount("packagg"));
        Assert.assertEquals("Incorrect number of classes for package 'packagg.subpackage1'",
                     1, stats.getPackageClassCount("packagg.subpackage1"));
        Assert.assertEquals("Incorrect number of classes for package 'packagg.subpackage2'",
                     2, stats.getPackageClassCount("packagg.subpackage2"));

        PackageSummary[] summary = stats.getRunSummary();
        Assert.assertEquals("Incorrect number of package summaries", 4, summary.length); // 3, +1 for root package

        // Check diagnostic set
        DiagnosticSet diags = stats.getDiagnostics();
        Assert.assertEquals("Incorrect number of diagnostics", 6, diags.size());

        Assert.assertEquals("Incorrect diags for package 'packagg'",
                     1, diags.getDiagnosticsForPackage("packagg", false).size());
        Assert.assertEquals("Incorrect diags for package 'packagg.subpackage1'",
                     2, diags.getDiagnosticsForPackage("packagg.subpackage1", false).size());
        Assert.assertEquals("Incorrect diags for package 'packagg.subpackage2'",
                     3, diags.getDiagnosticsForPackage("packagg.subpackage2", false).size());

        // Check the individual diagnostics
        for (Diagnostic diag : diags)
        {
            // We switch on the start line number, as it's the only thing
            // That all the tools have in common
            switch (diag.getStartLine())
            {
                case 11:
                {
                   Assert.assertEquals(INCORRECT_CLASSNAME_MSG,
                                 "packagg.ClassOne", diag.getClassName());

                    break;
                }
                case 22:
                {
                   Assert.assertEquals(INCORRECT_CLASSNAME_MSG,
                                 "packagg.subpackage1.ClassTwo", diag.getClassName());
                    break;
                }
                case 33:
                {
                   Assert.assertEquals(INCORRECT_CLASSNAME_MSG,
                                 "packagg.subpackage1.ClassTwo", diag.getClassName());

                    break;
                }
                case 44:
                {
                   Assert.assertEquals(INCORRECT_CLASSNAME_MSG,
                                 "packagg.subpackage2.ClassThree", diag.getClassName());

                    break;
                }
                case 55:
                {
                   Assert.assertEquals(INCORRECT_CLASSNAME_MSG,
                                 "packagg.subpackage2.ClassThree", diag.getClassName());

                    break;
                }
                case 66:
                {
                   Assert.assertEquals(INCORRECT_CLASSNAME_MSG,
                                 "packagg.subpackage2.ClassFour", diag.getClassName());

                    break;
                }
                default:
                {
                   Assert.fail("Extracted incorrect start line: " + diag.getStartLine());
                }
            }

            int testNum = diag.getStartLine() / 11;

            if (diag.getSource() != Diagnostic.SOURCE_PMD_CPD)
            {
                Assert.assertEquals(INCORRECT_RULENAME_MSG, "rule" + testNum, diag.getRuleName());

                Assert.assertEquals(INCORRECT_MESSAGE_TEXT,
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
        StringBuilder buf = new StringBuilder(data.length());
        int lastPos = 0;

        for (int pos = data.indexOf(search, 0); pos != -1; pos = data.indexOf(search, lastPos))
        {
            buf.append(data.substring(lastPos, pos)).append(replace);
            lastPos = pos + search.length();
        }

        buf.append(data.substring(lastPos));
        return buf.toString();
    }
}
