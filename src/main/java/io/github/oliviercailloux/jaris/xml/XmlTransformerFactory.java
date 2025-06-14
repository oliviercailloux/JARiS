package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import io.github.oliviercailloux.jaris.collections.CollectionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.jaxp.IdentityTransformer;
import net.sf.saxon.jaxp.TransformerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Instances of this class use two categories of event seriousness that can happen during parsing of
 * the schema or during transformation: information and problem.
 * </p>
 * <p>
 * An instance is either <em>normal</em> or {@link #pedantic <em>pedantic</em>}. If it is normal, it
 * logs information events and throws exceptions for problem events. If it is pedantic, it throws
 * exceptions for both information and problem events.
 * </p>
 * 
 * <p>
 * XSL
 * <a href="https://developer.mozilla.org/docs/Web/XML/XSLT/Reference/Element/message">messages</a>
 * are considered information events iff their <code>terminate</code> attribute value is
 * <code>no</code>. The {@link ErrorListener errors} sent by the underlying processor are considered
 * information events iff they have severity <em>warning</em> and problem events iff they have
 * severity <em>error</em> or <em>fatal</em>.
 * </p>
 * <p>
 * The {@link KnownFactory#JDK system default factory} sometimes spits errors to the console, which
 * escapes the mechanism described here above, due to
 * <a href="https://stackoverflow.com/a/21209904/">a bug</a> in the JDK.
 * </p>
 */
public class XmlTransformerFactory {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerFactory.class);

  public static final CharSource STRIP_WHITESPACE_STYLESHEET = Resources.asCharSource(
      Resources.getResource(XmlTransformerFactory.class, "Strip whitespace.xsl"),
      StandardCharsets.UTF_8);

  public static class OutputPropertyValue {
    private final Object value;

    public static OutputPropertyValue trueValue() {
      return new OutputPropertyValue(true);
    }

    public static OutputPropertyValue falseValue() {
      return new OutputPropertyValue(false);
    }

    public static OutputPropertyValue fromBoolean(boolean value) {
      return new OutputPropertyValue(value);
    }

    public static OutputPropertyValue fromInt(int value) {
      return new OutputPropertyValue(value);
    }

    private OutputPropertyValue(Object value) {
      this.value = value;
    }

    public String toOutputPropertyString() {
      if (value instanceof Boolean b) {
        return b ? "yes" : "no";
      }
      if (value instanceof Integer i) {
        return Integer.toString(i);
      }
      throw new VerifyException("Unsupported value: " + value);
    }
  }

  /**
   * See https://www.w3.org/TR/2021/REC-xslt20-20210330/#serialization
   */
  public static class OutputProperties {
    public static final URI XALAN_PROPERTIES_URI = URI.create("http://xml.apache.org/xslt");
    /**
     * specifies whether the Transformer may add additional whitespace when outputting the result
     * tree
     */
    public static final XmlName INDENT = XmlName.localName(OutputKeys.INDENT);
    public static final XmlName XALAN_INDENT_AMOUNT =
        XmlName.expandedName(XALAN_PROPERTIES_URI, "indent-amount");
    public static final XmlName OMIT_XML_DECLARATION =
        XmlName.localName(OutputKeys.OMIT_XML_DECLARATION);

    public static OutputProperties none() {
      return new OutputProperties(ImmutableMap.of());
    }

    /**
     * Indentation set to true; {@link KnownFactory#XALAN XALAN} indent amount set to 4.
     *
     * @return an OutputProperties object with indentation settings.
     */
    public static OutputProperties indent() {
      return new OutputProperties(ImmutableMap.of(INDENT, OutputPropertyValue.trueValue(),
          XALAN_INDENT_AMOUNT, OutputPropertyValue.fromInt(4)));
    }

    public static OutputProperties noIndent() {
      return new OutputProperties(ImmutableMap.of(INDENT, OutputPropertyValue.falseValue()));
    }

    public static OutputProperties omitXmlDeclaration() {
      return new OutputProperties(
          ImmutableMap.of(OMIT_XML_DECLARATION, OutputPropertyValue.trueValue()));
    }

    public static OutputProperties fromMap(Map<XmlName, OutputPropertyValue> properties) {
      return new OutputProperties(properties);
    }

    private final ImmutableMap<XmlName, OutputPropertyValue> properties;

    public OutputProperties(Map<XmlName, OutputPropertyValue> properties) {
      this.properties = ImmutableMap.copyOf(properties);
    }

    public ImmutableMap<XmlName, OutputPropertyValue> asMap() {
      return properties;
    }

    ImmutableMap<String, String> asStringMap() {
      return CollectionUtils.transformKeysAndValues(properties, XmlName::asFullName,
          ((x, s, b) -> b.toOutputPropertyString()));
    }
  }

  /**
   * Provides a transformer instance using the provided factory.
   *
   * @param factory the factory to use.
   * @return a transformer instance.
   * @see TransformerFactory#newInstance
   * @see TransformerFactory#newDefaultInstance
   */
  public static XmlTransformerFactory usingFactory(TransformerFactory factory) {
    return generalTransformer(factory, XmlTransformErrorListener.WARNING_NOT_GRAVE_ERROR_LISTENER);
  }

  private static XmlTransformerFactory generalTransformer(TransformerFactory factory,
      XmlTransformErrorListener errorListener) {
    LOGGER.debug("Creating our transformer using factory {}.", factory);
    return new XmlTransformerFactory(factory, errorListener);
  }

  private final TransformerFactory factory;
  private final XmlTransformErrorListener errorListener;

  private XmlTransformerFactory(TransformerFactory tf, XmlTransformErrorListener errorListener) {
    this.factory = checkNotNull(tf);
    this.errorListener = errorListener;
  }

  /**
   * Returns a transformer factory that creates transformers which throw exceptions upon
   * encountering information events, including messages, even if specified as non-terminating.
   *
   * @return a pedantic transformer factory
   */
  public XmlTransformerFactory pedantic() {
    return new XmlTransformerFactory(factory,
        XmlTransformErrorListener.EVERYTHING_GRAVE_ERROR_LISTENER);
  }

  TransformerFactory factory() {
    return factory;
  }

  /**
   * Returns a transformer that may be used to transform documents using the “identity”
   * transform and a default “indented” output property.
   *
   * @return a transformer
   */
  public XmlTransformer usingEmptyStylesheet() {
    return usingStylesheetInternal(null, ImmutableMap.of(), OutputProperties.indent());
  }

  /**
   * Returns a transformer that may be used to transform documents using the “identity”
   * transform.
   *
   * @param outputProperties any properties to be used with the transformer.
   * @return a transformer
   */
  public XmlTransformer usingEmptyStylesheet(OutputProperties outputProperties) {
    return usingStylesheetInternal(null, ImmutableMap.of(), outputProperties);
  }

  /**
   * Returns a transformer that may be used to transform documents using the provided
   * stylesheet and a default “indented” output property.
   * <p>
   * Equivalent to {@link #usingStylesheet(ByteSource, Map)} with an empty map of parameters.
   * </p>
   *
   * @param stylesheet the stylesheet that indicates the transform to perform.
   * @return a transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlTransformer usingStylesheet(ByteSource stylesheet) throws XmlException, IOException {
    return usingStylesheet(stylesheet, ImmutableMap.of(), OutputProperties.indent());
  }

  /**
   * Returns a transformer that may be used to transform documents using the provided
   * stylesheet and a default “indented” output property.
   * <p>
   * Equivalent to {@link #usingStylesheet(CharSource, Map)} with an empty map of parameters.
   * </p>
   *
   * @param stylesheet the stylesheet that indicates the transform to perform.
   * @return a transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlTransformer usingStylesheet(CharSource stylesheet) throws XmlException, IOException {
    return usingStylesheet(stylesheet, ImmutableMap.of(), OutputProperties.indent());
  }

  /**
   * Returns a transformer that may be used to transform documents using the provided
   * stylesheet and a default “indented” output property.
   * <p>
   * Equivalent to {@link #usingStylesheet(ByteSource, Map)} with an empty map of parameters.
   * </p>
   *
   * @param stylesheet the stylesheet that indicates the transform to perform.
   * @return a transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlTransformer usingStylesheet(URI stylesheet) throws XmlException {
    return usingStylesheet(stylesheet, ImmutableMap.of(), OutputProperties.indent());
  }

  /**
   * Returns a transformer that may be used to transform documents using the provided
   * stylesheet parameterized with the given parameters and using a default “indented” output
   * property.
   *
   * @param stylesheet the stylesheet that indicates the transform to perform.
   * @param parameters any string parameters to be used with the given stylesheet, may be empty,
   *        null keys or values not allowed.
   * @return a transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlTransformer usingStylesheet(ByteSource stylesheet, Map<XmlName, String> parameters)
      throws XmlException, IOException {
    return usingStylesheet(stylesheet, parameters, OutputProperties.indent());
  }

  /**
   * Returns a transformer that may be used to transform documents using the provided
   * stylesheet parameterized with the given parameters and using a default “indented” output
   * property.
   *
   * @param stylesheet the stylesheet that indicates the transform to perform.
   * @param parameters any string parameters to be used with the given stylesheet, may be empty,
   *        null keys or values not allowed.
   * @return a transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlTransformer usingStylesheet(CharSource stylesheet, Map<XmlName, String> parameters)
      throws XmlException, IOException {
    return usingStylesheet(stylesheet, parameters, OutputProperties.indent());
  }

  /**
   * Returns a transformer that may be used to transform documents using the provided
   * stylesheet parameterized with the given parameters and using a default “indented” output
   * property.
   *
   * @param stylesheet the stylesheet that indicates the transform to perform.
   * @param parameters any string parameters to be used with the given stylesheet, may be empty,
   *        null keys or values not allowed.
   * @return a transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlTransformer usingStylesheet(URI stylesheet, Map<XmlName, String> parameters)
      throws XmlException {
    return usingStylesheet(stylesheet, parameters, OutputProperties.indent());
  }

  public XmlTransformer usingStylesheet(CharSource stylesheet, Map<XmlName, String> parameters,
      OutputProperties outputProperties) throws XmlException, IOException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    try (Reader r = stylesheet.openStream()) {
      return usingStylesheetInternal(new StreamSource(r), parameters, outputProperties);
    }
  }

  public XmlTransformer usingStylesheet(ByteSource stylesheet, Map<XmlName, String> parameters,
      OutputProperties outputProperties) throws XmlException, IOException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    try (InputStream is = stylesheet.openStream()) {
      return usingStylesheetInternal(new StreamSource(is), parameters, outputProperties);
    }
  }

  /**
   * @param stylesheet will be resolved using the factory resolver and an empty base if a resolver
   *        exists; if the resolver exists and returns null, that is an error; if it throws an
   *        exception, it is thrown wrapped into an xmlexception. If no resolver exists, the given
   *        stylesheet URI is considered a system id.
   * @param parameters
   * @param outputProperties
   * @return a transformer
   * @throws XmlException
   */
  public XmlTransformer usingStylesheet(URI stylesheet, Map<XmlName, String> parameters,
      OutputProperties outputProperties) throws XmlException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    String stylesheetStr = stylesheet.toString();
    Source source;
    URIResolver resolver = factory.getURIResolver();
    if (resolver == null) {
      source = new StreamSource(stylesheetStr);
    } else {
      final Source resolvedSource;
      try {
        resolvedSource = resolver.resolve(stylesheetStr, "");
      } catch (TransformerException e) {
        throw new XmlException("Error resolving stylesheet URI.", e);
      }
      if (resolvedSource == null) {
        throw new XmlException(
            "URI resolver returned null for stylesheet URI " + stylesheetStr + ".");
      }
      source = resolvedSource;
    }
    verifyNotNull(source);
    return usingStylesheetInternal(source, parameters, outputProperties);
  }

  /**
   * Returns a transformer that may be used to transform documents using the provided
   * stylesheet parameterized with the given parameters.
   *
   * @param stylesheet the stylesheet that indicates the transform to perform.
   * @param parameters any string parameters to be used with the given stylesheet, may be empty,
   *        null keys or values not allowed.
   * @param outputProperties any properties to be used with the transformer.
   * @return a transformer
   * @throws XmlException iff an error occurs when parsing the stylesheet. Wraps a
   *         {@link TransformerConfigurationException}.
   */
  public XmlTransformer usingStylesheet(Source stylesheet, Map<XmlName, String> parameters,
      OutputProperties outputProperties) throws XmlException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    return usingStylesheetInternal(stylesheet, parameters, outputProperties);
  }

  /**
   * @param stylesheet may be null or empty, resolved already
   * @throws XmlException if there are errors when parsing the Source; wrapping a
   *         {@link TransformerConfigurationException}.
   */
  private XmlTransformer usingStylesheetInternal(Source stylesheet, Map<XmlName, String> parameters,
      OutputProperties outputProperties) throws XmlException {
    checkNotNull(parameters);
    checkNotNull(outputProperties);

    final Transformer transformer;
    LOGGER.debug("Obtaining transformer from stylesheet {}.", stylesheet);
    /*
     * Saxon says that this is deprecated
     * (https://www.saxonica.com/documentation12/index.html#!javadoc/net.sf.saxon.jaxp/
     * SaxonTransformerFactory@setErrorListener), but we use it anyway.
     * https://saxonica.plan.io/boards/3/topics/9906
     */
    ErrorListener current = factory.getErrorListener();
    if (stylesheet == null || stylesheet.isEmpty()) {
      try {
        factory.setErrorListener(errorListener);
        transformer = factory.newTransformer();
      } catch (TransformerConfigurationException e) {
        throw new XmlException("Failed creating transformer.", e);
      } finally {
        factory.setErrorListener(current);
      }
    } else {
      try {
        factory.setErrorListener(errorListener);
        transformer = factory.newTransformer(stylesheet);
      } catch (TransformerConfigurationException e) {
        throw new XmlException("Could not parse the provided stylesheet.", e);
      } finally {
        factory.setErrorListener(current);
      }
    }
    LOGGER.debug("Obtained transformer from stylesheet {}.", stylesheet);

    /* This is required because of no default transmission of listeners. */
    transformer.setErrorListener(errorListener);

    parameters.entrySet().stream()
        .forEach(e -> transformer.setParameter(e.getKey().asFullName(), e.getValue()));
    outputProperties.asStringMap().entrySet().stream()
        .forEach(e -> transformer.setOutputProperty(e.getKey(), e.getValue()));

    try {
      /*
       * https://stackoverflow.com/a/4699749.
       */
      verify(
          factory instanceof TransformerFactoryImpl == transformer instanceof IdentityTransformer);
      if (transformer instanceof TransformerImpl saxonTransformer) {
        saxonTransformer.getUnderlyingXsltTransformer()
            .setMessageHandler(SaxonMessageHandler.newInstance());
      }
      if (transformer instanceof TransformerImpl saxonTransformer) {
        if (errorListener.pedantic()) {
          return XmlTransformerSaxonPedanticImpl.using(saxonTransformer);
        }
      }
    } catch (NoClassDefFoundError e) {
      LOGGER.debug("Saxon not found, no special treatment.", e);
    }
    return XmlTransformerImpl.using(transformer);
  }
}
