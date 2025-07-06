package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.xml.XmlTransformerFactory.OutputProperties;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

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
  public void testKeepsWhitespacesDespiteAskingPolitely() throws Exception {
    final DOMImplementationLS domImpl =
        (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
    LSParser deser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    deser.getDomConfig().setParameter("validate", true);
    assertTrue(deser.getDomConfig().canSetParameter("normalize-characters", false));
    assertFalse(deser.getDomConfig().canSetParameter("normalize-characters", true));
    /*
     * From source code in XMLDocumentFragmentScannerImpl, it seems that whitespace is recognized
     * only when there is a DTD grammar util, which is probably the reason that this has no effect
     * in our case. Perhaps it is related to not normalizing characters, but I doubt it.
     */
    deser.getDomConfig().setParameter("element-content-whitespace", false);
    final LSInput input = domImpl.createLSInput();
    CharSource content = charSource("Article ns/Empty.xml");
    input.setCharacterStream(new StringReader(content.read()));
    Document doc = deser.parse(input);
    doc.getDomConfig().setParameter("validate", true);
    assertFalse(doc.getDomConfig().canSetParameter("normalize-characters", true));
    doc.getDomConfig().setParameter("element-content-whitespace", true);
    doc.normalize();
    doc.normalizeDocument();
    Element docE = doc.getDocumentElement();
    assertEquals("Article", docE.getNodeName());
    ImmutableList<Node> children = DomHelper.toList(docE.getChildNodes());
    assertEquals(3, children.size());
    assertEquals(Node.TEXT_NODE, children.get(0).getNodeType());
    assertEquals("\n    ", children.get(0).getNodeValue());
    /*
     * https://www.w3.org/TR/2004/REC-xml-infoset-20040204/#infoitem.character, result probably due
     * to missing declaration for the containing element.
     */
    assertEquals(false, ((Text) children.get(0)).isElementContentWhitespace());
  }

  @ParameterizedTest
  @EnumSource
  public void testWhitespacesPreserved(KnownFactory factory) throws Exception {
    CharSource input = charSource("Article ns/Empty.xml");
    XmlTransformer t = XmlTransformerFactory.usingFactory(factory.factory())
        .usingEmptyStylesheet(OutputProperties.noIndent());
    String noIndent = t.charsToChars(input);
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

  @ParameterizedTest
  @EnumSource
  public void testRemovesWhitespaces(KnownFactory factory) throws Exception {
    CharSource input = charSource("Article ns/Empty.xml");
    XmlTransformer t = XmlTransformerFactory.usingFactory(factory.factory()).usingStylesheet(
        XmlTransformerFactory.STRIP_WHITESPACE_STYLESHEET, ImmutableMap.of(),
        OutputProperties.noIndent());
    String noIndent = t.charsToChars(input);
    String expected = input.read().replaceAll("\n", "").replaceAll("    ", "");
    assertEquals(expected, noIndent);
    Document output = t.charsToDom(input);
    Element docE = output.getDocumentElement();
    assertEquals("Article", docE.getNodeName());
    ImmutableList<Node> children = DomHelper.toList(docE.getChildNodes());
    assertEquals(1, children.size());
    Node child = Iterables.getOnlyElement(children);
    assertEquals(Node.ELEMENT_NODE, child.getNodeType());
    assertEquals("k:Empty", child.getNodeName());
  }

  @ParameterizedTest
  @EnumSource
  public void testRemovesWhitespacesComplex(KnownFactory factory) throws Exception {
    CharSource input = charSource("Complex.fo");
    XmlTransformer t = XmlTransformerFactory.usingFactory(factory.factory()).usingStylesheet(
        XmlTransformerFactory.STRIP_WHITESPACE_STYLESHEET, ImmutableMap.of(),
        OutputProperties.noIndent());
    String noIndent = t.charsToChars(input);
    Files.writeString(Path.of("out.xml"), noIndent);
    //TODO this contains blanks then Jirka.
    assertTrue(false);
    String expected = input.read().replaceAll("\n", "").replaceAll("    ", "");
    assertEquals(expected, noIndent);
    Document output = t.charsToDom(input);
    Element docE = output.getDocumentElement();
    assertEquals("Article", docE.getNodeName());
    ImmutableList<Node> children = DomHelper.toList(docE.getChildNodes());
    assertEquals(1, children.size());
    Node child = Iterables.getOnlyElement(children);
    assertEquals(Node.ELEMENT_NODE, child.getNodeType());
    assertEquals("k:Empty", child.getNodeName());
  }
}
