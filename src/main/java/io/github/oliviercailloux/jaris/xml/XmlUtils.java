package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.AbstractList;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * A few helper methods to deal with XML, using the
 * <a href= "https://github.com/oliviercailloux/java-course/blob/master/DOM.adoc">Document Object
 * Model</a>, XSLT, or schema based validation.
 * </p>
 * <p>
 * The primary intended usage is with XML documents that are known (or legitimately supposed) to be
 * valid, such as those in the class path of a software or sent by web services. As a result, this
 * class adopts the simplest possible approach to deal with badly formed documents, by sending
 * unchecked exceptions upon encounter, to simplify usage while still failing fast.
 * </p>
 * <p>
 * This class API focuses on simplicity and validity of the documents that are produced.
 * </p>
 * <p>
 * As the focus is on simplicity (over flexibility), its use is appropriate if you need to do only
 * simple things with your documents, do not need much flexibility, and control the origin of the
 * documents (so do not need flexible error management).
 * </p>
 * <p>
 * As the focus is on validity (rather than versatility), this class will generally fail fast when
 * input documents are invalid.
 * </p>
 * <p>
 * The public API of this class favors {@link StreamSource} (from {@code javax.xml.transform}) to
 * {@link InputSource} (from {@code org.xml.sax}). Both classes come from the {@code java.xml}
 * module, and their APIs are almost identical, the only differences being that {@code StreamSource}
 * is part of a hierarchy (as it implements {@link Source}), which makes it nicer to use in this
 * context; and that {@code InputSource} has an “encoding” parameter, which we do not need. See also
 * <a href="https://stackoverflow.com/q/69194590">SO</a>.
 * </p>
 */
