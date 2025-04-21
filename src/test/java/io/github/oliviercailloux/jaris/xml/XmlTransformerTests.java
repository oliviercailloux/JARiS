package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Throwables;
import com.google.common.base.VerifyException;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import javax.xml.catalog.Catalog;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogFeatures.Feature;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.transform.TransformerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A denier proxy does not work because it needs to be unset after each set. System properties as well seem to have lasting effect. */
class XmlTransformerTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerTests.class);

  private static TransformerFactory withDocBookResolver(KnownFactory factory) {
    URL docBookCatalog = XmlTransformerTests.class.getResource("/io/github/oliviercailloux/docbook/catalog.xml");
    URI docBookCatalogUri;
    try {
      docBookCatalogUri = docBookCatalog.toURI();
    } catch (URISyntaxException e) {
      throw new VerifyException(e);
    }
    Catalog catalog = CatalogManager.catalog(
        CatalogFeatures.builder().with(Feature.RESOLVE, "continue").build(), docBookCatalogUri);
    CatalogResolver resolver = CatalogManager.catalogResolver(catalog);
    TransformerFactory s;
    try {
      s = factory.factory();
    } catch (ClassNotFoundException e) {
      throw new VerifyException(e);
    }
    s.setURIResolver(resolver);
    return s;
  }

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
    /*
     * Much faster (obtains transformer from stylesheet in 4 sec instead of 17 sec), but depends on
     * what is installed locally.
     */
    final URI myStyle =
        Path.of("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl").toUri();

    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    final XmlTransformerFactory t = XmlTransformerFactory.usingFactory(KnownFactory.JDK.factory());
    assertEquals("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
        t.factory().getClass().getName());
    final XmlException xalanExc =
        assertThrows(XmlException.class, () -> t.usingStylesheet(myStyle));
    final String reason = xalanExc.getCause().getMessage();
    assertTrue(reason.contains("JAXP0801003"), reason);
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().lines().count() > 100);
  }

  @SetSystemProperty(key = "https.proxyHost", value = "invalid.invalid")
  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testDocBookStyleOthers(KnownFactory factory) throws Exception {
    final URI myStyle =
        URI.create("http://docbook.sourceforge.net/release/xsl/current/fo/docbook.xsl");
    // TODO get 1.79… resolver?
    XmlTransformerFactory f = XmlTransformerFactory.usingFactory(withDocBookResolver(factory));

    assertDoesNotThrow(() -> f.usingStylesheet(myStyle));
  }

  @SetSystemProperty(key = "https.proxyHost", value = "invalid.invalid")
  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testMissInternet(KnownFactory factory) throws Exception {
    final URI myStyle = new URI("https://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl");

    XmlException exc = assertThrows(XmlException.class,
        () -> XmlTransformerFactory.usingFactory(factory.factory()).usingStylesheet(myStyle));
    Throwable connExc = Throwables.getRootCause(exc);
    assertEquals(java.net.UnknownHostException.class, connExc.getClass());
  }

  @SetSystemProperty(key = "https.proxyHost", value = "invalid.invalid")
  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testDocBookSimple(KnownFactory factory) throws Exception {
    final URI myStyle =
        URI.create("http://docbook.sourceforge.net/release/xsl/current/fo/docbook.xsl");
    final CharSource docBook = charSource("DocBook/Simple.xml");

    final String transformed = XmlTransformerFactory.usingFactory(withDocBookResolver(factory))
        .usingStylesheet(myStyle).charsToChars(docBook);
    assertTrue(transformed
        .matches("(?s).*<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\".* font-family=.*"));
  }

  @ClearSystemProperty(key = "https.proxyHost")
  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testDocBookComplex(KnownFactory factory) throws Exception {
    assertNull(System.getProperty("https.proxyHost"));
    final CharSource myStyle = charSource("DocBook/mystyle.xsl");
    final CharSource docBook = charSource("DocBook/Howto.xml");

    final String transformed = XmlTransformerFactory.usingFactory(factory.factory())
        .usingStylesheet(myStyle).charsToChars(docBook);
    LOGGER.debug("Transformed docbook howto: {}.", transformed);
    assertTrue(transformed
        .matches("(?s).*<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\".* font-family=.*"));
  }
}
