package net.sf.sanity4j.plugin;

import net.sf.sanity4j.plugin.preferences.PreferenceConstants;

import org.eclipse.core.runtime.Preferences;
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
    public static final String PLUGIN_ID = "net.sf.sanity4j.eclipse.plugin";

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
        Preferences prefs = getDefault().getPluginPreferences();
        return prefs.getString(PreferenceConstants.JAVA_RUNTIME);
    }
    
    /**
     * @return the file path to the products directory.
     */
    public static String getProductsDir()
    {
        Preferences prefs = getDefault().getPluginPreferences();
        return prefs.getString(PreferenceConstants.PRODUCTS_DIR);
    }
}
