Sanity4J Maven plugin user guide

* Intoduction

  Sanity4J has been created in order to simplify running ad-hoc static code 
  analysis over Java projects using a standardised set of rules types and priorities.

  Various tools are used to conduct the analysis, each with their own set of
  requirements. Both bytecode and source code are analysed, to ensure the best
  chance of finding potential problems. Sanity4J presents a single interface 
  to run all the tools and produce a combined report presenting all the findings 
  in a easily accessible manner.

* Integrating Sanity4J into your Maven project.

  To run the Maven Plugin, you will need the following:

	* The Sanity4J jar & POM.
	
	* Maven 2+
	
	* JDK/JRE 1.5.x+
	
	* A directory containing the tools needed by Sanity4J (see parameters below).
	
	* A Maven project, optionally with a Cobertura plugin already included.
	
	* Depending on the size of your project, you may need to increase the memory available to 
	  Maven. This can be set using the MAVEN_OPTS environment variable, e.g. "<<<set MAVEN_OPTS=-Xmx512M>>>" 
	  to use 512MB max heap.

** Parameters

*------------------------+--------------------------------------------------------+-----------+
|| Parameter             || Description                                           || Required |
*------------------------+--------------------------------------------------------+-----------+
| productsDir            | The productsDir must point to a directory containing   | Yes       | 
|                        | all the tools in use. For example, with a default      |           |
|                        | configuration, these could be findbugs-1.3.9,          |           |
|                        | pmd-4.2.5, checkstyle-4.4 and cobertura-1.9.2.         |           |
*------------------------+--------------------------------------------------------+-----------+
| reportDir              | The report output directory. This parameter defaults   | No        |
|                        | to the project's site directory.                       |           | 
*------------------------+--------------------------------------------------------+-----------+
| coverageDataFile       | The location of a coverage data file (e.g.             | No        |
|                        | cobertura.ser for Cobertura) if you want to include    |           |
|                        | unit testing coverage data in your report. If this     |           |
|                        | parameter is omitted, it is assumed that the default   |           |
|                        | Cobertura data file will be used.                      |           |
*------------------------+--------------------------------------------------------+-----------+
| aggregate              | If this Maven project is a multi module parent, then   | No        |
|                        | setting aggregate to true will enable merging of       |           |
|                        | coverage data files.                                   |           |
*------------------------+--------------------------------------------------------+-----------+
| coverageMergeDataFile  | The location of the merged coverage data file.         | No        |
|                        | Only used when aggregate is true. If this parameter is |           |
|                        | omitted, then cobertura-merged.ser will be used.       |           |
*------------------------+--------------------------------------------------------+-----------+
| externalPropertiesPath | The path location of the sanity4j.properties file.     | No        |
|                        | The default is the current directory.                  |           |
*------------------------+--------------------------------------------------------+-----------+
| javaRuntime            | The full path to the java run-time to use.             | No        | 
|                        | If not specified, "java" is used.                      |           |
*------------------------+--------------------------------------------------------+-----------+
| summaryDataFile        | The location of a (persistent) summary data file if    | No        | 
|                        | you want to include trend graphs over time.            |           |
*------------------------+--------------------------------------------------------+-----------+
| includeToolOutput      | If set to "true", the raw tool xml output is also      | No        | 
|                        | copied into the report directory. This is only useful  |           |
|                        | if there is some other part of your build process that |           |
|                        | requires it.                                           |           |
*------------------------+--------------------------------------------------------+-----------+
| sources                | This element accepts nested Paths pointing             | No        | 
|                        | to the location of the target project's source only.   |           |
|                        | If you have multiple source directories contained      |           |
|                        | within a main directory, you can just list the main    |           |
|                        | directory here - the task will search for all source   |           |
|                        | directories. The default is to use the project's "src" |           |
|                        | directory.                                             |           |
*------------------------+--------------------------------------------------------+-----------+
| classes                | This element accepts nested paths pointing to          | No        |
|                        | the location of the target project's built             |           |
|                        | classes/jars only. The default is to use the project's |           |
|                        | "target/classes" and "target/test-classes"             |           |
|                        | directories.                                           |           |
*------------------------+--------------------------------------------------------+-----------+
| libraries              | This element accepts nested Paths / FileSets pointing  | No        |
|                        | to the location of jars/classes that the target        |           |
|                        | project depends on. Don't use e.g. an entire local     |           |
|                        | maven repository; only include direct dependencies.    |           |  
|                        | The default is to use the project's test class path.   |           |
*------------------------+--------------------------------------------------------+-----------+
| useHistory             | Allows retrieval of the QA statistics history CSV file | No        |
|                        | from the current deployed site. For this to work the   |           |
|                        | Distribution Management must be defined within your    |           |
|                        | project's POM file for the location of the Site.       |           | 
|                        | The default is true.                                   |           |
*------------------------+--------------------------------------------------------+-----------+

  The source / class / library paths will accept multiple path entries. If there are multiple 
  source / class directories, it is possible to specify a common parent directory and have the tool 
  find all the nested source/class directories.

  The products.dir must point to a directory containing all the tools in use. For example, with 
  a default configuration, these could be findbugs-1.3.9, pmd-4.2.5, checkstyle-4.4 and 
  cobertura-1.9.2         

  Below is the minimal amount of XML necessary to integrate Sanity4J into your project's POM.

