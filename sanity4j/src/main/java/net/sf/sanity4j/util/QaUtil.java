package net.sf.sanity4j.util;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import net.sf.sanity4j.report.ExtractStaticContent;
import net.sf.sanity4j.workflow.QAConfig;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

/**
 * QaUtil - General utility methods.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class QaUtil
{
    /** No instance methods here. */
    private QaUtil()
    {
        // No instance methods here.
    }

    /** The path location to the external properties file. */
    private static String externalPropertiesPath;

    /** The size of the internal "copy" buffer. */
    private static final int BUFFER_SIZE = 4096;

    /**
     * This method copies the contents of an <b>InputStream</b> to an <b>OutputStream</b>.
     * 
     * @param inputStream The <b>InputStream</b> to be copied.
     * @param outputStream The <b>OutputStream</b> to which the <em>inputStream</em> is to be copied.
     * @throws IOException if <b>anything</b> goes wrong.
     */
    public static void copy(final InputStream inputStream, final OutputStream outputStream) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        while ((read = inputStream.read(buffer, 0, buffer.length)) > 0)
        {
            outputStream.write(buffer, 0, read);
        }

        outputStream.flush();
    }
    
    /**
     * Reads the internal classpath properties file located at <code>resourcePath</code>. 
     * 
     * @param resourcePath the internal resource path.
     * @return A Properties map containing the properties from the resource.
     */
    public static Properties readResourceProperties(final String resourcePath)
    {
        Properties properties = new Properties();
        
        if (FileUtil.hasValue(resourcePath))
        {
            String resourceFile;
            
            if (resourcePath.endsWith(".properties"))
            {
                resourceFile = resourcePath;
            }
            else
            {
                resourceFile = new File(resourcePath, "sanity4j.propeties").getPath();
            }
            
            InputStream inputStream = null;
            try
            {
                URL url = QaUtil.class.getResource(resourceFile);
                
                if (url != null)
                {
                    QaLogger.getInstance().debug("Resource Url: " + url);
                    
                    inputStream = url.openStream();
                    
                    properties.load(inputStream);
                }
            }
            catch (IOException e)
            {
                QaLogger.getInstance().error("Error reading resource properties file [" + resourceFile + "]", e);
            }
            finally
            {
                safeClose(inputStream);
            }
        }
        
        return properties;
    }
    
    /**
     * Reads a properties string as if it were the contents of a properties file. 
     * 
     * @param propertiesString the string containing the properties.
     * @return A Properties map containing the properties from the string.
     */
    public static Properties readStringProperties(final String propertiesString)
    {
        Properties stringProperties = new Properties();
        
        // Parse the properties string as if it were a properties file.
        if (FileUtil.hasValue(propertiesString))
        {
            InputStream stringStream = null;
            try 
            {
                byte[] bytes = propertiesString.getBytes("UTF-8");
                stringStream =  new ByteArrayInputStream(bytes);
                stringProperties.load(stringStream);
                
                QaLogger.getInstance().debug("Additional Properties:");
                for (Map.Entry<Object, Object> entry : stringProperties.entrySet())
                {
                    QaLogger.getInstance().debug(entry.getKey() + " = " + entry.getValue());
                }
            } 
            catch (UnsupportedEncodingException e) 
            {
                QaLogger.getInstance().error("Error reading Properties String", e);
            } 
            catch (IOException e) 
            {
                QaLogger.getInstance().error("Error reading Properties String", e);
            }
            finally
            {
                safeClose(stringStream);
            }
        }
        
        return stringProperties;
    }

    /**
     * Reads the properties file located at <code>resourcePath</code> and applies any overrides from external properties
     * files. External properties files may be specified in a file "sanity4j.properties" in the sanity4j directory
     * and/or in the user's home directory and/or Java system properties. Properties are given the following order of
     * precedence:
     * <ul>
     * <li>A Java system property.</li>
     * <li>A property in sanity4j.properties in the user's home directory.</li>
     * <li>A property in sanity4j.properties in the sanity4j directory.</li>
     * <li>Internal sanity4j properties from <code>resourcePath</code>.</li>
     * </ul>
     * 
     * @param resourcePath the internal resource path
     * @param propertiesString a String representing properties values (if any).
     * @return A Properties map containing the properties from the external file (if any).
     */
    public static Properties getProperties(final String resourcePath, final String propertiesString)
    {
        Properties properties = readResourceProperties(resourcePath);
        
        Properties stringProperties = readStringProperties(propertiesString);
        properties.putAll(stringProperties);
        
        // Apply overrides
        
        // Look for the external properties on the classpath first.
        if (FileUtil.hasValue(externalPropertiesPath))
        {
            String resource = externalPropertiesPath.startsWith("/") ? externalPropertiesPath : "/" + externalPropertiesPath;
            
            Properties resourceProperties = readResourceProperties(resource);
            properties.putAll(resourceProperties);
        }
        
        // Then look for the external properties on the file system next.
        Properties externalProperties = readExternalProperties();
        properties.putAll(externalProperties);

        Properties userHomeProperties = readUserHomeProperties();
        properties.putAll(userHomeProperties);

        Properties systemProperties = System.getProperties();
        properties.putAll(systemProperties);

        return properties;
    }
    
    /**
     * @see #getProperties(String, String)
     * 
     * @param resourcePath the internal resource path
     * @return A Properties map containing the properties from the external file (if any).
     */
    public static Properties getProperties(final String resourcePath)
    {
        return getProperties(resourcePath, null);
    }
    

    /**
     * Reads in the external properties file (if it exists). External properties may be specified in a file
     * "sanity4j.properties" in the sanity4j directory and/or in the user's home directory. Properties are given the
     * following order of precedence:
     * <ul>
     * <li>A property in sanity4j.properties in the user's home directory</li>
     * <li>A property in sanity4j.properties in the sanity4j directory</li>
     * <li>Internal sanity4j properties</li>
     * </ul>
     * 
     * @return A Properties map containing the properties from the external file (if any).
     */
    private static Properties readExternalProperties()
    {
        File propertiesFile = null;

        if (FileUtil.hasValue(externalPropertiesPath))
        {
            File externalProperties = new File(externalPropertiesPath);

            if (externalProperties.isDirectory())
            {
                propertiesFile = new File(externalProperties, "sanity4j.propeties");
            }
            else
            {
                propertiesFile = externalProperties;
            }
        }
        else
        {
            propertiesFile = new File("sanity4j.properties");
        }

        Properties externalProps = readProperties(propertiesFile);

        return externalProps;
    }

    /**
     * Reads in properties from the user's home directory. This method will use the java system properties "user.home"
     * to search for the user's home directory, and look for a file called "sanity4j.properties" within this
     * directory. If the externalPropertiesPath is set, then it will attempt to use the filename of the externalPropertiesPath
     * (not the full path name) to lookup a properties file in the user's home directory.
     * 
     * @return The properties defined within the sanity4j properties file within the user's home directory.
     */
    private static Properties readUserHomeProperties()
    {
        String userHome = System.getProperty("user.home");
        File userHomeFile = null;
        
        // We are just using the externalPropertiesPath to get the name of the file,
        // not the full pathname. Then we use that name to check if the
        // named file exists in the user home directory.
        
        if (FileUtil.hasValue(externalPropertiesPath))
        {
            File externalProperties = new File(externalPropertiesPath);

            if (externalProperties.isDirectory())
            {
                userHomeFile = new File(userHome, "sanity4j.properties");
            }
            else
            {
                String filename = externalProperties.getName();
                userHomeFile = new File(userHome, filename);
            }
        }
        else
        {
            userHomeFile = new File(userHome, "sanity4j.properties");
        }

        Properties userHomeProperties = readProperties(userHomeFile);
        
        return userHomeProperties;
    }

    /**
     * Reads in properties from an external file. If the properties file can not be read, an empty Properties is
     * returned.
     * 
     * @param file the file to read the properties from.
     * @return the properties from the given file, or an empty Properties on error.
     */
    private static Properties readProperties(final File file)
    {
        Properties properties = new Properties();

        if (file.canRead())
        {
            QaLogger.getInstance().debug("Reading properties file [" + file.getPath() + "]");

            FileInputStream fis = null;

            try
            {
                fis = new FileInputStream(file);
                properties.load(fis);
            }
            catch (IOException e)
            {
                QaLogger.getInstance().error("Error reading properties file [" + file.getPath() + "]", e);
            }
            finally
            {
                safeClose(fis);
            }
        }
        else
        {
            QaLogger.getInstance().debug("Could not find properties file [" + file.getPath() + "]");
        }

        return properties;
    }

    /**
     * Determines the package for an arbitrary file.
     * 
     * @param file the file
     * @return the package name for the given file, or null if it could not be determined.
     */
    public static String getPackageForFile(final File file)
    {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".java"))
        {
            return getPackageForSourceFile(file);
        }
        else if (name.endsWith(".class"))
        {
            return getPackageForClassFile(file);
        }

        return null;
    }

    /**
     * Determines the package for a class file, by parsing the source code.
     * 
     * @param file the source file
     * @return the package name for the given source file, or null on error.
     */
    public static String getPackageForSourceFile(final File file)
    {
        final String token = "package ";
        StringBuffer packageName = new StringBuffer();
        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(file);
            boolean inComment = false;
            boolean lineComment = false;
            int tokenPos = 0;
            int last = 0;

            for (int read = fis.read(); read != -1; read = fis.read())
            {
                if (inComment)
                {
                    if (last == '*' && read == '/')
                    {
                        inComment = false;
                    }
                }
                else if (lineComment)
                {
                    if (read == '\n' || read == '\r')
                    {
                        lineComment = false;
                    }
                }
                else
                {
                    if (last == '/' && read == '/')
                    {
                        lineComment = true;
                    }
                    else if (last == '/' && read == '*')
                    {
                        inComment = true;
                    }
                    else if (tokenPos < token.length())
                    {
                        tokenPos = (read == token.charAt(tokenPos)) ? tokenPos + 1 : 0;
                    }
                    else
                    {
                        if (read == ';')
                        {
                            return packageName.length() == 0 ? null : packageName.toString();
                        }

                        if (Character.isLetterOrDigit(read) || read == '.' || read == '_')
                        {
                            packageName.append((char) read);
                        }
                    }
                }

                last = read;
            }
        }
        catch (IOException e)
        {
            QaLogger.getInstance().error("Error reading source " + file.getPath(), e);
        }
        finally
        {
            safeClose(fis);
        }

        return null;
    }

    /**
     * Determines the package for a class file, by parsing the bytecode.
     * 
     * @param file the class file
     * @return the package name for the given class file, or null on error.
     */
    public static String getPackageForClassFile(final File file)
    {
        try
        {
            JavaClass javaClass = new ClassParser(file.getPath()).parse();
            return javaClass.getPackageName();
        }
        catch (Exception e)
        {
            QaLogger.getInstance().error("Error reading class " + file.getPath(), e);
        }

        return null;
    }

    /**
     * Closes the given Closeable, logging a message on any IO error.
     * 
     * @param closeable the Closeable to close.
     */
    public static void safeClose(final Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (IOException e)
            {
                QaLogger.getInstance().error("Error closing: " + closeable);
            }
        }
    }
    
    /**
     * Closes the given Closeable, logging a message on any IO error.
     * 
     * @param closeable the Closeable to close.
     */
    public static void safeClose(final JarFile closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (IOException e)
            {
                QaLogger.getInstance().error("Error closing: " + closeable);
            }
        }
    }
    
    /** The length of the 3 special tokens ${}. */
    private static final int TOKENLENGTH = 3;

    /**
     * Replaces parameter tokens in the given String with their equivalents from the given map, 
     * System properties or resources on the classpath. Tokens are specified using ${variableName} syntax.
     * 
     * @param string the String to modify
     * @param paramMap the parameter map
     * @param config the current configuration
     * @param auxClasspath an optional auxillary class path to search for resources.
     * @return the modified String
     */
    public static String replaceTokens(final String string, final Map<String, String> paramMap, final QAConfig config, final String auxClasspath)
    {
        int lastTokenEnd = -1;
        int tokenStart = ((string == null) ? -1 : string.indexOf('$'));

        // No tokens, just return the string
        if (tokenStart == -1)
        {
            return string;
        }

        StringBuffer result = new StringBuffer(string.length());

        while (tokenStart != -1 && tokenStart < string.length() - TOKENLENGTH)
        {
            // Append anything since the last token's end
            if (lastTokenEnd + 1 < tokenStart)
            {
                result.append(string.substring(lastTokenEnd + 1, tokenStart));
            }

            int tokenEnd = string.indexOf('}', tokenStart + 2);

            if (string.charAt(tokenStart + 1) == '{' && tokenEnd != -1)
            {
                String paramKey = string.substring(tokenStart + 2, tokenEnd);

                // First try substitution from the map
                if (paramMap.containsKey(paramKey))
                {
                    result.append(paramMap.get(paramKey));
                }
                // Next try a system property
                else if (System.getProperty(paramKey) != null)
                {
                    result.append(System.getProperty(paramKey));
                }
                else
                {
                    // Finally, see if it's a resource in the classpath which we need to extract.
                    File expanded;
                    
                    try
                    {
                        expanded = extractResource(config, paramKey, auxClasspath);
                    }
                    catch (IOException e)
                    {
                        expanded = null;
                    }
                    
                    if (expanded == null)
                    {
                        // Give up - just leave the token as is
                        result.append(string.substring(tokenStart, tokenEnd + 1));
                    }
                    else
                    {
                        result.append(expanded.getPath());
                    }
                }

                lastTokenEnd = tokenEnd;
                tokenStart = string.indexOf('$', lastTokenEnd + 1);
            }
            else
            {
                lastTokenEnd = tokenStart - 1;
                tokenStart = string.indexOf('$', tokenStart + 1);
            }
        }

        // Append anything left over
        if (lastTokenEnd + 1 < string.length())
        {
            result.append(string.substring(lastTokenEnd + 1));
        }

        return result.toString();
    }

    /**
     * Set the path location to the external properties file.
     * 
     * @param externalPropertiesPath the path location to the external properties file.
     */
    public static void setExternalPropertiesPath(final String externalPropertiesPath)
    {
        QaUtil.externalPropertiesPath = externalPropertiesPath;
    }

    /**
     * Retrieve the path set by {@link #setExternalPropertiesPath(String)}.
     * 
     * @return the path set by {@link #setExternalPropertiesPath(String)}.
     */
    public static String getExternalPropertiesPath()
    {
        return externalPropertiesPath;
    }
    
    /**
     * Attempts to extract a resource to the temp directory.
     *
     * @param config the current configuration.
     * @param resourceName the name of the resource to extract.
     * @param auxClasspath an optional auxillary class path to search for resources.
     * @return the path to the extracted resource, or null if nothing was extracted
     * @throws IOException if there is an error extracting the resource.
     */
    public static File extractResource(final QAConfig config, final String resourceName, final String auxClasspath) throws IOException
    {
        // Search for the Resource on the filesystem. 
        File resourceFile = new File(resourceName);
        
        if (resourceFile.exists())
        {
            // File found, add it as-is.
            QaLogger.getInstance().debug("Resource File: " + resourceName);
            return resourceFile;
        }
        else
        {
            // Search for the Resource on the classpath.
            URL resourceUrl = null;
            Enumeration<URL> resourceEnum = QaUtil.class.getClassLoader().getResources(resourceName);
            
            if (resourceEnum == null || !resourceEnum.hasMoreElements())
            {
                resourceEnum = ExtractStaticContent.class.getClassLoader().getResources(resourceName);
            }
            
            if (resourceEnum != null && resourceEnum.hasMoreElements())
            {
                QaLogger.getInstance().debug("Resource: " + resourceName);
                resourceUrl = resourceEnum.nextElement();
                
                if (resourceEnum.hasMoreElements())
                {                
                    QaLogger.getInstance().info("Resource [" + resourceName + "] found in multiple locations, using first.");
                }
            }
            
            if (resourceUrl == null && !StringUtil.empty(auxClasspath))
            {
                StringTokenizer tok = new StringTokenizer(auxClasspath, System.getProperty("path.separator"));

                while (tok.hasMoreTokens())
                {
                    File pathElem = new File(tok.nextToken());
                    
                    if (pathElem.canRead())
                    {
                        if (pathElem.isDirectory())
                        {
                            resourceFile = new File(pathElem, resourceName.replace('/', File.separatorChar));
                            
                            if (resourceFile.exists())
                            {
                                QaLogger.getInstance().debug("Resource File: " + resourceName);
                                return resourceFile;
                            }
                        }
                        else
                        {
                            // Try it as a jar
                            JarFile jar = null;
                            try
                            {
                                jar = new JarFile(pathElem);
                                ZipEntry entry = jar.getEntry(resourceName);
                                
                                if (entry != null)
                                {
                                    resourceUrl = new URL("jar:" + pathElem.toURI().toURL().toString() + "!/" + resourceName);
                                    QaLogger.getInstance().debug("Resource: " + resourceUrl);
                                    break;
                                }
                            }
                            catch (Exception e)
                            {
                                // Not a jar file, or could not read.
                            }
                            finally
                            {
                                safeClose(jar);
                            }
                        }
                    }
                }
            }
            
            if (resourceUrl != null)
            {
                QaLogger.getInstance().debug("Extract using URL: " + resourceUrl);
                
                InputStream inStream = null;
                FileOutputStream fos = null;
                try
                {
                    inStream = resourceUrl.openConnection().getInputStream();
                    
                    if (inStream == null)
                    {
                        // If not found, try loading from the current classloader. 
                        inStream = QaUtil.class.getClassLoader().getResourceAsStream(resourceName);
                    }
                    
                    if (inStream == null)
                    {
                        throw new IllegalArgumentException("Resource [" + resourceName + "] doesn't exist");
                    }
    
                    // Set up the destination file
                    File destFile = new File(config.getTempDir(), resourceName.replaceAll(".*/", ""));
    
                    if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs())
                    {
                        throw new IOException("Failed to create parent directory for file " + destFile.getPath());
                    }
    
                    // Copy the data
                    fos = new FileOutputStream(destFile);
                    byte[] buf = new byte[BUFFER_SIZE];
    
                    for (int count = inStream.read(buf); count != -1; count = inStream.read(buf))
                    {
                        fos.write(buf, 0, count);
                    }
    
                    return destFile;
                }
                finally
                {
                    safeClose(fos);
                    safeClose(inStream);
                }
            }
        }
        
        QaLogger.getInstance().debug("Can't find file or resource on classpath: " + resourceName);
        return null;
    }
}
