package net.sf.sanity4j.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is a class rather than an enumeration
 * as we need additional attributes.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class Tool
{
    /** <a href="http://checkstyle.sourceforge.net/">Checkstyle</a> static source code analyser. */
    public static final Tool CHECKSTYLE = new Tool("checkstyle", "Checkstyle");
    /** <a href="http://cobertura.sourceforge.net/">Cobertura</a> datafile merge tool. */
    public static final Tool COBERTURA_MERGE = new Tool("cobertura-merge", "Cobertura Merge");
    /** <a href="http://cobertura.sourceforge.net/">Cobertura</a> unit test coverage analysis. */
    public static final Tool COBERTURA = new Tool("cobertura", "Cobertura");
    /** <a href="http://findbugs.sourceforge.net/">FindBugs</a> static byte-code analyser. */
    public static final Tool FINDBUGS = new Tool("findbugs", "FindBugs");
    /** <a href="http://pmd.sourceforge.net/">PMD</a> static source code analyser. */
    public static final Tool PMD = new Tool("pmd", "PMD");
    /** <a href="http://pmd.sourceforge.net/">PMD CPD</a> copy & pasted source code detector. */
    public static final Tool PMD_CPD = new Tool("pmd-cpd", "PMD CPD");

    //public static final Tool JUNIT = new Tool("junit", "JUnit");

    /** A Read-only list of all the tools which are supported by Sanity4J. */
    public static final List<Tool> TOOLS = Collections.unmodifiableList(Arrays.asList(new Tool[]
    {
       CHECKSTYLE,
       COBERTURA_MERGE,
       COBERTURA,
       FINDBUGS,
       //JUNIT,
       PMD,
       PMD_CPD
    }));

    /** The tool id, which is used to e.g. look up values in tools.properties. */
    private final String id;

    /** The human-readable tool name. */
    private final String name;

    /**
     * Creates a tool with the given id and name.
     * @param id the tool id
     * @param name the tool name
     */
    private Tool(final String id, final String name)
    {
       this.id = id;
       this.name = name;
    }

    /** @return the id */
    public String getId()
    {
        return id;
    }

    /** @return the name */
    public String getName()
    {
        return name;
    }

    /** @return the id */
    public String toString()
    {
        return id;
    }

    /**
     * Retrieves the tool with the given id.
     * @param id the tool id.
     * @return the tool with the given id, or null if the tool does not exist.
     */
    public static Tool get(final String id)
    {
        for (Tool tool : TOOLS)
        {
            if (tool.getId().equals(id))
            {
                return tool;
            }
        }

        return null;
    }

    /**
     * Indicates if the given object is equal to this tool.
     * Two tools are considered equal if their IDs match.
     * 
     * @param obj the object to test for equality.
     * @return true if the object is equal to this tool.
     */
    public boolean equals(final Object obj)
    {
        return obj instanceof Tool && ((Tool) obj).id.equals(id);
    }

    /** {@inheritDoc} */
    public int hashCode()
    {
        return id.hashCode();
    }
}
