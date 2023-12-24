package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;

class DomHelperTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperTests.class);
  private Document document;
  private Element html;
  private Element head;

  private void initDoc() throws ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    document = builder.newDocument();

    html = document.createElementNS(DomHelper.HTML_NS_URI.toString(), "html");
    html.setAttribute("xml:lang", "en");
    document.appendChild(html);
    head = document.createElementNS(DomHelper.HTML_NS_URI.toString(), "head");
    html.appendChild(head);
    final Element meta = document.createElementNS(DomHelper.HTML_NS_URI.toString(), "meta");
    meta.setAttribute("http-equiv", "Content-type");
    meta.setAttribute("content", "text/html; charset=utf-8");
    head.appendChild(meta);
    final Element title = document.createElementNS(DomHelper.HTML_NS_URI.toString(), "title");
    head.appendChild(title);
    title.appendChild(document.createTextNode("Title"));
    final Element body = document.createElementNS(DomHelper.HTML_NS_URI.toString(), "body");
    html.appendChild(body);
  }

  @Test
  void testToStringDoc() throws Exception {
    initDoc();
    final String expected =
        Files.readString(Path.of(getClass().getResource("simple.xhtml").toURI()));
    assertEquals(expected, DomHelper.domHelper().toString(document));
  }

  @Test
  void testToStringNode() throws Exception {
    initDoc();
    final String expected =
        Files.readString(Path.of(getClass().getResource("partial.xml").toURI()));
    assertEquals(expected, DomHelper.domHelper().toString(head));
  }

  @Test
  void testToElements() throws Exception {
    initDoc();
    assertEquals(ImmutableList.of(html), DomHelper.toElements(document.getChildNodes()));
  }

  @Test
  void testNamespace() throws Exception {
    final String source =
        Files.readString(Path.of(getClass().getResource("short namespace.xml").toURI()));
    final Document articleDoc =
        DomHelper.domHelper().asDocument(new StreamSource(new StringReader(source)));
    final Element root = articleDoc.getDocumentElement();
    assertEquals("Article", root.getTagName());
    assertEquals("https://example.com/article", root.getNamespaceURI());
    ImmutableList<Node> children = DomHelper.toList(root.getChildNodes());
    Node textNode = children.get(0);
    assertEquals("\n    ", textNode.getNodeValue());
    LOGGER.info(DomHelper.toDebugString(textNode));
    Element title = (Element) children.get(1);
    assertEquals("k:Title", title.getTagName());
    String kNs = "https://example.com/article/k";
    assertEquals(kNs, title.getNamespaceURI());

    Element newElement = articleDoc.createElementNS(kNs, "k:Empty");
    root.insertBefore(newElement, title.getNextSibling());
    final String expected =
        Files.readString(Path.of(getClass().getResource("short namespace expanded.xml").toURI()));
    String expanded = DomHelper.domHelper().toString(articleDoc);
    assertEquals(expected, expanded);
  }
}
