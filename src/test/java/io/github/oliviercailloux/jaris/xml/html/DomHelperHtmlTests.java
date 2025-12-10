package io.github.oliviercailloux.jaris.xml.html;

import static io.github.oliviercailloux.jaris.xml.Resourcer.byteSource;
import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.xml.DomHelper;
import io.github.oliviercailloux.jaris.xml.Resourcer;
import io.github.oliviercailloux.jaris.xml.XmlException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class DomHelperHtmlTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperHtmlTests.class);

  @Test
  public void testDomHelperFails() throws Exception {
    CharSource simple = charSource("Html/Simple.html");
    DomHelper h = DomHelper.domHelper();
    XmlException e = assertThrows(XmlException.class, () -> h.asDocument(simple));
    String message = Throwables.getRootCause(e).getMessage();
    assertTrue(message.contains("/meta"), message);
  }

  @Test
  public void testDocBuilderFails() throws Exception {
    ByteSource simple = byteSource("Html/Simple.html");
    final DocumentBuilder builderNs = DocumentBuilderFactory.newNSInstance().newDocumentBuilder();
    try (InputStream is = simple.openStream()) {
      SAXParseException e = assertThrows(SAXParseException.class, () -> builderNs.parse(is));
      String message = Throwables.getRootCause(e).getMessage();
      assertTrue(message.contains("/meta"), message);
    }
    final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    try (InputStream is = simple.openStream()) {
      SAXParseException e = assertThrows(SAXParseException.class, () -> builder.parse(is));
      String message = Throwables.getRootCause(e).getMessage();
      assertTrue(message.contains("/meta"), message);
    }
  }

  @Test
  public void testHtmlSimple() throws Exception {
    DomHelper h = DomHelper.usingBuilder(new HtmlDocumentBuilder());
    Document doc = h.asDocument(byteSource("Html/Simple.html"));
    String str = h.toString(doc);
    // Files.writeString(Path.of("Simple.xhtml"), h.toString(doc));
    String expected = Resourcer.charSource("Xhtml/Simple.xhtml").read();
    assertEquals(expected, str);
  }

  @Test
  public void testHtmlIso() throws Exception {
    DomHelper h = DomHelper.usingBuilder(new HtmlDocumentBuilder());
    // Should throw as the builder warns about LATIN-1 encoding. See related tests and issue.
    Document doc = h.asDocument(byteSource("Html/Encoding ISO.html"));
    String str = h.toString(doc);
    // Files.writeString(Path.of("Simple.xhtml"), h.toString(doc));
    String expected = Resourcer.charSource("Xhtml/Simple.xhtml").read().replaceAll("charset=utf-8",
        "charset=iso-8859-1");
    assertEquals(expected, str);
  }

  @Test
  public void testHtml() throws Exception {
    DomHelper h = DomHelper.usingBuilder(new HtmlDocumentBuilder());
    Document doc = h.asDocument(byteSource("Html/Html is not Xml.html"));
    String str = h.toString(doc);
    assertTrue(str.contains("<?xml"));
    assertFalse(str.contains("DOCTYPE"));
    assertTrue(str.contains("</p>"));
    assertTrue(str.contains("<br/>"));
  }

  @Test
  public void testNuDirectParse() throws Exception {
    ByteSource simple = byteSource("Html/Simple.html");
    HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
    assertFalse(builder.isValidating());
    assertTrue(builder.isNamespaceAware());
    Document doc;
    try (InputStream is = simple.openStream()) {
      doc = builder.parse(is);
    }
    assertNotNull(doc);
    ImmutableList<Node> nodes = DomHelper.toList(doc.getDocumentElement().getChildNodes());
    assertEquals(3, nodes.size());
    ImmutableList<Element> elements = nodes.stream().filter(Element.class::isInstance)
        .map(Element.class::cast).collect(ImmutableList.toImmutableList());
    assertEquals(2, elements.size());
    Element head = elements.get(0);
    Element body = elements.get(1);
    assertEquals("head", head.getNodeName());
    assertEquals("body", body.getNodeName());
    ImmutableList<Node> headNodes = DomHelper.toList(head.getChildNodes());
    assertEquals(5, headNodes.size());
    ImmutableList<Element> headElements = headNodes.stream().filter(Element.class::isInstance)
        .map(Element.class::cast).collect(ImmutableList.toImmutableList());
    assertEquals(2, headElements.size());
    Element meta = headElements.get(0);
    assertEquals("meta", meta.getNodeName());
    assertEquals("text/html; charset=utf-8", meta.getAttribute("content"));

    ImmutableList<Element> metas =
        DomHelper.toElements(doc.getDocumentElement().getElementsByTagName("meta"));
    assertEquals(1, metas.size());
    ImmutableList<Element> noNs =
        DomHelper.toElements(doc.getDocumentElement().getElementsByTagNameNS("", "meta"));
    assertEquals(0, noNs.size());
    ImmutableList<Element> metasNs = DomHelper.toElements(
        doc.getDocumentElement().getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "meta"));
    assertEquals(1, metasNs.size());
  }

  @Test
  public void testNuDirectParseWrite() throws Exception {
    ByteSource simple = byteSource("Html/Simple.html");
    HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
    Document doc;
    try (InputStream is = simple.openStream()) {
      doc = builder.parse(is);
    }
    String str = DomHelper.domHelper().toString(doc);
    String expected = Resourcer.charSource("Xhtml/Simple.xhtml").read();
    assertEquals(expected, str);
  }
}
