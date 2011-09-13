package net.sf.sanity4j.workflow.tool;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import net.sf.sanity4j.gen.cobertura_1_9_2.Clazz;
import net.sf.sanity4j.gen.cobertura_1_9_2.Line;
import net.sf.sanity4j.gen.cobertura_1_9_2.Package;
import net.sf.sanity4j.model.coverage.ClassCoverage;
import net.sf.sanity4j.model.coverage.Coverage;
import net.sf.sanity4j.model.coverage.PackageCoverage;
import net.sf.sanity4j.util.ExtractStats;
import net.sf.sanity4j.util.JaxbMarshaller;
import net.sf.sanity4j.util.StartElementListener;

/**
 * CoberturaResultReader - Translates cobertura results into the common format used by the QA tool.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class CoberturaResultReader implements ResultReader, StartElementListener
{
    /** The ExtractStats to add the results to. */
    private ExtractStats stats;

    /** The Cobertura result file to read from. */
    private File coberturaResultFile;

    /** {@inheritDoc} */
    public void setResultFile(final File resultFile)
    {
        this.coberturaResultFile = resultFile;
    }

    /** {@inheritDoc} */
    public void setStats(final ExtractStats stats)
    {
        this.stats = stats;
    }

    /**
     * Extracts the Coverage statistics from the coberturaResultFile.
     */
    public void run()
    {
        JaxbMarshaller.traverse(coberturaResultFile, "net.sf.sanity4j.gen.cobertura_1_9_2", "http://net.sf.sanity4j/namespace/cobertura-1.9.2", this);
    }

    /** {@inheritDoc} */
    public void foundElement(final StartElement element, final XMLEventReader reader, final Unmarshaller unmarshaller) throws JAXBException
    {
        if ("coverage".equals(element.getName().getLocalPart()))
        {
            Coverage coverage = stats.getCoverage();
            coverage.setLineCoverage(Double.parseDouble(element.getAttributeByName(new QName("line-rate")).getValue()));
            coverage.setBranchCoverage(Double.parseDouble(element.getAttributeByName(new QName("branch-rate")).getValue()));
        }
        else if ("package".equals(element.getName().getLocalPart()))
	    {
            Package pakage = (Package) unmarshaller.unmarshal(reader);
            processPackage(pakage);
	    }
    }

    /**
     * Processes the coverage results for a single package.
     * @param pakage the Package to process.
     */
    private void processPackage(final Package pakage)
    {
        Coverage coverage = stats.getCoverage();
        PackageCoverage packageCoverage = new PackageCoverage(pakage.getName());
        coverage.addPackage(packageCoverage);

        packageCoverage.setLineCoverage(pakage.getLineRate().doubleValue());
        packageCoverage.setBranchCoverage(pakage.getBranchRate().doubleValue());

        List<Clazz> classes = pakage.getClasses().getClazz();

        // Loop through all the package's classes and add them to the coverage
        for (Clazz clazz : classes)
        {
            // We always use the enclosing class's name
            String fileName = clazz.getFilename();
            String className = fileName.substring(0, fileName.length() - ".java".length());
            className = className.replaceAll("[/\\\\]", ".");
            boolean isEnclosingClass = className.equals(clazz.getName());

            ClassCoverage classCoverage = packageCoverage.getClassCoverage(className);

            if (classCoverage == null)
            {
                classCoverage = new ClassCoverage(className);
                packageCoverage.addClass(classCoverage);
            }

            if (isEnclosingClass)
            {
                classCoverage.setLineCoverage(clazz.getLineRate().doubleValue());
                classCoverage.setBranchCoverage(clazz.getBranchRate().doubleValue());
            }

            // Add line coverage info
            for (Object obj : clazz.getLines().getContent())
            {
                if (obj instanceof Line)
                {
                    Line line = (Line) obj;

                    classCoverage.addLineCoverage(line.getNumber(), line.getHits(), line.isBranch());

                    if (line.getConditionCoverage() != null)
                    {
                        String conditionCoverage = line.getConditionCoverage();
                        int index = conditionCoverage.indexOf('%');
                        int percent = Integer.parseInt(conditionCoverage.substring(0, index));

                        classCoverage.addBranchCoverage(line.getNumber(), percent / 100.0);
                    }
                }
            }
        }
    }


    /**
     * @return the description of this WorkUnit
     */
    public String getDescription()
    {
        return "Reading Cobertura results";
    }
}