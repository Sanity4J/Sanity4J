package net.sf.sanity4j.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.workflow.QAConfig;
import net.sf.sanity4j.workflow.QAProcessor;

/**
 * QaConfigFrame is a Java Swing Frame used to modify the configuration properties used by the {@link QaApp} Swing
 * Application.
 * 
 * @author Brian Kavanagh
 * @since September 2011
 */
public class QaConfigFrame extends JFrame
{
    /** Default serialization identifier. */
    private static final long serialVersionUID = 1L;
    
    /** The column count. */
    private static final int COLUMN_COUNT = 3;
    /** The inset. */
    private static final int INSET = 5;
    /** Six. */
    private static final int SIX = 6;
    /** One hundred. */
    private static final int ONE_HUNDRED = 100;

    /**
     * A {@link TableModel} which models three columns derived from a <b>Properties</b> object.
     * <ol>
     * <li><b>name</b> - The name of the QaConfig property.</li>
     * <li><b>value</b> - The value of the QaConfig property.</li>
     * <li><b>derivedValue</b> - The value of the QaConfig property after a number string substitutions have been made.</li>
     * </ol>
     */
    private static class QaPropertyTableModel extends AbstractTableModel
    {
        /** Default serialization identifier. */
        private static final long serialVersionUID = 1L;

        /** The values modelled by this object. */
        private String[][] values;

        /**
         * Creates a <b>QaPropertyTableModel</b> for the given <b>Properties</b> object.
         * 
         * @param config The properties to be modelled.
         */
        public QaPropertyTableModel(final QAConfig config)
        {
            reset(config);
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex)
        {
            if (columnIndex == 1)
            {
                return true;
            }
            else
            {
                return super.isCellEditable(rowIndex, columnIndex);
            }
        }

        /** {@inheritDoc} */
        @Override
        public String getColumnName(final int column)
        {
            switch (column)
            {
                case 0:
                    return "Property";
                case 1:
                    return "Value";
                case 2:
                    return "Derived Value";
                default:
                    return "";
            }
        }

        /** {@inheritDoc} */
        public int getRowCount()
        {
            return values.length;
        }

        /** {@inheritDoc} */
        public int getColumnCount()
        {
            return COLUMN_COUNT;
        }

        /** {@inheritDoc} */
        public Object getValueAt(final int rowIndex, final int columnIndex)
        {
            return values[rowIndex][columnIndex];
        }

        /** {@inheritDoc} */
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
        {
            if ((rowIndex < values.length) && (columnIndex < COLUMN_COUNT))
            {
                if (aValue == null)
                {
                    values[rowIndex][columnIndex] = null;
                }
                else
                {
                    values[rowIndex][columnIndex] = aValue.toString();
                }
            }
        }

        /**
         * This method saves the contents of the current model to a {@link QAConfig} object.
         * 
         * @param config The {@link QAconfig} object to which the contents of the current {@link QaPropertyTableModel}
         *            are to be saved.
         */
        public void save(final QAConfig config)
        {
            for (int i = 0; i < values.length; i++)
            {
                String key = values[i][0];
                String value = values[i][1];

                config.setToolProperty(key, value);
            }
        }

        /**
         * This method resets the contents of the current {@link QaPropertyTableModel} to current values within a
         * {@link QAConfig} object.
         * 
         * @param config The {@link QAconfig} object to which the contents of the current {@link QaPropertyTableModel}
         *            are to be saved.
         */
        public void reset(final QAConfig config)
        {
            Set<String> keys = new TreeSet<String>();

            Properties properties = config.getToolProperties();

            for (Object o : properties.keySet())
            {
                keys.add(o.toString());
            }

            this.values = new String[keys.size()][COLUMN_COUNT];

            int index = 0;

            for (String key : keys)
            {
                String defaultValue = properties.getProperty(key);
                String value = QaUtil.replaceTokens(defaultValue, config.asParameterMap());

                this.values[index][0] = key;
                this.values[index][1] = defaultValue;
                this.values[index][2] = value;
                index++;
            }
        }
    }

    // ****************************************************************
    // MEMBER VARIABLES
    // ****************************************************************

    /** The processor that will be running the QA. */
    private final QAProcessor processor;

    /** The set of tools which can be configured. */
    private final JComboBox toolCombo = new JComboBox();

    /** The last tool selected within the "tools" drop-down list. */
    private String lastTool;

    /** The versions of the currently selected tool. */
    private final JComboBox versionCombo = new JComboBox();

    /** The last tool version selected within the "versions" drop-down list. */
    private String lastVersion;

