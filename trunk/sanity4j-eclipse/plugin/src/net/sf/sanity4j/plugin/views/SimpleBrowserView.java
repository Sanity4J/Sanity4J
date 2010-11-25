package net.sf.sanity4j.plugin.views;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * A simple view whch opens the url in a browser within Eclipse.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class SimpleBrowserView extends ViewPart
{
    /** The browser. */
    private Browser browser;

    /**
     * Creates the SWT controls for the browser view.
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl (org.eclipse.swt.widgets.Composite)
     * @param parent the parent control.
     */
    public void createPartControl(final Composite parent)
    {
        browser = new Browser(parent, 0);
    }

    /** 
     * Opens the given URL in the browser.
     * @param url the URL to open
     */ 
    public void navigateTo(final String url)
    {
        browser.setUrl(url);
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        // NOP
    }
}
