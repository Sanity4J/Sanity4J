package net.sf.sanity4j.ui; 

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import net.sf.sanity4j.util.QaLogger;
import net.sf.sanity4j.util.QaUtil;
import net.sf.sanity4j.workflow.QAProcessor;


/** 
 * QaApp is the main entry point to the Swing UI interface to Sanity4J.
 * 
 * TODO: Multiple project source etc. directories, includes, excludes
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public final class QaApp extends JFrame
{
    /** The file name that configuration data is saved in, in the user's home directory. */
    private static final String PROPERTIES_FILE_NAME = "sanity4j-ui.properties";
    
    /** File input field for selecting the tools directory. */
    private final FileInput productsDir = new FileInput(true);
    /** File input field for selecting the java runtime executable. */
    private final FileInput javaRuntime = new FileInput(false);
    /** File input field for selecting the project's class directory. */
    private final FileInput classDir = new FileInput(true);
    /** File input field for selecting the project's source directory. */
    private final FileInput srcDir = new FileInput(true);
    /** File input field for selecting the project's library directory. */
    private final FileInput libDir = new FileInput(true);
    /** File input field for selecting the project's coverage data file. */
    private final FileInput coverageFile = new FileInput(false);
    /** File input field for selecting the report output directory. */
    private final FileInput reportDir = new FileInput(true);
    /** File input field for selecting the summary CSV export. */
    private final FileInput summaryOutputFile = new FileInput(false);
    /** Controls whether to open the report on successful completion. */
    private final JCheckBox openReportOnCompletion = new JCheckBox("Open report on completion");
    /** The button which starts the analysis. */
    private final JButton runButton = new JButton("Analyse");
    /** Displays console output which is generated during the analysis process. */
    private final JTextArea console = new JTextArea(10, 60);

    /**
     * Creates a QaApp.
     */
    public QaApp()
    {
        super("Sanity4J UI " + QAProcessor.QA_VERSION);
        
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new GridBagLayout());
        setContentPane(contentPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(final WindowEvent event)
            {
                writeConfig();
            }
        });
        
        runButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent event)
            {
                runQA();
            }
        });
        
        console.setEditable(false);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        layoutInterface();
        pack();        
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2); 
        
        readConfig();
    }
    
    /**
     * Adds the various components to the app interace.
     */
    private void layoutInterface()
    {
        int y = 0;

        add(new JLabel("Tool configuration"), 0, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        
        add(new FieldLabel("Directory containing analysis tools", true), 0, y, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE); 
        add(productsDir, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL); 

        add(new FieldLabel("Path to java executable", true), 0, y, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE); 
        add(javaRuntime, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        
        add(new JLabel(" "), 0, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(new JLabel("Project settings"), 0, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        
        add(new FieldLabel("Source directory (or parent)", true), 0, y, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE); 
        add(srcDir, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        
        add(new FieldLabel("Class directory (or parent)", true), 0, y, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE); 
        add(classDir, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        
        add(new FieldLabel("Library directory (or parent)", false), 0, y, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE); 
        add(libDir, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        
        add(new FieldLabel("Test coverage data file", false), 0, y, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE); 
        add(coverageFile, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        
        add(new JLabel(" "), 0, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(new JLabel("Output options"), 0, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        
        add(new FieldLabel("Report output directory", true), 0, y, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE); 
        add(reportDir, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        
        add(new FieldLabel("Summary data CSV export file", false), 0, y, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE); 
        add(summaryOutputFile, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        
        add(openReportOnCompletion, 1, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);

        add(new JLabel(" "), 0, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(runButton, 0, y++, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);        
        
        add(new JLabel("Console"), 0, y++, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
        add(new JScrollPane(console), 0, y++, 2, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH);
    }
    
    /**
     * Validates the input which has been entered, returning false on error.
     * @return true if the input is valid, false otherwise.
     */
    private boolean validateInput()
    {
        if (productsDir.getFile() == null || !productsDir.getFile().exists())
        {
            JOptionPane.showMessageDialog(this, "The Products directory must be specified");
            return false;
        }
        
        if (javaRuntime.getFile() == null || !javaRuntime.getFile().exists())
        {
            JOptionPane.showMessageDialog(this, "The java runtime must be specified");
            return false;
        }

        if (srcDir.getFile() == null || !srcDir.getFile().exists())
        {
            JOptionPane.showMessageDialog(this, "The source directory must be specified");
            return false;
        }
        
        if (classDir.getFile() == null || !classDir.getFile().exists())
        {
            JOptionPane.showMessageDialog(this, "The class directory must be specified");
            return false;
        }

        if (libDir.getFile() != null && !libDir.getFile().exists())
        {
            JOptionPane.showMessageDialog(this, "The library directory is invalid (file not found)");
            return false;
        }

        if (coverageFile.getFile() != null && !coverageFile.getFile().exists())
        {
            JOptionPane.showMessageDialog(this, "The coverage data file is invalid (file not found)");
            return false;
        }
        
        return true;
    }
    
    /**
     * Runs the QA tool with the configuration entered through the UI.
     */
    private void runQA()
    {
        if (!validateInput())
        {
            return;
        }
        
        runButton.setEnabled(false);
        console.setText("");
        
        final QAProcessor processor = new QAProcessor();
        processor.getConfig().addSourcePath(srcDir.getFile().getPath());
        processor.getConfig().addClassPath(classDir.getFile().getPath());
        processor.getConfig().setProductsDir(productsDir.getFile().getPath());
        
        if (coverageFile.getFile() != null)
        {
            processor.getConfig().setCoverageDataFile(coverageFile.getFile().getPath());
        }
        
        processor.getConfig().setReportDir(reportDir.getFile().getPath());
        processor.getConfig().setJavaRuntime(javaRuntime.getFile().getPath());
        
        if (summaryOutputFile.getFile() != null)
        {
            processor.getConfig().setSummaryDataFile(summaryOutputFile.getFile().getPath());
        }
        
        new Thread()
        {
            public void run()
            {
                try
                {
                    processor.run();
                    
                    if (openReportOnCompletion.isSelected())
                    {
                        File index = new File(reportDir.getFile(), "index.html");
                        
                        // Use of reflections here is required as we still need to compile under java 1.5.
                        //Desktop.getDesktop().open(index);
                        try
                        {
                            Class<?> clazz = Class.forName("java.awt.Desktop");
                            Object instance = clazz.getMethod("getDesktop").invoke(null);
                            clazz.getMethod("open", File.class).invoke(instance, index);
                        }
                        catch (ClassNotFoundException e)
                        {
                            // Pre-java 1.6
                            QaLogger.getInstance().error("Open document failed, Requires java 1.6+");
                        }
                        catch (Exception e)
                        {
                            QaLogger.getInstance().error("Failed to open report", e);
                        }
                    }
                }
                catch (Exception e)
                {
                    QaLogger.getInstance().error("QA failed", e);
                }
                finally
                {
                    runButton.setEnabled(true);
                }
            }
        }.start();
    }
    
    /**
     * Adds a component to the content pane.
     * @param component the component to add.
     * @param gridx the x grid co-ordinate.
     * @param gridy the y grid co-ordinate.
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
     * Reads the QaApp configuration file, applying settings as necessary.
     * This is normally called once on application start-up.
     */
    private void readConfig()
    {
        Properties props = new Properties();
        File file = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);
        FileInputStream fis = null;
        
        if (file.exists())
        {
            try
            {
                fis = new FileInputStream(file);
                props.load(fis);
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(this, "Failed to load preferences from " + file);
            }
            finally
            {
                QaUtil.safeClose(fis);
            }
            
            productsDir.setText(props.getProperty("productsDir"));
            javaRuntime.setText(props.getProperty("javaRuntime"));
            classDir.setText(props.getProperty("classDir"));
            srcDir.setText(props.getProperty("srcDir"));
            libDir.setText(props.getProperty("libDir"));
            coverageFile.setText(props.getProperty("coverageFile"));
            reportDir.setText(props.getProperty("reportDir"));
            summaryOutputFile.setText(props.getProperty("summaryOutputFile"));
            openReportOnCompletion.setSelected("true".equalsIgnoreCase(props.getProperty("openReportOnCompletion")));
        }
    }
    
    /**
     * Writes the QaApp configuration file with the current settings.
     * This is normally called once on application shut-down.
     */
    private void writeConfig()
    {
        Properties props = new Properties();
        props.put("productsDir", productsDir.getText());
        props.put("productsDir", productsDir.getText());
        props.put("javaRuntime", javaRuntime.getText());
        props.put("classDir", classDir.getText());
        props.put("srcDir", srcDir.getText());
        props.put("libDir", libDir.getText());
        props.put("coverageFile", coverageFile.getText());
        props.put("reportDir", reportDir.getText());
        props.put("openReportOnCompletion", openReportOnCompletion.isSelected() ? "true" : "false");
        props.put("summaryOutputFile", summaryOutputFile.getText());
        
        File file = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);
        FileOutputStream fos = null;
        
        try
        {
            fos = new FileOutputStream(file);
            props.store(fos, "QaApp properties");
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Failed to save preferences to " + file);
        }
        finally
        {
            QaUtil.safeClose(fos);
        }
    }    

    /**
     * Main entry point to the QA App.
     * @param args ignored - there are no command line arguments.
     */
    public static void main(final String[] args)
    {
        QaApp app = new QaApp();
        QaLogger.setLogger(new QaLoggerJTextAreaImpl(app.console));
        
        app.setVisible(true);
    }
}
