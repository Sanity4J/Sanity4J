package com.github.sanity4j.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An enumeration of available tools.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public enum Tool
{
    /** <a href="http://checkstyle.sourceforge.net/">Checkstyle</a> static source code analyser. */
    CHECKSTYLE("checkstyle", "Checkstyle"),

    /** <a href="https://spotbugs.github.io/">SpotBugs</a> static byte-code analyser. */
    SPOTBUGS("spotbugs", "SpotBugs"),
    
    /** <a href="http://www.jacoco.org/jacoco/index.html">JaCoco</a> datafile merge tool. */
    JACOCO_MERGE("jacoco-merge", "JaCoCo merge"),
    
    /** <a href="http://www.jacoco.org/jacoco/index.html">JaCoco</a> unit test coverage analysis. */
    JACOCO("jacoco", "JaCoCo"),
    
    /** <a href="http://pmd.sourceforge.net/">PMD</a> static source code analyser. */
    PMD("pmd", "PMD"),
    
    /** <a href="http://pmd.sourceforge.net/">PMD CPD</a> copy &amp; pasted source code detector. */
    PMD_CPD("pmd-cpd", "PMD CPD");

    //JUNIT("junit", "JUnit");

    /** A Read-only list of all the tools which are supported by Sanity4J. */
    public static final List<Tool> TOOLS = Collections.unmodifiableList(Arrays.asList(new Tool[]
    {
       CHECKSTYLE,
       SPOTBUGS,
       JACOCO,
       JACOCO_MERGE,
       //JUNIT,
       PMD,
       PMD_CPD
    }));

    /** The tool id, which is used to e.g. look up values in tools.properties. */
    private final String toolId;

    /** The human-readable tool name. */
    private final String name;

    /**
     * Creates a tool with the given id and name.
     * @param toolId the tool id
     * @param name the tool name
     */
    private Tool(final String toolId, final String name)
    {
       this.toolId = toolId;
       this.name = name;
    }

    /** @return the id */
    public String getId()
    {
        return toolId;
    }

    /** @return the name */
    public String getName()
    {
        return name;
    }

    /** @return the id */
    @Override
    public String toString()
    {
        return toolId;
    }

    /**
     * Retrieves the tool with the given id.
     * @param toolId the tool id.
     * @return the tool with the given id, or null if the tool does not exist.
     */
    public static Tool get(final String toolId)
    {
        for (Tool tool : TOOLS)
        {
            if (tool.getId().equals(toolId))
            {
                return tool;
            }
        }

        return null;
    }
}
