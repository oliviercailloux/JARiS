package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junitpioneer.jupiter.RestoreSystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class XmlTransformerTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerTests.class);

  private static final String XALAN_FACTORY = "org.apache.xalan.processor.TransformerFactoryImpl";
  private static final String ARTICLE_NS = "https://example.com/article";
  private static final String ARTICLE_NS_K = "https://example.com/article/k";

  @Test
  void testTransformSimple() throws Exception {
    final ByteSource style =
        Resources.asByteSource(XmlTransformerTests.class.getResource("short.xsl"));
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected,
        XmlTransformer.pedanticTransformer(TransformerFactory.newDefaultInstance())
            .usingStylesheet(style).transform(input));
  }

  @RestoreSystemProperties
  @Test
  void testDocBookStyle() throws Exception {
    final StreamSource myStyle =
        /*
         * Much faster (obtains transformer from stylesheet in 4 sec instead of 17 sec), but depends
         * on what is installed locally.
         */
        new StreamSource(Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl")
            .toUri().toString());
    // new StreamSource("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

    {
      /* This is too complex for pure JDK embedded transformer. */
      /*
       * This spits plenty on the console (bypassing the logger mechanism) before crashing.
       */
      final OutputCapturer capturer = OutputCapturer.capturer();
      capturer.capture();

      final XmlTransformer t = XmlTransformer.usingSystemDefaultFactory();
      assertEquals("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
          t.factory().getClass().getName());
      final XmlException xalanExc =
          assertThrows(XmlException.class, () -> t.usingStylesheet(myStyle));
      final String reason = xalanExc.getCause().getMessage();
      // if (xalanIsInClassPath) {
      assertTrue(reason.contains("JAXP0801003"), reason);
      // } else {
      // assertTrue(reason.contains("org.apache.xalan.lib.NodeInfo.systemId"), reason);
      // assertTrue(reason.contains("insertCallouts"), reason);
      // }
      capturer.restore();
      assertTrue(capturer.out().isEmpty());
      assertTrue(capturer.err().lines().count() > 100);
    }

    /* The external Apache Xalan 2.7.2 implementation works. */
    {
      System.setProperty(XmlTransformer.FACTORY_PROPERTY, XALAN_FACTORY);
      assertDoesNotThrow(() -> XmlTransformer.usingFoundFactory().usingStylesheet(myStyle));
    }
    {
      assertDoesNotThrow(() -> XmlTransformer
          .usingFactory(new net.sf.saxon.TransformerFactoryImpl()).usingStylesheet(myStyle));
    }
  }

  @Test
  void testUsingByteSourceUrl() throws Exception {
    final ByteSource myStyle = Resources
        .asByteSource(new URL("https", "cdn.docbook.org", "/release/xsl/1.79.2/fo/docbook.xsl"));

    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    final XmlTransformer t = XmlTransformer.usingFactory(new net.sf.saxon.TransformerFactoryImpl());
    assertEquals("net.sf.saxon.TransformerFactoryImpl", t.factory().getClass().getName());
    final XmlException readExc = assertThrows(XmlException.class, () -> t.usingStylesheet(myStyle));
    final String reason = readExc.getCause().getMessage();
    assertTrue(reason.contains("I/O error reported by XML parser processing file:"), reason);
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformSimpleDocBook() throws Exception {
    final StreamSource docBook = new StreamSource(
        XmlTransformerTests.class.getResource("docbook simple article.xml").toString());
    final StreamSource myStyle =
        new StreamSource("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

    {
      System.setProperty(XmlTransformer.FACTORY_PROPERTY, XALAN_FACTORY);
      final String transformed =
          XmlTransformer.usingFoundFactory().usingStylesheet(myStyle).transform(docBook);
      assertTrue(transformed
          .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
    }
    {
      final String transformed =
          XmlTransformer.usingFactory(new net.sf.saxon.TransformerFactoryImpl())
              .usingStylesheet(myStyle).transform(docBook);
      LOGGER.debug("Transformed docbook howto: {}.", transformed);
      assertTrue(transformed.matches(
          "(?s).*<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\".* font-family=.*"));
    }
  }

  @Test
  void testTransformComplexDocBook() throws Exception {
    final StreamSource docBook =
        new StreamSource(XmlTransformerTests.class.getResource("docbook howto.xml").toString());
    final StreamSource myStyle =
        new StreamSource(XmlTransformerTests.class.getResource("mystyle.xsl").toString());

    {
      System.setProperty(XmlTransformer.FACTORY_PROPERTY, XALAN_FACTORY);
      final String transformed =
          XmlTransformer.usingFoundFactory().usingStylesheet(myStyle).transform(docBook);
      assertTrue(transformed
          .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
    }
    {
      final String transformed =
          XmlTransformer.usingFactory(new net.sf.saxon.TransformerFactoryImpl())
              .usingStylesheet(myStyle).transform(docBook);
      LOGGER.debug("Transformed docbook howto: {}.", transformed);
      assertTrue(transformed.matches(
          "(?s).*<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\".* font-family=.*"));
    }
  }

  @Test
  void testTransformInvalidXsl() throws Exception {
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short invalid.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    assertThrows(XmlException.class,
        () -> XmlTransformer.usingSystemDefaultFactory().usingStylesheet(style).transform(input));
  }

  @Test
  void testTransformInvalidXml() throws Exception {
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short invalid.xml").toString());
    assertThrows(XmlException.class,
        () -> XmlTransformer.usingSystemDefaultFactory().usingStylesheet(style).transform(input));
  }

  @ParameterizedTest
  @EnumSource(names = {"JDK", "XALAN"})
  void testTransformMessaging(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected,
        XmlTransformer.usingFactory(factory.factory()).usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingS() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected, XmlTransformer.usingFactory(KnownFactory.SAXON.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertEquals("A message that does not terminate\nA message that does not terminate\n",
        capturer.err());
  }

  @Test
  void testTransformMessagingPedanticWithJdkFailsToStop() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected, XmlTransformer.pedanticTransformer(KnownFactory.JDK.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingPedanticX() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.XALAN.factory()).usingStylesheet(style).transform(input));
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    assertTrue(thrown.getCause().getMessage().contains("A message that does not terminate"),
        thrown.getCause().getMessage());
    capturer.restore();
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingPedanticWithSaxonFailsToStop() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected, XmlTransformer.pedanticTransformer(KnownFactory.SAXON.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertEquals("A message that does not terminate\nA message that does not terminate\n",
        capturer.err());
  }

  @ParameterizedTest
  @EnumSource(names = {"JDK", "XALAN"})
  void testTransformMessagingTerminate(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final StreamSource style = new StreamSource(
        XmlTransformerTests.class.getResource("short messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    assertThrows(XmlException.class, () -> XmlTransformer.usingFactory(factory.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingTerminateS() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final StreamSource style = new StreamSource(
        XmlTransformerTests.class.getResource("short messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    assertThrows(XmlException.class, () -> XmlTransformer.usingFactory(KnownFactory.SAXON.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertEquals("A message about premature end\n", capturer.err());
  }

  @Test
  void testTransformMessagingTerminatePedantic() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final StreamSource style = new StreamSource(
        XmlTransformerTests.class.getResource("short messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.JDK.factory()).usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.err().isEmpty());
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    assertTrue(
        thrown.getCause().getMessage().contains("Termination forced by an xsl:message instruction"),
        thrown.getCause().getMessage());
  }

  @Test
  void testTransformMessagingTerminatePedanticX() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final StreamSource style = new StreamSource(
        XmlTransformerTests.class.getResource("short messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.XALAN.factory()).usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.err().isEmpty());
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    assertTrue(thrown.getCause().getMessage().contains("premature"),
        thrown.getCause().getMessage());
  }

  @Test
  void testTransformMessagingTerminatePedanticS() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final StreamSource style = new StreamSource(
        XmlTransformerTests.class.getResource("short messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.SAXON.factory()).usingStylesheet(style).transform(input));
    capturer.restore();
    assertEquals("A message about premature end\n", capturer.err());
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    assertTrue(thrown.getCause().getMessage().contains("Processing terminated by xsl:message at line 13"),
        thrown.getCause().getMessage());
  }

  @Test
  void testCopy() throws Exception {
    final String source =
        Files.readString(Path.of(getClass().getResource("short namespace.xml").toURI()));
    StreamSource streamSource = new StreamSource(new StringReader(source));
    // final Document articleDoc = DomHelper.domHelper().asDocument(streamSource);

    DOMResult result = new DOMResult();
    XmlTransformer.usingFoundFactory().usingEmptyStylesheet().transform(streamSource, result);
    Document docCopy = (Document) result.getNode();
    assertEquals(source, DomHelper.domHelper().toString(docCopy));

    final Element root = docCopy.getDocumentElement();
    assertEquals("Article", root.getTagName());
    assertEquals(ARTICLE_NS, root.getNamespaceURI());
    ImmutableList<Node> children = DomHelper.toList(root.getChildNodes());
    Node textNode = children.get(0);
    assertEquals("\n    ", textNode.getNodeValue());
    LOGGER.info(DomHelper.toDebugString(textNode));
    Element title = (Element) children.get(1);
    assertEquals(ARTICLE_NS_K, title.getNamespaceURI());
    assertEquals("k:Title", title.getTagName());
    Element newElement = docCopy.createElementNS(ARTICLE_NS_K, "k:Empty");
    root.insertBefore(newElement, title.getNextSibling());

    final String expected =
        Files.readString(Path.of(getClass().getResource("short namespace expanded.xml").toURI()));
    assertEquals(expected, DomHelper.domHelper().toString(docCopy));
  }

  @Test
  void testCreateNamespace() throws Exception {
    DomHelper h = DomHelper.domHelper();

    Document doc = h.createDocument(new QName(ARTICLE_NS, "Article"));
    doc.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:k",
        ARTICLE_NS_K);
    Element title = doc.createElementNS(ARTICLE_NS_K, "k:Empty");
    doc.getDocumentElement().appendChild(title);
    final String start =
        Files.readString(Path.of(getClass().getResource("very short namespace.xml").toURI()));
    String serialized = h.toString(doc);
    assertEquals(start, serialized);

    DOMResult result = new DOMResult();
    XmlTransformer.usingFoundFactory().usingEmptyStylesheet()
        .transform(new StreamSource(new StringReader(serialized)), result);
    Document docCopy = (Document) result.getNode();
    assertEquals(start, DomHelper.domHelper().toString(docCopy));

    final Element root = docCopy.getDocumentElement();
    assertEquals("Article", root.getTagName());
    assertEquals(ARTICLE_NS, root.getNamespaceURI());
    ImmutableList<Node> children = DomHelper.toList(root.getChildNodes());
    Node textNode = children.get(0);
    assertEquals("\n    ", textNode.getNodeValue());
    LOGGER.info(DomHelper.toDebugString(textNode));
    Element titleCopy = (Element) children.get(1);
    assertEquals(ARTICLE_NS_K, titleCopy.getNamespaceURI());
    assertEquals("k:Empty", titleCopy.getTagName());
    Element newElement = docCopy.createElementNS(ARTICLE_NS_K, "k:Empty");
    root.insertBefore(newElement, titleCopy.getNextSibling());

    final String expected = Files
        .readString(Path.of(getClass().getResource("very short namespace expanded.xml").toURI()));
    assertEquals(expected, DomHelper.domHelper().toString(docCopy));
  }
}
