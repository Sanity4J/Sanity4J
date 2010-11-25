package net.sf.sanity4j.workflow;

import java.io.File;
import java.io.IOException;

import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;

/**
 * A {@link WorkUnit} that copies a file.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class FileCopier implements WorkUnit
{
    /** The source file to copy. */
    private final File sourceFile;
    /** The destination file to copy to. */
    private final File destFile;

    /**
     * Creates a FileCopier.
     * @param sourceFile the source file.
     * @param destFile the destination file.
     */
    public FileCopier(final File sourceFile, final File destFile)
    {
        this.sourceFile = sourceFile;
        this.destFile = destFile;
    }

    /**
     * Copies {@link #sourceFile} to {@link #destFile}.
     * An error message will be logged on error.
     */
    public void run()
    {
        try
        {
            FileUtil.copy(sourceFile, destFile);
        }
        catch (IOException e)
        {
            throw new QAException("Failed to " + sourceFile.getPath() + " to " + destFile.getPath(), e);
        }
    }

    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Copying file";
    }
}
