package com.github.sanity4j.plugin.util;

import java.io.File;
import java.io.IOException;

/**
 * FileUtil - file based utility methods. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class FileUtil
{
    /** No instance methods here. */
    private FileUtil()
    {
    }
    
    /**
     * Deletes a file/directory and all its contents.
     * 
     * @param file the file to delete
     * @throws IOException if the delete fails
     */
    public static void delete(final File file) throws IOException
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            
            for (int i = 0; i < files.length; i++)
            {
                delete(files[i]);
            }
        }
        
        if (file.exists() && !file.delete())
        {
            throw new IOException("Failed to delete " + file);
        }
    }    

    /**
     * Evaluates a string for emptiness.
     * 
     * @param str the string to evaluate.
     * @return true if the string is non-null and has a trimmed length greater than zero.
     */
    public static boolean hasValue(final String str)
    {
    	return str == null ? false : str.trim().length() > 0;
    }
}
