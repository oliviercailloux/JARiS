package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class XmlTransformerCaptureTests {

  /**
   * DocBook XSLT 1 is incompatible with Saxon, so, expectedly, this produces tons of errors (see
   * Publish project for the gory details).
   */
  @Test
  void testUsingByteSourceUrl() throws Exception {
    final ByteSource myStyle = Resources
        .asByteSource(new URL("https", "cdn.docbook.org", "/release/xsl/1.79.2/fo/docbook.xsl"));

    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    final XmlTransformerFactory t =
        XmlTransformerFactory.usingFactory(new net.sf.saxon.TransformerFactoryImpl());
    assertEquals("net.sf.saxon.TransformerFactoryImpl", t.factory().getClass().getName());
    final XmlException readExc = assertThrows(XmlException.class, () -> t.usingStylesheet(myStyle));
    final String reason = readExc.getCause().getMessage();
    assertTrue(reason.contains("I/O error reported by XML parser processing file:"), reason);
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty(), capturer.err());
  }

  @ParameterizedTest
  @EnumSource
  void testTransformMessaging(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("Article/Messaging.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    final String expected = charSource("Article/Two authors.txt").read();
    assertEquals(expected, XmlTransformerFactory.usingFactory(factory.factory())
        .usingStylesheet(style).charsToChars(input));
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingPedanticWithJdkFailsToStop() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.captureErr();
    final CharSource style = charSource("Article/Messaging.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    final String expected = charSource("Article/Two authors.txt").read();
    assertEquals(expected, XmlTransformerFactory.pedanticTransformer(KnownFactory.JDK.factory())
        .usingStylesheet(style).charsToChars(input));
    capturer.restore();
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingPedanticX() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("Article/Messaging.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    XmlException thrown = assertThrows(XmlException.class,
        () -> XmlTransformerFactory.pedanticTransformer(KnownFactory.XALAN.factory())
            .usingStylesheet(style).charsToChars(input));
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
    final CharSource style = charSource("Article/Messaging.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    XmlException thrown = assertThrows(XmlException.class,
        () -> XmlTransformerFactory.pedanticTransformer(KnownFactory.SAXON.factory())
            .usingStylesheet(style).charsToChars(input));
    assertTrue(thrown.getMessage().contains("Error while transforming document"),
        thrown.getMessage());
    assertNull(thrown.getCause());
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @ParameterizedTest
  @EnumSource
  void testTransformMessagingTerminate(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("Article/Messaging terminate.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    assertThrows(XmlException.class, () -> XmlTransformerFactory.usingFactory(factory.factory())
        .usingStylesheet(style).charsToChars(input));
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @ParameterizedTest
  @EnumSource
  void testTransformMessagingTerminatePedantic(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("Article/Messaging terminate.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    XmlException thrown = assertThrows(XmlException.class,
        () -> XmlTransformerFactory.pedanticTransformer(factory.factory())
            .usingStylesheet(style).charsToChars(input));
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    String contained = switch (factory) {
      case JDK -> "Termination forced by an xsl:message instruction";
      case XALAN -> "premature";
      case SAXON -> "Processing terminated by xsl:message at line 13";
    };
    assertTrue(
        thrown.getCause().getMessage().contains(contained),
        thrown.getCause().getMessage());
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }
}