public class XmlUtils {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);

  /**
   * The XHTML namespace URI, defined to be {@code http://www.w3.org/1999/xhtml}.
   */
  public static final URI XHTML_NS_URI = URI.create("http://www.w3.org/1999/xhtml");

  /**
   * The empty source. Use to indicate that the source is not provided, not applicable or unknown.
   */
  public static final Source EMPTY_SOURCE = new Source() {
    @Override
    public void setSystemId(String systemId) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getSystemId() {
      return null;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }
  };

  /**
   * A runtime exception indicating an unexpected exception relating to XML treatment, supposed to
   * be generally not worth catching.
   */
  @SuppressWarnings("serial")
  public static class XmlException extends RuntimeException {
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public XmlException(String message) {
      super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause. (A {@code null} value is permitted, and indicates that the cause is
     *        nonexistent or unknown.)
     */
    public XmlException(String message, Throwable cause) {
      super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the class and
     * detail message of {@code cause}). This constructor is useful for XML exceptions that are
     * little more than wrappers for other throwables.
     *
     * @param cause the cause. (A {@code null} value is permitted, and indicates that the cause is
     *        nonexistent or unknown.)
     */
    public XmlException(Throwable cause) {
      super(cause);
    }
  }

  private XmlUtils() {
    /* Should not be instanciated. */
  }

  /**
   * Returns a source representing the given content.
   *
   * @param content the content held by the source.
   * @return a source
   */
  public static StreamSource asSource(String content) {
    return new StreamSource(new StringReader(content));
  }

  private static InputSource toInputSource(StreamSource document) {
    final InputSource inputSource = new InputSource();

    {
      @SuppressWarnings("resource")
      final InputStream inputStream = document.getInputStream();
      if (inputStream != null) {
        inputSource.setByteStream(inputStream);
      }
    }
    {
      @SuppressWarnings("resource")
      final Reader reader = document.getReader();
      if (reader != null) {
        inputSource.setCharacterStream(reader);
      }
    }
    {
      final String publicId = document.getPublicId();
      if (publicId != null) {
        inputSource.setPublicId(publicId);
      }
    }
    {
      final String systemId = document.getSystemId();
      if (systemId != null) {
        inputSource.setSystemId(systemId);
      }
    }
    return inputSource;
  }

  /**
   * Initializes and returns the DOM helper service.
   * <p>
   * This initializes the {@code DOMImplementationRegistry}, as described in
   * {@link DOMImplementationRegistry#newInstance()}, or throws an {@link XmlException} if it fails
   * to initialize or to obtain an implementation that provides the LS feature.
   * </p>
   *
   * @return a DOM helper instance
   * @throws XmlException If the {@link DOMImplementationRegistry} initialization fails or it finds
   *         no implementation providing the LS feature.
   */
  public static DomHelper loadAndSave() throws XmlException {
    final DOMImplementationRegistry registry;
    try {
      registry = DOMImplementationRegistry.newInstance();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | ClassCastException e) {
      throw new XmlException(e);
    }
    final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
    if (impl == null) {
      throw new XmlException(String.format(
          "Registry '%s' did not yield any DOM implementation providing the LS feature.",
          registry.toString()));
    }
    return new DomHelper(impl);
  }

  /**
   * Provides an instance of a schema helper, and initializes a schema factory using
   * {@link SchemaFactory#newInstance(String)} that reads W3C XML Schema 1.0.
   *
   * @return a schema helper instance.
   */
  public static SchemaHelper schemaHelper() {
    final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    return schemaHelper(factory);
  }

  /**
   * Provides an instance of a schema helper that will use the provided factory.
   *
   * @param factory the factory to be used by the returned schema helper.
   * @return a schema helper instance.
   */
  public static SchemaHelper schemaHelper(SchemaFactory factory) {
    factory.setErrorHandler(SchemaHelper.THROWING_ERROR_HANDLER);
    LOGGER.info("Using factory {}.", factory);
    return new SchemaHelper(factory);
  }

  /**
   * Class that helps with DOM manipulation, in particular, serialization and deserialization, and
   * with static methods for manipulating lists of nodes.
   */
  public static class DomHelper {
    private static class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
      private final NodeList delegate;

      NodeListWrapper(NodeList l) {
        delegate = l;
      }

      @Override
      public Node get(int index) {
        return delegate.item(index);
      }

      @Override
      public int size() {
        return delegate.getLength();
      }
    }

    private static class NodeListToElementsWrapper extends AbstractList<Element>
        implements RandomAccess {
      private final NodeList delegate;

      NodeListToElementsWrapper(NodeList l) {
        delegate = l;
      }

      @Override
      public Element get(int index) {
        return (Element) delegate.item(index);
      }

      @Override
      public int size() {
        return delegate.getLength();
      }
    }

    /**
     * Returns an immutable copy of the given list of nodes, using a proper generic collection.
     *
     * @param nodes the nodes to copy
     * @return an immutable copy of the nodes
     */
    public static ImmutableList<Node> toList(NodeList nodes) {
      return ImmutableList.copyOf(new NodeListWrapper(nodes));
    }

    /**
     * Returns an immutable copy of the given list of nodes as a list of elements, using a proper
     * generic collection.
     *
     * @param nodes the nodes to copy
     * @return an immutable copy of the nodes
     * @throws ClassCastException if some node in the provided list cannot be cast to an element.
     */
    public static ImmutableList<Element> toElements(NodeList nodes) throws ClassCastException {
      return ImmutableList.copyOf(new NodeListToElementsWrapper(nodes));
    }

    /**
     * Returns the node type, its local name, its namespace, its value, and its name.
     *
     * @param node the node from which to extract debug information
     * @return a string containing information pertaining to the node
     */
    public static String toDebugString(Node node) {
      return String.format("Node type %s, Local %s, NS %s, Value %s, Name %s.", node.getNodeType(),
          node.getLocalName(), node.getNamespaceURI(), node.getNodeValue(), node.getNodeName());
    }

    private static class ThrowingDomErrorHandler implements DOMErrorHandler {
      @Override
      public boolean handleError(DOMError error) {
        return false;
      }
    }

    private static final ThrowingDomErrorHandler THROWING_DOM_ERROR_HANDLER =
        new ThrowingDomErrorHandler();
    private final DOMImplementationLS impl;
    private LSSerializer ser;

    private LSParser deser;

    private DomHelper(DOMImplementationLS impl) {
      this.impl = checkNotNull(impl);
      ser = null;
      deser = null;
    }

    LSInput toLsInput(StreamSource document) {
      final LSInput input = impl.createLSInput();

      {
        @SuppressWarnings("resource")
        final InputStream inputStream = document.getInputStream();
        if (inputStream != null) {
          input.setByteStream(inputStream);
        }
      }
      {
        @SuppressWarnings("resource")
        final Reader reader = document.getReader();
        if (reader != null) {
          input.setCharacterStream(reader);
        }
      }
      {
        final String publicId = document.getPublicId();
        if (publicId != null) {
          input.setPublicId(publicId);
        }
      }
      {
        final String systemId = document.getSystemId();
        if (systemId != null) {
          input.setSystemId(systemId);
        }
      }
      return input;
    }

    private void lazyInitSer() {
      if (ser != null) {
        return;
      }
      ser = impl.createLSSerializer();
      ser.getDomConfig().setParameter("error-handler", THROWING_DOM_ERROR_HANDLER);
      /* Not supported by the default implementation. */
      // ser.getDomConfig().setParameter("ignore-unknown-character-denormalizations", true);
      ser.getDomConfig().setParameter("format-pretty-print", true);
    }

    private void lazyInitDeser() {
      if (deser != null) {
        return;
      }
      try {
        deser = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
      } catch (DOMException e) {
        throw new VerifyException("Implementation does not support synchronous mode.", e);
      }
      deser.getDomConfig().setParameter("error-handler", THROWING_DOM_ERROR_HANDLER);
    }

    /**
     * Retrieves the content of the given stream as a document.
     *
     * @param input the content
     * @return a document
     * @throws XmlException iff loading the XML document failed.
     */
    public Document asDocument(StreamSource input) throws XmlException {
      lazyInitDeser();
      final Document doc;
      try {
        doc = deser.parse(toLsInput(input));
      } catch (LSException e) {
        throw new XmlException("Unable to parse the provided document.", e);
      }

      return doc;
    }

    /**
     * I favor the DOM LS parser to the DocumentBuilder: DOM LS is a W3C standard (see
     * <a href="https://stackoverflow.com/a/38153986">SO</a>) and I need an LS serializer anyway.
     */
    @SuppressWarnings("unused")
    private Document asDocumentUsingBuilder(StreamSource input)
        throws ParserConfigurationException, SAXException, IOException {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      final DocumentBuilder builder = factory.newDocumentBuilder();

      final Document doc = builder.parse(toInputSource(input));

      final Element docE = doc.getDocumentElement();
      LOGGER.debug("Main tag name: {}.", docE.getTagName());

      return doc;
    }

    /**
     * Returns a pretty-printed textual representation of the node.
     *
     * @param node the node whose textual representation is sought
     * @return a pretty-printed representation
     */
    public String toString(Node node) {
      checkNotNull(node);
      lazyInitSer();
      final StringWriter writer = new StringWriter();
      final LSOutput output = impl.createLSOutput();
      output.setCharacterStream(writer);
      try {
        ser.write(node, output);
      } catch (LSException e) {
        /* I don’t think it is possible to not be able to serialize a node to a string. */
        throw new VerifyException("Unable to serialize the provided node.", e);
      }
      /*
       * See <a href="https://bugs.openjdk.java.net/browse/JDK-7150637">7150637</a> and <a
       * href="https://bugs.openjdk.java.net/browse/JDK-8054115">8054115 - LSSerializer remove a
       * '\n' following the xml declaration</a>. I filed bug
       * https://bugs.openjdk.java.net/browse/JDK-8249867 in July 2020.
       *
       * I got an email on the 10th of March, 2021 about JDK-8249867/Incident Report 9153520,
       * stating that the incident has been fixed at https://jdk.java.net/17/. The bug still happens
       * on my computer running openjdk 17-ea 2021-09-14; OpenJDK Runtime Environment (build
       * 17-ea+19-Debian-1); OpenJDK 64-Bit Server VM (build 17-ea+19-Debian-1, mixed mode,
       * sharing). I have not checked with a more recent JDK.
       */
      return writer.toString();
    }
  }

  /**
   * <p>
   * Helper for creating schemas and validating documents.
   * </p>
   * <p>
   * Instances of this class fail fast (throwing an exception) if encountering a warning, an error
   * or a fatalError upon reading a schema or validating a document.
   * </p>
   */
  public static class SchemaHelper {
    private static final class LoggingOrThrowingErrorHandler implements ErrorHandler {
      @Override
      public void warning(SAXParseException exception) {
        LOGGER.debug("Warning while processing.", exception);
      }

      @Override
      public void fatalError(SAXParseException exception) throws SAXParseException {
        throw exception;
      }

      @Override
      public void error(SAXParseException exception) throws SAXParseException {
        throw exception;
      }
    }

    private static final class ThrowingErrorHandler implements ErrorHandler {
      @Override
      public void warning(SAXParseException exception) throws SAXParseException {
        throw exception;
      }

      @Override
      public void fatalError(SAXParseException exception) throws SAXParseException {
        throw exception;
      }

      @Override
      public void error(SAXParseException exception) throws SAXParseException {
        throw exception;
      }
    }

    static final ErrorHandler LOGGING_OR_THROWING_ERROR_HANDLER =
        new LoggingOrThrowingErrorHandler();
    private static final ErrorHandler THROWING_ERROR_HANDLER = new ThrowingErrorHandler();
    private final SchemaFactory factory;
    private Schema schema;

    private SchemaHelper(SchemaFactory tf) {
      this.factory = checkNotNull(tf);
      schema = null;
    }

    /**
     * Produces the schema corresponding to the given source, or throws.
     *
     * @param schemaSource the source
     * @return the corresponding schema
     * @throws XmlException iff an error is produced while parsing the schema.
     */
    public Schema asSchema(Source schemaSource) throws XmlException {
      final Schema asSchema;
      try {
        asSchema = factory.newSchema(schemaSource);
      } catch (SAXException e) {
        throw new XmlException("While parsing schema.", e);
      }
      return asSchema;
    }

    /**
     * Sets the schema that this validator will use when validating documents.
     *
     * @param schemaSource the source to use for reading the schema.
     * @return this instance.
     * @throws XmlException iff the provided schema cannot be interpreted.
     */
    public SchemaHelper setSchema(Source schemaSource) throws XmlException {
      this.schema = asSchema(schemaSource);
      return this;
    }

    /**
     * <p>
     * Throws an exception iff the provided document is invalid.
     * </p>
     * <p>
     * The schema must have been set previously with {@link #setSchema(Source)}.
     * </p>
     *
     * @param document the document to validate.
     * @throws VerifyException iff the document is invalid, equivalently, iff a warning, error or
     *         fatalError is encountered while validating the provided document
     * @throws XmlException if the Source is an XML artifact that the implementation cannot validate
     *         (for example, a processing instruction)
     * @throws IOException if the validator is processing a javax.xml.transform.sax.SAXSource and
     *         the underlying org.xml.sax.XMLReader throws an IOException.
     */
    public void verifyValid(Source document) throws VerifyException, XmlException, IOException {
      checkState(schema != null, "Schema not set.");
      final javax.xml.validation.Validator validator = schema.newValidator();
      try {
        validator.validate(document);
      } catch (IllegalArgumentException e) {
        throw new XmlException(e);
      } catch (SAXException e) {
        throw new VerifyException(e);
      }
    }
  }
}
