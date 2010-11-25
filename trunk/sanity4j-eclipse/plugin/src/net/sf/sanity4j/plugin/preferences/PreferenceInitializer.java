package net.sf.sanity4j.plugin.preferences;

import net.sf.sanity4j.plugin.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    /** Initialises the default plugin preferences. */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.JAVA_RUNTIME, "java.exe");
        store.setDefault(PreferenceConstants.PRODUCTS_DIR, "products");
    }
}
