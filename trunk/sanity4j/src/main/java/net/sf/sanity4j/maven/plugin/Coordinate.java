package net.sf.sanity4j.maven.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Maven Coordinate.
 * 
 * A Coordinate can be constructed by parsing a String of the form:
 * <pre><code>
 * [groupId:]artifactId[:version][:packaging][:classifier][:scope]
 * </code></pre>
 * @author Darian Bridge.
 * @since Sanity4J 1.1.1
 */
public class Coordinate
{
    /** The GroupId. */
    private String groupId;
    /** The Artifact Id. */
    private String artifactId;
    /** The Version. */
    private String version;
    /** The Packaging. */
    private String packaging;
    /** The Classifier. */
    private String classifier;
    /** The Scope. */
    private String scope;

    /**
     * Default Constructor.
     */
    public Coordinate()
    {
        // Default Constructor.
    }
    
    /**
     * Construct a Coordinate by parsing the specified line and/or file.
     * 
     * @param line The line to parse.
     */
    public Coordinate(final String line)
    {
        parse(line);
    }

    /**
     * Copy Constructor.
     * 
     * @param coordinate The Coordinate to copy.
     */
    public Coordinate(final Coordinate coordinate)
    {
        setGroupId(coordinate.getGroupId());
        setArtifactId(coordinate.getArtifactId());
        setVersion(coordinate.getVersion());
        setPackaging(coordinate.getPackaging());
        setClassifier(coordinate.getClassifier());
        setScope(coordinate.getScope());
    }
    
    /**
     * @return The GroupId. 
     */
    public String getGroupId() 
    {
        return groupId;
    }

    /**
     * @param groupId The GroupId. 
     */
    public void setGroupId(final String groupId) 
    {
        this.groupId = groupId;
    }

    /**
     * @return The Artifact Id. 
     */
    public String getArtifactId() 
    {
        return artifactId;
    }

    /**
     * @param artifactId The Artifact Id. 
     */
    public void setArtifactId(final String artifactId) 
    {
        this.artifactId = artifactId;
    }

    /**
     * @return The Version. 
     */
    public String getVersion() 
    {
        return version;
    }

    /**
     * @param version The Version. 
     */
    public void setVersion(final String version) 
    {
        this.version = version;
    }

    /**
     * @return The Packaging.
     */
    public String getPackaging() 
    {
        return packaging;
    }

    /**
     * @param packaging The Packaging.
     */
    public void setPackaging(final String packaging) 
    {
        this.packaging = packaging;
    }

    /**
     * @return The Classifier.
     */
    public String getClassifier() 
    {
        return classifier;
    }

    /**
     * @param classifier The Classifier.
     */
    public void setClassifier(final String classifier) 
    {
        this.classifier = classifier;
    }

    /**
     * @param scope The Scope.
     */
    public void setScope(final String scope) 
    {
        this.scope = scope;
    }

    /**
     * @return The Scope.
     */
    public String getScope() 
    {
        return scope;
    }
    
    /**
     * The Pattern for parsing the Maven Coordinate.
     */
    private static final String COORDINATE_PATTERN = "(?:([^: ]+))?(?:[:]([^: ]+))?(?:[:]([^: ]+))?(?:[:]([^: ]+))?(?:[:]([^: ]+))?(?:[:]([^: ]+))?";
    
    /**
     * Parse a line interpreting it as a Maven artifact.
     * 
     * @param line The line to parse.
     */
    protected void parse(final String line) 
    {
        if (line != null)
        {
            Pattern pattern = Pattern.compile(COORDINATE_PATTERN);
            Matcher matcher = pattern.matcher(line.trim());
            
            if (matcher.matches()) 
            {
                String position1 = matcher.group(1);
                String position2 = matcher.group(2);
                String position3 = matcher.group(3);
                String position4 = matcher.group(4);
                String position5 = matcher.group(5);
                String position6 = matcher.group(6);
                
                int colons = occurrences(line, ':');
    
                switch (colons) 
                {
                    case 0:
                        setArtifactId(position1);
                        break;
                    case 1:
                        setGroupId(position1);
                        setArtifactId(position2);
                        break;
                    case 2:
                        setGroupId(position1);
                        setArtifactId(position2);
                        setVersion(position3);
                        break;
                    case 3:
                        setGroupId(position1);
                        setArtifactId(position2);
                        setVersion(position3);
                        setPackaging(position4);
                        break;
                    case 4:
                        setGroupId(position1);
                        setArtifactId(position2);
                        setVersion(position3);
                        setPackaging(position4);
                        setClassifier(position5);
                        break;
                    case 5:
                        setGroupId(position1);
                        setArtifactId(position2);
                        setVersion(position3);
                        setPackaging(position4);
                        setClassifier(position5);
                        setScope(position6);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    /**
     * Count the number of occurrences of the specified item within the character sequence.
     *  
     * @param sequence The character sequence within to count. 
     * @param item The item to count occurrences of.
     * @return The count of the number of occurrences of the specified item within the character sequence.
     */
    private static int occurrences(final CharSequence sequence, final char item) 
    {
        int counter = 0;
        for (int i = 0; i < sequence.length(); i++) 
        {
            if (sequence.charAt(i) == item) 
            {
                counter++;
            }
        }
        
        return counter;
    }

    /**
     * @return A String representing the Coordinate. 
     */
    @Override
    public String toString()
    {
        return getArtifact();
    }
    
    /**
     * @return A String representing the Maven Coordinate. 
     */
    public String getArtifact()
    {
        StringBuilder buf = new StringBuilder();
        
        append(buf, null, groupId);
        append(buf, ":", artifactId);
        append(buf, ":", version);
        append(buf, ":", packaging);
        append(buf, ":", classifier);
        append(buf, ":", scope);
        
        return buf.toString();
    }
    
    /**
     * Appends a delimiter and then an item to a StringBuilder.
     * Does nothing if the item is null.
     * 
     * @param buf The StringBuilder to append to.
     * @param delim A delimiter to append first (if not null).
     * @param item The item to append.
     */
    private void append(final StringBuilder buf, final String delim, final String item)
    {
        if (item != null) 
        {
            if (buf.length() > 0 && delim != null)
            {
                buf.append(delim);
            }
            
            buf.append(item);
        }
    }
}
