package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.collect.ImmutableList;
import java.io.StringReader;
import java.net.URI;
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

public class DomHelperArticleNsTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperArticleNsTests.class);

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
    final String expected = Resourcer.charSource("Article ns/Empty.xml").read();
    assertEquals(expected, h.toString(doc));
  }

  @Test
  void testCreateNamespaceWithoutXmlnsNs() throws Exception {
    DomHelper h = DomHelper.domHelper();
    Document doc = h.createDocument(new QName(ARTICLE_NS, "Article"));
    doc.getDocumentElement().setAttribute("xmlns:k", ARTICLE_NS_K);
    Element title = doc.createElementNS(ARTICLE_NS_K, "k:Empty");
    doc.getDocumentElement().appendChild(title);
    final String expected = Resourcer.charSource("Article ns/Empty.xml").read();
    assertNotEquals(expected, h.toString(doc));
  }

  @Test
  void testNamespace() throws Exception {
    final String source =
    Resourcer.charSource("Article ns/Title two authors.xml").read();
    final Document articleDoc =
        DomHelper.domHelper().asDocument(new StreamSource(new StringReader(source)));
    final Element root = articleDoc.getDocumentElement();
    assertEquals("Article", root.getTagName());
    assertEquals(ARTICLE_NS, root.getNamespaceURI());
    assertEquals(XmlName.expandedName(URI.create(ARTICLE_NS), "Article"), DomHelper.xmlName(root));

    ImmutableList<Node> children = DomHelper.toList(root.getChildNodes());
    Node textNode = children.get(0);
    assertEquals("\n    ", textNode.getNodeValue());
    LOGGER.info(DomHelper.toDebugString(textNode));
    Element title = (Element) children.get(1);
    assertEquals("k:Title", title.getTagName());
    assertEquals(ARTICLE_NS_K, title.getNamespaceURI());
    assertEquals(XmlName.expandedName(URI.create(ARTICLE_NS_K), "Title"), DomHelper.xmlName(title));

    Element authors = (Element) children.get(3);
    assertEquals("Authors", authors.getTagName());
    assertEquals(ARTICLE_NS, authors.getNamespaceURI());
    assertEquals(XmlName.expandedName(URI.create(ARTICLE_NS), "Authors"),
        DomHelper.xmlName(authors));

    Element newElement = articleDoc.createElementNS(ARTICLE_NS_K, "k:Empty");
    root.insertBefore(newElement, title.getNextSibling());
    final String expected =
    Resourcer.charSource("Article ns/Title empty two authors.xml").read();
    String expanded = DomHelper.domHelper().toString(articleDoc);
    assertEquals(expected, expanded);
  }
}
