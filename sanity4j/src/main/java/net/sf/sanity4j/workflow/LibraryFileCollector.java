package net.sf.sanity4j.workflow; 

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;


/** 
 * LibraryFileCollector collects library files from multiple directory 
 * hierarchies and places them in a single directory hierarchy. This is 
 * necessary, as some tools don't allow multiple library directories 
 * to be specified.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class LibraryFileCollector extends AbstractFileCollector
{
    /** The file extensions to include in the collection.  */
    private static final Set<String> FILE_EXTS = new HashSet<String>(Arrays.asList(new String[]{"class", "jar"}));
    
    /**
     * Creates a LibraryFileCollector.
     * @param config the current QA run's configuration.
     */
    public LibraryFileCollector(final QAConfig config)
    {
        super(FILE_EXTS, config.getLibraryDirs(), config.getCombinedLibraryDir());
    }
    
    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Collecting library files";
    }
    
    /**
     * Returns the item type being copied. 
     * @return "library" 
     */
    protected String getItemType()
    {
        return "library";
    }

    /**
     * Copies a single file, placing it in the correct package.
     * @param file the source file
     * @param destDir the destination directory root.
     */
    protected void copyFile(final File file, final File destDir)
    {
        // Just copy jar archives to the top level
        if (file.getName().endsWith(".jar"))
        {
            File dest = new File(destDir, file.getName());
            
            if (dest.exists())
            {
                String msg = "Duplicate file, analysis may be innaccurate: " + dest;
                QaLogger.getInstance().warn(msg); 
            }
            else
            {
                try
                {
                    FileUtil.copy(file, dest);
                }
                catch (IOException e)
                {
                    throw new QAException("Failed to copy file", e);
                }           
            }
        }
        else
        {
            super.copyFile(file, destDir);
        }
    }
}
