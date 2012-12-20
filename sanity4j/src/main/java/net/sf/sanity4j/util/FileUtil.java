package net.sf.sanity4j.util; 

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * FileUtil - File related utility methods.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public final class FileUtil
{
    /** The buffer size to use for copy operations. */
    private static final int BUFFER_SIZE = 4096;
    
    /** No instance methods here. */
    private FileUtil()
    {
    }
    
    /**
     * Creates a directory if it doesn't exist.
     * @param path the directory path.
     * 
     * @return a File Object representing the directory.
     */
    public static File createDir(final String path)
    {
        File dir = new File(path);
        
        if (!dir.exists() && !dir.mkdirs())
        {
            throw new QAException("Failed to create directory: " + path);
        }
        
        return dir;
    }    
    
    /** 
     * Searches for jar files within the given directory.
     * The full path of each jar will be added to the given list.
     * 
     * @param base the base directory
     * @param jars the list of jars to add to
     */
    public static void findJars(final File base, final List<String> jars)
    {
        // Does this directory contain a subdir that contains class files?
        File[] files = base.listFiles();
        
        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    findJars(files[i], jars);
                }
                else if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".jar"))
                {
                    jars.add(files[i].getPath());
                }
            }           
        }
    }
    
    /**
     * Deletes a file/directory and all its contents.
     * 
     * @param file the file to delete.
     * @throws IOException if the delete fails.
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
     * Copies a file.
     * 
     * @param source the source file
     * @param dest the destination file
     * @throws IOException if there is an error during the copy.
     */
    public static void copy(final File source, final File dest) throws IOException
    {
        if (!source.canRead())
        {
            throw new IOException("Unable to read from file: " + source.getPath());
        }
        
        if (dest.exists() && !dest.canWrite())
        {
            throw new IOException("Unable to read to file: " + dest.getPath());
        }
        
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs())
        {
            throw new IOException("Unable to create parent directory" + dest.getParentFile().getPath());
        }
        
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        
        try
        {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(dest);
            
            final byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead = inStream.read(buf);
            
            while (bytesRead != -1)
            {
                outStream.write(buf, 0, bytesRead);
                bytesRead = inStream.read(buf);
            }
            
            outStream.flush();
        }
        finally
        {
            QaUtil.safeClose(inStream);
            QaUtil.safeClose(outStream);
        }        
    }
    
    /**
     * Reads the entire binary contents of the given file.
     * 
     * @param file the file to read from
     * @return the binary content of the given file.
     * @throws IOException if there is an error reading from the file.
     */
    public static byte[] read(final File file) throws IOException
    {
        byte[] data = new byte[(int) file.length()];
        
        FileInputStream stream = null;
        
        try
        {
            stream = new FileInputStream(file);
            int pos = 0;
            int bytesRead = stream.read(data);
            
            while (bytesRead != -1 && pos < data.length)
            {
                pos += bytesRead;
                bytesRead = stream.read(data, pos, data.length - pos);
            }
        }
        finally
        {
            QaUtil.safeClose(stream);
        }
        
        return data;
    }

    /**
     * Writes a string to a file in UTF-8. Parent directories will be created.
     * 
     * @param string the String to write
     * @param file the File to write to
     * 
     * @throws IOException if there is an error writing to the file
     */
    public static void writeToFile(final String string, final File file) throws IOException
    {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
        {
            throw new IOException("Failed to create parent directory for " + file);
        }
        
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(string.getBytes("UTF-8"));
        fos.close();
    }
    
    /**
     * Flattens a list of jars into a classpth.
     * If an exitsing classpath is provided, then the new list is 
     * appended to the end of the old classpath.
     * 
     * @param jarList the list of jars to flatten.
     * @param classpath an existing classpath to append to, can be null.
     * @return the flattened classpath.
     */
    public static String flattenClasspath(final List<String> jarList, final String classpath)
    {
        if (jarList.isEmpty())
        {
        	return "";
        }
        else
        {
	        StringBuffer buf = new StringBuffer(); 

        	if (classpath != null)
        	{
	        	buf.append(classpath);
        	}
	        
        	String separator = System.getProperty("path.separator");
        	for (String toolJar : jarList)
        	{
        		buf.append(separator).append(toolJar);
        	}
        	
        	if (classpath == null)
        	{
        		buf.delete(0, separator.length());
        	}
        	
        	return buf.toString();
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
