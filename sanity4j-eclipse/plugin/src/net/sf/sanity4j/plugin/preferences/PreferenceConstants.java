package com.github.sanity4j.plugin.preferences;

/**
 * Constant definitions for plug-in preferences.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class PreferenceConstants
{
    /** The PREFIX used by all sanity4j constants. */
    public static final String PREFIX = "com.github.sanity4j.plugin.preference.";

    /** The preference constant for the location of the java runtime executable. */
    public static final String JAVA_RUNTIME = PREFIX + "java.runtime";

    /** The preference constant for the location of the products directory. */
    public static final String PRODUCTS_DIRECTORY = PREFIX + "products.directory";

    /** The preference constant for the location of the sanity4j.properties file. */
    public static final String SANITY4J_PROPERTIES = PREFIX + "sanity4j.properties";

    /** The preference constant for the location of the sanity4j.properties file. */
    public static final String DIAGNOSTICS_FIRST = PREFIX + "diagnostics.first";

    /** The preference constant prefix for the string used to customize a tool's config. */
    public static final String TOOL_CONFIG_PREFIX = PREFIX + "tool.config.";
    
    /** The preference constant prefix for the string used to customize a tool's class path. */
    public static final String TOOL_CONFIG_CLASSPATH_PREFIX = PREFIX + "tool.config.classpath.";

    /** No instance methods here. */
    private PreferenceConstants()
    {
    }
}
