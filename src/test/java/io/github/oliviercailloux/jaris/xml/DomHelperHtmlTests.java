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

class DomHelperHtmlTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperHtmlTests.class);
  private Document document;
  private Element html;
  private Element head;

  private void initDoc() {
    document = DomHelper.domHelper().html();
    html = document.getDocumentElement();
    html.setAttribute("xml:lang", "en");
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
}
