https://jira.codehaus.org/browse/MNG-5133

Issue:

Problem generating a maven site with multi module projects.

Summary:

There is a problem when aggregating surefire reports at the parent
level with multi module projects where the child has a reference 
to the parent.


Description:

In a simple parent / child project configuration, the reactor builds
the child first and then the parent last. The simple configuration
is such that the parent has a sub module definition and the child
knows nothing about the parent.

    <modules>
      <module>child1</module>
    </modules>

    Reactor Build Order:
    child1
    example1



The reactor order changes to build the parent first when the child 
project is modified so that it has a reference to the parent.

  <name>child1</name>
  <artifactId>child1</artifactId>

    <parent>
      <groupId>muck</groupId>
      <artifactId>example1</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <relativePath>../pom.xml</relativePath>
    </parent>

    Reactor Build Order:
    example1
    child1



This feature may be by design, but it causes a problem with surefire
and aggregation of it's reports at the parent level. 

If we want to build a top level site and aggregate the surefire 
report at the top level so that it includes all the child modules, 
then building the parent site first will cause the surefire report
to display no tests.

    <modules>
      <module>child1</module>
      <module>child2</module>
    </modules>
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-site-plugin</artifactId>
          <version>3.0-beta-3</version>
          <configuration>       
            <reportPlugins>
              ...
              <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-report-plugin</artifactId>
                <version>2.9</version>
                <configuration>
                  <aggregate>true</aggregate>
                </configuration>
              </plugin>



Removal of the parent reference from the child pom will allow the
site to generate the surefire report correctly (at the parent) as the
parent is built last.



Perhaps the site lifecycle in the reactor should be modified so 
that it always runs last at the parent level?



I have a simple test project that exhibits the above behavior. 



Steps to reproduce the issue:

Example 1 is a project configuration with child references 
back to the parent, where aggregation of surefire reports exhibit 
the unwanted behavior.

1.  Extract out all the files from the maven3-jira.zip into a directory of
    your choice. I will use E:\projects\maven3-jira
   
2.  From the "example1" directory, run:
   
        mvn clean
   
    Then run:
   
        mvn package site
        
    Note that I have collected a log for this step with:
        mvn -X package site > ../build1.log

3.  Note the reactor build order:

        Reactor Build Order:
        example1
        child1
        child2      

4.  Inspect the surefire report:

        E:\projects\maven3-jira\example1\target\site\surefire-report.html        

5.  Note that there are 0 tests in the report.

6.  Run the command again:

        mvn package site

    Note that I have collected a log for this step with:
        mvn -X package site > ../build2.log

7.  Inspect the surefire report:

        E:\projects\maven3-jira\example1\target\site\surefire-report.html        

8.  Note that there are now some tests results in the report.
    I believe that they are the results from the previous run, and not 
    this current run.

9.  Run a "mvn clean", and then another "mvn package site" and note
    that the site report displays 0 tests again.

Example 2 is a project configuration with no child references 
back to the parent.

10. From the "example2" directory, run:

        mvn clean
   
    Then run:
   
        mvn package site

11. Note the reactor build order:

        Reactor Build Order:
        child1
        child2
        example2

12. Inspect the surefire report:

        E:\projects\maven3-jira\example2\target\site\surefire-report.html        

13. Note thet there are tests results in the report.

