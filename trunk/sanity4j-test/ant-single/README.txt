This directory contains an example "ant" (http://ant.apache.org) project
which uses the Sanity4j (http://sanity4j.sourceforge.net) ant plugin.

The top level directory contains an 'build.xml' file which can be used
to build and analyse the example code found within the 'src'
sub-directory.

This 'build.xml' file is configured by a single external property
(products.home).  This property is expected to point to a directory that
contains the products used by the sanity4j tool.

Before playing with the example code, you will need to ensure that both
JDK1.5+ and apache-ant.1.6.2+ have been installed within your local
environment and are available on your system PATH.  Once this has been
done, you should be able to run the following commands from a command
line prompt to run the sanity4j application from ant:

  ant -Dproducts.home=<products-home>

Where:

  <products-home> is the location of a directory containing binary
  distributesion for:

    checkstyle-4.4
    cobertura-1.9.2
    findbugs-1.3.9
    junit4.8.2
    pmd-4.2.5
    sanity4j

