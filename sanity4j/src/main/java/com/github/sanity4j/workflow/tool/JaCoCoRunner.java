package com.github.sanity4j.workflow.tool;

import com.github.sanity4j.util.Tool;

/**
 * JaCoCoRunner - work unit which produces a JaCoCo report.
 * 
 * Note: JaCoCo CLI is coming in a pending pull request - https://github.com/jacoco/jacoco/pull/525 . For now, we use the API directly
* 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.8
 */
public class JaCoCoRunner extends AbstractToolRunner
{
   /**
    * Creates a JaCoCoRunner
    */
   public JaCoCoRunner()
   {
      super(Tool.JACOCO);
   }
   
   /**
    * Future point to run JaCoco. Does nothing for now.
    * @param commandLine the JaCoCo command line.
    */
   @Override
   protected void runTool(final String commandLine)
   {
      // TODO: Does nothing
      // TODO: Hack - pass the config to the reader.
      JaCoCoResultReader.setConfig(getConfig());
   }
   
   /**
    * @return the description of this WorkUnit
    */
   @Override
   public String getDescription()
   {
       return "Running JaCoco report";
   }   
}
