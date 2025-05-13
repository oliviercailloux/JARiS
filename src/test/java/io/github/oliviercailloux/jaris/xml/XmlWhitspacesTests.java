package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.xml.XmlTransformerFactory.OutputProperties;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlWhitspacesTests {
  @Test
  public void testKeepsWhitespaces() throws Exception {
    Document input = DomHelper.domHelper().asDocument(charSource("Article ns/Empty.xml"));
    Element docE = input.getDocumentElement();
    assertEquals("Article", docE.getNodeName());
    ImmutableList<Node> children = DomHelper.toList(docE.getChildNodes());
    assertEquals(3, children.size());
    assertEquals(Node.TEXT_NODE, children.get(0).getNodeType());
    assertEquals("\n    ", children.get(0).getNodeValue());
    assertEquals(Node.ELEMENT_NODE, children.get(1).getNodeType());
    assertEquals("k:Empty", children.get(1).getNodeName());
    assertEquals(Node.TEXT_NODE, children.get(2).getNodeType());
    assertEquals("\n", children.get(2).getNodeValue());
  }

  @Test
  public void testWhitespacesPreserved() throws Exception {
    CharSource input = charSource("Article ns/Empty.xml");
    XmlTransformer t = XmlTransformerFactory.usingFactory(KnownFactory.JDK.factory())
        .usingEmptyStylesheet(OutputProperties.noIndent());
    String noIndent = t.charsToChars(input);
    // String expected = input.read().replaceAll("\n", "").replaceAll(" ", "");
    String expectedExtended = input.read().replaceFirst("\n", "");
    assertEquals("\n",
        expectedExtended.subSequence(expectedExtended.length() - 1, expectedExtended.length()));
    String expected = expectedExtended.substring(0, expectedExtended.length() - 1);
    assertEquals(expected, noIndent);
    Document output = t.charsToDom(input);
    Element docE = output.getDocumentElement();
    assertEquals("Article", docE.getNodeName());
    ImmutableList<Node> children = DomHelper.toList(docE.getChildNodes());
    assertEquals(3, children.size());
    assertEquals(Node.TEXT_NODE, children.get(0).getNodeType());
    assertEquals("\n    ", children.get(0).getNodeValue());
  }

  // @Test TODO
  public void testRemovesWhitespaces() throws Exception {
    CharSource input = charSource("Article ns/Empty.xml");
    XmlTransformer t = XmlTransformerFactory.usingFactory(KnownFactory.JDK.factory())
        .usingEmptyStylesheet(OutputProperties.noIndent());
    String noIndent = t.charsToChars(input);
    String expected = input.read().replaceAll("\n", "").replaceAll(" ", "");
    assertEquals(expected, noIndent);
    Document output = t.charsToDom(input);
    Element docE = output.getDocumentElement();
    assertEquals("Article", docE.getNodeName());
    ImmutableList<Node> children = DomHelper.toList(docE.getChildNodes());
    assertEquals(3, children.size());
    assertEquals(Node.TEXT_NODE, children.get(0).getNodeType());
    assertEquals("\n    ", children.get(0).getNodeValue());
  }
}
