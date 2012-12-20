package net.sf.sanity4j.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * JaxbMarshaller - utility for marshalling/unmarshalling Objects to/from XML using JAXB.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class JaxbMarshaller 
{
    /** No instance methods here. */
    private JaxbMarshaller()
    {
    }

    /**
     * Uses JAXB to unmarshal the results from a tool's XML output into a Java Object.
     * As the tools often don't use namespaces, a namespace can be applied to the document.
     * 
     * @param file the file to read from
     * @param pkg the package containing the JAXB generated classes
     * @param namespace the namespace to change the document to, or null to leave it as is 
     * @return the unmarshalled object
     */
    public static Object unmarshal(final File file, final String pkg, final String namespace) throws QAException
    {
        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(file);
            InputStream stream = namespace == null ? fis : new RegexpReplaceInputStream(fis, "<([^!?])([^>]*)>", "<$1$2 xmlns=\"" + namespace + "\">");

            //Create an XMLReader to use with our filter
            XMLReader reader = XMLReaderFactory.createXMLReader();

            // since DNS can be be flaky (or disabled in some organisations), we want to ignore all DTDs etc.        
            reader.setEntityResolver(new EntityResolver() 
            {
                public InputSource resolveEntity(final String publicId, final String systemId)
                throws SAXException, java.io.IOException
                {
                    // this deactivates all DTDs
                    return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                }
            });               

            //Create a SAXSource specifying the filter
            SAXSource source = new SAXSource(reader, new InputSource(stream));

            //Do unmarshalling
            JAXBContext jaxbContext = JAXBContext.newInstance(pkg, JaxbMarshaller.class.getClassLoader());              
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(source);
        }
        catch (SAXParseException e)
        {
            String details = e.toString() +
            "\n Line number: " + e.getLineNumber() +
            "\n Column number: " + e.getColumnNumber()+
            "\n Public ID: " + e.getPublicId() +
            "\n System ID: " + e.getSystemId() ;

            throw new QAException("Error reading xml: " + file.getName() + details, e);
        }
        catch (IOException e)
        {
            throw new QAException("Error reading result file: " + file.getName(), e);
        }
        catch (Exception e)
        {
            throw new QAException("Error reading xml: " + file.getName(), e);
        }
        finally
        {
            QaUtil.safeClose(fis);      
        }
    }

    /**
     * Traverses the XML in the given file, notifying the listener of each start element.
     * As the tools often don't use namespaces, a namespace can be applied to the document.
     * 
     * @param file the file to read from
     * @param pkg the package containing the JAXB generated classes
     * @param namespace the namespace to change the document to, or null to leave it as is 
     * @param StartElementListener the listener that will be notified of each start element.
     */
    public static void traverse(final File file, final String pkg, final String namespace, final StartElementListener listener) throws QAException
    {
        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(file);
            InputStream stream = namespace == null ? fis : new RegexpReplaceInputStream(fis, "<([^!?])([^>]*)>", "<$1$2 xmlns=\"" + namespace + "\">");
            
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            XMLEventReader eventReader = xmlif.createXMLEventReader(stream);    
            
            // Iterate through all the StartElement events in the filtered event reader
            JAXBContext jaxbContext = JAXBContext.newInstance(pkg, JaxbMarshaller.class.getClassLoader());
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            for (XMLEvent currentEvent = eventReader.peek(); currentEvent != null; currentEvent = eventReader.peek())
            {
                // We can't use a filter on the reader, as JAXB needs all events
                if (currentEvent.isStartElement())
                {
                    StartElement element = currentEvent.asStartElement();
                    listener.foundElement(element, eventReader, unmarshaller);

                    // If the event has not been 'consumed' by the listener, move on to the next event
                    XMLEvent nextEvent = eventReader.peek();

                    if (nextEvent == currentEvent)
                    {
                        eventReader.nextEvent();
                    }
                }
                else
                {
                    if (!eventReader.hasNext())
                    {
                        break;
                    }

                    eventReader.nextEvent();
                }
            }

            eventReader.close();
        }
        catch (IOException ex)
        {
            throw new QAException("Error reading result file: " + file.getName(), ex);
        }
        catch (Exception ex)
        {
            throw new QAException("Error reading xml: " + file.getName(), ex);
        }
        finally
        {
            QaUtil.safeClose(fis);      
        }
    }
}
