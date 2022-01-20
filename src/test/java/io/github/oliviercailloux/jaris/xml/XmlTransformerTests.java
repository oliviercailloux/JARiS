package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class XmlTransformerTests {
  private static final String XALAN_FACTORY = "org.apache.xalan.processor.TransformerFactoryImpl";
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerTests.class);

  @Test
  void testTransformSimple() throws Exception {
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected, XmlTransformer.pedanticTransformer().transform(input, style));
  }

  @Test
  void testTransformSimpleDocBook() throws Exception {
    final StreamSource docBook = new StreamSource(
        XmlTransformerTests.class.getResource("docbook simple article.xml").toString());
    final StreamSource myStyle =
        /*
         * Much faster (obtains transformer from stylesheet in 4 sec instead of 17 sec), but depends
         * on what is installed locally.
         */
        // new
        // StreamSource(Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl")
        // .toUri().toString());
        new StreamSource("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

    final boolean xalanIsInClassPath =
        Class.forName(getClass().getClassLoader().getUnnamedModule(), XALAN_FACTORY) != null;
    if (!xalanIsInClassPath) {
      /* This is too complex for pure JDK embedded transformer (Apache Xalan). */
      final XmlException xalanExc = assertThrows(XmlException.class,
          () -> XmlTransformer.usingSystemDefaultFactory().transform(docBook, myStyle));
      final String reason = xalanExc.getCause().getMessage();
      assertTrue(reason.contains("insertCallouts"), reason);
    }
    /*
     * Oddly enough, the error changes when including xalan in the class path even though we still
     * use the system default transformer. Might be related to
     * https://xml.apache.org/xalan-j/features.html#source_location.
     */
    if (xalanIsInClassPath) {
      final XmlException xalanExc = assertThrows(XmlException.class,
          () -> XmlTransformer.usingSystemDefaultFactory().transform(docBook, myStyle));
      final String reason = xalanExc.getCause().getMessage();
      assertTrue(reason.contains("org.apache.xalan.lib.NodeInfo.systemId"), reason);
    }
    /* The external Apache Xalan 2.7.2 implementation works. */
    if (xalanIsInClassPath) {
      System.setProperty(XmlTransformer.FACTORY_PROPERTY, XALAN_FACTORY);
      final String transformed = XmlTransformer.usingFoundFactory().transform(docBook, myStyle);
      assertTrue(transformed
          .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
    }
    {
      final String transformed = XmlTransformer
          .usingFactory(new net.sf.saxon.TransformerFactoryImpl()).transform(docBook, myStyle);
      LOGGER.debug("Transformed docbook howto: {}.", transformed);
      assertTrue(transformed
          .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
    }
  }

  @Test
  void testTransformComplexDocBook() throws Exception {
    final StreamSource docBook =
        new StreamSource(XmlTransformerTests.class.getResource("docbook howto.xml").toString());
    final StreamSource myStyle =
        new StreamSource(XmlTransformerTests.class.getResource("mystyle.xsl").toString());

    final boolean xalanIsInClassPath =
        Class.forName(getClass().getClassLoader().getUnnamedModule(), XALAN_FACTORY) != null;
    LOGGER.info("Xalan in class path? {}.", xalanIsInClassPath);
    if (!xalanIsInClassPath) {
      final XmlException xalanExc = assertThrows(XmlException.class,
          () -> XmlTransformer.usingSystemDefaultFactory().transform(docBook, myStyle));
      final String reason = xalanExc.getCause().getMessage();
      assertTrue(reason.contains("insertCallouts"), reason);
    }
    if (xalanIsInClassPath) {
      final XmlException xalanExc = assertThrows(XmlException.class,
          () -> XmlTransformer.usingSystemDefaultFactory().transform(docBook, myStyle));
      final String reason = xalanExc.getCause().getMessage();
      assertTrue(reason.contains("org.apache.xalan.lib.NodeInfo.systemId"), reason);
    }
    if (xalanIsInClassPath) {
      System.setProperty(XmlTransformer.FACTORY_PROPERTY, XALAN_FACTORY);
      final String transformed = XmlTransformer.usingFoundFactory().transform(docBook, myStyle);
      assertTrue(transformed
          .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
    }
    {
      final String transformed = XmlTransformer
          .usingFactory(new net.sf.saxon.TransformerFactoryImpl()).transform(docBook, myStyle);
      LOGGER.debug("Transformed docbook howto: {}.", transformed);
      assertTrue(transformed
          .contains("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" font-family="));
    }
  }

  @Test
  void testTransformInvalidXsl() throws Exception {
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short invalid.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    assertThrows(XmlException.class,
        () -> XmlTransformer.usingSystemDefaultFactory().transform(input, style));
  }

  @Test
  void testTransformInvalidXml() throws Exception {
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short invalid.xml").toString());
    assertThrows(XmlException.class,
        () -> XmlTransformer.usingSystemDefaultFactory().transform(input, style));
  }

  @Test
  void testTransformMessaging() throws Exception {
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("short messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    final String expected =
        Files.readString(Path.of(XmlTransformerTests.class.getResource("transformed.txt").toURI()));
    assertEquals(expected, XmlTransformer.usingSystemDefaultFactory().transform(input, style));

    assertThrows(XmlException.class,
        () -> XmlTransformer.pedanticTransformer().transform(input, style));
  }

  @Test
  void testTransformMessagingTerminate() throws Exception {
    final StreamSource style = new StreamSource(
        XmlTransformerTests.class.getResource("short messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("short.xml").toString());
    assertThrows(XmlException.class,
        () -> XmlTransformer.usingSystemDefaultFactory().transform(input, style));
  }
}
