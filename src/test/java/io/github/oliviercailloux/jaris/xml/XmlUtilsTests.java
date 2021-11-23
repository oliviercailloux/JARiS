package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import io.github.oliviercailloux.jaris.xml.XmlUtils.DomHelper;
import io.github.oliviercailloux.jaris.xml.XmlUtils.XmlException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XmlUtilsTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtilsTests.class);
  private Document document;
  private Element html;
  private Element head;

  private void initDoc() throws ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    document = builder.newDocument();

    html = document.createElementNS(XmlUtils.XHTML_NS_URI.toString(), "html");
    html.setAttribute("lang", "en");
    document.appendChild(html);
    head = document.createElementNS(XmlUtils.XHTML_NS_URI.toString(), "head");
    html.appendChild(head);
    final Element meta = document.createElementNS(XmlUtils.XHTML_NS_URI.toString(), "meta");
    meta.setAttribute("http-equiv", "Content-type");
    meta.setAttribute("content", "text/html; charset=utf-8");
    head.appendChild(meta);
    final Element body = document.createElementNS(XmlUtils.XHTML_NS_URI.toString(), "body");
    html.appendChild(body);
  }

  @Test
  void testToStringDoc() throws Exception {
    initDoc();
    final String expected =
        Files.readString(Path.of(getClass().getResource("simple.html").toURI()));
    assertEquals(expected, XmlUtils.loadAndSave().toString(document));
  }

  @Test
  void testToStringNode() throws Exception {
    initDoc();
    final String expected =
        Files.readString(Path.of(getClass().getResource("partial.xml").toURI()));
    assertEquals(expected, XmlUtils.loadAndSave().toString(head));
  }

  @Test
  void testToElements() throws Exception {
    initDoc();
    assertEquals(ImmutableList.of(html), DomHelper.toElements(document.getChildNodes()));
  }

  @Test
  void testTransformSimple() throws Exception {
    final StreamSource style =
        new StreamSource(XmlUtilsTests.class.getResource("short.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlUtilsTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlUtilsTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected, XmlUtils.pedanticTransformer().transform(input, style));
  }

  @Test
  void testTransformSimpleDocBook() throws Exception {
    final StreamSource docBook =
        new StreamSource(XmlUtilsTests.class.getResource("docbook simple article.xml").toString());
    final StreamSource myStyle =
        /*
         * Much faster (obtains transformer from stylesheet in 4 sec instead of 17 sec), but depends
         * on what is installed locally.
         */
        // new
        // StreamSource(Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl")
        // .toUri().toString());
        new StreamSource("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

    /* This is too complex for JDK embedded transformer (Apache Xalan). */
    // XmlUtils.transformer().transform(docBook, myStyle);
    final String transformed =
        XmlUtils.transformer(new TransformerFactoryImpl()).transform(docBook, myStyle);
    LOGGER.debug("Transformed docbook howto: {}.", transformed);
    assertTrue(transformed
        .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
  }

  @Test
  void testTransformComplexDocBook() throws Exception {
    final StreamSource docBook =
        new StreamSource(XmlUtilsTests.class.getResource("docbook howto.xml").toString());
    final StreamSource myStyle =
        new StreamSource(XmlUtilsTests.class.getResource("mystyle.xsl").toString());

    final String transformed =
        XmlUtils.transformer(new TransformerFactoryImpl()).transform(docBook, myStyle);
    LOGGER.debug("Transformed docbook howto: {}.", transformed);
    assertTrue(transformed
        .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
  }

  @Test
  void testTransformInvalidXsl() throws Exception {
    final StreamSource style =
        new StreamSource(XmlUtilsTests.class.getResource("short invalid.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlUtilsTests.class.getResource("short.xml").toString());
    assertThrows(XmlException.class, () -> XmlUtils.transformer().transform(input, style));
  }

  @Test
  void testTransformInvalidXml() throws Exception {
    final StreamSource style =
        new StreamSource(XmlUtilsTests.class.getResource("short.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlUtilsTests.class.getResource("short invalid.xml").toString());
    assertThrows(XmlException.class, () -> XmlUtils.transformer().transform(input, style));
  }

  @Test
  void testTransformMessaging() throws Exception {
    final StreamSource style =
        new StreamSource(XmlUtilsTests.class.getResource("short messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlUtilsTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlUtilsTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected, XmlUtils.transformer().transform(input, style));

    assertThrows(XmlException.class, () -> XmlUtils.pedanticTransformer().transform(input, style));
  }

  @Test
  void testTransformMessagingTerminate() throws Exception {
    final StreamSource style = new StreamSource(
        XmlUtilsTests.class.getResource("short messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlUtilsTests.class.getResource("short.xml").toString());
    assertThrows(XmlException.class, () -> XmlUtils.transformer().transform(input, style));
  }
}
