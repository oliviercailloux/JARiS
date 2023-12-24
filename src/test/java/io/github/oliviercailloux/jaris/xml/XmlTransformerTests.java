package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.ImmutableList;

import io.github.oliviercailloux.jaris.testutils.OutputCapturer;

class XmlTransformerTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerTests.class);

  private static final String XALAN_FACTORY = "org.apache.xalan.processor.TransformerFactoryImpl";

  @Test
  void testTransformSimple() throws Exception {
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected,
        XmlTransformer.pedanticTransformer(TransformerFactory.newDefaultInstance())
            .usingStylesheet(style).transform(input));
  }

  @Test
  void testDocBookStyle() throws Exception {
    final StreamSource myStyle =
        /*
         * Much faster (obtains transformer from stylesheet in 4 sec instead of 17 sec), but depends
         * on what is installed locally.
         */
        // new
        // StreamSource(Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl")
        // .toUri().toString());
        new StreamSource("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

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

  @Test
  void testTransformMessaging() throws Exception {
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected,
        XmlTransformer.usingSystemDefaultFactory().usingStylesheet(style).transform(input));

    assertThrows(XmlException.class,
        () -> XmlTransformer.pedanticTransformer(TransformerFactory.newDefaultInstance())
            .usingStylesheet(style).transform(input));
  }

  @Test
  void testTransformMessagingTerminate() throws Exception {
    final StreamSource style = new StreamSource(
        XmlTransformerTests.class.getResource("short messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    assertThrows(XmlException.class,
        () -> XmlTransformer.usingSystemDefaultFactory().usingStylesheet(style).transform(input));
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
        assertEquals("https://example.com/article", root.getNamespaceURI());
        ImmutableList<Node> children = DomHelper.toList(root.getChildNodes());
        Node textNode = children.get(0);
        assertEquals("\n    ", textNode.getNodeValue());
        LOGGER.info(DomHelper.toDebugString(textNode));
        Element title = (Element) children.get(1);
        String kNs = "https://example.com/article/k";
        assertEquals(kNs, title.getNamespaceURI());
        assertEquals("k:Title", title.getTagName());
        Element newElement = docCopy.createElementNS(kNs, "k:Empty");
        root.insertBefore(newElement, title.getNextSibling());
        
        final String expected = Files.readString(
            Path.of(getClass().getResource("short namespace expanded.xml").toURI()));
            assertEquals(expected, DomHelper.domHelper().toString(docCopy));
        }
}
