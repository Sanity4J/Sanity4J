package com.github.sanity4j.report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.github.sanity4j.model.diagnostic.Diagnostic;
import com.github.sanity4j.model.diagnostic.DiagnosticFactory;
import com.github.sanity4j.util.FileUtil;
import com.github.sanity4j.workflow.QAProcessor;

/**
 * RuleCatalogueWriter writes the rule catalogue.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class RuleCatalogueWriter
{
    /** The destination directory. */
    private final File reportDir;

    /** Initial buffer size. */
    private static final int INITIAL_BUF_SIZE = 256;
    
    /**
     * Creates a RuleCatalogueWriter.
     * 
     * @param reportDir the report directory
     */
    public RuleCatalogueWriter(final File reportDir)
    {
        this.reportDir = reportDir;
    }

    /**
     * Writes the rule catalogue to the report directory.
     * 
     * @throws IOException if there is an error writing any file
     */
    public void writeRuleCatalogue() throws IOException
    {
        StringBuffer xml = new StringBuffer(INITIAL_BUF_SIZE);

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                       + "<?xml-stylesheet type=\"text/xsl\" href=\"xslt/rule-catalogue.xsl\"?>\n"
                       + "<rules qaVersion=\"").append(QAProcessor.QA_VERSION).append("\" runDate=\"")
            .append(new Date()).append("\">\n");

        DiagnosticFactory diagnosticFactory = DiagnosticFactory.getInstance();

        List<DiagnosticProperty> diagnosticProperties = getDiagnosticProperties();

        for (DiagnosticProperty property : diagnosticProperties)
        {
            // Create a diagnostic, to get e.g. default severities.
            Diagnostic diag = diagnosticFactory.getDiagnostic();

            diag.setSource(getDiagnosticSource(property.getTool()));
            diag.setRuleName(property.getRule());
            diag.setSeverity(property.getSeverity());

            if (diag.getSeverity() >= Diagnostic.SEVERITY_INFO && diag.getSeverity() <= Diagnostic.SEVERITY_HIGH)
            {
                xml.append("<rule name=\"").append(diag.getRuleName()).append("\" tool=\"")
                    .append(diag.getSourceDescription()).append("\" severity=\"").append(diag.getSeverity())
                    .append("\"/>\n");
            }
        }

        xml.append("</rules>");

        FileUtil.writeToFile(xml.toString(), new File(reportDir, "rule-catalogue.xml"));
    }

    /**
     * Retrieves the diagnostic properties file.
     * 
     * @return a list of DiagnosticProperty objects
     * @throws IOException if there is an error reading the properties file
     */
    private List<DiagnosticProperty> getDiagnosticProperties() throws IOException
    {
        Properties properties = new Properties();
        InputStream diagnosticStream = Diagnostic.class.getResourceAsStream("Diagnostic.properties");

        if (diagnosticStream != null)
        {
            properties.load(diagnosticStream);
            diagnosticStream.close();
        }

        Map<String, DiagnosticProperty> keyedProperties = new HashMap<String, DiagnosticProperty>();

        for (Entry<Object, Object> entry : properties.entrySet())
        {
            String propertyName = (String) entry.getKey();
            String propertyValue = (String) entry.getValue();

            // each property in the properties file is in the following format:
            // <source>.<ruleName>.<option>=<value>
            String[] parts = propertyName.split("\\.");
            String tool = parts[0];
            String rule = parts[1];
            String option = parts[2];
            String key = parts[0] + '.' + parts[1];

            DiagnosticProperty property = keyedProperties.get(key);

            if (property == null)
            {
                property = new DiagnosticProperty(tool, rule);
                keyedProperties.put(key, property);
            }

            if ("severity".equalsIgnoreCase(option))
            {
                property.setSeverity(Integer.parseInt(propertyValue.trim()));
            }
        }

        List<DiagnosticProperty> result = new ArrayList<DiagnosticProperty>();
        result.addAll(keyedProperties.values());

        Collections.sort(result, new Comparator<DiagnosticProperty>()
        {
            public int compare(final DiagnosticProperty prop1, final DiagnosticProperty prop2)
            {
                int diff = prop2.severity - prop1.severity;

                if (diff == 0)
                {
                    diff = prop1.rule.toLowerCase().compareTo(prop2.rule.toLowerCase());
                }

                return diff;
            }
        });

        return result;
    }

    /**
     * Return the source of a Diagnostic, given a tool name. This is the reverse of Diagnostic.getSourceDescription.
     * 
     * @param toolName the tool name, e.g. "Checkstyle".
     * @return the code for the given tool
     */
    private int getDiagnosticSource(final String toolName)
    {
        for (int i = Diagnostic.SOURCE_OTHER; i <= Diagnostic.SOURCE_CHECKSTYLE; i++)
        {
            if (Diagnostic.getSourceDescription(i).equals(toolName))
            {
                return i;
            }
        }

        return Diagnostic.SOURCE_OTHER;
    }

    /**
     * Encapsulates entries in the Diagnostic properties file.
     */
    private static final class DiagnosticProperty
    {
        /** The tool name. */
        private String tool;

        /** The rule name. */
        private String rule;

        /** The severity of the diagnostic. */
        private int severity = Diagnostic.SEVERITY_INFO;

        /**
         * Creates a DiagnosticProperty.
         * 
         * @param tool the tool name
         * @param rule the rule name
         */
        public DiagnosticProperty(final String tool, final String rule)
        {
            this.tool = tool;
            this.rule = rule;
        }

        /**
         * @return Returns the rule.
         */
        public String getRule()
        {
            return rule;
        }

        /**
         * @param rule The rule to set.
         */
        public void setRule(final String rule)
        {
            this.rule = rule;
        }

        /**
         * @return Returns the severity.
         */
        public int getSeverity()
        {
            return severity;
        }

        /**
         * @param severity The severity to set.
         */
        public void setSeverity(final int severity)
        {
            this.severity = severity;
        }

        /**
         * @return Returns the tool.
         */
        public String getTool()
        {
            return tool;
        }

        /**
         * @param tool The tool to set.
         */
        public void setTool(final String tool)
        {
            this.tool = tool;
        }
    }
}
