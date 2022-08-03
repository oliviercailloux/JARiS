package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Instances of this class make a best effort to log warnings and to fail fast (throwing an
 * exception) if an error or a fatalError is raised during the parsing of the schema or of the
 * document to transform.
 * </p>
 */
public class XmlTransformer {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformer.class);

  public static final String FACTORY_PROPERTY = "javax.xml.transform.TransformerFactory";

  /**
   * Provides a transformer instance using the TransformerFactory builtin system-default
   * implementation.
   *
   * @return a transformer instance.
   */
  public static XmlTransformer usingSystemDefaultFactory() {
    final TransformerFactory factory = TransformerFactory.newDefaultInstance();
    return usingFactory(factory);
  }

  /**
   * Provides a transformer instance using the TransformerFactory found using the
   * <a href="../../../module-summary.html#LookupMechanism">JAXP Lookup Mechanism</a>, thus,
   * equivalent to the one obtained with {@link TransformerFactory#newInstance()}.
   * <p>
   * The system property that determines which Factory implementation to create is
   * {@link #FACTORY_PROPERTY}.
   * </p>
   *
   * @return a transformer instance.
   */
  public static XmlTransformer usingFoundFactory() {
    final TransformerFactory factory = TransformerFactory.newInstance();
    return usingFactory(factory);
  }

  /**
   * Provides a transformer instance using the provided factory.
   *
   * @param factory the factory to use.
   * @return a transformer instance.
   */
  public static XmlTransformer usingFactory(TransformerFactory factory) {
    return generalTransformer(factory,
        XmlTransformRecordingErrorListener.WARNING_NOT_GRAVE_ERROR_LISTENER);
  }

  /**
   * Provides a transformer instance using the TransformerFactory builtin system-default
   * implementation.
   * <p>
   * The returned transformer throws exceptions upon encountering warnings (as well as errors).
   * </p>
   *
   * @return a transformer instance.
   */
  public static XmlTransformer pedanticTransformer() {
    final TransformerFactory factory = TransformerFactory.newDefaultInstance();
    return generalTransformer(factory,
        XmlTransformRecordingErrorListener.EVERYTHING_GRAVE_ERROR_LISTENER);
  }

  /**
   * Provides a transformer instance using the provided factory.
   * <p>
   * The returned transformer throws exceptions upon encountering warnings (as well as errors).
   * </p>
   *
   * @param factory the factory to use.
   * @return a transformer instance.
   */
  public static XmlTransformer pedanticTransformer(TransformerFactory factory) {
    return generalTransformer(factory,
        XmlTransformRecordingErrorListener.EVERYTHING_GRAVE_ERROR_LISTENER);
  }

  private static XmlTransformer generalTransformer(TransformerFactory factory,
      XmlTransformRecordingErrorListener errorListener) {
    factory.setErrorListener(errorListener);
    /*
     * https://www.saxonica.com/html/documentation/configuration/config-features.html;
     * https://stackoverflow.com/a/4699749.
     *
     * The default implementation (from Apache Xalan) seems to have a bug preventing it from using
     * the provided error listener, see https://stackoverflow.com/a/21209904/.
     */
    try {
      factory.setAttribute("http://saxon.sf.net/feature/messageEmitterClass",
          "net.sf.saxon.serialize.MessageWarner");
    } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
      LOGGER.debug("saxon messageEmitterClass attribute not supported, not set");
    }
    LOGGER.info("Using factory {}.", factory);
    return new XmlTransformer(factory);
  }

  private final TransformerFactory factory;

  private XmlTransformer(TransformerFactory tf) {
    this.factory = checkNotNull(tf);
    checkArgument(factory.getErrorListener() instanceof XmlTransformRecordingErrorListener);
  }

  TransformerFactory factory() {
    return factory;
  }

  /**
   * Returns a sourced transformer that may be used to transform documents using the “identity”
   * transform.
   *
   * @return a sourced transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet.
   */
  public XmlToStringConfiguredTransformer usingEmptySource() throws XmlException {
    return forSourceInternal(null, ImmutableMap.of());
  }

  /**
   * Returns a sourced transformer that may be used to transform documents using the provided
   * stylesheet.
   * <p>
   * Equivalent to {@link #forSource(Source, Map)} with an empty map of parameters.
   * </p>
   *
   * @param stylesheet the stylesheet that indicates the transform to perform, not empty.
   * @return a sourced transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet.
   */
  public XmlToStringConfiguredTransformer forSource(Source stylesheet) throws XmlException {
    return forSource(stylesheet, ImmutableMap.of());
  }

  /**
   * Returns a sourced transformer that may be used to transform documents using the provided
   * stylesheet parameterized with the given parameters.
   *
   * @param stylesheet the stylesheet that indicates the transform to perform, not empty.
   * @param parameters any string parameters to be used with the given stylesheet, may be empty,
   *        null keys or values not allowed.
   * @return a sourced transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet.
   */
  public XmlToStringConfiguredTransformer forSource(Source stylesheet,
      Map<XmlName, String> parameters) throws XmlException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    checkArgument(!stylesheet.isEmpty());
    return forSourceInternal(stylesheet, parameters);
  }

  /**
   * @param stylesheet may be null or empty
   * @param parameters
   * @return
   */
  private XmlConfiguredTransformerImpl forSourceInternal(Source stylesheet,
      Map<XmlName, String> parameters) {
    checkNotNull(parameters);

    final Transformer transformer;
    try {
      LOGGER.debug("Obtaining transformer from stylesheet {}.", stylesheet);
      if (stylesheet == null || stylesheet.isEmpty()) {
        transformer = factory.newTransformer();
      } else {
        transformer = factory.newTransformer(stylesheet);
        LOGGER.debug("Obtained transformer from stylesheet {}.", stylesheet);
      }
    } catch (TransformerConfigurationException e) {
      throw new XmlException("Could not parse the provided stylesheet.", e);
    }
    transformer.setErrorListener(factory.getErrorListener());
    parameters.entrySet().stream()
        .forEach(e -> transformer.setParameter(e.getKey().asFullName(), e.getValue()));

    return XmlConfiguredTransformerImpl.using(transformer);
  }
}