    /** The label for the tool configuration. */
    private final JLabel configLabel = new JLabel();
    
    /** The label for the tool configuration. */
    private final JLabel configClasspathLabel = new JLabel();
    
    /** A text field used to enter the tool configuration. */
    private final JTextField configText = new JTextField(20);

    /** A text field used to enter the tool configuration class path. */
    private final JTextField configClasspathText = new JTextField();

    /** A table containing the QaConfig properties. */
    private final JTable table = new JTable();

    /** Button to save configuration changes. */
    private final JButton okButton = new JButton();

    /** Button to cancel configuration changes. */
    private final JButton cancelButton = new JButton();

    /**
     * A Map from a "tool" to the value stored within the Tool Configuration {@link JTextField}.
     */
    private final Map<String, String> toolConfig = new HashMap<String, String>();

    /**
     * A Map from a "tool" to the value stored within the Tool Configuration class path {@link JTextField}.
     */
    private final Map<String, String> toolConfigClasspath = new HashMap<String, String>();

    // ****************************************************************
    // PRIVATE METHODS
    // ****************************************************************

    /**
     * This method initializes the {@link #toolConfig} and {@link #toolConfigClasspath} variables from the
     * {@link QAConfig}.
     * 
     * @param qaConfig the config.
     */
    private void loadToolConfig(final QAConfig qaConfig)
    {
        String[] tools = qaConfig.getToolsToRun();

        for (String tool : tools)
        {
            String[] versions = qaConfig.getToolVersions(tool);

            for (String version : versions)
            {
                String key = tool + "." + version;

                String config = qaConfig.getToolConfig(key, null);

                if (config == null)
                {
                    config = "";
                }

                toolConfig.put(key, config);

                String configClasspath = qaConfig.getToolConfigClasspath(key, null);

                if (configClasspath == null)
                {
                    configClasspath = "";
                }

                toolConfigClasspath.put(key, configClasspath);
            }
        }
    }

    /**
     * This method saves the data input into the {@link QaConfigFrame} to the {@link QAConfig} object.
     */
    private void save()
    {
        QAConfig config = processor.getConfig();
        ((QaPropertyTableModel) table.getModel()).save(config);

        toolConfig.put(lastTool + "." + lastVersion, configText.getText());
        toolConfigClasspath.put(lastTool + "." + lastVersion, configClasspathText.getText());
        
        String[] tools = config.getToolsToRun();

        for (String tool : tools)
        {
            String[] versions = config.getToolVersions(tool);

            for (String version : versions)
            {
                String key = tool + "." + version;

                String toolConfigString = toolConfig.get(key);
                String toolConfigClasspathString = toolConfigClasspath.get(key);

                config.setToolConfig(tool, null, toolConfigString, toolConfigClasspathString);
                config.setToolConfig(tool, version, toolConfigString, toolConfigClasspathString);
            }
        }
    }

    /**
     * This method resets the data input within the {@link QaConfigFrame} to the values within {@link QAConfig} object.
     */
    private void reset()
    {
        QAConfig config = processor.getConfig();
        ((QaPropertyTableModel) table.getModel()).reset(config);
        loadToolConfig(config);
    }

    /**
     * Adds a component to the content pane.
     * 
     * @param component the component to add.
     * @param gridx the x grid coordinate.
     * @param gridy the y grid coordinate.
     * @param gridwidth the number of horizontal grid units to occupy.
     * @param gridheight the number of vertical grid units to occupy.
     * @param anchor the position of the component within the cell.
     * @param fill the fill mode when the component is smaller than the cell.
     */
    private void add(final JComponent component, final int gridx, final int gridy, final int gridwidth,
                     final int gridheight, final int anchor, final int fill)
    {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.insets = new Insets(1, 1, 1, 1);

        if (fill == GridBagConstraints.HORIZONTAL || fill == GridBagConstraints.BOTH)
        {
            constraints.weightx = 1.0;
        }

        if (fill == GridBagConstraints.VERTICAL || fill == GridBagConstraints.BOTH)
        {
            constraints.weighty = 1.0;
        }

        getContentPane().add(component);
        ((GridBagLayout) getContentPane().getLayout()).setConstraints(component, constraints);
    }

