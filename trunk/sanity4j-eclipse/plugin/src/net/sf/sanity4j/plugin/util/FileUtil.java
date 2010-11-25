package net.sf.sanity4j.plugin.util;

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
}