+-------------------------------+
  <reporting>
    <plugins>
      ...
      <plugin>
        <groupId>net.sf.sanity4j</groupId>
        <artifactId>sanity4j</artifactId>
        <version>1.0</version>
                
        <configuration>
          <!-- Replace the following parameter with the location of the analysis tools --> 
          <productsDir>/javaTools</productsDir>
        </configuration>
      </plugin>
      ...
    </plugins>
  </reporting>
+-------------------------------+

Customising Sanity4J analysis

  The analysis can be customised using an external configuration file. On start-up, Sanity4J
  attempts to read external configuration from a standard Java properties file named 
  "<<<sanity4j.properties>>>" located in the working directory or the user's home directory.
  (The location of the file can be further specificed by using the <<<externalPropertiesPath>>>.)

* Customising which tools are run.

  The "<<<sanity4j.toolsToRun>>>" property controls which tools will be run by Sanity4J. Multiple tools
  can be listed here, in a comma-separated list. The Available tools are listed in the table below.
 
*-----------------+-------------------------------------------------------+---------------------------------------------------------+------------------------------+
||ID              || Tool name                                            || Description                                            || Supported versions          |
*-----------------+-------------------------------------------------------+---------------------------------------------------------+------------------------------+
| checkstyle      | {{{http://checkstyle.sourceforge.net}Checkstyle}}     | Static source code analyser.                            | 4.4                          |
| cobertura-merge | {{{http://cobertura.sourceforge.net}Cobertura Merge}} | Merges multiple datafiles before the coverage analysis. | 1.9.2                        |
| cobertura       | {{{http://cobertura.sourceforge.net}Cobertura}}       | Unit test coverage analysis.                            | 1.8, 1.9, 1.9.2              |
| findbugs        | {{{http://findbugs.sourceforge.net}FindBugs}}         | Static byte-code analyser.                              | 1.3.9                        |
| pmd             | {{{http://pmd.sourceforge.net/}PMD}}                  | Static source code analyser.                            | 4.2.1, 4.2.2, 4.2.5          |
| pmd-cpd         | {{{http://pmd.sourceforge.net/}PMD CPD}}              | Detects copy & pasted source code.                      | 4.2.1, 4.2.2, 4.2.5          |
*-----------------+-------------------------------------------------------+---------------------------------------------------------+------------------------------+

  The default value for this parameter is:
  
+---------------------------------------------------------------------------+
sanity4j.toolsToRun=checkstyle,cobertura-merge,cobertura,findbugs,pmd,pmd-cpd
+---------------------------------------------------------------------------+

* Customising tool configuration.

  The default configuration of each tool may not be appropriate to your project, so tool options can
  be modified as necessary e.g. to modify the rules in use or adjust performance options. Since each
  tool is launched in its own separate process, launch options for each tool can be modified 
  individually using a parameter of the form: <<<sanity4j.tool.<tool id>.<version>.command>>>. 
 
  Variable substitution is available for tool locations and command lines. For 
  example, $\{java\} will be replaced by the path to the JRE executable. The Variables available are 
  detailed in the table below.
   
*------------------------+-----------------------------------------------------------------------------+
|| Variable              || Value                                                                      |
*------------------------+-----------------------------------------------------------------------------+
| java                   | Path to JRE executable.                                                     |
| javaArgs               | JRE arguments (memory, classpath, defines).                                 |
| products               | The location of the tools directory.                                        |
| source                 | The path to the directory containing the combined sources to be analysed.   |
| classes                | The path to the directory containing the combined classes to be analysed.   |
| libs                   | The path to the directory containing the combined libraries to be analysed. |
| coverageDataFile       | The unit test coverage data file.                                           |
| coverageMergeDataFiles | The list of unit test coverage data files.                                  |
| tempDir                | The temporary directory location.                                           |
| File.separatorChar     | OS-specific File path separator character.                                  |
| File.pathSeparator     | OS-specific path separator character.                                       |
| outputFile             | The location where the tool should place it's output.                       |
| toolHome               | The home directory for the tool (not available for ".home" properties).     |
*------------------------+-----------------------------------------------------------------------------+
 
  Here is an example of configuring Checkstyle 4.4 to only use the Sun Code Conventions:
 
+-------------------------------+
sanity4j.tool.checkstyle.4.4.command="${java}" ${javaArgs} com.puppycrawl.tools.checkstyle.Main
-c "${toolHome}${File.separatorChar}sun_checks.xml" -f xml -o "${outputFile}" -r ${source}
+-------------------------------+
 
* Customising issue categories and severities.

  Issue weightings and exclusions can be adjusted in settings files for each individual tool 
  (where supported) or centrally in the external Sanity4J property file. The names for each 
  parameter are of the form <<< <source>.<ruleName>.<option> >>>.
  
  The "<<<source>>>" corresponds to the tool which generates the issue. Possible values are
  "<<<Findbugs>>>", "<<<PMD>>>" and "<<<Checkstyle>>>".
  
  The "<<<ruleName>>>" is the tool-specific name of the rule which generates the issue (e.g. <<<NP_LOAD_OF_KNOWN_NULL_VALUE>>>).
  
  The "<<<option>>>" must be one of the following: 
  
   * The "category" option is used to set category paths. A category path is a text string, 
     with path elements delimited by '/'. Multiple paths may be supplied, delimited by commas.
     For example, the value "Style,Maintenance/Code Size" would place issues under the 
     categories "Style" and "Maintenance" --> "Code Size"

   * The "severity" option is used to set the issue severity. The parameter's value must be 
     0-4 (0=info, 1=low, 2=moderate, 3=significant, 4=high).

   * The "includes" option controls which Java class names should have the rule applied to 
     them. All files are included by default. The parameter value should be a comma-delimited 
     list of class names to include, using java regular expressions. 

   * The "excludes" option controls which Java class names should be excluded from this rule. 
     A class is excluded if it is matched by at least one exclude. The default exclude is 
     nothing. The parameter's value should be a comma-delimited list of class names to exclude.

  The following example shows parameters to exclude jUnit 3.x tests from CheckStyle's 
  magic-number check, and set the severity for this issue in other classes to low.

+-----------------------------------------------------+
Checkstyle.MagicNumberCheck.severity=1
Checkstyle.MagicNumberCheck.excludes=Test[A-Z].*,.*Test
+-----------------------------------------------------+

* Customising report output.

  The weighting for the quality metric can be modified if necessary. The default weighting is shown below. 
  
+-------------------------------+
sanity4j.report.metric.quality.expression=1.0 - ((#highCount * 100.0 + #significantCount * 20.0 +
#moderateCount * 5.0 + #lowCount * 2.0 + #infoCount * 0.0) / #linesOfCode)
+-------------------------------+
