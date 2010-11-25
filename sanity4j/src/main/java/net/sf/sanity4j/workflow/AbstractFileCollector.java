package net.sf.sanity4j.workflow; 

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaUtil;

/** 
 * AbstractFileCollector collects files from multiple directory 
 * hierarchies and places them in a single directory hierarchy. This is 
 * necessary, as some tools don't allow multiple directories 
 * to be specified.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public abstract class AbstractFileCollector implements WorkUnit
{
    /** The file extensions to include in the collection.  */
    private final Set<String> includedFileExtensions;
    
    /** The destination directory. */
    private final File destDir;
    
    /** The source directories. */
    private final List<String> sourceDirs;
    
    /**
     * Creates a AbstractFileCollector.
     * @param includedFileExtensions the file extensions to include in the copy.
     * @param sourceDirs the list of source directories to copy from.
     * @param destDir the destination directory to copy to.
     */
    public AbstractFileCollector(final Set<String> includedFileExtensions, final List<String> sourceDirs, final File destDir)
    {
        this.includedFileExtensions = includedFileExtensions;
        this.destDir = destDir;
        this.sourceDirs = sourceDirs;
    }
    
    /**
     * Indicates whether at least one file must be copied. 
     * This default implementation returns false. 
     * 
     * @return false
     */
    protected boolean isMandatory()
    {
        return false;
    }

    /**
     * Performs the file copying.
     */
    public void run()
    {
        QaLogger.getInstance().info("Creating combined " + getItemType() + " directory");        
        FileUtil.createDir(destDir.getPath());
        
        int count = copyFiles(sourceDirs, destDir);        
        QaLogger.getInstance().info("Copied " + count + " files");
        
        if (count == 0 && isMandatory())
        {
            throw new QAException("No " + getItemType() + " found");
        }        
    }
    
    /** @return the type of item being copied, e.g. "source", "classes". */
    protected abstract String getItemType();
    
    /**
     * Copies class files to the given directory, correcting for package names
     * along the way. Optionally copies all files with the given extension.
     * 
     * The root packages is set to be the top level of the destination directory, 
     * regardless of where the file originated from.
     * 
     * @param filePaths the files to copy
     * @param destDir the destination directory
     * 
     * @return the number of files copied
     */
    private int copyFiles(final List<String> filePaths, final File destDir)
    {
        int count = 0;

        // We want to copy files from arbitrary subdirectories,
        // while maintaining the package structure
        for (Iterator<String> i = filePaths.iterator(); i.hasNext();)
        {
            String fileName = i.next();
            File source = new File(fileName);

            // If it's a directory, recurse...
            if (source.isDirectory())
            {
                String[] containedFiles = source.list();
                
                for (int j = 0; j < containedFiles.length; j++)
                {
                    StringBuffer sb = new StringBuffer(fileName.length() + containedFiles[j].length() + 1);
                    sb.append(fileName);
                    sb.append(File.separatorChar);
                    sb.append(containedFiles[j]);
                    
                    containedFiles[j] = sb.toString();
                }
                
                count += copyFiles(Arrays.asList(containedFiles), destDir);
            }           
            else
            {
                // Check whether it is a file which we should be copying
                String fileExt = source.getName().substring(source.getName().lastIndexOf('.') + 1);
                
                if (includedFileExtensions.contains(fileExt.toLowerCase()))
                {
                    copyFile(source, destDir);
                    count++;
                }
            }
        }
        
        return count;       
    }
    
    /**
     * Copies a single file, placing it in the correct package.
     * @param file the file to copy
     * @param destDir the destination directory root.
     */
    protected void copyFile(final File file, final File destDir)
    {
        try
        {
            String packageName = QaUtil.getPackageForFile(file);
            
            if (packageName != null)
            {                    
                String destPath = packageName.replace('.', File.separatorChar) + File.separatorChar + file.getName();
                File destFile = new File(destDir, destPath);
                
                if (destFile.exists())
                {
                    String msg = "Duplicate file, analysis may be innaccurate: " + destPath;
                    QaLogger.getInstance().warn(msg); 
                }
                else
                {
                    FileUtil.copy(file, destFile);
                }
            }
            else
            {
                // Unable to determine package, use relative path from root.
                StringBuffer relativePath = new StringBuffer(file.getName());
                
                for (File f = file.getParentFile(); f != null; f = f.getParentFile())
                {
                    if (sourceDirs.contains(f.getPath()))
                    {
                        // We've hit one of the top-level dirs, so copy using the relative path from this dir
                        File dest = new File(destDir, relativePath.toString());
                        FileUtil.copy(file, dest);
                        break;
                    }
                    else
                    {
                        // Prepend the parent directory name to the relative path
                        relativePath.insert(0, File.separatorChar);
                        relativePath.insert(0, f.getName());
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new QAException("Failed to copy file", e);
        }           
    }
}
