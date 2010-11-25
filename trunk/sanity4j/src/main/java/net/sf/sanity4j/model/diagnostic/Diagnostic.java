package net.sf.sanity4j.model.diagnostic;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaUtil;

/**
 * Diagnostic - represents a diagnostic message
 * that has been generated from one of the QA tools.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class Diagnostic
{
    /** 
     * A sequence used to generate id numbers. Safe as Sanity4J 
     * only runs within one VM. 
     */
    private static int idCounter = 1;

    /** The diagnostic properties, which controls severities, exclusions and categorisation. */
    private static final Properties properties = QaUtil.getProperties("/net/sf/sanity4j/model/diagnostic/Diagnostic.properties");
    
    // Source of the diagnostic.
    // Int range from SOURCE_ALL to SOURCE_CHECKSTYLE
    /** Any source. */
    public static final int SOURCE_ALL = -1;
    /** A Diagnostic sourced from an undefined tool. Should not occur. */
    public static final int SOURCE_OTHER = 0;
    /** A Diagnostic sourced from FindBugs. */
    public static final int SOURCE_FINDBUGS = 1;
    /** A Diagnostic sourced from PMD. */
    public static final int SOURCE_PMD = 2;
    /** A Diagnostic sourced from PMD CPD. */
    public static final int SOURCE_PMD_CPD = 3;
    /** A Diagnostic sourced from Checkstyle. */
    public static final int SOURCE_CHECKSTYLE = 4;  
    
    // Ratings taken from the Risk Rating table in the code review template
    // Int range from SEVERITY_ALL to SEVERITY_HIGH
    /** Any severity. */
    public static final int SEVERITY_ALL = -1;
    /** Information-only - for advice to junior developers. */
    public static final int SEVERITY_INFO = 0;
    /** Low priority - e.g. formatting issues. */
    public static final int SEVERITY_LOW = 1;
    /** Moderate priority - e.g. hard to understand code. */
    public static final int SEVERITY_MODERATE = 2;
    /** Significant priority - e.g. Probable NullPointerException. */
    public static final int SEVERITY_SIGNIFICANT = 3;
    /** High priority - e.g. calling System.exit from a Web-app. */
    public static final int SEVERITY_HIGH = 4;

    /** Class name. */
    private String className;
    
    /** Source file name. */
    private String fileName;
    
    /** Start line in source code. */
    private int startLine = -1;
    
    /** End line in source code. */
    private int endLine = -1;
    
    /** Start column in source code. */
    private int startColumn = -1;
    
    /** End column in source code. */
    private int endColumn = -1;
    
    /** Severity of the error. */   
    private int severity = -1;
    
    /** Source of the Diagnostic (which tool). */
    private int source = -1;
    
    /** Tool's rule name, if applicable. */
    private String ruleName;
    
    /** Diagnostic message. */
    private String message;
    
    /** The id of the diagnostic. */
    private final int id = nextId();
    
    /** @return the next id in the id sequence. */
    private static synchronized int nextId() 
    {
        return idCounter++;
    }

    /**
     * @return Returns the className.
     */
    public String getClassName()
    {
        return className;
    }
    
    /**
     * @return Returns the package name.
     */
    public String getPackageName()
    {
        int index = className.lastIndexOf('.');
        
        return (index == -1) ? "" : className.substring(0, index);
    }
    
    /**
     * @param className The className to set.
     */
    public void setClassName(final String className)
    {
        this.className = className;
    }
    
    /**
     * @return Returns the endColumn.
     */
    public int getEndColumn()
    {
        return endColumn;
    }
    
    /**
     * @param endColumn The endColumn to set.
     */
    public void setEndColumn(final int endColumn)
    {
        this.endColumn = endColumn;
    }
    
    /**
     * @return Returns the endLine.
     */
    public int getEndLine()
    {
        return endLine;
    }
    
    /**
     * @param endLine The endLine to set.
     */
    public void setEndLine(final int endLine)
    {
        this.endLine = Math.max(1, endLine);
    }
    
    /**
     * @return Returns the fileName.
     */
    public String getFileName()
    {
        return fileName;
    }
    
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(final String fileName)
    {
        this.fileName = fileName;
    }
    
    /**
     * @return Returns the message.
     */
    public String getMessage()
    {
        return message;
    }
    
    /**
     * @param message The message to set.
     */
    public void setMessage(final String message)
    {
        this.message = message;
    }
    
    /**
     * @return Returns the severity.
     */
    public int getSeverity()
    {
        return severity;
    }
    
    /**
     * @param severity The severity to set.
     */
    public void setSeverity(final int severity)
    {
        this.severity = severity;
    }
    
    /**
     * @return Returns the source.
     */
    public int getSource()
    {
        return source;
    }
    
    /**
     * @param source The source to set.
     */
    public void setSource(final int source)
    {
        this.source = source;
    }
    
    /**
     * @return Returns the className.
     */
    public String getRuleName()
    {
        return ruleName;
    }
    
    /**
     * @param ruleName The ruleName to set.
     */
    public void setRuleName(final String ruleName)
    {
        this.ruleName = ruleName;
    }
    
    /**
     * @return Returns the startColumn.
     */
    public int getStartColumn()
    {
        return startColumn;
    }
    
    /**
     * @param startColumn The startColumn to set.
     */
    public void setStartColumn(final int startColumn)
    {
        this.startColumn = startColumn;
        
        if (endColumn < startColumn)
        {
            endColumn = startColumn;
        }
    }
    
    /**
     * @return Returns the startLine.
     */
    public int getStartLine()
    {
        return startLine;
    }
    
    /**
     * @param startLine The startLine to set.
     */
    public void setStartLine(final int startLine)
    {
        this.startLine = Math.max(1, startLine);
        
        if (endLine < startLine)
        {
            endLine = startLine;
        }
    }
    
    /**
     * @return this Diagnostic's unique (within a VM) id.
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * @return a textual description of this Diagnostic's severity.
     */
    public String getSeverityDescription()
    {
        return getSeverityDescription(getSeverity());
    }
    
    /**
     * Returns a textual description of the severity.
     * 
     * @param severity the severity
     * @return a textual description of the severity
     */
    public static String getSeverityDescription(final int severity)
    {
        switch (severity)
        {
            case SEVERITY_ALL:
                return "All";
            
            case SEVERITY_INFO:
                return "Info";
            
            case SEVERITY_LOW:
                return "Low";
            
            case SEVERITY_MODERATE:
                return "Moderate";
            
            case SEVERITY_SIGNIFICANT:
                return "Significant";
            
            case SEVERITY_HIGH:
                return "High";
            
            default:
                return "Other";
        }
    }
    
    /**
     * @return a textual description of this Diagnostic's source.
     */
    public String getSourceDescription()
    {
        return getSourceDescription(getSource());
    }
    
    /**
     * Returns a textual description of the source.
     * 
     * @param source the source
     * @return a textual description of the source
     */
    public static String getSourceDescription(final int source)
    {
        switch (source)
        {
            case SOURCE_ALL:
                return "All";
            
            case SOURCE_OTHER:
                return "Other";
            
            case SOURCE_FINDBUGS:
                return "Findbugs";
            
            case SOURCE_PMD:
                return "PMD";
            
            case SOURCE_PMD_CPD:
                return "PMD-CPD";
            
            case SOURCE_CHECKSTYLE:
                return "Checkstyle";
            
            default:
                return "Other";
        }
    }
    
    /**
     * Returns the categories of this diagnostic, based upon the source and ruleName.
     * The categories are read from Diagnostic.properties.
     * 
     * @return the categories of this diagnostic
     */
    public String[] getCategories()
    {
        String sourceName = getSourceDescription();
        String key = sourceName + '.' + ruleName + ".category";
        String value = properties.getProperty(key); 
        
        return (value == null) ? new String[0] : value.split(",");
    }
    
    /**
     * Calculates the severity based on the Diagnostic.properties.
     */
    public void calcSeverity()
    {
        String sourceName = getSourceDescription();
        
        String key = sourceName + '.' + ruleName + ".severity";
        String value = properties.getProperty(key); 
        
        if (value == null)
        {       
            String msg = "Missing diagnostic severity for " + sourceName + ' ' + ruleName
                       + ", will default to INFO";

            QaLogger.getInstance().warn(msg);
            properties.put(key, String.valueOf(SEVERITY_INFO));
            setSeverity(SEVERITY_INFO);
        }
        else
        {
            setSeverity(Integer.parseInt(value));
        }
    }
    
    /**
     * Should this diagnostic be excluded from the report?
     * 
     * This is controlled by the Diagnostic.properties file, as some of the
     * tools don't support exclusions by class + rule.
     * 
     * @return true if the diagnostic should be excluded
     */
    public boolean isExcluded()
    {
        String sourceName = getSourceDescription();
        String keyBase = sourceName + '.' + ruleName; 
        
        String includes = properties.getProperty(keyBase + ".includes"); 
        String excludes = properties.getProperty(keyBase + ".excludes"); 
        
        // Is the file included by at least one include?
        if (includes != null)
        {
            boolean include = false;
            
            for (StringTokenizer st = new StringTokenizer(includes, ","); st.hasMoreTokens();)
            {
                if (Pattern.matches(st.nextToken(), className))
                {
                    include = true;
                    break;
                }
            }
                        
            if (!include)
            {
                return true;
            }
        }
        
        // Is the file excluded by at least one exclude?
        if (excludes != null)
        {
            for (StringTokenizer st = new StringTokenizer(excludes, ","); st.hasMoreTokens();)
            {
                if (Pattern.matches(st.nextToken(), className))
                {
                    return true;
                }               
            }            
        }       
        
        return false;
    }
}
