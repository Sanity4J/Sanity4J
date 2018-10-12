package com.github.sanity4j.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.sanity4j.gen.checkstyle_4_4.Checkstyle;
import com.github.sanity4j.gen.checkstyle_4_4.File;

/**
 * JaxbMarshaller_Test - unit test for JaxbMarshaller.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class JaxbMarshaller_Test
{
    /** A temporary file to write the test data to. */
    private java.io.File tempFile;

    /** The target java package for unmarshalling the test data. */
    private static final String TARGET_PACKAGE = "com.github.sanity4j.gen.checkstyle_4_4";

    /** The XML data used for testing. */
    private static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<checkstyle version=\"4.4\">\n"
      + "    <file name=\"/someDir/SomeJavaFile.java\">\n"
      + "        <error line=\"33\" column=\"53\" severity=\"warning\" message=\"&apos;;&apos; is preceded with whitespace.\" source=\"com.puppycrawl.tools.checkstyle.checks.whitespace.NoWhitespaceBeforeCheck\"/>\n"
      + "        <error line=\"33\" column=\"67\" severity=\"warning\" message=\"&apos;;&apos; is preceded with whitespace.\" source=\"com.puppycrawl.tools.checkstyle.checks.whitespace.NoWhitespaceBeforeCheck\"/>\n"
      + "        <error line=\"33\" column=\"69\" severity=\"warning\" message=\"&apos;;&apos; is followed by whitespace.\" source=\"com.puppycrawl.tools.checkstyle.checks.whitespace.EmptyForIteratorPadCheck\"/>\n"
      + "    </file>\n"
      + "</checkstyle>\n";

    @Before
    public void setUp() throws Exception
    {
        tempFile = java.io.File.createTempFile("JaxbMarshaller_Test", ".xml");
    }

    @After
    public void tearDown() throws Exception
    {
        if (!tempFile.delete())
        {
            throw new IOException("Failed to delete temp file: " + tempFile.getPath());
        }
    }

    @Test
    public void testUnmarshalMissingFile()
    {
        try
        {
            java.io.File file = new java.io.File("JaxbMarshaller_Test.nonExistantFile");
            JaxbMarshaller.unmarshal(file, TARGET_PACKAGE, "http://com.github.sanity4j/namespace/dummy");
            Assert.fail("Should have thrown a QAException");
        }
        catch (QAException expected)
        {
            Assert.assertNotNull("Thrown exception should contain a message", expected.getMessage());
            Assert.assertTrue("Throws exception should contain the original cause", expected.getCause() instanceof IOException);
        }
    }

    @Test
    public void testUnmarshallMalformedFile() throws IOException
    {
        try
        {
            FileUtil.writeToFile(XML.substring(0, XML.length() / 2), tempFile);
            JaxbMarshaller.unmarshal(tempFile, TARGET_PACKAGE, "http://com.github.sanity4j/namespace/dummy");
            Assert.fail("Should have thrown a QAException");
        }
        catch (QAException expected)
        {
            Assert.assertNotNull("Thrown exception should contain a message", expected.getMessage());
            Assert.assertNotNull("Throws exception should contain the original cause", expected.getCause());
        }
    }

    @Test
    public void testUnmarshall() throws IOException
    {
        FileUtil.writeToFile(XML, tempFile);

        Object result = JaxbMarshaller.unmarshal(tempFile, TARGET_PACKAGE, "http://com.github.sanity4j/namespace/checkstyle-4.4");

        Assert.assertNotNull("unmarshall returned null", result);

        Assert.assertTrue("Incorrect type unmarshalled",
                   Checkstyle.class.isAssignableFrom(result.getClass()));

        Checkstyle checkStyle = (Checkstyle) result;

        Assert.assertEquals("Incorrect version field value unmarshalled",
                     new BigDecimal("4.4"), checkStyle.getVersion());

        Assert.assertEquals("Incorrect number of files unmarshalled",
                     1, checkStyle.getFile().size());

        File file = checkStyle.getFile().get(0);
        Assert.assertEquals("Incorrect file name value unmarshalled",
                     "/someDir/SomeJavaFile.java", file.getName());

        List<Object> errors = new ArrayList<Object>();

        for (Object obj : file.getContent())
        {
            if (obj instanceof com.github.sanity4j.gen.checkstyle_4_4.Error)
            {
                errors.add(obj);
            }
        }

        Assert.assertEquals("Incorrect number of errors unmarshalled",
                     3, errors.size());
    }
}
