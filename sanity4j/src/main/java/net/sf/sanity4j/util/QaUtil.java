package net.sf.sanity4j.util; 

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

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
    }
    
    /** The path location to the external properties file. */
    private static String externalPropertiesPath;
    
    /**
     * Reads the properties file located at <code>resourcePath</code> and
     * applies any overrides from external properties files. External 
     * properties files may be specified in a file "sanity4j.properties" in the
     * sanity4j directory and/or in the user's home directory. Properties are given
     * the following order of precedence:
     * <ul>
     * <li>A property in sanity4j.properties in the user's home directory</li>
     * <li>A property in sanity4j.properties in the sanity4j directory</li>
     * <li>Internal sanity4j properties from <code>resourcePath</code></li>
     * </ul>
     * 
     * @param resourcePath the internal resource path
     * @return A Properties map containing the properties from the external file (if any).
     */
    public static Properties getProperties(final String resourcePath)
    {
       Properties properties = new Properties();
       InputStream is = null;
       
       try
       {
          is = QaUtil.class.getResourceAsStream(resourcePath);
          properties.load(is);
       }
       catch (IOException e)
       {
           QaLogger.getInstance().error("Error reading internal properties file from: " + resourcePath, e);
       }
       finally
       {
          safeClose(is);
       }

       // Apply overrides
       Properties externalProperties = readExternalProperties();
       properties.putAll(externalProperties);
       
       return properties;
    }
    
    /**
     * Reads in the external properties file (if it exists).
     * External properties may be specified in a file "sanity4j.properties" in the
     * sanity4j directory and/or in the user's home directory. Properties are given
     * the following order of precedence:
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
        String propFile;
        
        if (externalPropertiesPath != null && externalPropertiesPath.length() > 0)
        {
            if (externalPropertiesPath.endsWith(File.pathSeparator))
            {
                propFile = externalPropertiesPath + "sanity4j.properties";
            }
            else
            {
                propFile = externalPropertiesPath + File.pathSeparator + "sanity4j.properties";
            }
        }
        else
        {
            propFile = "sanity4j.properties";
        }
        
        Properties externalProps = readProperties(new File(propFile));
        
        Properties userProps = readProperties(new File(
            System.getProperty("user.home"), "sanity4j.properties"));
        
        externalProps.putAll(userProps);
        
        return externalProps;
    }
    
    /**
     * Reads in properties from an external file. If the properties file
     * can not be read, an empty Properties is returned.
     * 
     * @param file the file to read the properties from.
     * @return the properties from the given file, or an empty Properties on error.
     */
    private static Properties readProperties(final File file)
    {
       Properties externalProps = new Properties();
       
       if (file.canRead())
       {
           FileInputStream fis = null;
           
           try
           {
               fis = new FileInputStream(file);
               externalProps.load(fis);
           }
           catch (IOException e)
           {
               QaLogger.getInstance().error("Error reading external properties file: " + file.getPath(), e);
           }
           finally
           {
               safeClose(fis);
           }
       }
       
       return externalProps;
    }
    
    /**
     * Determines the package for an arbitrary file.
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
                QaLogger.getInstance().error("Error closing " + closeable);
            }
        }
    }

    /**
     * Replaces parameter tokens in the command-line with their equivalents from the given map. Tokens are specified
     * using ${variableName} syntax.
     * 
     * @param in the String to modify
     * @param paramMap the parameter map
     * @return the modified String
     */
    public static String replaceTokens(final String in, final Map<String, String> paramMap)
    {
        int lastTokenEnd = -1;
        int tokenStart = in == null ? -1 : in.indexOf('$');

        // No tokens, just return the string
        if (tokenStart == -1)
        {
            return in;
        }

        StringBuffer result = new StringBuffer(in.length());

        while (tokenStart != -1 && tokenStart < in.length() - 3)
        {
            // Append anything since the last token's end
            if (lastTokenEnd + 1 < tokenStart)
            {
                result.append(in.substring(lastTokenEnd + 1, tokenStart));
            }

            int tokenEnd = in.indexOf('}', tokenStart + 2);

            if (in.charAt(tokenStart + 1) == '{' && tokenEnd != -1)
            {
                String paramKey = in.substring(tokenStart + 2, tokenEnd);

                if (paramMap.containsKey(paramKey))
                {
                    result.append(paramMap.get(paramKey));
                }
                else
                {
                    result.append(in.substring(tokenStart, tokenEnd + 1));
                }

                lastTokenEnd = tokenEnd;
                tokenStart = in.indexOf('$', lastTokenEnd + 1);
            }
            else
            {
                lastTokenEnd = tokenStart - 1;
                tokenStart = in.indexOf('$', tokenStart + 1);
            }
        }

        // Append anything left over
        if (lastTokenEnd + 1 < in.length())
        {
            result.append(in.substring(lastTokenEnd + 1));
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
     * Get the path location to the external properties file.
     * 
     * @return the path location to the external properties file.
     */
    public static String getExternalPropertiesPath()
    {
        return externalPropertiesPath;
    }
}
