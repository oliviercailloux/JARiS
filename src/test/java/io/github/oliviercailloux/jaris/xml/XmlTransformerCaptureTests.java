package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlTransformerCaptureTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerCaptureTests.class);

  @ParameterizedTest
  @EnumSource
  void testMissingImport(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("/io/github/oliviercailloux/docbook/fo/docbook.xsl");
    final XmlTransformerFactory t = XmlTransformerFactory.usingFactory(factory.factory());
    final XmlException readExc = assertThrows(XmlException.class, () -> t.usingStylesheet(style));
    final String reason = readExc.getCause().getMessage();
    assertTrue(reason.contains("ptc.xsl") || reason.contains("VERSION.xsl"), reason);
    capturer.restore();
    assertTrue(capturer.out().isEmpty(), capturer.out());
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
    assertEquals(expected, XmlTransformerFactory.usingFactory(KnownFactory.JDK.factory()).pedantic()
        .usingStylesheet(style).charsToChars(input));
    capturer.restore();
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  void testTransformMessagingPedanticXalan() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    final CharSource style = charSource("Article/Messaging.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    XmlException thrown = assertThrows(XmlException.class,
        () -> XmlTransformerFactory.usingFactory(KnownFactory.XALAN.factory()).pedantic()
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
        () -> XmlTransformerFactory.usingFactory(KnownFactory.SAXON.factory()).pedantic()
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
    XmlException thrown = assertThrows(XmlException.class, () -> XmlTransformerFactory
        .usingFactory(factory.factory()).pedantic().usingStylesheet(style).charsToChars(input));
    assertTrue(thrown.getMessage().contains("Error while transforming document."),
        thrown.getMessage());
    String contained = switch (factory) {
      case JDK -> "Termination forced by an xsl:message instruction";
      case XALAN -> "premature";
      case SAXON -> "Processing terminated by xsl:message at line 13";
    };
    assertTrue(thrown.getCause().getMessage().contains(contained), thrown.getCause().getMessage());
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }
}
