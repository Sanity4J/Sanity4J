package net.sf.sanity4j.workflow; 

import java.util.ArrayList;
import java.util.List;

/** 
 * WorkUnitGroup provides a way to group {@link WorkUnit}s. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class WorkUnitGroup implements WorkUnit
{
    /** A short description of this WorkUnitGroup. */
    private final String description;
    
    /** The WorkUnits contained in this group. */
    private final List<WorkUnit> work = new ArrayList<WorkUnit>();

    /**
     * Creates a WorkUnitGroup where parallel work unit execution is disabled.
     * @param description a short textual description of the WorkUnitGroup's purpose.
     */
    public WorkUnitGroup(final String description)
    {
        this(false, description);
    }
    
    /**
     * Creates a WorkUnitGroup where parallel work unit execution is allowed.
     * 
     * @param allowConcurrent true if WorkUnits in this group can be executed in parallel, false if not.
     * @param description a short textual description of the WorkUnitGroup's purpose.
     */
    public WorkUnitGroup(final boolean allowConcurrent, final String description)
    {
        // TODO: this.allowConcurrent = allowConcurrent;
        this.description = description;
    }
    
    /** {@inheritDoc} */
    public String getDescription()
    {
        return description;
    }

    /**
     * Executes all the WorkUnits in this group.
     */
    public void run()
    {
        // TODO: Implement threading
        
        for (WorkUnit workUnit : work)
        {
            workUnit.run();
        }
    }

    /**
     * Adds a unit of work to this work group.
     * @param workUnit the work unit to add.
     */
    public void add(final WorkUnit workUnit) 
    {
        work.add(workUnit);
    }
    
    /**
     * Sets the maximum number of threads which are allowed globally for WorkUnitGroups.
     * This effectively controls how many WorkUnits can be run in parallel.
     * 
     * @param maxThreads the maximum number of threads allowed.
     */
    public static final void setMaxThreads(final int maxThreads)
    {
        // TODO: set up thread pool.
    }    
}
