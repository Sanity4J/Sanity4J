package com.github.sanity4j.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.util.QaLogger;
import com.github.sanity4j.workflow.QAConfig;

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
     * Copies a resource.
     *
     * @param classLoader the classloader used to load the resource.
     * @param resourcePath the path to the resource
     * @param destDir the directory to write the resource to
     * @param destPath the destination path, relative to destDir
     *
     * @throws IOException if there is an error writing the file
     */
    private static void copyResource(final ClassLoader classLoader,
                                     final String resourcePath, final File destDir,
                                     final String destPath) throws IOException
    {
        // Get the resource as a stream
        InputStream inStream = classLoader.getResourceAsStream(resourcePath);
        
        if (inStream == null)
        {
            // If not found, try loading from the default. 
            inStream = ExtractStaticContent.class.getResourceAsStream(resourcePath);
        }

        if (inStream == null)
        {
            // If not found, try loading from the current classloader. 
            inStream = ExtractStaticContent.class.getClassLoader().getResourceAsStream(resourcePath);
        }
        
        if (inStream == null)
        {
        	QaLogger.getInstance().error("Resource [" + resourcePath + "] doesn't exist");
        	return;
        }

        // Set up the destination file
        File destFile = new File(destDir, destPath);

        if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs())
        {
            QaLogger.getInstance().error("Failed to create parent directory for file [" + destPath + "]");
            return;
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

    /**
     * Extracts all of the static content specified by a Properties object to the destination directory.
     *
     * @param classLoader the classloader used to load the resources.
     * @param destDir the destination directory
     * @param properties a properties file containing name / value pairs
     * representing the source resources, and their destinations.
     * @throws IOException if there is an error extracting the static content
     */
    private static void extractContent(final ClassLoader classLoader, final File destDir, final Properties properties) throws IOException
    {
        for (Enumeration<Object> keyEnum = properties.keys(); keyEnum.hasMoreElements();)
        {
            String source = (String) keyEnum.nextElement();
            String dest = (String) properties.get(source);
            copyResource(classLoader, source.trim(), destDir, dest.trim());
        }
    }

    /**
     * This method extracts the static content specified by the properties stored within of an <b>InputStream</b>.
     *
     * @param classLoader the classloader used to load the resource.
     * @param destinationDirectory The directory to which the static content should be extracted.
     * @param inputStream The input stream representing the input to the <b>java.util.Properties#load(InputStream)</b> method.
     * @throws IOException If there is an error reading from the <em>inputStream</em> or writing to the <em>destDir</em>.
     */
    private static void extractStreamContent(final ClassLoader classLoader, final File destinationDirectory, final InputStream inputStream) throws IOException
    {
        if (inputStream == null)
        {
            QaLogger.getInstance().debug("Nothing to extract.");            
        }
        else
        {
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            extractContent(classLoader, destinationDirectory, properties);
        }
    }

    /**
     * Converts a String representing "path.separator" separated list of URLs into an array of java.net.URL objects.
     *  
     * @param classpath a String representing "path.separator" separated list of URLs.
     * @return An array of java.net.URL objects.
     * @throws MalformedURLException if entries within the classpath are invalid.
     */
    private static URL[] classpathToUrlArray(final String classpath) throws MalformedURLException
    {
        List<URL> urlList = new ArrayList<URL>();
        
        if (classpath != null)
        {
            String pathSeparator = System.getProperty("path.separator");
            StringTokenizer stringTokenizer = new StringTokenizer(classpath, pathSeparator);
            
            while (stringTokenizer.hasMoreTokens())
            {
                String stringUrl = stringTokenizer.nextToken();
                URL url = null;
                
                try
                {
                    url = new URL(stringUrl);
                }
                catch (MalformedURLException mue)
                {
                    // Assume the URL is a local file.
                    if (stringUrl.toUpperCase().endsWith(".JAR"))
                    {
                        url = new URL("jar:file:///" + stringUrl + "!/");
                    }
                    else
                    {
                        url = new URL("file:///" + stringUrl);
                    }
                }
                
                QaLogger.getInstance().debug("URL: " + url);
                urlList.add(url);
            }
        }
        
        URL[] urls = urlList.toArray(new URL[urlList.size()]);
        
        return urls;
    }
    
    /**
     * Extracts the static content to the destination directory.
     *
     * @param config the configuration object for the sanity4j tool.
     * @param destDir the destination directory.
     * @param propertiesFile the name of the properties file.
     * @throws IOException if there is an error extracting the static content.
     */
    public static void extractContent(final QAConfig config, final File destDir, final String propertiesFile) throws IOException
    {
        ClassLoader classLoader = ExtractStaticContent.class.getClassLoader();
        
        // Load default "ExtractStaticContent.properties" files.
        InputStream inStream = ExtractStaticContent.class.getResourceAsStream(propertiesFile);
        QaLogger.getInstance().debug("Extract using file: " + propertiesFile);
        extractStreamContent(classLoader, destDir, inStream);

        // Load all other "ExtractStaticContent.properties" files from classpath.
        QaLogger.getInstance().debug("Extract using file: " + propertiesFile);
        Enumeration<URL> resourceEnum = classLoader.getResources(propertiesFile);

        while (resourceEnum.hasMoreElements())
        {
            URL resourceUrl = resourceEnum.nextElement();
            
            QaLogger.getInstance().debug("Extract using URL: " + resourceUrl);
            
            inStream = resourceUrl.openStream();
            extractStreamContent(classLoader, destDir, inStream);
        }
        
        // Load all other "ExtractStaticContent.properties files from tool classpaths.
        for (String tool : config.getToolsToRun())
        {
            String version = config.getToolVersion(tool);
            String configClasspath = config.getToolConfigClasspath(tool, version);
            
            if (FileUtil.hasValue(configClasspath))
            {
                URL[] urls = classpathToUrlArray(configClasspath);
                ClassLoader toolClassLoader = new URLClassLoader(urls);
                resourceEnum = toolClassLoader.getResources(propertiesFile);

                while (resourceEnum.hasMoreElements())
                {
                    URL resourceUrl = resourceEnum.nextElement();

                    QaLogger.getInstance().debug("Extract using URL: " + resourceUrl);
                    
                    inStream = resourceUrl.openStream();
                    extractStreamContent(toolClassLoader, destDir, inStream);
                }
            }
        }
    }
}
