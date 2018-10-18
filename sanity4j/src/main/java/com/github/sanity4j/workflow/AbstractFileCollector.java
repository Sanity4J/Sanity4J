package com.github.sanity4j.workflow; 

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.util.QaUtil;

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
    private final List<File> sourceDirs;
    
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
        
        this.sourceDirs = new ArrayList<File>(sourceDirs.size());
        
        for (String dir : sourceDirs)
        {
            this.sourceDirs.add(new File(dir));
        }
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
    @Override
    public void run()
    {
        QaLogger.getInstance().info("Creating combined " + getItemType() + " directory.");        
        FileUtil.createDir(destDir.getPath());
        
        int count = copyFiles(sourceDirs, destDir);        
        QaLogger.getInstance().info("Copied " + count + " files.");
        
        if (count == 0 && isMandatory())
        {
            throw new QAException("\"" + getItemDescription() + "\" are required, but none were found.");
        }        
    }
    
    /** @return the type of item being copied, e.g. "source", "class". */
    protected abstract String getItemType();
    
    /** @return the brief description of the type of item being copied, e.g. "sources", "classes". */
    protected abstract String getItemDescription();
    
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
    private int copyFiles(final List<File> filePaths, final File destDir)
    {
        int count = 0;

        // We want to copy files from arbitrary subdirectories,
        // while maintaining the package structure
        for (Iterator<File> i = filePaths.iterator(); i.hasNext();)
        {
            File source = i.next();

            // If it's a directory, recurse...
            if (source.isDirectory())
            {
                String[] containedNames = source.list();
                File[] containedFiles = new File[containedNames.length];
                
                for (int j = 0; j < containedFiles.length; j++)
                {
                    containedFiles[j] = new File(source, containedNames[j]);
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
            
            if (packageName == null)
            {
                // Unable to determine package, use relative path from root.
                StringBuilder relativePath = new StringBuilder(file.getName());
                File parent = null;
                
                for (parent = file.getParentFile(); parent != null; parent = parent.getParentFile())
                {
                    if (sourceDirs.contains(parent))
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
                        relativePath.insert(0, parent.getName());
                    }
                }
                
                // If we couldn't find a relative path, then just copy the file.
                if (parent == null)
                {
                    File dest = new File(destDir, file.getName());
                    FileUtil.copy(file, dest);
                }
            }
            else
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
        }
        catch (IOException e)
        {
            throw new QAException("Failed to copy file", e);
        }           
    }
}
