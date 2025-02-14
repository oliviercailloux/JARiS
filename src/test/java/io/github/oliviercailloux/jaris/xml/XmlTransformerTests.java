package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import io.github.oliviercailloux.jaris.xml.XmlTransformer.OutputProperties;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
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
    final CharSource style = charSource("short.xsl");
    final CharSource input = charSource("short.xml");
    final String expected =
        charSource("transformed.txt").read();
    assertEquals(expected,
        XmlTransformer.pedanticTransformer(TransformerFactory.newDefaultInstance())
            .usingStylesheet(style).transform(input));
  }

  @RestoreSystemProperties
  @Test
  void testDocBookStyle() throws Exception {
    /*
     * Much faster (obtains transformer from stylesheet in 4 sec instead of 17 sec), but depends
     * on what is installed locally.
     */
    final URI myStyle =
    Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl").toUri();
    // final CharSource myStyle =
    //     charSource(Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl"));
    // new CharSource("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

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
    assertTrue(capturer.err().isEmpty(), capturer.err());
  }

  @Test
  void testTransformSimpleDocBook() throws Exception {
    final CharSource docBook = charSource("docbook simple article.xml");
    // final CharSource myStyle =
    // charSource(new URL("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl"));
    final URI myStyle =
        new URI("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

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
    final CharSource docBook = charSource("docbook howto.xml");
    final CharSource myStyle = charSource("mystyle.xsl");

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
    final CharSource style = charSource("short invalid.xsl");
    final CharSource input = charSource("short.xml");
    assertThrows(XmlException.class,
        () -> XmlTransformer.usingSystemDefaultFactory().usingStylesheet(style).transform(input));
  }

  @Test
  void testTransformInvalidXml() throws Exception {
    final CharSource style = charSource("short.xsl");
    final CharSource input = charSource("short invalid.xml");
    assertThrows(XmlException.class,
        () -> XmlTransformer.usingSystemDefaultFactory().usingStylesheet(style).transform(input));
  }

  @ParameterizedTest
  @EnumSource(names = {"JDK", "XALAN"})
  void testTransformMessaging(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("short messaging.xsl");
    final CharSource input = charSource("short.xml");
    final String expected =
        charSource("transformed.txt").read();
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
    final CharSource style = charSource("short messaging.xsl");
    final CharSource input = charSource("short.xml");
    final String expected = charSource("transformed.txt").read();
    assertEquals(expected, XmlTransformer.usingFactory(KnownFactory.SAXON.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingPedanticWithJdkFailsToStop() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final CharSource style = charSource("short messaging.xsl");
    final CharSource input = charSource("short.xml");
    final String expected = charSource("transformed.txt").read();
    assertEquals(expected, XmlTransformer.pedanticTransformer(KnownFactory.JDK.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingPedanticX() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("short messaging.xsl");
    final CharSource input = charSource("short.xml");
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.XALAN.factory()).usingStylesheet(style).transform(input));
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    assertTrue(thrown.getCause().getMessage().contains("A message that does not terminate"),
        thrown.getCause().getMessage());
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingPedanticSaxon() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("short messaging.xsl");
    final CharSource input = charSource("short.xml");
    RuntimeException thrown = assertThrows(RuntimeException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.SAXON.factory()).usingStylesheet(style).transform(input));
    assertEquals(XmlException.class, thrown.getClass());
    assertTrue(thrown.getMessage().contains("Error while transforming document"),
        thrown.getMessage());
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty(), capturer.err());
  }

  @ParameterizedTest
  @EnumSource(names = {"JDK", "XALAN"})
  void testTransformMessagingTerminate(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("short messaging terminate.xsl");
    final CharSource input = charSource("short.xml");
    assertThrows(XmlException.class, () -> XmlTransformer.usingFactory(factory.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingTerminateS() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("short messaging terminate.xsl");
    final CharSource input = charSource("short.xml");
    assertThrows(XmlException.class, () -> XmlTransformer.usingFactory(KnownFactory.SAXON.factory())
        .usingStylesheet(style).transform(input));
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingTerminatePedantic() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("short messaging terminate.xsl");
    final CharSource input = charSource("short.xml");
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.JDK.factory()).usingStylesheet(style).transform(input));
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    assertTrue(
        thrown.getCause().getMessage().contains("Termination forced by an xsl:message instruction"),
        thrown.getCause().getMessage());
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingTerminatePedanticX() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("short messaging terminate.xsl");
    final CharSource input = charSource("short.xml");
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.XALAN.factory()).usingStylesheet(style).transform(input));
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    assertTrue(thrown.getCause().getMessage().contains("premature"),
        thrown.getCause().getMessage());
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingTerminatePedanticS() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("short messaging terminate.xsl");
    final CharSource input = charSource("short.xml");
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformer
        .pedanticTransformer(KnownFactory.SAXON.factory()).usingStylesheet(style).transform(input));
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    assertTrue(
        thrown.getCause().getMessage().contains("Processing terminated by xsl:message at line 13"),
        thrown.getCause().getMessage());
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testCopy() throws Exception {
    final CharSource source = charSource("short namespace.xml");
    Document docCopy = XmlTransformer.usingFoundFactory().usingEmptyStylesheet().charsToDom(source);
    assertEquals(source.read(), DomHelper.domHelper().toString(docCopy));
    String directResult =
        XmlTransformer.usingFoundFactory().usingEmptyStylesheet().transform(source);
    assertEquals(source.read().replaceAll("    ", "   "), directResult);

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

    final String expected = charSource("short namespace expanded.xml").read();
    assertEquals(expected, DomHelper.domHelper().toString(docCopy));
  }

  @Test
  void testPrettySaxon() throws Exception {
    final CharSource source = charSource("short namespace.xml");
    final CharSource sourceOneline = charSource("short namespace oneline.xml");

    Document docCopy =
        XmlTransformer.usingFoundFactory().usingEmptyStylesheet().charsToDom(sourceOneline);
    assertEquals(source.read(), DomHelper.domHelper().toString(docCopy));
    String directResult = XmlTransformer.usingFactory(KnownFactory.SAXON.factory())
        .usingEmptyStylesheet().transform(sourceOneline);
    assertEquals(source.read().replaceAll("    ", "   "), directResult);
  }

  @Test
  void testPretty() throws Exception {
    final CharSource source = charSource("short namespace.xml");
    final CharSource sourceOneline = charSource("short namespace oneline.xml");

    Document docCopy =
        XmlTransformer.usingFoundFactory().usingEmptyStylesheet().charsToDom(sourceOneline);
    assertEquals(source.read(), DomHelper.domHelper().toString(docCopy));
    String directResult = XmlTransformer.usingFactory(KnownFactory.XALAN.factory())
        .usingEmptyStylesheet().transform(sourceOneline);
    String expectedBug = source.read().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    assertEquals(expectedBug, directResult);
  }

  @Test
  void testNotPretty() throws Exception {
    final CharSource source = charSource("short namespace.xml");
    final CharSource sourceOneline = charSource("short namespace oneline.xml");

    Document docCopy = 
    XmlTransformer.usingFoundFactory().usingEmptyStylesheet()
        .charsToDom(sourceOneline);
    assertEquals(source.read(), DomHelper.domHelper().toString(docCopy));
    String directResult =
        XmlTransformer.usingFoundFactory().usingEmptyStylesheet(OutputProperties.noIndent())
            .transform(sourceOneline);
    assertEquals(sourceOneline.read(), directResult);
  }

  @Test
  void testCreateNamespace() throws Exception {
    DomHelper h = DomHelper.domHelper();

    Document doc = h.createDocument(new QName(ARTICLE_NS, "Article"));
    doc.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:k",
        ARTICLE_NS_K);
    Element title = doc.createElementNS(ARTICLE_NS_K, "k:Empty");
    doc.getDocumentElement().appendChild(title);
    final CharSource start =
        charSource(Path.of(getClass().getResource("very short namespace.xml").toURI()));
    String serialized = h.toString(doc);
    assertEquals(start.read(), serialized);

    Document docCopy = 
    XmlTransformer.usingFoundFactory().usingEmptyStylesheet()
        .charsToDom(CharSource.wrap(serialized));
    assertEquals(start.read(), DomHelper.domHelper().toString(docCopy));

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
