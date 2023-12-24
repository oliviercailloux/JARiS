package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.ImmutableList;

public class DomHelperArticleTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperArticleTests.class);
  
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
