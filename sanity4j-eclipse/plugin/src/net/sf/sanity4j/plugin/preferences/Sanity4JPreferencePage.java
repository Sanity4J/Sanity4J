package net.sf.sanity4j.plugin.preferences;

import net.sf.sanity4j.plugin.Activator;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class Sanity4JPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    /** Creates a QaPreferencePage. */
    public Sanity4JPreferencePage()
    {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Sanity4J preferences");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
     * types of preferences. Each field editor knows how to save and restore itself.
     */
    public void createFieldEditors()
    {
        addField(new DirectoryFieldEditor(PreferenceConstants.PRODUCTS_DIR, "Products &directory:",
                                          getFieldEditorParent()));

        addField(new FileFieldEditor(PreferenceConstants.JAVA_RUNTIME, "&Java 1.5 executable:", 
                                     true, getFieldEditorParent()));
    }

    /**
     * Initializes this preference page for the given workbench.
     * @param workbench the workbench
     */
    public void init(final IWorkbench workbench)
    {
        // Nothing to init
    }
}
