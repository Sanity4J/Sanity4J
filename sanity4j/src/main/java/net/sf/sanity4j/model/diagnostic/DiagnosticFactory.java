package net.sf.sanity4j.model.diagnostic;

import java.util.Properties;

import net.sf.sanity4j.util.QaUtil;

/**
 * A factory for creating {@link Diagnostic} Messages. The factory is associated with a given set of
 * "external properties". These properties are used to define the diagnostic severities, exclusions and categorisation
 * for all of the {@link Diagnostic} messages created by this factory.
 * 
 * @author Brian Kavanagh
 */
public final class DiagnosticFactory
{
    /**
     * The <b>default</b> {@link DiagnosticFactory} instance properties. These properties control the severities,
     * exclusions and categorisation of the {@link Diagnostic} messages created by the default {@link DiagnosticFactory}
     * .
     */
    private static final Properties properties = QaUtil.getProperties("/net/sf/sanity4j/model/diagnostic/Diagnostic.properties");

    /**
     * The default {@link DiagnosticFactory} instance.
     */
    private static DiagnosticFactory instance = new DiagnosticFactory();

    /**
     * Private constructor to disallow explicit instantiation of a {@link DiagnosticFactory}.
     */
    private DiagnosticFactory()
    {
        // NO-OP.
    }

    /**
     * Private constructor used by {@link #getInstance(Properties)} to create a {@link DiagnosticFactory} with a
     * specific set of external properties.
     * 
     * @param properties The specific set of external properties to be used by the newly created
     *            {@link DiagnosticFactory}
     */
    private DiagnosticFactory(final Properties properties)
    {
        this.properties.putAll(properties);
    }

    /**
     * Returns the {@link DiagnosticFactory} default singleton.
     * 
     * @return The {@link DiagnosticFactory} default singleton.
     */
    public static DiagnosticFactory getInstance()
    {
        return instance;
    }

    /**
     * Returns a {@link DiagnosticFactory} with a specific set of external properties.
     * 
     * @param properties The specific set of external properties to be used by the newly created
     *            {@link DiagnosticFactory}.
     * @return a {@link DiagnosticFactory} with the specified set of <em>properties</em>
     */
    public static DiagnosticFactory getInstance(final Properties properties)
    {
        return new DiagnosticFactory(properties);
    }

    /**
     * Returns a newly created {@link Diagnostic}.
     * 
     * @return A newly created {@link Diagnostic}.
     */
    public Diagnostic getDiagnostic()
    {
        return new Diagnostic(this);
    }

    /**
     * Gets a {@link DiagnosticFactory} property.
     * 
     * @param key the key for the property to be retrieved.
     * @return The value of the property associated with the given <em>key</em>, or <b>null</b> if no property with the
     *         given <em>key</em> exists.
     */
    public String getProperty(final String key)
    {
        return properties.getProperty(key);
    }

    /**
     * Sets a {@link DiagnosticFactory} property.
     * 
     * @param key the key for the property to be set.
     * @param value the value of the property to be associated with the given <em>key</em>.
     */
    public void setProperty(final String key, final String value)
    {
        properties.setProperty(key, value);
    }
}
