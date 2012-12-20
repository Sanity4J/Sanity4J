package net.sf.sanity4j.report;

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

import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.Tool;
import net.sf.sanity4j.workflow.QAConfig;

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
            throw new IllegalArgumentException("Resource [" + resourcePath + "] doesn't exist");
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
        Enumeration<URL> resourceEnum = (Enumeration<URL>) classLoader.getResources(propertiesFile);

        if (resourceEnum == null)
        {
            QaLogger.getInstance().debug("Nothing to extract.");            
        }
        else
        {
            while (resourceEnum.hasMoreElements())
            {
                URL resourceUrl = resourceEnum.nextElement();
                
                QaLogger.getInstance().debug("Extract using URL: " + resourceUrl);
                
                inStream = resourceUrl.openStream();
                extractStreamContent(classLoader, destDir, inStream);
            }
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
                resourceEnum = (Enumeration<URL>) toolClassLoader.getResources(propertiesFile);

                if (resourceEnum == null)
                {
                    QaLogger.getInstance().debug("Nothing to extract.");            
                }
                else
                {
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
    
    /**
     * Extracts the resource configuration to the destination directory.
     * Sets the config to the location of the extracted resource.
     * If the resource is a jar, then no extraction occurs, instead the config is set 
     * to the matching classpath found.
     *
     * @param config the configuration object for the sanity4j tool.
     * @param destDir the destination directory.
     * @param resourceName the name of the resource to extract.
     * @param tool the tool.
     * @param version the tool version.
     * @throws IOException if there is an error extracting the resource.
     */
    public static void expandResource(final QAConfig config, final File destDir, final String resourceName, final Tool tool, final String version) throws IOException
    {
        // Locate and extract the resource from the tool's classpath.
        
        String toolId = tool.getId();
        String configClasspath = config.getToolConfigClasspath(toolId, version);
        
        QaLogger.getInstance().debug("Tool: " + toolId);
        QaLogger.getInstance().debug("Version: " + version);

        if (config.getQADependency() != null)
        {
            List<String> toolJars = config.getQaDependencyJarList(toolId, version);
            if (toolJars != null)
            {
                QaLogger.getInstance().debug("Adding Maven Dependencies to Config Classpath.");
                
                configClasspath = FileUtil.flattenClasspath(toolJars, configClasspath);
                QaLogger.getInstance().debug("Config Classpath: " + configClasspath);
            }
        }
        
        List<String> toolJars = new ArrayList<String>();
        FileUtil.findJars(new File(config.getToolHome(toolId, version)), toolJars);
        configClasspath = FileUtil.flattenClasspath(toolJars, configClasspath);
        
        if (FileUtil.hasValue(configClasspath))
        {
            String pathSeparator = System.getProperty("path.separator");
            
            URL[] urls = classpathToUrlArray(configClasspath);
            String[] configClasspathItems = configClasspath.split(pathSeparator);
            ClassLoader toolClassLoader = new URLClassLoader(urls);

            List<String> configItems = new ArrayList<String>();
            
            final String tokens = " ," + pathSeparator;
            
            Enumeration<Object> tokeniser = new StringTokenizer(resourceName, tokens, true);
            
            while (tokeniser.hasMoreElements())
            {
                String resource = (String) tokeniser.nextElement();
                
                if (tokens.contains(resource))
                {
                    QaLogger.getInstance().debug("Token: [" + resource + "]");
                    
                    // Remember the tokens to add them back in after resource expansion is complete.
                    configItems.add(resource);
                }
                else if (resource.startsWith("-") || !resource.contains("."))
                {
                    QaLogger.getInstance().debug("Param: " + resource);
                    
                    // The resource is a config parameter, use it as-is.
                    configItems.add(resource);
                }
                else
                {
                    // Search for the Resource on the filesystem. 
                    File resourceFile = new File(resource);
                    
                    if (resourceFile.exists())
                    {
                        QaLogger.getInstance().debug("Resource File: " + resource);
                        
                        // File found, add it as-is.
                        configItems.add(resource);
                    }
                    else if (resource.toLowerCase().endsWith(".jar"))
                    {
                        QaLogger.getInstance().debug("Resource Jar: " + resource);
                        
                        // The resource is a jar, which is expected to be on the classpath
                        // so no extraction required. Instead the config is set to the matching classpath.
                        QaLogger.getInstance().debug("Config Classpath: " + configClasspath);
                        
                        String[] segments = resource.split("\\.jar");
                        
                        boolean matched = false;
                        
                        for (String segment : segments)
                        {
                            QaLogger.getInstance().debug("Segment: " + segment);
                            
                            for (String configClasspathItem : configClasspathItems)
                            {
                                if (configClasspathItem.contains(segment))
                                {
                                    matched = true;
                                    
                                    QaLogger.getInstance().debug("Matched: " + segment);
                                    QaLogger.getInstance().debug("Config Classpath Item: " + configClasspathItem);
                                    
                                    // replace the config resource with it's matching classpath.
                                    configItems.add(configClasspathItem);
                                }
                            }
                        }
                        
                        if (!matched)
                        {
                            QaLogger.getInstance().info("Can't find Resource Jar on classpath: " + resource);            
                            
                            // If we can't find it, just add it as-is.
                            // Maybe it's somewhere else not on the classpath.
                            configItems.add(resource);
                        }
                    }
                    else
                    {
                        // Search for the Resource on the classpath.
                        Enumeration<URL> resourceEnum = (Enumeration<URL>) toolClassLoader.getResources(resource);
                        if (resourceEnum == null || !resourceEnum.hasMoreElements())
                        {
                            resourceEnum = (Enumeration<URL>) ExtractStaticContent.class.getClassLoader().getResources(resource);
                        }
                        
                        if (resourceEnum != null && resourceEnum.hasMoreElements())
                        {
                            QaLogger.getInstance().debug("Resource: " + resource);
                            
                            for (int index = 0; resourceEnum.hasMoreElements(); index++)
                            {
                                URL resourceUrl = resourceEnum.nextElement();
                                
                                if (index > 1)
                                {
                                    QaLogger.getInstance().debug("Skipping: " + resourceUrl);
                                }
                                else if (index > 0)
                                {
                                    QaLogger.getInstance().info("Resource [" + resource + "] found in multiple locations, using first.");
                                    QaLogger.getInstance().debug("Skipping: " + resourceUrl);
                                }
                                else
                                {
                                    QaLogger.getInstance().debug("Extract using URL: " + resourceUrl);
                                    
                                    copyResource(toolClassLoader, resource, destDir, resource);
                                    
                                    File resourcePath = new File(destDir, resource);
                                    configItems.add(resourcePath.getAbsolutePath());
                                }
                            }
                        }
                        else
                        {
                            QaLogger.getInstance().info("Can't find Resource on classpath: " + resource);            
                            
                            // If we can't find it, just add it as-is.
                            // Maybe it's somewhere else not on the classpath.
                            configItems.add(resource);
                        }
                    }
                }
            }
            
            StringBuffer buf = new StringBuffer();
            
            for (String item : configItems)
            {
                buf.append(item);
            }
            
            config.setToolConfig(toolId, version, buf.toString(), configClasspath);
        }
        else
        {
            QaLogger.getInstance().debug("No classpath set. Nothing to extract.");            
        }
    }
}