    /**
     * Adds Swing components to the {@link QaConfigFrame} ContentPane.
     */
    private void layoutInterface()
    {
        // ************************************************************
        // Configure components
        // ************************************************************

        QAConfig config = processor.getConfig();
        
        String[] tools = config.getToolsToRun();
        toolCombo.setModel(new DefaultComboBoxModel(tools));
        lastTool = tools[0];
        configLabel.setText(lastTool + " config:");
        configClasspathLabel.setText(lastTool + " config classpath:");

        String[] versions = config.getToolVersions(tools[0]);
        versionCombo.setModel(new DefaultComboBoxModel(versions));
        lastVersion = versions[0];
        
        ItemListener toolComboListener = new ItemListener()
        {
            public void itemStateChanged(final ItemEvent event)
            {
                toolConfig.put(lastTool + "." + lastVersion, configText.getText());
                toolConfigClasspath.put(lastTool + "." + lastVersion, configClasspathText.getText());

                JComboBox combo = (JComboBox) event.getSource();
                String prefix = "";

                if (combo.getSelectedIndex() >= 0)
                {
                    lastTool = (String) combo.getItemAt(combo.getSelectedIndex());

                    String[] versions = processor.getConfig().getToolVersions(lastTool);
                    versionCombo.setModel(new DefaultComboBoxModel(versions));
                    versionCombo.setSelectedIndex(0);
                    
                    lastVersion = versions[0];

                    String config = toolConfig.get(lastTool + "." + lastVersion);
                    String configClasspath = toolConfigClasspath.get(lastTool + "." + lastVersion);

                    configText.setText(config == null ? "" : config);
                    configClasspathText.setText(configClasspath == null ? "" : configClasspath);

                    prefix = lastTool + " ";
                }

                configLabel.setText(prefix + "config:");
                configClasspathLabel.setText(prefix + "config classpath:");
                QaConfigFrame.this.pack();
            }
        };

        toolCombo.addItemListener(toolComboListener);

        ItemListener versionComboListener = new ItemListener()
        {
            public void itemStateChanged(final ItemEvent event)
            {
                toolConfig.put(lastTool + "." + lastVersion, configText.getText());
                toolConfigClasspath.put(lastTool + "." + lastVersion, configClasspathText.getText());

                JComboBox combo = (JComboBox) event.getSource();

                if (combo.getSelectedIndex() >= 0)
                {
                    lastVersion = (String) combo.getItemAt(combo.getSelectedIndex());

                    String config = toolConfig.get(lastTool + "." + lastVersion);
                    String configClasspath = toolConfigClasspath.get(lastTool + "." + lastVersion);

                    configText.setText(config == null ? "" : config);
                    configClasspathText.setText(configClasspath == null ? "" : configClasspath);
                }

                QaConfigFrame.this.pack();
            }
        };

        versionCombo.addItemListener(versionComboListener);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        TableModel model = new QaPropertyTableModel(processor.getConfig());
        table.setModel(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(((int) screenSize.getWidth()) / 2, ONE_HUNDRED));

        Action okAction = new AbstractAction()
        {
            /** Default serialization identifier. */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(final ActionEvent event)
            {
                table.editingStopped(new ChangeEvent(table));
                save();
                QaConfigFrame.this.setVisible(false);
            }
        };
        okButton.setAction(okAction);
        okButton.setText("OK");

        Action cancelAction = new AbstractAction()
        {
            /** Default serialization identifier. */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(final ActionEvent event)
            {
                reset();
                QaConfigFrame.this.setVisible(false);
            }
        };

        cancelButton.setAction(cancelAction);
        cancelButton.setText("Cancel");

        // ************************************************************
        // Layout components within the GridBagLayout
        // ************************************************************

        int gridy = 0;

        add(new JLabel("Tool: "), 0, gridy, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(toolCombo, 1, gridy++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(new JLabel("Version: "), 0, gridy, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(versionCombo, 1, gridy++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(configLabel, 0, gridy, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(configText, 1, gridy++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        add(configClasspathLabel, 0, gridy, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(configClasspathText, 1, gridy++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);

        add(new JLabel("Sanity4J Configuration: "), 0, gridy++, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);

        for (int i = 0; i < table.getColumnCount(); i++)
        {
            table.getColumnModel().getColumn(i).setPreferredWidth(((int) screenSize.getWidth()) / SIX);
        }

        add(scrollPane, 0, gridy++, 2, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH);

        add(okButton, 0, gridy, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(cancelButton, 1, gridy, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    }

    // ************************************************************
    // Layout components within the GridBagLayout
    // ************************************************************

    /**
     * @param processor the processor.
     */
    QaConfigFrame(final QAProcessor processor)
    {
        this.processor = processor;

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(INSET, INSET, INSET, INSET));
        contentPane.setLayout(new GridBagLayout());

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(final WindowEvent event)
            {
                reset();
            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        layoutInterface();
        reset();
        pack();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
    }
}
