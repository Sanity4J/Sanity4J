package net.sf.sanity4j.maven.plugin;

import java.util.ArrayList;
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
	public List<QADependency> getDependencies() 
	{
		return dependencies;
	}

	/**
	 * @param dependency The dependency to add to the list.
	 */
	public void addDependency(final QADependency dependency) 
	{
		this.dependencies.add(dependency);
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
}
