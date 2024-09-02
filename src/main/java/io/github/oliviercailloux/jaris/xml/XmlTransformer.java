package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.collections.CollectionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Instances of this class make a best effort to log warnings (unless configured otherwise) and to
 * fail fast (throwing an exception) if an error or a fatalError is raised during the parsing of the
 * schema or of the document to transform.
 * </p>
 */
public class XmlTransformer {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformer.class);

  public static final String FACTORY_PROPERTY = "javax.xml.transform.TransformerFactory";

  /**
   * See https://www.w3.org/TR/2021/REC-xslt20-20210330/#serialization
   */
  public static class OutputProperties {
    /**
     * specifies whether the Transformer may add additional whitespace when outputting the result
     * tree
     */
    public static final XmlName INDENT = XmlName.localName(OutputKeys.INDENT);
    public static final XmlName OMIT_XML_DECLARATION =
        XmlName.localName(OutputKeys.OMIT_XML_DECLARATION);

    public static OutputProperties none() {
      return new OutputProperties(ImmutableMap.of());
    }

    public static OutputProperties indent() {
      return new OutputProperties(ImmutableMap.of(INDENT, Boolean.TRUE));
    }

    public static OutputProperties noIndent() {
      return new OutputProperties(ImmutableMap.of(INDENT, Boolean.FALSE));
    }

    public static OutputProperties omitXmlDeclaration() {
      return new OutputProperties(ImmutableMap.of(OMIT_XML_DECLARATION, Boolean.TRUE));
    }

    public static OutputProperties fromMap(Map<XmlName, Boolean> properties) {
      return new OutputProperties(properties);
    }

    private final ImmutableMap<XmlName, Boolean> properties;

    public OutputProperties(Map<XmlName, Boolean> properties) {
      this.properties = ImmutableMap.copyOf(properties);
    }

    public ImmutableMap<XmlName, Boolean> asMap() {
      return properties;
    }

    ImmutableMap<String, String> asStringMap() {
      return CollectionUtils.transformKeysAndValues(properties, XmlName::asFullName,
          ((x, s, b) -> asLegacyBooleanString(b)));
    }

    static String asLegacyBooleanString(Boolean b) {
      return b ? "yes" : "no";
    }
  }

  /**
   * Provides a transformer instance using the TransformerFactory builtin system-default
   * implementation, thus, equivalent to the one obtained with
   * {@link TransformerFactory#newDefaultInstance}.
   * <p>
   * The system default factory sometimes spits errors to the console instead of through the logging
   * system due to <a href="https://stackoverflow.com/a/21209904/">a bug</a> in the JDK.
   * </p>
   *
   * @return a transformer instance.
   */
  public static XmlTransformer usingSystemDefaultFactory() {
    final TransformerFactory factory = TransformerFactory.newDefaultInstance();
    return usingFactory(factory);
  }

  /**
   * Provides a transformer instance using the TransformerFactory found using the JAXP Lookup
   * Mechanism, thus, equivalent to the one obtained with {@link TransformerFactory#newInstance()}.
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
     * the provided error listener.
     */
    try {
      factory.setAttribute("http://saxon.sf.net/feature/messageEmitterClass",
          "net.sf.saxon.serialize.MessageWarner");
    } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
      LOGGER.debug("saxon messageEmitterClass attribute not supported, not set");
    }
    LOGGER.debug("Creating transformer using factory {}.", factory);
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
   * transform and a default output property {@link OutputProperties#INDENT}.
   *
   * @return a configured transformer
   */
  public XmlConfiguredTransformer usingEmptyStylesheet() {
    return usingStylesheetInternal(null, ImmutableMap.of(), OutputProperties.indent());
  }

  /**
   * Returns a sourced transformer that may be used to transform documents using the provided
   * stylesheet and a default output property {@link OutputProperties#INDENT}.
   * <p>
   * Equivalent to {@link #usingStylesheet(ByteSource, Map)} with an empty map of parameters.
   * </p>
   *
   * @param stylesheet the stylesheet that indicates the transform to perform, not empty.
   * @return a sourced transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlConfiguredTransformer usingStylesheet(Source stylesheet) throws XmlException {
    return usingStylesheet(stylesheet, ImmutableMap.of(), OutputProperties.indent());
  }

  public XmlConfiguredTransformer usingStylesheet(ByteSource stylesheet) throws XmlException, IOException {
    return usingStylesheet(stylesheet, ImmutableMap.of(), OutputProperties.indent());
  }

  /**
   * Returns a sourced transformer that may be used to transform documents using the provided
   * stylesheet parameterized with the given parameters and using a default output property
   * {@link OutputProperties#INDENT}.
   *
   * @param stylesheet the stylesheet that indicates the transform to perform, not empty.
   * @param parameters any string parameters to be used with the given stylesheet, may be empty,
   *        null keys or values not allowed.
   * @return a sourced transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlConfiguredTransformer usingStylesheet(Source stylesheet,
      Map<XmlName, String> parameters) throws XmlException {
    return usingStylesheet(stylesheet, parameters, OutputProperties.indent());
  }

  public XmlConfiguredTransformer usingStylesheet(ByteSource stylesheet,
      Map<XmlName, String> parameters) throws XmlException, IOException {
    return usingStylesheet(stylesheet, parameters, OutputProperties.indent());
  }

  /**
   * Returns a sourced transformer that may be used to transform documents using the provided
   * stylesheet parameterized with the given parameters.
   *
   * @param stylesheet the stylesheet that indicates the transform to perform, not empty.
   * @param parameters any string parameters to be used with the given stylesheet, may be empty,
   *        null keys or values not allowed.
   * @param outputProperties any properties to be used with the transformer.
   * @return a sourced transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlConfiguredTransformer usingStylesheet(Source stylesheet,
      Map<XmlName, String> parameters, OutputProperties outputProperties) throws XmlException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    checkArgument(!stylesheet.isEmpty());
    return usingStylesheetInternal(stylesheet, parameters, outputProperties);
  }

  public XmlConfiguredTransformer usingStylesheet(CharSource stylesheet,
      Map<XmlName, String> parameters, OutputProperties outputProperties) throws XmlException, IOException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    checkArgument(!stylesheet.isEmpty());
    try (Reader r = stylesheet.openStream()) {
      return usingStylesheetInternal(new StreamSource(r), parameters, outputProperties);
    }
  }

  public XmlConfiguredTransformer usingStylesheet(ByteSource stylesheet,
      Map<XmlName, String> parameters, OutputProperties outputProperties) throws XmlException, IOException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    checkArgument(!stylesheet.isEmpty());
    try (InputStream is = stylesheet.openStream()) {
      return usingStylesheetInternal(new StreamSource(is), parameters, outputProperties);
    }
  }

  /**
   * @param stylesheet may be null or empty
   * @throws XmlException if there are errors when parsing the Source; wrapping a
   *         {@link TransformerConfigurationException}.
   */
  private XmlConfiguredTransformerImpl usingStylesheetInternal(Source stylesheet,
      Map<XmlName, String> parameters, OutputProperties outputProperties) throws XmlException {
    checkNotNull(parameters);

    final Transformer transformer;
    LOGGER.debug("Obtaining transformer from stylesheet {}.", stylesheet);
    if (stylesheet == null || stylesheet.isEmpty()) {
      try {
        transformer = factory.newTransformer();
      } catch (TransformerConfigurationException e) {
        throw new VerifyException(e);
      }
    } else {
      try {
        transformer = factory.newTransformer(stylesheet);
      } catch (TransformerConfigurationException e) {
        throw new XmlException("Could not parse the provided stylesheet.", e);
      }
    }
    LOGGER.debug("Obtained transformer from stylesheet {}.", stylesheet);
    transformer.setErrorListener(factory.getErrorListener());
    parameters.entrySet().stream()
        .forEach(e -> transformer.setParameter(e.getKey().asFullName(), e.getValue()));
    outputProperties.asStringMap().entrySet().stream()
        .forEach(e -> transformer.setOutputProperty(e.getKey(), e.getValue()));

    return XmlConfiguredTransformerImpl.using(transformer);
  }
}
