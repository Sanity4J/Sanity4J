package net.sf.sanity4j.workflow; 

/** 
 * WorkUnit - a unit of work.
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public interface WorkUnit extends Runnable
{
    /**
     * @return the description of this WorkUnit
     */
    String getDescription();
}
