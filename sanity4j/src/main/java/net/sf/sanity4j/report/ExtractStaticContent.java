package net.sf.sanity4j.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
     * Extracts all of the static content specified by a Properties object to the destination directory.
     *
     * @param destDir the destination directory
     * @param properties a properties file containing name / value pairs
     * representing the source resources, and their destinations.
     * @throws IOException if there is an error extracting the static content
     */
    private static void extractContent(final File destDir, final Properties properties) throws IOException
    {
        for (Enumeration<Object> keyEnum = properties.keys(); keyEnum.hasMoreElements();)
        {
            String source = (String) keyEnum.nextElement();
            String dest = (String) properties.get(source);
            copyResource(source.trim(), destDir, dest.trim());
        }
    }

    /**
     * Extracts the static content to the destination directory.
     *
     * @param destDir the destination directory
     * @throws IOException if there is an error extracting the static content
     */
    public static void extractContent(final File destDir) throws IOException
    {
        // Load default "ExtractStaticContent.properties" files.
        Properties properties = new Properties();
        InputStream inStream = ExtractStaticContent.class.getResourceAsStream("ExtractStaticContent.properties");

        if (inStream != null)
        {
            properties.load(inStream);
            inStream.close();
            extractContent(destDir, properties);
        }

        // Load all other "ExtractStaticContent.properties" files from classpath.
        ClassLoader classLoader = ExtractStaticContent.class.getClassLoader();
        Enumeration<URL> resourceEnum = (Enumeration<URL>) classLoader.getResources("ExtractStaticContent.properties");

        if (resourceEnum != null)
        {
            while (resourceEnum.hasMoreElements())
            {
                URL resourceUrl = resourceEnum.nextElement();
                InputStream resourceStream = resourceUrl.openStream();

                if (resourceStream != null)
                {
                    Properties resourceProperties = new Properties();
                    resourceProperties.load(resourceStream);
                    resourceStream.close();
                    extractContent(destDir, resourceProperties);
                }
            }
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
