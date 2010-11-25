package net.sf.sanity4j.model.summary; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.sanity4j.util.QaLogger;

/** 
 * SummaryCsvMarshaller - reads/writes summary entries to a CSV file.
 * 
 * As we know that the values written do not contain new line or comma
 * characters, we don't need to escape any content. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class SummaryCsvMarshaller
{    
    /** The number of data columns in the CSV file. */
    private static final int NUM_COLS = 10;
    
    /** The column index for the run date. */
    private static final int DATE_COLUMN_INDEX = 0;
    /** The column index for the package name. */
    private static final int PACKAGE_NAME_COLUMN_INDEX = 1;
    /** The column index for the line coverage ratio. */
    private static final int LINE_COVERAGE_COLUMN_INDEX = 2;
    /** The column index for the branch coverage ratio. */
    private static final int BRANCH_COVERAGE_COLUMN_INDEX = 3;
    /** The column index for the number of info-severity diagnostics. */
    private static final int INFO_COUNT_COLUMN_INDEX = 4;
    /** The column index for the number of low-severity diagnostics. */
    private static final int LOW_COUNT_COLUMN_INDEX = 5;
    /** The column index for the number of moderate-severity diagnostics. */
    private static final int MODERATE_COUNT_COLUMN_INDEX = 6;
    /** The column index for the number of significant-severity diagnostics. */
    private static final int SIGNIFICANT_COUNT_COLUMN_INDEX = 7;
    /** The column index for the number of high-severity diagnostics. */
    private static final int HIGH_COUNT_COLUMN_INDEX = 8;
    /** The column index for the number the source line count. */
    private static final int LINE_COUNT_COLUMN_INDEX = 9;
    
    
    /** The character encoding when writing the file. */
    private static final String ENCODING = "UTF-8";
    
    /** The date format used to encode dates. */
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm");
    
    /**
     * Reads all the entries from the given file.
     * @param file the file to read from.
     * @return the entries contained in the file, may be empty.
     */
    public PackageSummary[] read(final File file)
    {
        List<PackageSummary> entries = new ArrayList<PackageSummary>((int) (file.length() / 80)); // assume approx 80 chars per line

        BufferedReader reader = null;

        try
        {
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis));

            // Skip the header
            String line = reader.readLine();
            int lineNum = 1;

            for (line = reader.readLine(); line != null; line = reader.readLine())
            {
                lineNum++;
                String[] cols = line.split(",");

                if (cols.length == NUM_COLS)
                {
                    try
                    {
                        PackageSummary entry = new PackageSummary();
                        entry.setRunDate(dateFormat.parse(cols[DATE_COLUMN_INDEX]));
                        entry.setPackageName(cols[PACKAGE_NAME_COLUMN_INDEX]);
                        entry.setLineCoverage(Double.parseDouble(cols[LINE_COVERAGE_COLUMN_INDEX]));
                        entry.setBranchCoverage(Double.parseDouble(cols[BRANCH_COVERAGE_COLUMN_INDEX]));
                        entry.setInfoCount(Integer.parseInt(cols[INFO_COUNT_COLUMN_INDEX]));
                        entry.setLowCount(Integer.parseInt(cols[LOW_COUNT_COLUMN_INDEX]));
                        entry.setModerateCount(Integer.parseInt(cols[MODERATE_COUNT_COLUMN_INDEX]));
                        entry.setSignificantCount(Integer.parseInt(cols[SIGNIFICANT_COUNT_COLUMN_INDEX]));
                        entry.setHighCount(Integer.parseInt(cols[HIGH_COUNT_COLUMN_INDEX]));
                        entry.setLineCount(Integer.parseInt(cols[LINE_COUNT_COLUMN_INDEX]));

                        entries.add(entry);
                    }
                    catch (ParseException e)
                    {
                        String msg = "Error reading line " + lineNum;
                        QaLogger.getInstance().warn(msg, e);
                    }
                    catch (NumberFormatException e)
                    {
                        String msg = "Error reading line " + lineNum;
                        QaLogger.getInstance().warn(msg, e);
                    }
                }
                else
                {
                    // Bad line - file probably truncated on previous run.
                    String msg = "Incorrect number of columns on line " + lineNum
                                 + "- expected " + NUM_COLS + ", got " + cols.length;
                    
                    QaLogger.getInstance().warn(msg);
                }
            }
            
            safeClose(reader);            
        }
        catch (IOException e)
        {
            QaLogger.getInstance().error("Error reading summary data", e);
            safeClose(reader);
        }

        return entries.toArray(new PackageSummary[entries.size()]);
    }

    /**
     * Appends the given SummaryEntries to the specified file.
     * 
     * @param entries the entries to write.
     * @param file the file to append to.
     */
    public void write(final PackageSummary[] entries, final File file)
    {
        boolean newFile = !file.exists() || file.length() == 0;

        FileOutputStream fos = null;
        StringBuffer line = new StringBuffer();

        // Write header if new file
        try
        {
            fos = new FileOutputStream(file, true);

            if (newFile)
            {
                fos.write("Run date,Package name,Line coverage,Branch coverage,Info diags,Low diags,Moderate diags,Significant diags,High diags,Line count".getBytes(ENCODING));
            }

            for (int i = 0; i < entries.length; i++)
            {
                line.setLength(0);
                line.append("\r\n");
                line.append(dateFormat.format(entries[i].getRunDate()));
                line.append(',');
                line.append(entries[i].getPackageName());
                line.append(',');
                line.append(entries[i].getLineCoverage());
                line.append(',');
                line.append(entries[i].getBranchCoverage());
                line.append(',');
                line.append(entries[i].getInfoCount());
                line.append(',');
                line.append(entries[i].getLowCount());
                line.append(',');
                line.append(entries[i].getModerateCount());
                line.append(',');
                line.append(entries[i].getSignificantCount());
                line.append(',');
                line.append(entries[i].getHighCount());
                line.append(',');
                line.append(entries[i].getLineCount());

                fos.write(line.toString().getBytes(ENCODING));
                fos.flush();
            }
            
            safeClose(fos);
        }
        catch (IOException e)
        {
            QaLogger.getInstance().error("Error writing summary data", e);
            safeClose(fos);
        }
    }
    
    /**
     * Closes a reader, logging any exceptions.
     * @param reader the reader to close.
     */
    private void safeClose(final Reader reader)
    {
        if (reader != null)
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                // Don't really care
                QaLogger.getInstance().debug("Error closing stream", e);
            }
        }
    }
    
    /**
     * Closes an OutputStream, logging any exceptions.
     * @param stream the OutputStream to close.
     */
    private void safeClose(final OutputStream stream)
    {
        if (stream != null)
        {
            try
            {
                stream.close();
            }
            catch (IOException e)
            {
                // Don't really care
                QaLogger.getInstance().debug("Error closing stream", e);
            }
        }
    }
}
