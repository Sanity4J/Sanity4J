package com.github.sanity4j.ui; 

import javax.swing.JLabel;

/**
 * A Label for an input field.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class FieldLabel extends JLabel
{
    /**
     * Creates a FieldLabel.
     * 
     * @param text the field label text.
     * @param mandatory whether the associated field is mandatory. 
     */
    public FieldLabel(final String text, final boolean mandatory)
    {
        super(mandatory ? ("<html><em style='color: red;'>* </em>" + text + ": </html>")
                         : (text + ": "));
    }
}
