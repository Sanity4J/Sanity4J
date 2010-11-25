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
 * ClassFileCollector collects class files from multiple directory 
 * hierarchies and places them in a single directory hierarchy. This is 
 * necessary, as some tools don't allow multiple class directories 
 * to be specified.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class ClassFileCollector extends AbstractFileCollector
{
    /** The file extensions to include in the collection.  */
    private static final Set<String> FILE_EXTS = new HashSet<String>(Arrays.asList(new String[]{"class", "jar"}));
    
    /**
     * Creates a ClassFileCollector.
     * @param config the current QA run's configuration.
     */
    public ClassFileCollector(final QAConfig config)
    {
        super(FILE_EXTS, config.getClassDirs(), config.getCombinedClassDir());
    }
    
    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Collecting class files";
    }
    
    /**
     * Returns the item type being copied. 
     * @return "class" 
     */
    protected String getItemType()
    {
        return "class";
    }

    /**
     * Copies a single source file, placing it in the correct package.
     * @param source the source file
     * @param destDir the destination directory root.
     */
    protected void copyFile(final File source, final File destDir)
    {
        // Just copy jar archives to the top level
        if (source.getName().endsWith(".jar"))
        {
            File dest = new File(destDir, source.getName());
            
            if (dest.exists())
            {
                String msg = "Duplicate file, analysis may be innaccurate: " + dest;
                QaLogger.getInstance().warn(msg); 
            }
            else
            {
                try
                {
                    FileUtil.copy(source, dest);
                }
                catch (IOException e)
                {
                    throw new QAException("Failed to copy file", e);
                }           
            }
        }
        else
        {
            super.copyFile(source, destDir);
        }
    }
    
    /** @return true, at least one class file must exist. */
    @Override
    protected boolean isMandatory()
    {
        return true;
    }
}
