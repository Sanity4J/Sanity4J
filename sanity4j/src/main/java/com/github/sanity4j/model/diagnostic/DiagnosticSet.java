package com.github.sanity4j.model.diagnostic; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** 
 * DiagnosticSet a set of Diagnostics.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public final class DiagnosticSet implements Cloneable, Iterable<Diagnostic>
{   
    /** A list of all the diagnostics contained in this DiagnosticSet. */
    private final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

    /** Diagnostics keyed by the source file name. */
    private Map<String, List<Diagnostic>> diagnosticsByFileName;
    /** Diagnostics keyed by the class name. */
    private Map<String, List<Diagnostic>> diagnosticsByClassName;
    /** Diagnostics keyed by the package name. */
    private Map<String, List<Diagnostic>> diagnosticsByPackageName;
    /** Diagnostics keyed by their severity. */
    private Map<String, List<Diagnostic>> diagnosticsBySeverity;
    /** Diagnostics keyed by their source (tool). */
    private Map<String, List<Diagnostic>> diagnosticsByTool;

    /**
     * @return the current Lists of Diagnostics, keyed by the source file name
     */
    public Map<String, List<Diagnostic>> getDiagnosticsByFileName()
    {
        // Lazy initialization of collection
        if (diagnosticsByFileName == null)
        {
            diagnosticsByFileName = new HashMap<String, List<Diagnostic>>();
            final int numDiagnositcs = diagnostics.size();

            for (int i = 0; i < numDiagnositcs; i++)
            {
                Diagnostic diagnostic = diagnostics.get(i);

                if (diagnostic.getFileName() != null)
                {
                    String fileName = diagnostic.getFileName();
                    addToMapList(diagnosticsByFileName, fileName, diagnostic);
                }
            }
        }

        return diagnosticsByFileName;
    }

	/**
     * @return the current Lists of Diagnostics, keyed by the class name
     */
    public Map<String, List<Diagnostic>> getDiagnosticsByClassName()
    {
        // Lazy initialization of collection
        if (diagnosticsByClassName == null)
        {
            diagnosticsByClassName = new HashMap<String, List<Diagnostic>>();
            final int numDiagnositcs = diagnostics.size();

            for (int i = 0; i < numDiagnositcs; i++)
            {
                Diagnostic diagnostic = diagnostics.get(i);

                if (diagnostic.getClassName() != null)
                {
                    String className = diagnostic.getClassName();
                    addToMapList(diagnosticsByClassName, className, diagnostic);
                }
            }
        }

        return diagnosticsByClassName;
    }

	/**
     * @return the current Lists of Diagnostics, keyed by package
     */
    public Map<String, List<Diagnostic>> getDiagnosticsByPackageName()
    {
        // Lazy initialization of collection
        if (diagnosticsByPackageName == null)
        {
            diagnosticsByPackageName = new HashMap<String, List<Diagnostic>>();
            final int numDiagnositcs = diagnostics.size();

            for (int i = 0; i < numDiagnositcs; i++)
            {
                Diagnostic diagnostic = diagnostics.get(i);

                if (diagnostic.getClassName() != null)
                {
                    String pkg = diagnostic.getClassName();

                    for (int index = pkg.lastIndexOf('.'); index != -1; index = pkg.lastIndexOf('.'))
                    {
                        pkg = pkg.substring(0, index);

                        addToMapList(diagnosticsByPackageName, pkg, diagnostic);
                    }
                }
            }
        }

        return diagnosticsByPackageName;
    }
	
	/**
     * @return the current Lists of Diagnostics, keyed by severity
     */
    public Map<String, List<Diagnostic>> getDiagnosticsBySeverity()
    {
        // Lazy initialization of collection
        if (diagnosticsBySeverity == null)
        {
            diagnosticsBySeverity = new HashMap<String, List<Diagnostic>>();
            final int numDiagnositcs = diagnostics.size();

            for (int i = 0; i < numDiagnositcs; i++)
            {
                Diagnostic diagnostic = diagnostics.get(i);

                String severity = String.valueOf(diagnostic.getSeverity());
                addToMapList(diagnosticsBySeverity, severity, diagnostic);
            }
        }

        return diagnosticsBySeverity;
    }

	/**
     * @return the current Lists of Diagnostics, keyed by tool
     */
    public Map<String, List<Diagnostic>> getDiagnosticsByTool()
    {
        // Lazy initialization of collection
        if (diagnosticsByTool == null)
        {
            diagnosticsByTool = new HashMap<String, List<Diagnostic>>();
            final int numDiagnositcs = diagnostics.size();

            for (int i = 0; i < numDiagnositcs; i++)
            {
                Diagnostic diagnostic = diagnostics.get(i);

                String tool = String.valueOf(diagnostic.getSource());
                addToMapList(diagnosticsByTool, tool, diagnostic);
            }
        }

        return diagnosticsByTool;
    }
	
	/**
     * Adds the given diagnostic to the set of diagnostics.
     * 
     * @param diagnostic the diagnostic to add.
     */
    public void add(final Diagnostic diagnostic)
    {
        if (!diagnostic.isExcluded())
        {
            diagnostics.add(diagnostic);
        }
    }
	
	/**
	 * Utility method to add a Diagnostic to a list of Diagnostics in a Map.
	 * 
	 * @param map the map to add to
	 * @param key the map key containing the list
	 * @param diag the Diagnostic to add
	 */
	private void addToMapList(final Map<String, List<Diagnostic>> map, final String key, final Diagnostic diag)
    {
        List<Diagnostic> list = map.get(key);

        if (list == null)
        {
            list = new ArrayList<Diagnostic>();
            map.put(key, list);
        }

        list.add(diag);
    }
	
	/**
	 * Returns the diagnostics obtained from the given tool.
	 * 
	 * @param tool the tool, see Diagnostic.SOURCE_*
	 * 
	 * @return the set of diagnostics for the given tool, may be empty
	 */
	public DiagnosticSet getDiagnosticsForTool(final int tool)
    {
        // No-op for all
        if (tool == Diagnostic.SOURCE_ALL)
        {
            return (DiagnosticSet) clone();
        }

        DiagnosticSet subset = new DiagnosticSet();

        List<Diagnostic> list = getDiagnosticsByTool().get(String.valueOf(tool));

        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Diagnostic diagnostic = list.get(i);

                if (diagnostic.getSource() == tool)
                {
                    subset.diagnostics.add(diagnostic);
                }
            }
        }

        return subset;
    }		

	/**
	 * Returns the diagnostics with the given severity.
	 * 
	 * @param severity the severity, see Diagnostic.SEVERITY_*
	 * 
	 * @return the set of diagnostics for the given severity, may be empty
	 */
	public DiagnosticSet getDiagnosticsForSeverity(final int severity)
    {
        // No-op for all
        if (severity == Diagnostic.SEVERITY_ALL)
        {
            return (DiagnosticSet) clone();
        }

        DiagnosticSet subset = new DiagnosticSet();

        List<Diagnostic> list = getDiagnosticsBySeverity().get(String.valueOf(severity));

        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Diagnostic diagnostic = list.get(i);

                if (diagnostic.getSeverity() == severity)
                {
                    subset.diagnostics.add(diagnostic);
                }
            }
        }

        return subset;
    }		

	/**
	 * Returns the diagnostics for the given package and sub-packages.
	 * 
	 * @param packageName the package name
	 * 
	 * @return the set of diagnostics for the given package and sub-packages
	 */
	public DiagnosticSet getDiagnosticsForPackage(final String packageName)
    {
        return getDiagnosticsForPackage(packageName, true);
    }
	
	/**
	 * Returns the diagnostics for the given package, and optionally, sub-packages.
	 * 
	 * @param packageName the package name
	 * @param includeSubpackages if true, also include diagnostics for sub-packages 
	 * 
	 * @return the set of diagnostics for the given package
	 */
	public DiagnosticSet getDiagnosticsForPackage(final String packageName, 
                                                  final boolean includeSubpackages)
    {
        DiagnosticSet subset = new DiagnosticSet();

        List<Diagnostic> list = getDiagnosticsByPackageName().get(packageName);

        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Diagnostic diag = list.get(i);

                if (includeSubpackages || packageName.equals(diag.getPackageName()))
                {
                    subset.diagnostics.add(diag);
                }
            }
        }

        return subset;
    }

	/**
	 * Returns the diagnostics for the given file.
	 * 
	 * @param fileName the name of the file.
	 * 
	 * @return the set of diagnostics for the given file.
	 */
	public DiagnosticSet getDiagnosticsForFile(final String fileName)
    {
        DiagnosticSet subset = new DiagnosticSet();

        List<Diagnostic> list = getDiagnosticsByFileName().get(fileName);

        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Diagnostic diagnostic = list.get(i);
                subset.diagnostics.add(diagnostic);
            }
        }

        return subset;
    }

    /**
     * @return a shallow copy of this DiagnosticSet.
     */
    public Object clone()
    {
        Object clone = null;

        try
        {
            clone = super.clone();
        }
        catch (CloneNotSupportedException ignored)
        {
            // impossible, as implements Cloneable
        }

        return clone;
    }
	
	/**
	 * Returns the number of diagnostics with the given severity.
	 * 
	 * @param severity the severity.
	 * @return the number of diagnostics with the given severity.
	 */
	public int getCountForSeverity(final int severity)
    {
        if (severity == Diagnostic.SEVERITY_ALL)
        {
            return size();
        }

        List<Diagnostic> list = getDiagnosticsBySeverity().get(String.valueOf(severity));

        return list == null ? 0 : list.size();
    }

    /**
     * @return the number of Diagnostics in this DiagnosticSet.
     */
    public int size()
    {
        return diagnostics.size();
    }

    /**
     * @return true if this DiagnosticSet contains no Diagnostics.
     */
    public boolean isEmpty()
    {
        return diagnostics.isEmpty();
    }

    /**
     * @return an iteration over the diagnostics in this DiagnosticSet.
     */
    public Iterator<Diagnostic> iterator()
    {
        return new ReadOnlyIterator<Diagnostic>(diagnostics);
    }

	/** 
	 * Read only iterator for iterating over the diagnostics in this DiagnosticSet .
	 */
	private static final class ReadOnlyIterator<T> implements Iterator<T>
	{
        /** The current position in the list. */
        private int index = 0;
        /** The list being iterated over. */
        private final List<T> list;
	    
        /**
         * Creates a ReadOnlyIterator for the given list.
         * 
         * @param list the list to iterate over.
         */
        ReadOnlyIterator(final List<T> list)
        {
            this.list = list;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext()
        {
            return index < list.size();
        }

        /**
         * @return the next element in the iteration.
         */
        public T next()
        {
            if (index == list.size())
            {
                throw new NoSuchElementException("iteration has no more elements");
            }
            
            return list.get(index++);
        }

        /**
         * Remove is not supported, calling this method will
         * result in an UnsupportedOperationException being thrown.
         */
        public void remove()
        {
            throw new UnsupportedOperationException("ReadOnlyIterator does not support remove");
        }
	}
}
