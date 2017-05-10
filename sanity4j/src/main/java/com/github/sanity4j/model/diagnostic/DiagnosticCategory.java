package com.github.sanity4j.model.diagnostic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * DiagnosticCategory - used for categorising {@link Diagnostic}s.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class DiagnosticCategory 
{
    /** The parent category, or null if this is the root category. */
	private final DiagnosticCategory parent;    
    /** The list of diagnostics for this category. */
	private final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
    /** A list of DiagnosticCategory objects that are sub-categories. */
	private final List<DiagnosticCategory> subCategories = new ArrayList<DiagnosticCategory>();
    /** The name of this category. */
	private final String name;
	
	/**
	 * Creates a root DiagnosticCategory.
	 */
	public DiagnosticCategory()
	{
		this("All categories", null);
	}
	
	/**
	 * Creates a sub-category with the given name and parent.
	 * 
	 * @param name the sub-category's name
	 * @param parent the sub-category's parent
	 */
	private DiagnosticCategory(final String name, final DiagnosticCategory parent)
	{
		this.name = name;
		this.parent = parent;
	}

	/**
	 * Adds the given diagnostic to this category, allocating sub-categories
	 * as necessary.
	 * 
	 * @param diagnostic the Diagnostic to add
	 */
	public void addDiagnostic(final Diagnostic diagnostic)
	{
		String[] categories = diagnostic.getCategories();
		
		for (int i = 0; i < categories.length; i++)
		{
			String category = categories[i]; 
			DiagnosticCategory targetCategory = this;
			
			// The category string will be in the form category[/subcategory[/...]]			
			for (StringTokenizer pathTok = new StringTokenizer(category, "/"); pathTok.hasMoreTokens();)
			{
				String pathElement = pathTok.nextToken();
				targetCategory = targetCategory.getCategory(pathElement);
			}
			
			targetCategory.diagnostics.add(diagnostic);
		}
	}
	
	/**
	 * @return this category's name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return the list of sub-categories
	 */
	public List<DiagnosticCategory> getSubCategories()
	{
		return subCategories;
	}
	
	/**
	 * @return the list of Diagnostics for this category only
	 */
	public List<Diagnostic> getDiagnostics()
	{
		return diagnostics;
	}
	
	/**
	 * @return the total number of diagnostics for this category 
	 * and all sub-categories.
	 */
	public int getDiagnosticCount()
	{
		int count = diagnostics.size();
		
		for (int i = 0; i < subCategories.size(); i++)
		{
			DiagnosticCategory category = subCategories.get(i); 
			count += category.getDiagnosticCount();
		}
		
		return count;
	}
	
	/**
     * Retrieves the total number of diagnostics for this category 
     * and all sub-categories, that have the given severity.
     * 
     * @param severity the severity of diagnostics to include in the count
	 * @return the count of diagnostics
	 */
	public int getDiagnosticCount(final int severity)
	{
		int count = 0;
		
	    if (severity == Diagnostic.SEVERITY_ALL)
	    {
		    // Simple case for all severities
	        count += diagnostics.size();
	    }
	    else
	    {
			for (int i = 0; i < diagnostics.size(); i++)
			{
				Diagnostic diagnostic = diagnostics.get(i);
				
				if (diagnostic.getSeverity() == severity)
				{
					count++;
				}
			}
	    }
	    
		for (int i = 0; i < subCategories.size(); i++)
		{
			DiagnosticCategory category = subCategories.get(i); 
			count += category.getDiagnosticCount(severity);
		}
		
		return count;
	}
	
	/**
	 * @return an iterator of sub-categories
	 */
	public Iterator<DiagnosticCategory> subCategoriesIterator()
	{
		return subCategories.iterator();
	}

	/**
	 * @return the parent DiagnosticCategory
	 */
	public DiagnosticCategory getParent()
	{
		return parent;
	}

	/**
	 * Returns the level of this node; 0 for the root node, 
	 * 1 for it's children, 2 for grand-children, etc.
	 * 
	 * @return the level of this node
	 */
	public int getLevel()
	{
		int level = 0;
		
		for (DiagnosticCategory c = getParent(); c != null; c = c.getParent())
		{
			level++;
		}
		
		return level;
	}
	
	/**
	 * @return true if this DiagnosticCategory is the root category
	 */
	public boolean isRoot()
	{
		return parent == null;
	}
	
	/**
	 * Returns the category with the given name, or creates one if it isn't found.
	 *  
	 * @param name the name of the category
	 * @return a subcategory with the given name
	 */
	private DiagnosticCategory getCategory(final String name)
	{
		for (int i = 0; i < subCategories.size(); i++)
		{
			DiagnosticCategory category = subCategories.get(i); 
			
			if (name.equals(category.name))
			{
				return category;
			}
		}
		
		DiagnosticCategory category = new DiagnosticCategory(name, this);
		subCategories.add(category);
		
		return category;
	}
}
