package net.sf.sanity4j.workflow; 

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** 
 * SourceFileCollector collects source files from multiple directory 
 * hierarchies and places them in a single directory hierarchy. This is 
 * necessary, as some tools don't allow multiple source directories 
 * to be specified.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class SourceFileCollector extends AbstractFileCollector
{
    /** The file extensions to include in the collection.  */
    private static final Set<String> FILE_EXTS = new HashSet<String>(Arrays.asList(new String[]{"java", "jsp"}));
    
    /**
     * Creates a SourceFileCollector.
     * @param config the current QA run's configuration.
     */
    public SourceFileCollector(final QAConfig config)
    {
        super(FILE_EXTS, config.getSourceDirs(), config.getCombinedSourceDir());
    }
    
    /**
     * Returns the item type being copied. 
     * @return "source" 
     */
    protected String getItemType()
    {
        return "source";
    }
    
    /** {@inheritDoc} */
    public String getDescription()
    {
        return "Collecting source files";
    }
    
    /** @return true, at least one source file must exist. */
    @Override
    protected boolean isMandatory()
    {
        return true;
    }
}
