package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.oliviercailloux.jaris.testutils.OutputCapturer;
import io.github.oliviercailloux.jaris.xml.XmlTransformerFactory.OutputProperties;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.s9api.Message;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTransformerTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(JavaTransformerTests.class);

  @Test
  public void testSaxonIdentityNullListener() throws Exception {
    TransformerFactoryImpl factory = new net.sf.saxon.TransformerFactoryImpl();
    Transformer transformer = factory.newTransformer();
    /* Bug in 12.4, https://saxonica.plan.io/issues/6689: is null. Will be corrected. */
    assertNull(transformer.getErrorListener());
  }

  @Test
  public void testSaxonMessageNonTerminating() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    TransformerFactoryImpl factory = new net.sf.saxon.TransformerFactoryImpl();
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("Article/Messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("Article/One author.xml").toString());
    final String expected = Files.readString(
        Path.of(XmlTransformerTests.class.getResource("Article/One author.txt").toURI()));
    TransformerImpl transformer = (TransformerImpl) factory.newTransformer(style);
    ErrorListener listener = Mockito.mock(ErrorListener.class);
    /* https://saxonica.plan.io/issues/6685 */
    @SuppressWarnings("unchecked")
    Consumer<Message> consumer = Mockito.mock(Consumer.class);
    transformer.getUnderlyingXsltTransformer().setMessageHandler(consumer);
    transformer.setErrorListener(listener);

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);
    transformer.transform(input, result);
    assertEquals(expected, resultWriter.toString());

    capturer.restore();
    assertTrue(capturer.out().isEmpty(), capturer.out());
    assertTrue(capturer.err().isEmpty(), capturer.err());

    {
      Mockito.verifyNoMoreInteractions(listener);
    }
    {
      ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
      Mockito.verify(consumer).accept(argument.capture());
      assertEquals("A message that does not terminate", argument.getValue().getStringValue());
    }
  }

  @Test
  public void testSaxonMessageTerminating() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    TransformerFactoryImpl factory = new net.sf.saxon.TransformerFactoryImpl();
    final StreamSource style = new StreamSource(
        XmlTransformerTests.class.getResource("Article/Messaging terminate.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("Article/One author.xml").toString());
    final String expected = Files.readString(
        Path.of(XmlTransformerTests.class.getResource("Article/One author.txt").toURI()));
    TransformerImpl transformer = (TransformerImpl) factory.newTransformer(style);
    ErrorListener listener = Mockito.mock(ErrorListener.class);
    @SuppressWarnings("unchecked")
    Consumer<Message> consumer = Mockito.mock(Consumer.class);
    transformer.getUnderlyingXsltTransformer().setMessageHandler(consumer);
    transformer.setErrorListener(listener);

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);
    assertThrows(TerminationException.class, () -> transformer.transform(input, result));
    assertEquals(expected, resultWriter.toString());

    capturer.restore();
    assertTrue(capturer.out().isEmpty(), capturer.out());
    assertTrue(capturer.err().isEmpty(), capturer.err());

    {
      ArgumentCaptor<TransformerException> argument =
          ArgumentCaptor.forClass(TransformerException.class);
      Mockito.verify(listener, Mockito.never()).warning(ArgumentMatchers.any());
      Mockito.verify(listener, Mockito.never()).error(ArgumentMatchers.any());
      Mockito.verify(listener).fatalError(argument.capture());
      assertTrue(argument.getValue().getMessage()
          .contains("Processing terminated by xsl:message at line"));
    }
    {
      ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
      Mockito.verify(consumer).accept(argument.capture());
      assertEquals("A message about premature end", argument.getValue().getStringValue());
    }
  }

  @Test
  public void testXalan() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    TransformerFactory factory = KnownFactory.XALAN.factory();
    final StreamSource style =
        new StreamSource(XmlTransformerTests.class.getResource("Article/Messaging.xsl").toString());
    final StreamSource input =
        new StreamSource(XmlTransformerTests.class.getResource("Article/One author.xml").toString());
    final String expected = Files.readString(
        Path.of(XmlTransformerTests.class.getResource("Article/One author.txt").toURI()));
    Transformer transformer = factory.newTransformer(style);
    ErrorListener listener = Mockito.mock(ErrorListener.class);
    transformer.setErrorListener(listener);

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);
    transformer.transform(input, result);
    assertEquals(expected, resultWriter.toString());

    capturer.restore();
    assertTrue(capturer.out().isEmpty(), capturer.out());
    assertTrue(capturer.err().isEmpty());

    ArgumentCaptor<TransformerException> argument =
        ArgumentCaptor.forClass(TransformerException.class);
    Mockito.verify(listener).warning(argument.capture());
    assertEquals("A message that does not terminate", argument.getValue().getMessage());
  }

  @Test
  public void testXalanNotPretty() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    TransformerFactory factory = KnownFactory.XALAN.factory();
    final StreamSource sourceOneline = new StreamSource(
        XmlTransformerTests.class.getResource("short namespace oneline.xml").toString());
    final String expected = Files.readString(
        Path.of(XmlTransformerTests.class.getResource("short namespace oneline.xml").toURI()));
    Transformer transformer = factory.newTransformer();
    ErrorListener listener = Mockito.mock(ErrorListener.class);
    transformer.setErrorListener(listener);

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);
    transformer.transform(sourceOneline, result);
    assertEquals(expected, resultWriter.toString());

    capturer.restore();
    assertTrue(capturer.out().isEmpty(), capturer.out());
    assertTrue(capturer.err().isEmpty());

    Mockito.verifyNoMoreInteractions(listener);
  }

  @Test
  public void testXalanIndent() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    TransformerFactory factory = KnownFactory.XALAN.factory();
    final StreamSource sourceOneline = new StreamSource(
        XmlTransformerTests.class.getResource("short namespace oneline.xml").toString());
    final String expected = Files
        .readString(Path.of(XmlTransformerTests.class.getResource("Article ns/Title two authors.xml").toURI()));
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputProperties.INDENT.localName(), "yes");
    ErrorListener listener = Mockito.mock(ErrorListener.class);
    transformer.setErrorListener(listener);

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);
    transformer.transform(sourceOneline, result);
    String expectedOnLeftMargin = expected.replaceAll("    ", "");
    assertEquals(expectedOnLeftMargin, XmlDeclarationCorrector.terminateXmlDeclaration(resultWriter.toString()));

    capturer.restore();
    assertTrue(capturer.out().isEmpty(), capturer.out());
    assertTrue(capturer.err().isEmpty());

    Mockito.verifyNoMoreInteractions(listener);
  }

  @Test
  public void testXalanPretty() throws Exception {
    final OutputCapturer capturer = OutputCapturer.capturer();
    capturer.capture();

    TransformerFactory factory = KnownFactory.XALAN.factory();
    final StreamSource sourceOneline = new StreamSource(
        XmlTransformerTests.class.getResource("short namespace oneline.xml").toString());
    final String expected = Files
        .readString(Path.of(XmlTransformerTests.class.getResource("Article ns/Title two authors.xml").toURI()));
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputProperties.INDENT.localName(), "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    ErrorListener listener = Mockito.mock(ErrorListener.class);
    transformer.setErrorListener(listener);

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);
    transformer.transform(sourceOneline, result);
    assertEquals(expected, XmlDeclarationCorrector.terminateXmlDeclaration(resultWriter.toString()));

    capturer.restore();
    assertTrue(capturer.out().isEmpty(), capturer.out());
    assertTrue(capturer.err().isEmpty());

    Mockito.verifyNoMoreInteractions(listener);
  }
}
