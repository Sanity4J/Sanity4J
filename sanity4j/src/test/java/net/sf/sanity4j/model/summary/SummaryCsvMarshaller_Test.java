package net.sf.sanity4j.model.summary; 

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

/** 
 * SummaryCsvMarshaller_Test - unit tests for {@link SummaryCsvMarshaller}. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class SummaryCsvMarshaller_Test extends TestCase
{
    /** The tolerance for comparing doubles (as floating point arithmetic is imprecise) - 0.01%. */
    private static final double DOUBLE_FIELD_TOLERANCE = 0.0001;
    
    /**
     * Tests summary marshalling by writing/reading in a set of summaries
     * and comparing the original and read data.
     */
    public void testMarshalling() throws Exception
    {
        PackageSummary[] summaries = new PackageSummary[]
        {
             createSummary(0, "", 0.0, 0.0, new int[] { 1, 2, 3, 4, 5 }, 100),
             createSummary(-1, "", 0.0, 0.0, new int[] { 1, 0, 0, 0, 0 }, 101),
             createSummary(0, "a", 0.0, 0.2, new int[] { 0, 1, 0, 0, 0 }, 102),
             createSummary(0, "a.b", 0.3, 0.0, new int[] { 0, 1, 0, 0, 0 }, 103),
             createSummary(-1, "a.b", 0.3, 0.0, new int[] { 0, 0, 1, 0, 0 }, 104),
             createSummary(-2, "a.b", 0.5, 0.6, new int[] { 0, 0, 0, 1, 0 }, 105),
             createSummary(0, "a.b.c", 0.7, 0.8, new int[] { 0, 0, 0, 0, 1 }, 106),
             createSummary(0, "b", 1.0, 1.0, new int[] { 0, 0, 0, 0, 0 }, 107)
        };
        
        File tempFile = File.createTempFile("SummaryCsvMarshaller_Test", "csv");
        tempFile.deleteOnExit();
        
        SummaryCsvMarshaller marshaller = new SummaryCsvMarshaller();
        marshaller.write(summaries, tempFile);
        
        marshaller = new SummaryCsvMarshaller();
        PackageSummary[] readSummaries = marshaller.read(tempFile);
        
        assertNotNull("Read summaries were null", readSummaries);
        assertEquals("Incorrect number of summaries read", 
                     summaries.length, readSummaries.length);
        
        for (int i = 0; i < summaries.length; i++)
        {
            assertEquals("Incorrect run date read", 
                         summaries[i].getRunDate(), readSummaries[i].getRunDate());
            
            assertEquals("Incorrect package name read", 
                         summaries[i].getPackageName(), readSummaries[i].getPackageName());
            
            assertEquals("Incorrect line coverage read", 
                         summaries[i].getLineCoverage(), readSummaries[i].getLineCoverage(), DOUBLE_FIELD_TOLERANCE);
            
            assertEquals("Incorrect branch coverage read", 
                         summaries[i].getBranchCoverage(), readSummaries[i].getBranchCoverage(), DOUBLE_FIELD_TOLERANCE);
            
            assertEquals("Incorrect Line count read", 
                         summaries[i].getLineCount(), readSummaries[i].getLineCount());
            
            assertEquals("Incorrect Info count read", 
                         summaries[i].getInfoCount(), readSummaries[i].getInfoCount());
            
            assertEquals("Incorrect Low count read", 
                         summaries[i].getLowCount(), readSummaries[i].getLowCount());
            
            assertEquals("Incorrect Moderate cound read", 
                         summaries[i].getModerateCount(), readSummaries[i].getModerateCount());
            
            assertEquals("Incorrect Significant count read", 
                         summaries[i].getSignificantCount(), readSummaries[i].getSignificantCount());
            
            assertEquals("Incorrect High count read", 
                         summaries[i].getHighCount(), readSummaries[i].getHighCount());
        }
        
        tempFile.delete();
    }
    
    public void testTruncatedAndBadData() throws Exception
    {
        File tempFile = File.createTempFile("SummaryCsvMarshaller_Test", "csv");
        tempFile.deleteOnExit();
        
        // Write a run for yesterday
        PackageSummary[] summaries = new PackageSummary[]
        {
             createSummary(-1, "a.b.c", 0.3, 0.4, new int[] { 5, 6, 7, 8, 9}, 100)
        };
        
        // Write some bad data        
        append("\r\nBAD DATE,mydomain.dummy,0.0,0.0,1.0", tempFile);
        append("\r\n2008/10/16-07:00,mydomain.dummy,BAD NUMBER,0.0,1.0", tempFile);
        
        SummaryCsvMarshaller marshaller = new SummaryCsvMarshaller();
        marshaller.write(summaries, tempFile);

        // Write a run for today
        summaries = new PackageSummary[]
        {
            createSummary(0, "a.b.c", 0.6, 0.7, new int[] { 1, 2, 3, 4, 5}, 110)
        };

        marshaller = new SummaryCsvMarshaller();
        marshaller.write(summaries, tempFile);
        
        // Write some truncated data
        append("\r\n2008/10/16-07:00,mydomain.dummy,", tempFile);
        
        // Read in the data
        marshaller = new SummaryCsvMarshaller();
        PackageSummary[] readSummaries = marshaller.read(tempFile);

        assertNotNull("Read summaries were null", readSummaries);
        assertEquals("Incorrect number of summaries read", 
                     2, readSummaries.length);        
    }

    /**
     * Creates a PackageSummary.
     * 
     * @param daysAgo the number of days ago the summary was created
     * @param packageName the package name
     * @param lineCoverage the line coverage 
     * @param branchCoverage the branch coverage
     * @param diagCounts the diagnostic counts (info --&gt; high)
     * @param lineCount the line count
     * 
     * @return a PackageSummary with the specified attributes
     */
    private PackageSummary createSummary(final int daysAgo, final String packageName, 
                                         final double lineCoverage, final double branchCoverage,
                                         final int[] diagCounts, final int lineCount)
    {
        // Seconds / milliseconds aren't stored in the CSV file
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, daysAgo);

        PackageSummary summary = new PackageSummary();
        summary.setRunDate(cal.getTime());
        summary.setPackageName(packageName);
        summary.setBranchCoverage(branchCoverage);
        summary.setLineCoverage(lineCoverage);
        summary.setInfoCount(diagCounts[0]);
        summary.setLowCount(diagCounts[1]);
        summary.setModerateCount(diagCounts[2]);
        summary.setSignificantCount(diagCounts[3]);
        summary.setHighCount(diagCounts[4]);
        summary.setLineCount(lineCount);

        return summary;
    }

    /**
     * Appends a string to a file.
     * 
     * @param string the string to append
     * @param file the file to append to
     * @throws IOException if there was an error writing to the file
     */
    private void append(final String string, final File file) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write(string.getBytes("UTF-8"));
        fos.close();
    }
}
