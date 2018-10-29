package com.github.sanity4j.plugin.preferences;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.github.sanity4j.plugin.Activator;

/**
 * This class represents an Eclipse "Preference page" for the Sanity4J application. It is contributed to the Eclipse
 * "Preferences" dialog.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 * 
 * @author Yiannis Paschalidis
 */
public class Sanity4JPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    /** A separator string used within the tool configuration maps. */
    private static final String SEPARATOR = ":";

    /** A {@link Text} containing the configuration for the Sanity4J "products" directory. */
    private Text toolsDirectoryText;

    /** A {@link Text} containing the location of a "1.8" JVM executable. */
    private Text javaExecutableText;

    /** A {@link Button} used to flag whether diagnostics should be listed first. */
    private Button diagnosticFirstCheck;
    
    /** A {@link Text} containing the fully qualified path to a Sanity4J external "properties" file. */
    private Text sanity4jPropertiesText;

    /** The last tool selected within the "tools" drop-down list. */
    private String lastTool;

    /** The last tool version selected within the "versions" drop-down list. */
    private String lastVersion;

    /** The {@link Label} for the {@link #configText}. */
    private Label configLabel;

    /** A {@link Text} containing the config string for the currently selected "tool". */
    private Text configText;
    
    /** The {@link Label} for the {@link #configClasspathText}. */
    private Label configClasspathLabel;

    /** A {@link Text} containing the configuration class path for the currently selected "tool". */
    private Text configClasspathText;

    /**
     * A Map from a "tool" to the value stored within the Tool Configuration {@link Text}.
     */
    private final Map<String, String> toolConfig = new HashMap<String, String>();

    /**
     * A Map from a "tool" to the value stored within the Tool Configuration class path {@link Text}.
     */
    private final Map<String, String> toolConfigClasspath = new HashMap<String, String>();
    
    /** Columns. */
    private static final int COLUMNS = 3;
    
    /** Vertical spacing. */
    private static final int VERTICAL_SPACING = 10;
    
    /** A big width hint. */
    private static final int BIG_WIDTH_HINT = 300;
    
    /** A small width hint. */
    private static final int SMALL_WIDTH_HINT = 50;

    // ****************************************************************
    // UTILITY METHODS
    // ****************************************************************

    /**
     * This method will attempt to derive the location of a "1.8" java executable from the System properties of the
     * currently running JVM.
     * 
     * @return The fully qualified path to a java executable, or <b>null</b> if none could be found by this method.
     */
    private static String getJavaExecutable()
    {
        String javaExecutable = "";
        String javaHome = System.getProperty("java.home");
        String javaSpecificationVersion = System.getProperty("java.specification.version");

        // Naive method of ensuring we are getting at least a 1.8 JVM.
        if (("1.8".equalsIgnoreCase(javaSpecificationVersion) || "9".equalsIgnoreCase(javaSpecificationVersion) || "10".equalsIgnoreCase(javaSpecificationVersion))
            && javaHome != null)
        {
            File javaHomeFile = new File(javaHome);
            File javaFile = new File(javaHomeFile, "bin" + System.getProperty("file.separator") + "java");
            File windowsJavaFile = new File(javaHomeFile, "bin" + System.getProperty("file.separator") + "java.exe");
            // TODO: Generalize this for other operation systems.

            if (windowsJavaFile.exists())
            {
                javaExecutable = windowsJavaFile.getAbsolutePath();
            }
            else if (javaFile.exists())
            {
                javaExecutable = javaFile.getAbsolutePath();
            }
        }

        return javaExecutable;
    }

    /**
     * This method will prompt the user for a file.
     * 
     * @param typeName The "extra" type name displayed within the "Files of Type" drop-down filter.
     * @param typeExtension The "extra" extension filter to be used when the given "typeName" is selected in the
     *            "Files of Type" drop-down filter.
     * @param initialFilename A file used as a "default".
     * @return The name of the file selected by the user.
     */
    private String browseFile(final String typeName, final String typeExtension, final String initialFilename)
    {
        Shell shell = getShell();
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);

        String[] filterNames = null;
        String[] filterExtensions = null;

        String platform = SWT.getPlatform();

        if ("win32".equals(platform) || "wpf".equals(platform))
        {
            filterNames = new String[] { typeName, "All Files (*.*)" };
            filterExtensions = new String[] { typeExtension, "*.*" };
        }
        else
        {
            filterNames = new String[] { typeName, "All Files (*)" };
            filterExtensions = new String[] { typeExtension, "*" };
        }

        dialog.setFilterNames(filterNames);
        dialog.setFilterExtensions(filterExtensions);
        dialog.setFileName(initialFilename);

        dialog.open();

        String filename = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName();

        return filename;
    }

    /**
     * This method will prompt the user for a directory location.
     * 
     * @param initialDirectoryName A file used as a "default" directory.
     * @return The name of the file selected by the user.
     */
    private String browseDirectory(final String initialDirectoryName)
    {
        Shell shell = getShell();
        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
        dialog.setText(initialDirectoryName);

        dialog.open();

        String directoryName = dialog.getFilterPath();

        return directoryName;
    }

    /**
     * This method returns an array of Strings representing the names of the tools that will be run by the Sanity4J
     * application.
     * 
     * @return An array of Strings representing the names of the tools that will be run by the Sanity4J application.
     */
    private String[] getTools()
    {
        QAConfig qaConfig = new QAConfig();
        String externalPropertiesPath = sanity4jPropertiesText.getText();

        if (!StringUtil.empty(externalPropertiesPath))
        {
            qaConfig.setExternalPropertiesPath(externalPropertiesPath);
        }

        String[] tools = qaConfig.getToolsToRun();

        return tools;
    }

    /**
     * This method returns an array of Strings representing the defined versions of the given tool that will be run by
     * the Sanity4J application.
     * 
     * @param tool The tool for which the versions are to be retrieved.
     * @return An array of Strings representing the version of the of given <em>tool</em>.
     */
    private String[] getToolVersions(final String tool)
    {
        QAConfig qaConfig = new QAConfig();
        String externalPropertiesPath = sanity4jPropertiesText.getText();

        if (!StringUtil.empty(externalPropertiesPath))
        {
            qaConfig.setExternalPropertiesPath(externalPropertiesPath);
        }

        String[] versions = qaConfig.getToolVersions(tool);

        return versions;
    }

    /**
     * This method initializes the {@link #toolConfig} and {@link #toolConfigClasspath} variables from the
     * {@link QAConfig} and the {@link IPreferenceStore}.
     * 
     * @param override if <b>true</b> entries within the {@link IPreferenceStore} will "override" entries within the
     *            QAConfig. If <b>false</b> entries within the {@link IPreferenceStore} will be ignored.
     * @param store The plugin's {@link IPreferenceStore}. This values within this store will override those stored
     *            within the {@link QAConfig}.
     */
    private void loadToolConfig(final boolean override, final IPreferenceStore store)
    {
        QAConfig qaConfig = new QAConfig();
        qaConfig.setExternalPropertiesPath(sanity4jPropertiesText.getText());
        String[] tools = getTools();

        for (String tool : tools)
        {
            String[] versions = getToolVersions(tool);

            for (String version : versions)
            {
                String key = tool + SEPARATOR + version;

                String config = qaConfig.getToolConfig(tool, version);
                String configClasspath = qaConfig.getToolConfigClasspath(tool, version);

                if (config == null)
                {
                    config = "";
                }

                if (configClasspath == null)
                {
                    configClasspath = "";
                }
                
                if (override)
                {
                    String configClasspathOverride = store.getString(PreferenceConstants.TOOL_CONFIG_CLASSPATH_PREFIX + key);

                    if (!StringUtil.empty(configClasspathOverride))
                    {
                        configClasspath = configClasspathOverride;
                    }
                }
                
                toolConfig.put(key, config);
                toolConfigClasspath.put(key, configClasspath);
            }
        }
    }

    /**
     * Load the preferences page from the given store.
     * 
     * @param override if <b>true</b> entries within the {@link IPreferenceStore} will "override" entries within the
     *            QAConfig. If <b>false</b> entries within the {@link IPreferenceStore} will be ignored.
     * @param store The store containing the save preferences.
     */
    private void loadFromStore(final boolean override, final IPreferenceStore store)
    {
        toolsDirectoryText.setText(store.getString(PreferenceConstants.PRODUCTS_DIRECTORY));
        javaExecutableText.setText(store.getString(PreferenceConstants.JAVA_RUNTIME));
        sanity4jPropertiesText.setText(store.getString(PreferenceConstants.SANITY4J_PROPERTIES));
        diagnosticFirstCheck.setSelection(store.getBoolean(PreferenceConstants.DIAGNOSTICS_FIRST));

        loadToolConfig(override, store);
        String config = toolConfig.get(lastTool + SEPARATOR + lastVersion);
        String configClasspath = toolConfigClasspath.get(lastTool + SEPARATOR + lastVersion);
        configText.setText(config == null ? "" : config);
        configClasspathText.setText(configClasspath == null ? "" : configClasspath);
    }

    /**
     * Validates the data entered into the {@link Sanity4JPreferencesPage}. If invalid data is found, and error message
     * will be displayed to the user describing the error.
     * 
     * @return <b>true</b> if the data is valid, <b>false</b> otherwise.
     */
    private boolean validate()
    {
        // productsDirectoryText;
        if (StringUtil.empty(toolsDirectoryText.getText()))
        {
            MessageDialog.openError(getShell(), "Invalid tools directory", "Please enter a tools directory");
            return false;
        }

        File toolsDirectory = new File(toolsDirectoryText.getText());

        if (!toolsDirectory.exists() || !toolsDirectory.isDirectory())
        {
            MessageDialog.openError(getShell(), "Invalid tools direcotory", "Could not find tools directory ["
                                                                               + toolsDirectoryText.getText() + "]");
            return false;
        }

        // javaExecutableText;
        if (StringUtil.empty(javaExecutableText.getText()))
        {
            MessageDialog.openError(getShell(), "Invalid java executable", "Please enter a java executable");
            return false;
        }

        File javaExecutable = new File(javaExecutableText.getText());

        if (!javaExecutable.exists())
        {
            MessageDialog.openError(getShell(), "Invalid java executable", "Could not find java executable ["
                                                                           + javaExecutableText.getText() + "]");
            return false;
        }

        // sanity4jPropertiesText;
        if (!StringUtil.empty(sanity4jPropertiesText.getText()))
        {
            File sanity4jProperties = new File(sanity4jPropertiesText.getText());

            if (!sanity4jProperties.exists())
            {
                MessageDialog.openError(getShell(), "Invalid Sanity4J properties files",
                                        "Could not find Sanity4J properties files [" + sanity4jPropertiesText.getText()
                                            + "]");
                return false;
            }
        }

        // configText;

        // configClasspathText;

        return true;
    }

    /**
     * Updates the current tool selection based upon the last tool / version selected.
     */
    private void setCurrentTool()
    {
        String config = toolConfig.get(lastTool + SEPARATOR + lastVersion);
        String configClasspath = toolConfigClasspath.get(lastTool + SEPARATOR + lastVersion);

        configText.setText(config == null ? "" : config);
        configClasspathText.setText(configClasspath == null ? "" : configClasspath);

        String prefix = lastTool + " ";

        configLabel.setText(prefix + "&config:");
        configClasspathLabel.setText(prefix + "co&nfig classpath:");
    }

    // ****************************************************************
    // UI Creation
    // ****************************************************************

    /**
     * This method returns a {@link Composite} used as the top level container for the Sanity4J preferences Page.
     * 
     * @param parent The parent object in which the preferences Page is created.
     * @return The {@link Composite} used as the top level container for the Sanity4J preferences Page.
     */
    private Composite createComposite(final Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = VERTICAL_SPACING;
        layout.numColumns = COLUMNS;
        composite.setLayout(layout);

        return composite;
    }

    /**
     * This method creates a "External locations" {@link Group} containing {@link Text} fields for the
     * "tools directory" and "Java executable".
     * 
     * @param parent The {@link Composite} in which the group is to be created.
     */
    private void createExternalLocations(final Composite parent)
    {
        Font font = getFont();

        // External Locations group.
        Group externalLocationsGroup = new Group(parent, SWT.LEFT);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = COLUMNS;
        externalLocationsGroup.setLayoutData(layoutData);
        externalLocationsGroup.setFont(font);
        externalLocationsGroup.setText("External locations");
        Layout layout = new GridLayout(COLUMNS, false);
        externalLocationsGroup.setLayout(layout);

        // Descriptive text.
        Label externalLocationsDescription = new Label(externalLocationsGroup, SWT.WRAP);
        externalLocationsDescription.setFont(font);
        externalLocationsDescription
            .setText("The installations for the tools used by Sanity4J are expected to be found within an externally located directory.  The directory defined below is substituted as a variable into the command defined by the ${sanity4j.tool.<tool>.<version>.command} property as the value of the variable ${products}.");
        GridData externalLocationDescriptionData = new GridData(GridData.FILL_HORIZONTAL);
        externalLocationDescriptionData.horizontalSpan = COLUMNS;
        externalLocationDescriptionData.widthHint = BIG_WIDTH_HINT;
        externalLocationsDescription.setLayoutData(externalLocationDescriptionData);

        // Tools directory.
        Label toolsLabel = new Label(externalLocationsGroup, SWT.NULL);
        toolsLabel.setFont(font);
        toolsLabel.setText("&Tools directory: ");

        toolsDirectoryText = new Text(externalLocationsGroup, SWT.BORDER);
        toolsDirectoryText.setFont(font);
        GridData productsDirectoryData = new GridData(GridData.FILL_HORIZONTAL);
        toolsDirectoryText.setLayoutData(productsDirectoryData);
        Button productsBrowseButton = new Button(externalLocationsGroup, SWT.PUSH);
        productsBrowseButton.setText("&Browse");

        SelectionListener productsButtonListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent event)
            {
                toolsDirectoryText.setText(browseDirectory(toolsDirectoryText.getText()));
            }
        };

        productsBrowseButton.addSelectionListener(productsButtonListener);

        // Java 1.8 exectable.
        Label javaLocationLabel = new Label(externalLocationsGroup, SWT.NULL);
        javaLocationLabel.setFont(font);
        javaLocationLabel.setText("&Java executable: ");

        javaExecutableText = new Text(externalLocationsGroup, SWT.BORDER);
        javaExecutableText.setFont(font);
        GridData javaExecutableData = new GridData(GridData.FILL_HORIZONTAL);
        javaExecutableText.setLayoutData(javaExecutableData);
        String javaExecutable = getJavaExecutable();
        javaExecutableText.setText(javaExecutable);

        Button javaExecutableBrowseButton = new Button(externalLocationsGroup, SWT.PUSH);
        javaExecutableBrowseButton.setFont(font);
        javaExecutableBrowseButton.setText("B&rowse");

        SelectionListener javaExectuableButtonListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent event)
            {
                javaExecutableText.setText(browseFile("Java Executable", "*.exe", javaExecutableText.getText()));
            }
        };

        javaExecutableBrowseButton.addSelectionListener(javaExectuableButtonListener);

        // Print Diagnostics First?
        Label diagnosticFirstLabel = new Label(externalLocationsGroup, SWT.NULL);
        diagnosticFirstLabel.setFont(font);
        diagnosticFirstLabel.setText("&Print diagnostics first?: ");

        diagnosticFirstCheck = new Button(externalLocationsGroup, SWT.CHECK);
        diagnosticFirstCheck.setFont(font);
        final IPreferenceStore store = getPreferenceStore();
        diagnosticFirstCheck.setSelection(store.getBoolean(PreferenceConstants.DIAGNOSTICS_FIRST));
    }

    /**
     * This method creates a "Sanity4J properties" file {@link Text} field.
     * 
     * @param parent The {@link Composite} in which the group is to be created.
     */
    private void createSanity4jProperties(final Composite parent)
    {
        Font font = getFont();

        Label label = new Label(parent, SWT.NULL);
        label.setFont(font);
        label.setText("&Sanity4j properties file: "); // TODO: I18N

        sanity4jPropertiesText = new Text(parent, SWT.BORDER);
        sanity4jPropertiesText.setFont(font);
        GridData sanity4jPropertiesData = new GridData(GridData.FILL_HORIZONTAL);
        sanity4jPropertiesText.setLayoutData(sanity4jPropertiesData);

        // Browse Button.
        Button sanity4jPropertiesBrowseButton = new Button(parent, SWT.BUTTON1);
        sanity4jPropertiesBrowseButton.setFont(font);
        sanity4jPropertiesBrowseButton.setText("Bro&wse");

        SelectionListener buttonListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent event)
            {
                sanity4jPropertiesText.setText(browseFile("Properties Files", "*.properties",
                                                          sanity4jPropertiesText.getText()));
            }
        };

        sanity4jPropertiesBrowseButton.addSelectionListener(buttonListener);
    }

    /**
     * This method creates a "Tools" {@link Group} containing {@link Text} fields for the "Tool Configuration" and
     * "Tool Configuration Class Path".
     * 
     * @param parent The {@link Composite} in which the group is to be created.
     */
    private void createToolsGroup(final Composite parent)
    {
        Font font = getFont();

        // Tools group.
        final Group toolsGroup = new Group(parent, SWT.NULL);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = COLUMNS;
        toolsGroup.setLayoutData(layoutData);
        toolsGroup.setFont(font);
        toolsGroup.setText("Tool configuration");
        Layout layout = new GridLayout(2, false);
        toolsGroup.setLayout(layout);

        // Config Descriptive Text.
        Label toolConfigDescription = new Label(toolsGroup, SWT.WRAP);
        toolConfigDescription.setFont(font);
        toolConfigDescription
            .setText("Tool \"configurations\" are substituted as variables into the command defined by the ${sanity4j.tool.<tool>.<version>.command} property.  They are substituted as the value of the variable ${sanity4j.tool.<tool>.<version>.config}");
        GridData toolConfigDescriptionData = new GridData(GridData.FILL_HORIZONTAL);
        toolConfigDescriptionData.horizontalSpan = 2;
        toolConfigDescriptionData.widthHint = BIG_WIDTH_HINT;
        toolConfigDescription.setLayoutData(toolConfigDescriptionData);
        
        // Tool drop-down.
        Label toolLabel = new Label(toolsGroup, SWT.NULL);
        toolLabel.setFont(font);
        toolLabel.setText("&Tool: ");

        Combo toolCombo = new Combo(toolsGroup, SWT.DROP_DOWN);
        toolCombo.setFont(font);
        String[] tools = getTools();
        lastTool = tools[0];
        toolCombo.setItems(tools);
        toolCombo.setText(lastTool);

        // Version drop-down.
        Label versionLabel = new Label(toolsGroup, SWT.NULL);
        versionLabel.setFont(font);
        versionLabel.setText("&Version: ");

        final Combo versionCombo = new Combo(toolsGroup, SWT.DROP_DOWN);
        versionCombo.setFont(font);
        String[] versions = getToolVersions(tools[0]);
        lastVersion = versions[0];
        versionCombo.setItems(versions);
        versionCombo.setText(lastVersion);

        // Tool config configuration
        configLabel = new Label(toolsGroup, SWT.NULL);
        configLabel.setFont(font);
        configLabel.setText(tools[0] + " &config: ");

        configText = new Text(toolsGroup, SWT.BORDER);
        configText.setFont(font);
        GridData configData = new GridData(GridData.FILL_HORIZONTAL);
        configData.widthHint = SMALL_WIDTH_HINT;
        configText.setLayoutData(configData);
        
        // Tool configuration class path.
        configClasspathLabel = new Label(toolsGroup, SWT.NULL);
        configClasspathLabel.setFont(font);
        configClasspathLabel.setText(tools[0] + " co&nfig classpath: ");

        configClasspathText = new Text(toolsGroup, SWT.BORDER);
        configClasspathText.setFont(font);
        GridData configClasspathData = new GridData(GridData.FILL_HORIZONTAL);
        configClasspathData.widthHint = SMALL_WIDTH_HINT;
        configClasspathText.setLayoutData(configClasspathData);

        final Button loadButton = new Button(toolsGroup, SWT.NULL);
        loadButton.setFont(font);
        loadButton.setText("&Load from properties");
        GridData loadData = new GridData(GridData.END);
        loadButton.setLayoutData(loadData);

        SelectionListener toolComboListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent event)
            {
                toolConfig.put(lastTool + SEPARATOR + lastVersion, configText.getText());
                toolConfigClasspath.put(lastTool + SEPARATOR + lastVersion, configClasspathText.getText());

                Combo combo = (Combo) event.widget;

                if (combo.getSelectionIndex() >= 0)
                {
                    lastTool = combo.getItem(combo.getSelectionIndex());

                    String[] versions = getToolVersions(lastTool);
                    versionCombo.setItems(versions);
                    versionCombo.select(0);
                    lastVersion = versions[0];

                    setCurrentTool();
                }
            }
        };

        toolCombo.addSelectionListener(toolComboListener);

        SelectionListener versionComboListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent event)
            {
                toolConfig.put(lastTool + SEPARATOR + lastVersion, configText.getText());
                toolConfigClasspath.put(lastTool + SEPARATOR + lastVersion, configClasspathText.getText());

                Combo combo = (Combo) event.widget;

                if (combo.getSelectionIndex() >= 0)
                {
                    lastVersion = combo.getItem(combo.getSelectionIndex());
                    setCurrentTool();
                }
            }
        };

        versionCombo.addSelectionListener(versionComboListener);

        SelectionListener loadListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent event)
            {
                IPreferenceStore store = Activator.getDefault().getPreferenceStore();
                loadToolConfig(false, store);
                setCurrentTool();
            }
        };

        loadButton.addSelectionListener(loadListener);
    }

    // ****************************************************************
    // PreferencePage Interface
    // ****************************************************************

    /**
     * Init.
     * @param workbench the workbench.
     */
    @Override
    public void init(final IWorkbench workbench)
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    /**
     * Create Contents.
     * @param parent the parent composite.
     * @return the control.
     */
    @Override
    protected Control createContents(final Composite parent)
    {
        IPreferenceStore store = getPreferenceStore();

        Composite composite = createComposite(parent);

        createExternalLocations(composite);
        createSanity4jProperties(composite);
        createToolsGroup(composite);

        loadFromStore(true, store);

        return composite;
    }

    /**
     * Perform Defaults.
     */
    @Override
    protected void performDefaults()
    {
        IPreferenceStore store = getPreferenceStore();

        store.setValue(PreferenceConstants.PRODUCTS_DIRECTORY, "tools");
        store.setValue(PreferenceConstants.JAVA_RUNTIME, getJavaExecutable());
        store.setValue(PreferenceConstants.SANITY4J_PROPERTIES, "");
        store.setValue(PreferenceConstants.DIAGNOSTICS_FIRST, false);
        
        toolConfig.clear();
        toolConfigClasspath.clear();

        String[] tools = getTools();

        for (String tool : tools)
        {
            String[] versions = getToolVersions(tool);

            for (String version : versions)
            {
                String key = tool + SEPARATOR + version;

                store.setValue(PreferenceConstants.TOOL_CONFIG_PREFIX + key, "");
                store.setValue(PreferenceConstants.TOOL_CONFIG_CLASSPATH_PREFIX + key, "");
            }
        }

        loadFromStore(true, store);

        super.performDefaults();
    }

    /**
     * Perform OK.
     * @return false if validation fails, otherwise true.
     */
    @Override
    public boolean performOk()
    {
        IPreferenceStore store = getPreferenceStore();

        if (!validate())
        {
            return false;
        }

        store.setValue(PreferenceConstants.PRODUCTS_DIRECTORY, toolsDirectoryText.getText());
        store.setValue(PreferenceConstants.JAVA_RUNTIME, javaExecutableText.getText());
        store.setValue(PreferenceConstants.SANITY4J_PROPERTIES, sanity4jPropertiesText.getText());
        store.setValue(PreferenceConstants.DIAGNOSTICS_FIRST, diagnosticFirstCheck.getSelection());
        
        toolConfig.put(lastTool + SEPARATOR + lastVersion, configText.getText());
        toolConfigClasspath.put(lastTool + SEPARATOR + lastVersion, configClasspathText.getText());

        for (Map.Entry<String, String> entry : toolConfig.entrySet())
        {
            store.setValue(PreferenceConstants.TOOL_CONFIG_PREFIX + entry.getKey(), entry.getValue());
        }
        
        for (Map.Entry<String, String> entry : toolConfigClasspath.entrySet())
        {
            store.setValue(PreferenceConstants.TOOL_CONFIG_CLASSPATH_PREFIX + entry.getKey(), entry.getValue());
        }

        return true;
    }
}
