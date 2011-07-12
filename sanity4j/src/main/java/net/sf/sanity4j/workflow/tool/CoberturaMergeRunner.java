package net.sf.sanity4j.workflow.tool; 

import net.sf.sanity4j.util.ExternalProcessRunner;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.Tool;

/**
 * CoberturaMergeRunner - a work unit that merges Cobertura data files.
 *
 * @author Darian Bridge
 * @since Sanity4J 1.0.4
 */
public class CoberturaMergeRunner extends AbstractToolRunner
{
    /**
     * Creates a CoberturaReportRunner.
     */
    public CoberturaMergeRunner()
    {
        super(Tool.COBERTURA_MERGE);
    }

    /**
     * Produces the Merged Cobertura data file.
     * @param commandLine the Merge Cobertura data file command line.
     */
    public void runTool(final String commandLine)
    {
        int result = ExternalProcessRunner.runProcess(commandLine, System.out, System.err);

        if (result != 0)
        {
            throw new QAException("Cobertura Merge returned error code " + result);
        }
    }

    /**
     * @return the file path where the tool should place it's output.
     */
    protected String getToolResultFile()
    {
        return null;
    }

    /**
     * @return the description of this WorkUnit.
     */
    public String getDescription()
    {
        return "Running Cobertura Data File Merge";
    }
}
