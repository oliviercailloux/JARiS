package io.github.oliviercailloux.jaris.xml.html;

import static io.github.oliviercailloux.jaris.xml.Resourcer.byteSource;
import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import io.github.oliviercailloux.jaris.xml.DomHelper;
import io.github.oliviercailloux.jaris.xml.Resourcer;
import io.github.oliviercailloux.jaris.xml.XmlException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.html.dom.HTMLBuilder;
import org.apache.html.dom.HTMLDOMImplementationImpl;
import org.apache.xerces.parsers.DOMParser;
import org.junit.jupiter.api.Test;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.html.HTMLDOMImplementation;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class DomHelperHtmlTests {
  @Test
  public void testSimpleDomHelper() throws Exception {
    CharSource simple = charSource("Html/Simple.html");
    DomHelper h = DomHelper.domHelper();
    XmlException e = assertThrows(XmlException.class, () -> h.asDocument(simple));
    String message = Throwables.getRootCause(e).getMessage();
    assertTrue(message.contains("/meta"), message);
  }

  @Test
  public void testSimpleDocBuilder() throws Exception {
    ByteSource simple = byteSource("Html/Simple.html");
    final DocumentBuilder builderNs = DocumentBuilderFactory.newNSInstance().newDocumentBuilder();
    try (InputStream is = simple.openStream()) {
      SAXParseException e = assertThrows(SAXParseException.class, () -> builderNs.parse(is));
      String message = Throwables.getRootCause(e).getMessage();
      assertTrue(message.contains("/meta"), message);
    }
    final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    try (InputStream is = simple.openStream()) {
      SAXParseException e = assertThrows(SAXParseException.class, () -> builder.parse(is));
      String message = Throwables.getRootCause(e).getMessage();
      assertTrue(message.contains("/meta"), message);
    }
  }

  @Test
  public void testDirectDocBuilder() throws Exception {
    ByteSource simple = byteSource("Html/Simple.html");
    DOMParser parser = new DOMParser();
    try (InputStream is = simple.openStream()) {
      SAXParseException e =
          assertThrows(SAXParseException.class, () -> parser.parse(new InputSource(is)));
      String message = Throwables.getRootCause(e).getMessage();
      assertTrue(message.contains("/meta"), message);
    }
    // URL input = Resources.getResource(Resourcer.class, "Html/Simple.html");
    // parser.parse(input.toString());
  }

  @Test
  public void testNoHtmlFeature() throws Exception {
    final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
    assertFalse(registry.getDOMImplementation("Core").hasFeature("HTML", "1.0"));
    /* https://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109/html.html#ID-1176245063 */
    assertFalse(registry.getDOMImplementation("Core").hasFeature("HTML", "2.0"));
    assertFalse(registry.getDOMImplementation("XML").hasFeature("HTML", "2.0"));
    assertNull(registry.getDOMImplementation("HTML"));
  }

  @Test
  public void testCreateNotProperHtml() throws Exception {
    HTMLDOMImplementation d = HTMLDOMImplementationImpl.getHTMLDOMImplementation();
    assertNotNull(d);
    HTMLDocument doc = d.createHTMLDocument("Ze title");
    String ser = DomHelper.domHelper().toString(doc);
    // Files.writeString(Path.of("zetitle.html"), ser);
    assertFalse(ser.contains("DOCTYPE"));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testSaxIncomplete() throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.getParser().setDocumentHandler(new HTMLBuilder());
  }
}
