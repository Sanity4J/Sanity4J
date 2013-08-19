Sanity4J Java quality assurance tool 
Copyright (C) 2010  The Sanity4J Team, http://sanity4j.sf.net/

This read-me file is for the Sanity4J source distribution, and is intended for 
developers who are interested in contributing to the Sanity4J project. If you
are only intending to use Sanity4J as a tool for your project, you should 
download the binary distribution from http://sanity4j.sourceforge.net/ .
	
------------------------------------------------------------------------------
Licence
------------------------------------------------------------------------------

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

------------------------------------------------------------------------------
Acknowledgements
------------------------------------------------------------------------------

   This product includes software developed by
   The Apache Software Foundation (http://www.apache.org/).

   This product includes also software developed by :
     - the W3C consortium (http://www.w3c.org) ,
     - the SAX project (http://www.saxproject.org)

   Please read the different LICENSE files present in the root directory of
   this distribution.

   The names "Ant" and  "Apache Software Foundation"  must not be used to
   endorse  or promote  products derived  from this  software without prior
   written permission. For written permission, please contact
   apache@apache.org.

   This product includes software developed by the Checkstyle project
   http://checkstyle.sourceforge.net/
   
   This product includes software developed by the Cobertura project
   http://cobertura.sourceforge.net/

   This product includes software developed by the FindBugs project
   http://findbugs.sourceforge.net/
   
   This product includes software developed by the JFreeChart project
   http://www.jfree.org/jfreechart/

   This product includes software developed by the PMD  project
   http://pmd.sourceforge.net/
   
   This product includes software developed in part by support from
   the Defense Advanced Research Project Agency (DARPA).

------------------------------------------------------------------------------
Introduction
------------------------------------------------------------------------------

This directory contains the source code for the Sanity4J tool. Sanity4J is 
designed to run quality assurance analysis on Java projects using the 
Apache ANT build tool (http://ant.apache.org) or via a stand-alone UI.

See docs/index.html for more information.
See sanity4j_ant_user_guide.html for more information on Ant usage.

------------------------------------------------------------------------------
Getting started
------------------------------------------------------------------------------

   The project follows the standard maven structure:
   
   sanity4j
     |__ docs         	     -- project documentation / user guides
     |__ src			     -- source code
	 |    |__ main           -- source code for the Sanity4J jar file
     |    |  |__ java        -- java source code for Sanity4J jar file
     |    |  \__ resources   -- resources to be included in the Sanity4J jar file
	 |    |                     also includes XML schemas for JAXB.
     |    |__ test           -- unit tests
	 |       |__ java        -- jUnit tests
     |       \__ resources   -- resources for unit tests
     |__ pom.xml             -- The Maven POM file for the Sanity4J project
     |__ LICENSE.*.txt       -- various licenses for the third party libs
     |__ LICENSE.txt         -- the licence for the Sanity4J source code
     \__ README.txt          -- this read-me file

   You will probably want to follow the instructions in the next 3 sections,
   in order, to build and start using the task.

------------------------------------------------------------------------------
Building the project
------------------------------------------------------------------------------

   Run "mvn package" in the Sanity4J directory to have JAXB generate the java 
   code for XML marshalling, run the unit tests and produce the combined 
   Sanity4J jar file.

------------------------------------------------------------------------------------
Suggested IDE
------------------------------------------------------------------------------------

   The suggested IDE for Sanity4J development is the eclipse (http://eclipse.org/) 
   IDE. You should be able to import the Maven project into eclipse using the 
   m2eclipse plugin (http://m2eclipse.sonatype.org/).
     
------------------------------------------------------------------------------------
Running Sanity4J:
------------------------------------------------------------------------------------

 - Sanity4J can be run as an Ant task, an annotated example is shown in
   the file docs/sanity4j_ant_user_guide.html. Alternatively, the Sanity4J UI can be
   invoked by running the sanity4j jar file directly (java -jar, or launching from
   the desktop e.g. double-click on the jar in the Microsoft Windows OS).

------------------------------------------------------------------------------------
Basic Sanity4J workflow:
------------------------------------------------------------------------------------

1) Since some of the analysis tools don't support multiple source/class directories, 
   Sanity4J task first produces "combined" source/class/lib directories in a 
   directory under the system "temp" dir ("/tmp" or "C:\windows\temp", etc.). 
   For projects with multiple source/class directories, only the top-level 
   directory needs to be specified. Sanity4J will search for directories 
   containing sources/classes to be analysed.
   
2) Each tool is then run in turn, using its command-line interface, and the 
   output xml is written to the temporary directory.
   
3) The tool output is then read and converted into a common format which is used
   to create the combined report.

   