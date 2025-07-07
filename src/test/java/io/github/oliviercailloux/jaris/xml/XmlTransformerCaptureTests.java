package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static io.github.oliviercailloux.jaris.xml.Resourcer.streamSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import io.github.oliviercailloux.jaris.xml.XmlTransformerFactory.OutputProperties;
import java.io.StringWriter;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.jaxp.TransformerImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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

  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  public void testAmbiguousRemoveWhitespacesSpaced(KnownFactory factory) throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();
    CharSource style = charSource("Whitespace/Ambiguous strip whitespace.xsl");
    CharSource input = charSource("Whitespace/Spaced.xml");
    XmlTransformer t = XmlTransformerFactory.usingFactory(factory.factory()).usingStylesheet(style,
        ImmutableMap.of(), OutputProperties.noIndent());
    String stripped = t.charsToChars(input);
    CharSource half = charSource("Whitespace/Half spaced.xml");
    assertEquals(half.read(), stripped);
    capturer.restore();
    assertTrue(capturer.out().isEmpty());
    assertTrue(capturer.err().isEmpty());
  }

  @Test
  @Disabled("see testSaxonSwallowsException")
  public void testAmbiguousRemoveWhitespacesSpacedPedantic() throws Exception {
    CharSource style = charSource("Whitespace/Ambiguous strip whitespace.xsl");
    CharSource input = charSource("Whitespace/Spaced.xml");
    // final CharSource style = charSource("Article/Messaging.xsl");
    // final CharSource input = charSource("Article/Two authors.xml");
    XmlTransformer t = XmlTransformerFactory.usingFactory(KnownFactory.SAXON.factory()).pedantic()
        .usingStylesheet(style, ImmutableMap.of(), OutputProperties.noIndent());
    assertThrows(XmlException.class, () -> t.charsToChars(input));
  }

  /**
   * Saxon does not stop when listener warning throws: https://saxonica.plan.io/issues/6857
   *
   * @throws Exception
   */
  @Test
  public void testSaxonSwallowsException() throws Exception {
    StreamSource stylesheet = streamSource("Whitespace/Ambiguous strip whitespace.xsl");
    StreamSource input = streamSource("Whitespace/Spaced.xml");

    TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
    TransformerImpl transformer = (TransformerImpl) factory.newTransformer(stylesheet);
    ErrorListener errorListener = Mockito.mock(ErrorListener.class);
    Mockito.doThrow(new TransformerException("ErrorListener called")).when(errorListener)
        .warning(ArgumentMatchers.any());
    transformer.setErrorListener(errorListener);

    SaxonMessageHandler handler = SaxonMessageHandler.newInstance();
    transformer.getUnderlyingXsltTransformer().setMessageHandler(handler);
    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);
    assertDoesNotThrow(() -> transformer.transform(input, result));
    assertFalse(handler.hasBeenCalled());
    Mockito.verify(errorListener, Mockito.times(1)).warning(ArgumentMatchers.any());
  }

  @Test
  public void testXalanDoesNotWarnAboutAmbiguity() throws Exception {
    StreamSource stylesheet = streamSource("Whitespace/Ambiguous strip whitespace.xsl");
    StreamSource input = streamSource("Whitespace/Spaced.xml");

    TransformerFactory factory = new org.apache.xalan.processor.TransformerFactoryImpl();
    ErrorListener errorListener = Mockito.mock(ErrorListener.class);
    Mockito.doThrow(new TransformerException("ErrorListener called")).when(errorListener)
        .warning(ArgumentMatchers.any());
    factory.setErrorListener(errorListener);
    Transformer transformer = factory.newTransformer(stylesheet);
    transformer.setErrorListener(errorListener);

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);
    assertDoesNotThrow(() -> transformer.transform(input, result));
    Mockito.verifyNoMoreInteractions(errorListener);
  }
}
