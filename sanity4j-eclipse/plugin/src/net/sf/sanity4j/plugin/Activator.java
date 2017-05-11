package com.github.sanity4j.plugin;

import com.github.sanity4j.plugin.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class Activator extends AbstractUIPlugin
{
    /** The plug-in ID. Must match the id in the plugin.xml.*/
    public static final String PLUGIN_ID = "com.github.sanity4j.eclipse.plugin";

    /** The shared instance. */
    private static Activator plugin;

    /**
     * Constructs an Activator.
     */
    public Activator()
    {
        plugin = this;
    }

    /** 
     * Called when the plugin is stopped. Nulls the shared instance.
     * 
     * @param context the plugin context
     * @throws Exception thrown by the superclass 
     */
    public void stop(final BundleContext context) throws Exception
    {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static Activator getDefault()
    {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param path the relative path of the image.
     * @return the image descriptor.
     */
    public static ImageDescriptor getImageDescriptor(final String path)
    {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    /**
     * @return the file path to the java runtime executable .
     */
    public static String getJavaRuntime()
    {
        IPreferenceStore prefs = getDefault().getPreferenceStore();
        return prefs.getString(PreferenceConstants.JAVA_RUNTIME);
    }
    
    /**
     * @return the file path to the products directory.
     */
    public static String getProductsDir()
    {
        IPreferenceStore prefs = getDefault().getPreferenceStore();
        return prefs.getString(PreferenceConstants.PRODUCTS_DIRECTORY);
    }
    
    /**
     * @return the flag indicating whether diagnostics should be displayed first.
     */
    public static boolean getDiagnosticsFirst()
    {
        IPreferenceStore prefs = getDefault().getPreferenceStore();
        return prefs.getBoolean(PreferenceConstants.DIAGNOSTICS_FIRST);
    }
    
    /**
     * @return the external Sanity4J properties file.
     */
    public static String getSanity4JProperties()
    {
        IPreferenceStore prefs = getDefault().getPreferenceStore();
        return prefs.getString(PreferenceConstants.SANITY4J_PROPERTIES);
    }
    
    /**
     * This method returns the configuration parameters for the specified tool / version.
     * 
     * @param tool The name of the tool for which the configuration is to be retrieved.
     * @param version The version of the tool for which the configuration is to be retrieved.
     * @return the specified tool's configuration.
     */
    public static String getToolConfig(final String tool, final String version)
    {
        IPreferenceStore prefs = getDefault().getPreferenceStore();
        return prefs.getString(PreferenceConstants.TOOL_CONFIG_PREFIX + tool + ":" + version);
    }

    /**
     * This method returns the configuration class path parameters for the specified tool / version.
     * 
     * @param tool The name of the tool for which the configuration class path is to be retrieved.
     * @param version The version of the tool for which the configuration class path is to be retrieved.
     * @return the specified tool's configuration class path.
     */
    public static String getToolConfigClasspath(final String tool, final String version)
    {
        IPreferenceStore prefs = getDefault().getPreferenceStore();
        return prefs.getString(PreferenceConstants.TOOL_CONFIG_CLASSPATH_PREFIX + tool + ":" + version);
    }
}
