package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import io.github.oliviercailloux.jaris.xml.XmlTransformerFactory.OutputProperties;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
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

  @ParameterizedTest
  @EnumSource
  void testInvalidXsl(KnownFactory factory) throws Exception {
    final CharSource style = charSource("Invalid.xsl");
    assertThrows(XmlException.class, () -> XmlTransformerFactory.usingFactory(factory.factory())
        .usingStylesheet(style));
  }

  @ParameterizedTest
  @EnumSource
  void testInvalidXml(KnownFactory factory) throws Exception {
    final CharSource style = charSource("Article/To text.xsl");
    final CharSource input = charSource("Invalid.xml");
    assertThrows(XmlException.class, () -> XmlTransformerFactory.usingFactory(factory.factory())
        .usingStylesheet(style).charsToChars(input));
  }

  @Test
  void testSimple() throws Exception {
    final CharSource style = charSource("Article/To text.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    final String expected = charSource("Article/Two authors.txt").read();
    assertEquals(expected,
        XmlTransformerFactory.pedanticTransformer(TransformerFactory.newDefaultInstance())
            .usingStylesheet(style).charsToChars(input));
  }

  @RestoreSystemProperties
  @Test
  void testDocBookStyle() throws Exception {
    /*
     * Much faster (obtains transformer from stylesheet in 4 sec instead of 17 sec), but depends on
     * what is installed locally.
     */
    final URI myStyle =
        Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl").toUri();
    // final CharSource myStyle =
    // charSource(Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl"));
    // new CharSource("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

    {
      /* This is too complex for pure JDK embedded transformer. */
      /*
       * This spits plenty on the console (bypassing the logger mechanism) before crashing.
       */
      final OutputCapturer capturer = OutputCapturer.capturer();
      capturer.capture();

      final XmlTransformerFactory t = XmlTransformerFactory.usingSystemDefaultFactory();
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

    /* The external Apache Xalan implementation works. */
    {
      System.setProperty(XmlTransformerFactory.FACTORY_PROPERTY, XALAN_FACTORY);
      assertDoesNotThrow(() -> XmlTransformerFactory.usingFoundFactory().usingStylesheet(myStyle));
    }
    {
      assertDoesNotThrow(() -> XmlTransformerFactory
          .usingFactory(new net.sf.saxon.TransformerFactoryImpl()).usingStylesheet(myStyle));
    }
  }

  @Test
  void testSimpleDocBook() throws Exception {
    final CharSource docBook = charSource("DocBook/Simple.xml");
    // final CharSource myStyle =
    // charSource(new URL("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl"));
    final URI myStyle = new URI("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

    {
      System.setProperty(XmlTransformerFactory.FACTORY_PROPERTY, XALAN_FACTORY);
      final String transformed =
          XmlTransformerFactory.usingFoundFactory().usingStylesheet(myStyle).charsToChars(docBook);
      assertTrue(transformed
          .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
    }
    {
      final String transformed =
          XmlTransformerFactory.usingFactory(new net.sf.saxon.TransformerFactoryImpl())
              .usingStylesheet(myStyle).charsToChars(docBook);
      LOGGER.debug("Transformed docbook howto: {}.", transformed);
      assertTrue(transformed.matches(
          "(?s).*<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\".* font-family=.*"));
    }
  }

  @Test
  void testComplexDocBook() throws Exception {
    final CharSource docBook = charSource("DocBook/Howto.xml");
    final CharSource myStyle = charSource("DocBook/mystyle.xsl");

    {
      System.setProperty(XmlTransformerFactory.FACTORY_PROPERTY, XALAN_FACTORY);
      final String transformed =
          XmlTransformerFactory.usingFoundFactory().usingStylesheet(myStyle).charsToChars(docBook);
      assertTrue(transformed
          .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
    }
    {
      final String transformed =
          XmlTransformerFactory.usingFactory(new net.sf.saxon.TransformerFactoryImpl())
              .usingStylesheet(myStyle).charsToChars(docBook);
      LOGGER.debug("Transformed docbook howto: {}.", transformed);
      assertTrue(transformed.matches(
          "(?s).*<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\".* font-family=.*"));
    }
  }
}
