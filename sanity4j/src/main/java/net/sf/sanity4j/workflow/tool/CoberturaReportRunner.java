package net.sf.sanity4j.workflow.tool;

import java.io.File;

import net.sf.sanity4j.util.ExternalProcessRunner;
import net.sf.sanity4j.util.FileUtil;
import net.sf.sanity4j.util.QAException;
import net.sf.sanity4j.util.Tool;

/**
 * CoberturaReportRunner - a work unit that produces a Cobertura report.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class CoberturaReportRunner extends AbstractToolRunner
{
    /**
     * Creates a CoberturaReportRunner.
     */
    public CoberturaReportRunner()
    {
        super(Tool.COBERTURA);
    }

    /**
     * Produces the Cobertura report file.
     * @param commandLine the Cobertura command line.
     */
    public void runTool(final String commandLine)
    {
        FileUtil.createDir(getConfig().getToolResultFile(Tool.COBERTURA).getParent());

        int result = ExternalProcessRunner.runProcess(commandLine, System.out, System.err);

        if (result != 0)
        {
            throw new QAException("Cobertura returned error code " + result);
        }
    }
    
    /**
     * This needs to be overridden as Cobertura doesn't support specifying 
     * a file name for the report, only a destination directory.
     * 
     * @return the file path where cobertura places it's output.
     */
    protected String getToolResultFile()
    {
        return new File(getConfig().getTempDir().getPath(), "coverage.xml").getPath();
    }

    /**
     * @return the description of this WorkUnit
     */
    public String getDescription()
    {
        return "Running CoberturaReport";
    }
}
