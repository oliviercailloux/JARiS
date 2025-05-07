package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Throwables;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.docbook.DocBookResources;
import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import java.net.URI;
import javax.xml.transform.TransformerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SetSystemProperty(key = "https.proxyHost", value = "invalid.invalid")
class XmlTransformerTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerTests.class);

  @ParameterizedTest
  @EnumSource
  void testInvalidXsl(KnownFactory factory) throws Exception {
    final CharSource style = charSource("Invalid.xsl");
    assertThrows(XmlException.class,
        () -> XmlTransformerFactory.usingFactory(factory.factory()).usingStylesheet(style));
  }

  @ParameterizedTest
  @EnumSource
  void testInvalidXml(KnownFactory factory) throws Exception {
    final CharSource style = charSource("Article/To text.xsl");
    final CharSource input = charSource("Invalid.xml");
    assertThrows(XmlException.class, () -> XmlTransformerFactory.usingFactory(factory.factory())
        .usingStylesheet(style).charsToChars(input));
  }

  @ParameterizedTest
  @EnumSource
  void testSimple(KnownFactory factory) throws Exception {
    final CharSource style = charSource("Article/To text.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    final String expected = charSource("Article/Two authors.txt").read();
    assertEquals(expected, XmlTransformerFactory.usingFactory(factory.factory()).pedantic()
        .usingStylesheet(style).charsToChars(input));
  }

  @Test
  void testDocBookStyleTooComplexForJdk() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    TransformerFactory underlying = KnownFactory.JDK.factory();
    underlying.setURIResolver(DocBookResources.RESOLVER);
    final XmlTransformerFactory t = XmlTransformerFactory.usingFactory(underlying);
    final XmlException e =
        assertThrows(XmlException.class, () -> t.usingStylesheet(DocBookResources.XSLT_1_FO_URI));
    final String reason = e.getCause().getMessage();
    assertTrue(reason.contains("JAXP0801003"), reason);
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().lines().count() > 100);
  }

  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testDocBookStyleOthers(KnownFactory factory) throws Exception {
    TransformerFactory underlying = factory.factory();
    underlying.setURIResolver(DocBookResources.RESOLVER);
    assertDoesNotThrow(() -> XmlTransformerFactory.usingFactory(underlying).usingStylesheet(DocBookResources.XSLT_1_FO_URI));
  }

  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testMissInternet(KnownFactory factory) throws Exception {
    final URI myStyle = new URI("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

    final XmlTransformerFactory t = XmlTransformerFactory.usingFactory(factory.factory());
    XmlException exc = assertThrows(XmlException.class,
        () -> t.usingStylesheet(myStyle));
    Throwable connExc = Throwables.getRootCause(exc);
    assertEquals(java.net.UnknownHostException.class, connExc.getClass());
  }

  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testDocBookSimple(KnownFactory factory) throws Exception {
    final CharSource docBook = charSource("DocBook/Simple.xml");

    TransformerFactory underlying = factory.factory();
    underlying.setURIResolver(DocBookResources.RESOLVER);
    final String transformed = XmlTransformerFactory.usingFactory(underlying)
        .usingStylesheet(DocBookResources.XSLT_1_FO_URI).charsToChars(docBook);
    assertTrue(transformed
        .matches("(?s).*<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\".* font-family=.*"));
  }

  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testDocBookComplex(KnownFactory factory) throws Exception {
    final CharSource myStyle = charSource("DocBook/mystyle.xsl");
    final CharSource docBook = charSource("DocBook/Howto.xml");

    TransformerFactory underlying = factory.factory();
    underlying.setURIResolver(DocBookResources.RESOLVER);
    final String transformed = XmlTransformerFactory.usingFactory(underlying)
        .usingStylesheet(myStyle).charsToChars(docBook);
    LOGGER.debug("Transformed docbook howto: {}.", transformed);
    assertTrue(transformed
        .matches("(?s).*<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\".* font-family=.*"));
  }
}
