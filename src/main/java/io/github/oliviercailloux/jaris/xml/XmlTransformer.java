package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.collections.CollectionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.function.Consumer;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.jaxp.IdentityTransformer;
import net.sf.saxon.jaxp.SaxonTransformerFactory;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.s9api.Message;
import net.sf.saxon.s9api.XmlProcessingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Instances of this class use two categories of event seriousness that can happen during parsing of
 * the schema or during transformation: information and problem.
 * </p>
 * <p>
 * An instance is either <em>normal</em> or {@link #pedanticTransformer <em>pedantic</em>}. If it is
 * normal, it logs information events and throws exceptions for problem events. If it is pedantic,
 * it throws exceptions for both information and problem events.
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
 * The {@link #usingSystemDefaultFactory() system default factory} sometimes spits errors to the
 * console, which escapes the mechanism described here above, due to
 * <a href="https://stackoverflow.com/a/21209904/">a bug</a> in the JDK.
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
    return generalTransformer(factory, XmlTransformErrorListener.WARNING_NOT_GRAVE_ERROR_LISTENER);
  }

  /**
   * Provides a transformer instance using the provided factory.
   * <p>
   * The returned transformer throws exceptions upon encountering information events, including
   * messages, even if specified as non-terminating.
   * </p>
   *
   * @param factory the factory to use.
   * @return a transformer instance.
   */
  public static XmlTransformer pedanticTransformer(TransformerFactory factory) {
    return generalTransformer(factory, XmlTransformErrorListener.EVERYTHING_GRAVE_ERROR_LISTENER);
  }

  private static XmlTransformer generalTransformer(TransformerFactory factory,
      XmlTransformErrorListener errorListener) {
    /**
     * Saxon says that this is deprecated
     * (https://www.saxonica.com/documentation12/index.html#!javadoc/net.sf.saxon.jaxp/SaxonTransformerFactory@setErrorListener),
     * but we use it anyway. https://saxonica.plan.io/boards/3/topics/9906
     * 
     */
    factory.setErrorListener(errorListener);
    LOGGER.debug("Creating our transformer using factory {}.", factory);
    return new XmlTransformer(factory);
  }

  private final TransformerFactory factory;

  private XmlTransformer(TransformerFactory tf) {
    this.factory = checkNotNull(tf);
    verify(factory.getErrorListener() instanceof XmlTransformErrorListener);
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

  public XmlConfiguredTransformer usingStylesheet(ByteSource stylesheet)
      throws XmlException, IOException {
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
      Map<XmlName, String> parameters, OutputProperties outputProperties)
      throws XmlException, IOException {
    checkNotNull(stylesheet);
    checkNotNull(parameters);
    checkArgument(!stylesheet.isEmpty());
    try (Reader r = stylesheet.openStream()) {
      return usingStylesheetInternal(new StreamSource(r), parameters, outputProperties);
    }
  }

  public XmlConfiguredTransformer usingStylesheet(ByteSource stylesheet,
      Map<XmlName, String> parameters, OutputProperties outputProperties)
      throws XmlException, IOException {
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
  private XmlConfiguredTransformer usingStylesheetInternal(Source stylesheet,
      Map<XmlName, String> parameters, OutputProperties outputProperties) throws XmlException {
    checkNotNull(parameters);

    final Transformer transformer;
    LOGGER.debug("Obtaining transformer from stylesheet {}.", stylesheet);
    if (stylesheet == null || stylesheet.isEmpty()) {
      try {
        transformer = factory.newTransformer();
      } catch (TransformerConfigurationException e) {
        throw new XmlException("Failed creating transformer.", e);
      }
    } else {
      try {
        transformer = factory.newTransformer(stylesheet);
      } catch (TransformerConfigurationException e) {
        throw new XmlException("Could not parse the provided stylesheet.", e);
      }
    }
    LOGGER.debug("Obtained transformer from stylesheet {}.", stylesheet);
    XmlTransformErrorListener errorListener =
        (XmlTransformErrorListener) factory.getErrorListener();

    /*
     * https://stackoverflow.com/a/4699749.
     */
    verify(factory instanceof TransformerFactoryImpl == transformer instanceof IdentityTransformer);
    if (transformer instanceof TransformerImpl saxonTransformer) {
      saxonTransformer.getUnderlyingXsltTransformer()
          .setMessageHandler(SaxonMessageHandler.newInstance());
    }
    /* This is required because of no default transmission of listeners. */
    transformer.setErrorListener(errorListener);

    parameters.entrySet().stream()
        .forEach(e -> transformer.setParameter(e.getKey().asFullName(), e.getValue()));
    outputProperties.asStringMap().entrySet().stream()
        .forEach(e -> transformer.setOutputProperty(e.getKey(), e.getValue()));

    if (transformer instanceof TransformerImpl saxonTransformer && errorListener.pedantic()) {
      return XmlConfiguredTransformerSaxonPedanticImpl.using(saxonTransformer);
    }
    return XmlConfiguredTransformerImpl.using(transformer);
  }
}
