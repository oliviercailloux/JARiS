package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.collect.ImmutableList;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DomHelperArticleTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperArticleTests.class);

  static final String ARTICLE_NS = "https://example.com/article";
  static final String ARTICLE_NS_K = "https://example.com/article/k";

  @Test
  void testCreateNamespace() throws Exception {
    DomHelper h = DomHelper.domHelper();
    Document doc = h.createDocument(new QName(ARTICLE_NS, "Article"));
    doc.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:k",
        ARTICLE_NS_K);
    Element title = doc.createElementNS(ARTICLE_NS_K, "k:Empty");
    doc.getDocumentElement().appendChild(title);
    final String expected =
        Files.readString(Path.of(getClass().getResource("very short namespace.xml").toURI()));
    assertEquals(expected, h.toString(doc));
  }

  @Test
  void testCreateNamespaceWithoutXmlnsNs() throws Exception {
    DomHelper h = DomHelper.domHelper();
    Document doc = h.createDocument(new QName(ARTICLE_NS, "Article"));
    doc.getDocumentElement().setAttribute("xmlns:k", ARTICLE_NS_K);
    Element title = doc.createElementNS(ARTICLE_NS_K, "k:Empty");
    doc.getDocumentElement().appendChild(title);
    final String expected =
        Files.readString(Path.of(getClass().getResource("very short namespace.xml").toURI()));
    assertNotEquals(expected, h.toString(doc));
  }

  @Test
  void testNamespace() throws Exception {
    final String source =
        Files.readString(Path.of(getClass().getResource("short namespace.xml").toURI()));
    final Document articleDoc =
        DomHelper.domHelper().asDocument(new StreamSource(new StringReader(source)));
    final Element root = articleDoc.getDocumentElement();
    assertEquals("Article", root.getTagName());
    assertEquals(ARTICLE_NS, root.getNamespaceURI());
    ImmutableList<Node> children = DomHelper.toList(root.getChildNodes());
    Node textNode = children.get(0);
    assertEquals("\n    ", textNode.getNodeValue());
    LOGGER.info(DomHelper.toDebugString(textNode));
    Element title = (Element) children.get(1);
    assertEquals("k:Title", title.getTagName());
    assertEquals(ARTICLE_NS_K, title.getNamespaceURI());

    Element newElement = articleDoc.createElementNS(ARTICLE_NS_K, "k:Empty");
    root.insertBefore(newElement, title.getNextSibling());
    final String expected =
        Files.readString(Path.of(getClass().getResource("short namespace expanded.xml").toURI()));
    String expanded = DomHelper.domHelper().toString(articleDoc);
    assertEquals(expected, expanded);
  }
}
