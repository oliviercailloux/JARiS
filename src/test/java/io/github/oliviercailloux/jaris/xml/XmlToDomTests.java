package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlToDomTests {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlToDomTests.class);

  private static final String ARTICLE_NS = "https://example.com/article";
  private static final String ARTICLE_NS_K = "https://example.com/article/k";

  @ParameterizedTest
  @EnumSource
  void testModify(KnownFactory factory) throws Exception {
    final CharSource source = charSource("Article ns/Title two authors.xml");
    Document docCopy = XmlTransformerFactory.usingFactory(factory.factory()).usingEmptyStylesheet()
        .charsToDom(source);
    final Element root = docCopy.getDocumentElement();
    assertEquals("Article", root.getTagName());
    assertEquals(ARTICLE_NS, root.getNamespaceURI());
    ImmutableList<Node> children = DomHelper.toList(root.getChildNodes());
    Node textNode = children.get(0);
    assertEquals("\n    ", textNode.getNodeValue());
    LOGGER.info(DomHelper.toDebugString(textNode));
    Element title = (Element) children.get(1);
    assertEquals(ARTICLE_NS_K, title.getNamespaceURI());
    assertEquals("k:Title", title.getTagName());
    Element newElement = docCopy.createElementNS(ARTICLE_NS_K, "k:Empty");
    root.insertBefore(newElement, title.getNextSibling());

    final String expected = charSource("Article ns/Title empty two authors.xml").read();
    assertEquals(expected, DomHelper.domHelper().toString(docCopy));
  }

  @ParameterizedTest
  @EnumSource
  void testCreateNamespace(KnownFactory factory) throws Exception {
    DomHelper h = DomHelper.domHelper();

    Document doc = h.createDocument(new QName(ARTICLE_NS, "Article"));
    doc.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:k",
        ARTICLE_NS_K);
    Element title = doc.createElementNS(ARTICLE_NS_K, "k:Empty");
    doc.getDocumentElement().appendChild(title);
    final CharSource start =
        charSource(Path.of(getClass().getResource("Article ns/Empty.xml").toURI()));
    String serialized = h.toString(doc);
    assertEquals(start.read(), serialized);

    Document docCopy = XmlTransformerFactory.usingFactory(factory.factory()).usingEmptyStylesheet()
        .charsToDom(CharSource.wrap(serialized));
    assertEquals(start.read(), h.toString(docCopy));

    final Element root = docCopy.getDocumentElement();
    assertEquals("Article", root.getTagName());
    assertEquals(ARTICLE_NS, root.getNamespaceURI());
    ImmutableList<Node> children = DomHelper.toList(root.getChildNodes());
    Node textNode = children.get(0);
    assertEquals("\n    ", textNode.getNodeValue());
    LOGGER.info(DomHelper.toDebugString(textNode));
    Element titleCopy = (Element) children.get(1);
    assertEquals(ARTICLE_NS_K, titleCopy.getNamespaceURI());
    assertEquals("k:Empty", titleCopy.getTagName());
    Element newElement = docCopy.createElementNS(ARTICLE_NS_K, "k:Empty");
    root.insertBefore(newElement, titleCopy.getNextSibling());

    final String expected =
        Files.readString(Path.of(getClass().getResource("Article ns/Empties.xml").toURI()));
    assertEquals(expected, h.toString(docCopy));
  }
}
