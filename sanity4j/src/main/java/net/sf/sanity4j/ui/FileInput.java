package net.sf.sanity4j.ui; 

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.sanity4j.util.StringUtil;


/** 
 * FileInput provides a UI to pick a file/directory. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class FileInput extends JPanel implements ActionListener
{
    /** The text field which holds the file name. */
    private final JTextField fileName = new JTextField(40);
    
    /** Controls the file selection mode (true = directories only, false = files only). */
    private final boolean directory;
    
    /**
     * Creates a FileInput.
     * @param directory true to only allow directories can be selected, false to only allow files can be selected.
     */
    public FileInput(final boolean directory)
    {
        this.directory = directory;
        
        setLayout(new BorderLayout());
        add(fileName, BorderLayout.CENTER);
        
        JButton browseButton = new JButton("Browse");
        add(browseButton, BorderLayout.EAST);
        browseButton.addActionListener(this);
    }
    
    /**
     * ActionListener implementation for the browseButton.
     * @param event the event that occurred.
     */
    public void actionPerformed(final ActionEvent event)
    {
        JFileChooser jfc = new JFileChooser(getFile());
        
        if (directory)
        {
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        else
        {
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            setFile(jfc.getSelectedFile());
        }
    }
    
    /**
     * @return the file name text, which may or may not be a file
     */
    public String getText()
    {
        return fileName.getText();
    }
    
    /**
     * Sets the file name text.
     * @param text the file name text.
     */
    public void setText(final String text)
    {
        fileName.setText(text);
    }
    
    /**
     * Retrieves the selected file. Note that that file may not exist, or be a valid file.
     * @return the selected file, or null if no file has been selected.
     */
    public File getFile()
    {
        if (StringUtil.empty(fileName.getText()))
        {
            return null;
        }
        
        return new File(fileName.getText());
    }
    
    /**
     * Sets the file. 
     * @param file the file to set.
     */
    public void setFile(final File file)
    {
        fileName.setText(file.getPath());
    }
}
