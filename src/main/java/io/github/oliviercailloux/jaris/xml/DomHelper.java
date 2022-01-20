package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.AbstractList;
import java.util.RandomAccess;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class that helps with DOM manipulation, in particular, serialization and deserialization, and
 * with static methods for manipulating lists of nodes.
 */
public class DomHelper {
  /**
   * The XHTML namespace URI, defined to be {@code http://www.w3.org/1999/xhtml}.
   */
  public static final URI XHTML_NS_URI = URI.create("http://www.w3.org/1999/xhtml");
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelper.class);

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
  public static DomHelper domHelper() throws XmlException {
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
    return String.format("Node type %s, Local %s, NS %s, Value %s, Name %s.", node.getNodeType(),
        node.getLocalName(), node.getNamespaceURI(), node.getNodeValue(), node.getNodeName());
  }

  private static class ThrowingDomErrorHandler implements DOMErrorHandler {
    @Override
    public boolean handleError(DOMError error) {
      return false;
    }
  }

  private static final DomHelper.ThrowingDomErrorHandler THROWING_DOM_ERROR_HANDLER =
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
     * href="https://bugs.openjdk.java.net/browse/JDK-8054115">8054115 - LSSerializer remove a '\n'
     * following the xml declaration</a>. I filed bug
     * https://bugs.openjdk.java.net/browse/JDK-8249867 in July 2020.
     *
     * I got an email on the 10th of March, 2021 about JDK-8249867/Incident Report 9153520, stating
     * that the incident has been fixed at https://jdk.java.net/17/. The bug still happens on my
     * computer running openjdk 17-ea 2021-09-14; OpenJDK Runtime Environment (build
     * 17-ea+19-Debian-1); OpenJDK 64-Bit Server VM (build 17-ea+19-Debian-1, mixed mode, sharing).
     * I have not checked with a more recent JDK.
     */
    return writer.toString();
  }
}
