package net.sf.sanity4j.report; 

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/** 
 * ExtractStaticContent - extracts static content for the report.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public final class ExtractStaticContent
{
    /** The buffer size to use when copying the data. */ 
    private static final int BUFFER_SIZE = 1024;
    
    /** ExtractStaticContent should not be instantiated. */
    private ExtractStaticContent()
    {
    }
    
    /**
     * Extracts the static content to the destination directory.
     *  
     * @param destDir the destination directory
     * @throws IOException if there is an error extracting the static content
     */
    public static void extractContent(final File destDir) throws IOException
    {
        Properties properties = new Properties();
        InputStream inStream = ExtractStaticContent.class.getResourceAsStream("ExtractStaticContent.properties");
        properties.load(inStream);
        inStream.close();
        
        for (Enumeration<Object> keyEnum = properties.keys(); keyEnum.hasMoreElements();)
        {
            String source = (String) keyEnum.nextElement();
            String dest = (String) properties.get(source);
            copyResource(source.trim(), destDir, dest.trim());		
        }
    }
    
	/**
	 * Copies a resource.
	 * 
	 * @param resourcePath the path to the resource
	 * @param destDir the directory to write the resource to
	 * @param destPath the destination path, relative to destDir
	 * 
	 * @throws IOException if there is an error writing the file
	 */
	private static void copyResource(final String resourcePath, final File destDir, 
                                     final String destPath) throws IOException
	{
	    // Get the resource as a stream
		InputStream inStream = ExtractStaticContent.class.getResourceAsStream(resourcePath);
		
		if (inStream == null)
		{
		    throw new IllegalArgumentException("Resource " + resourcePath + " doesn't exist");
		}
		
		// Set up the destination file
		File destFile = new File(destDir, destPath);
		
		if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs())
	    {
	        throw new IOException("Failed to create parent directory for file " + destPath);
	    }
		
		// Copy the data
		FileOutputStream fos = new FileOutputStream(destFile);
		byte[] buf = new byte[BUFFER_SIZE];
		
		for (int count = inStream.read(buf); count != -1; count = inStream.read(buf)) 
		{
			fos.write(buf, 0, count);
		}
		
		fos.close();
		inStream.close();
	}    
}
