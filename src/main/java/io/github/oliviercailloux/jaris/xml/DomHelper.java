package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.RandomAccess;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class that helps with DOM manipulation, in particular, serialization and deserialization, and
 * with static methods for manipulating lists of nodes.
 */
public class DomHelper {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelper.class);

  /**
   * The <a href="https://infra.spec.whatwg.org/#html-namespace">HTML namespace</a> URI, defined as
   * {@code http://www.w3.org/1999/xhtml}.
   */
  public static final URI HTML_NS_URI = URI.create("http://www.w3.org/1999/xhtml");

  /**
   * Initializes and returns the DOM helper service.
   * <p>
   * This initializes the {@code DOMImplementationRegistry}, as described in
   * {@link DOMImplementationRegistry#newInstance()}.
   * </p>
   *
   * @return a DOM helper instance
   * @throws XmlException If the {@link DOMImplementationRegistry} initialization fails or it finds
   *         no implementation providing the LS feature or no implementation providing the XML
   *         feature.
   */
  public static DomHelper domHelper() throws XmlException {
    final DOMImplementationRegistry registry;
    try {
      registry = DOMImplementationRegistry.newInstance();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | ClassCastException e) {
      throw new XmlException(e);
    }
    final DOMImplementationLS implLs = (DOMImplementationLS) registry.getDOMImplementation("LS");
    if (implLs == null) {
      throw new XmlException(String.format(
          "Registry '%s' did not yield any DOM implementation providing the LS feature.",
          registry.toString()));
    }
    final DOMImplementation implXml = registry.getDOMImplementation("XML");
    if (implXml == null) {
      throw new XmlException(String.format(
          "Registry '%s' did not yield any DOM implementation providing the XML feature.",
          registry.toString()));
    }
    return new DomHelper(implLs, implXml);
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
    return String.format("Node type %s, local %s, namespace %s, value %s, name %s",
        node.getNodeType(), node.getLocalName(), node.getNamespaceURI(), node.getNodeValue(),
        node.getNodeName());
  }

  private static class ThrowingDomErrorHandler implements DOMErrorHandler {
    @Override
    public boolean handleError(DOMError error) {
      return false;
    }
  }

  private static final DomHelper.ThrowingDomErrorHandler THROWING_DOM_ERROR_HANDLER =
      new ThrowingDomErrorHandler();
  private final DOMImplementationLS implLs;
  private final DOMImplementation implXml;
  private LSSerializer ser;

  private LSParser deser;

  private DomHelper(DOMImplementationLS implLs, DOMImplementation implXml) {
    this.implLs = checkNotNull(implLs);
    this.implXml = checkNotNull(implXml);
    ser = null;
    deser = null;
  }

  /**
   * Creates a new HTML DOM Document, containing only the HTML document element.
   *
   * @return a new {@code Document} object with a document element having namespace
   *         {@link #HTML_NS_URI} and name “{@code html}”.
   */
  public Document html() {
    return createDocument(new QName(HTML_NS_URI.toString(), "html"));
  }

  /**
   * Creates a DOM Document with the specified document element.
   *
   * @param namespaceUri The namespace URI of the document element to create
   * @param qualifiedName The qualified name of the document element to be created
   * @return A new {@code Document} object with its document element
   * @exception DOMException INVALID_CHARACTER_ERR: Raised if the specified qualified name is not an
   *            XML name according to [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML
   *            1.0</a>]. <br>
   *            NAMESPACE_ERR: Raised if the {@code qualifiedName} is malformed, or if the
   *            {@code qualifiedName} has a prefix that is “{@code xml}” and the
   *            {@code namespaceUri} is different from {@link XMLConstants#XML_NS_URI}.
   */
  public Document createDocument(QName name) {
    String namespaceUri = name.getNamespaceURI();
    String prefix = name.getPrefix();
    String localPart = name.getLocalPart();
    String qualifiedName = prefix.isEmpty() ? localPart : prefix + ":" + localPart;
    final Document doc = implXml.createDocument(namespaceUri, qualifiedName, null);
    verify(Iterables.getOnlyElement(toElements(doc.getChildNodes())).getTagName()
        .equals(qualifiedName));
    return doc;
  }

  public static boolean hasAttribute(Element element, XmlName name) {
    return element.hasAttributeNS(name.namespace().map(URI::toString).orElse(null),
        name.localName());
  }

  public static String getAttribute(Element element, XmlName name) {
    checkArgument(hasAttribute(element, name));
    return element.getAttributeNS(name.namespace().map(URI::toString).orElse(null),
        name.localName());
  }

  LSInput toLsInput(StreamSource document) {
    final LSInput input = implLs.createLSInput();

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
    ser = implLs.createLSSerializer();
    ser.getDomConfig().setParameter("error-handler", THROWING_DOM_ERROR_HANDLER);
    /* Not supported by the default implementation. */
    // ser.getDomConfig().setParameter("ignore-unknown-character-denormalizations", true);
    ser.getDomConfig().setParameter("format-pretty-print", true);
    /* See https://bugs.openjdk.org/browse/JDK-8259502 */
    ser.getDomConfig().setParameter("http://www.oracle.com/xml/jaxp/properties/isStandalone", true);
  }

  private void lazyInitDeser() {
    if (deser != null) {
      return;
    }
    try {
      deser = implLs.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    } catch (DOMException e) {
      throw new VerifyException("Implementation does not support synchronous mode.", e);
    }
    deser.getDomConfig().setParameter("error-handler", THROWING_DOM_ERROR_HANDLER);
  }

  /**
   * Retrieves the content of the given input as a document.
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
   * Retrieves the content of the given input as a document.
   *
   * @param input the content
   * @return a document
   * @throws XmlException iff loading the XML document failed.
   */
  public Document asDocument(ByteSource input) throws XmlException, IOException {
    lazyInitDeser();
    final Document doc;
    final LSInput lsInput = implLs.createLSInput();
    try (InputStream stream = input.openStream()) {
      lsInput.setByteStream(stream);
      doc = deser.parse(lsInput);
    } catch (LSException e) {
      throw new XmlException("Unable to parse the provided document.", e);
    }

    return doc;
  }

  /**
   * Retrieves the content of the given input as a document.
   *
   * @param input the content
   * @return a document
   * @throws XmlException iff loading the XML document failed.
   */
  public Document asDocument(CharSource input) throws XmlException, IOException {
    lazyInitDeser();
    final Document doc;
    final LSInput lsInput = implLs.createLSInput();
    try (Reader r = input.openStream()) {
      lsInput.setCharacterStream(r);
      doc = deser.parse(lsInput);
    } catch (LSException e) {
      throw new XmlException("Unable to parse the provided document.", e);
    }

    return doc;
  }

  @SuppressWarnings("unused")
  private DOMImplementation createDocumentImplementationUsingDocumentBuilderFactory()
      throws ParserConfigurationException {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newNSInstance();
    final DocumentBuilder builder = dbf.newDocumentBuilder();
    return builder.getDOMImplementation();
  }

  /**
   * I favor the DOM LS parser to the DocumentBuilder: DOM LS is a W3C standard (see
   * <a href="https://stackoverflow.com/a/38153986">SO</a>) and I need an LS serializer anyway.
   */
  @SuppressWarnings("unused")
  private Document asDocumentUsingBuilder(StreamSource input)
      throws ParserConfigurationException, SAXException, IOException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newNSInstance();
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
    final LSOutput output = implLs.createLSOutput();
    output.setEncoding(StandardCharsets.UTF_8.name());
    output.setCharacterStream(writer);
    boolean res;
    try {
      res = ser.write(node, output);
    } catch (LSException e) {
      /* I don’t think it is possible to not be able to serialize a node to a string. */
      throw new VerifyException("Unable to serialize the provided node.", e);
    }
    verify(res, "Write failed");
    return writer.toString();
  }

  @SuppressWarnings("unused")
  private Document cloneDocument(Document doc) {
    // Thanks to https://stackoverflow.com/questions/5226852/cloning-dom-document-object .
    // As a DOMSource is not a StreamSource, I ignore how to (and perhaps cannot) use the LS API.
    // So, this should probably be moved to XmlTransformer.
    DOMResult result = new DOMResult();
    XmlTransformer.usingFoundFactory().usingEmptyStylesheet().transform(new DOMSource(doc), result);
    Document d = (Document) result.getNode();
    return d;
  }
}
