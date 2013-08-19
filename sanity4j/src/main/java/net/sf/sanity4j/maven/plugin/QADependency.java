package net.sf.sanity4j.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A cut down Maven dependency.
 * 
 * @author Darian Bridge
 * @since Sanity4J 1.0.6
 */
public class QADependency 
{
	/** The Maven Group Id. */
	private String groupId;
	/** The Maven Artifact Id. */
	private String artifactId;
	/** The Maven Version. */
	private String version;
	/** The artifact path. */
	private File path;
	
	/** A List of child dependencies. */
	private final List<QADependency> dependencies = new ArrayList<QADependency>();

	/**
	 * @return The Maven Group Id.
	 */
	public String getGroupId() 
	{
		return groupId;
	}

	/**
	 * @param groupId The Maven Group Id to set.
	 */
	public void setGroupId(final String groupId) 
	{
		this.groupId = groupId;
	}

	/**
	 * @return The Maven Artifact Id.
	 */
	public String getArtifactId() 
	{
		return artifactId;
	}

	/**
	 * @param artifactId The Maven Artifact Id to set.
	 */
	public void setArtifactId(final String artifactId) 
	{
		this.artifactId = artifactId;
	}

	/**
	 * @return The Maven Version
	 */
	public String getVersion() 
	{
		return version;
	}

	/**
	 * @param version The Maven Version to set.
	 */
	public void setVersion(final String version) 
	{
		this.version = version;
	}

	/**
	 * @return The list of dependencies.
	 */
	public Collection<QADependency> getDependencies() 
	{
		return dependencies;
	}

	/**
	 * @param dependency The dependency to add to the list.
	 */
	public void addDependency(final QADependency dependency) 
	{
	    if (dependency != null && !dependency.equals(this))
	    {
	        dependencies.add(dependency);
	    }
	}
	
	/**
	 * Search for a particular dependency in the child list.
	 * 
	 * @param artifactId The Artifact Id to look for.
	 * @return The dependency from the child list that matches the Artifact Id.
	 */
	public QADependency getDependency(final String artifactId) 
	{
		for (QADependency dependency : dependencies)
		{
			if (artifactId.equals(dependency.getArtifactId()))
			{
				return dependency;
			}
		}
		
		return null;
	}

    /**
     * @return Returns the artifact path.
     */
    public File getPath()
    {
        return path;
    }

    /**
     * Sets the artifact path.
     * @param path The path to set.
     */
    public void setPath(final File path)
    {
        this.path = path;
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return groupId.hashCode() + artifactId.hashCode() + version.hashCode();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof QADependency))
        {
            return false;
        }
        
        QADependency other = (QADependency) obj;
        
        return groupId.equals(other.groupId)
             && artifactId.equals(other.artifactId)
             && version.equals(other.version);
    }
}
