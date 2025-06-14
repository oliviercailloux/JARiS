package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * How to produce an (<a href="https://www.w3.org/TR/xhtml11/conformance.html">arguably</a>)
 * conforming HTML in XML syntax. It is recommended
 * <a href="https://html.spec.whatwg.org/multipage/xhtml.html#the-xhtml-syntax">not</a> to do this!
 */
class DomHelperXhtmlTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperXhtmlTests.class);
  /**
   * The <a href="https://infra.spec.whatwg.org/#html-namespace">HTML namespace</a> URI, defined as
   * {@code http://www.w3.org/1999/xhtml}.
   */
  public static final URI HTML_NS_URI = URI.create("http://www.w3.org/1999/xhtml");
  private Document document;
  private Element html;
  private Element head;

  @Test
  void testHeader() throws Exception {
    DomHelper h = DomHelper.domHelper();
    Document doc = h.html();
    h.writeXmlDeclaration(false);
    assertEquals("<html/>\n", h.toString(doc));
  }

  private void initDoc() {
    document = DomHelper.domHelper().createDocument(XmlName.expandedName(HTML_NS_URI, "html").toQName());
    html = document.getDocumentElement();
    html.setAttribute("lang", "en");
    head = document.createElementNS(HTML_NS_URI.toString(), "head");
    html.appendChild(head);
    final Element meta = document.createElementNS(HTML_NS_URI.toString(), "meta");
    meta.setAttribute("http-equiv", "Content-type");
    meta.setAttribute("content", "text/html; charset=utf-8");
    head.appendChild(meta);
    final Element title = document.createElementNS(HTML_NS_URI.toString(), "title");
    head.appendChild(title);
    title.appendChild(document.createTextNode("Title"));
    final Element body = document.createElementNS(HTML_NS_URI.toString(), "body");
    html.appendChild(body);
  }

  @Test
  void testToStringDoc() throws Exception {
    initDoc();
    final String expected =
        Files.readString(Path.of(getClass().getResource("Xhtml/Simple.xhtml").toURI()));
    assertEquals(expected, DomHelper.domHelper().toString(document));
  }

  @Test
  void testToStringNode() throws Exception {
    initDoc();
    final String expected =
        Files.readString(Path.of(getClass().getResource("Xhtml/Partial.xml").toURI()));
    assertEquals(expected, DomHelper.domHelper().toString(head));
  }

  @Test
  void testToElements() throws Exception {
    initDoc();
    assertEquals(ImmutableList.of(html), DomHelper.toElements(document.getChildNodes()));
  }
}
