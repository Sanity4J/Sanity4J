Sanity4J Java quality assurance tool 
Copyright (C) 2010  The Sanity4J Team, http://sanity4j.sf.net/

This read-me file is for the Sanity4J binary distribution, and is intended for 
end-users who wish to run Sanity4J run on their own projects. If you are 
interested in contributing to the Sanity4J project, you should download the 
source distribution from http://sanity4j.sourceforge.net/ .
	
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

This directory contains the Sanity4J tool. Sanity4J is designed to run quality 
assurance analysis on Java projects using the Maven build tool 
(http://maven.apache.org/). An annotated example is shown in the file 
Readme_Maven.txt .

To install Sanity4J into your local Maven repository, run the following command:
mvn install:install-file -Dfile=sanity4j-1.0.3.jar -DpomFile=pom.xml

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
