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
import io.github.oliviercailloux.jaris.xml.DomHelper;
import io.github.oliviercailloux.jaris.xml.XmlException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.html.dom.HTMLBuilder;
import org.apache.html.dom.HTMLDOMImplementationImpl;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.DOMParserImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.html.HTMLDOMImplementation;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class DomHelperHtmlTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperHtmlTests.class);

  @Test
  public void testDomHelperFails() throws Exception {
    CharSource simple = charSource("Html/Simple.html");
    DomHelper h = DomHelper.domHelper();
    XmlException e = assertThrows(XmlException.class, () -> h.asDocument(simple));
    String message = Throwables.getRootCause(e).getMessage();
    assertTrue(message.contains("/meta"), message);
  }

  @Test
  public void testDocBuilderFails() throws Exception {
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
  public void testXercesLacksHtmlFeature() throws Exception {
    final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
    DOMImplementation impl = registry.getDOMImplementation("Core");
    assertEquals(org.apache.xerces.dom.CoreDOMImplementationImpl.class, impl.getClass());
    assertFalse(impl.hasFeature("HTML", "1.0"));
    /* https://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109/html.html#ID-1176245063 */
    assertFalse(registry.getDOMImplementation("Core").hasFeature("HTML", "2.0"));
    assertFalse(registry.getDOMImplementation("XML").hasFeature("HTML", "2.0"));
    assertNull(registry.getDOMImplementation("HTML"));
    assertNull(registry.getDOMImplementation("HTML 1.0"));
  }

  @Test
  public void testXercesDomParserFails() throws Exception {
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
  public void testXercesDomParserImplFails() throws Exception {
    ByteSource simple = byteSource("Html/Simple.html");
    // HTMLDOMImplementationImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    LSParser deser = new DOMParserImpl("org.apache.xerces.parsers.XIncludeAwareParserConfiguration", null);
    final Document docRead;
    final LSInput lsInput = new DOMInputImpl();
    try (InputStream is = simple.openStream()) {
      lsInput.setByteStream(is);
      LSException e =
          assertThrows(LSException.class, () -> deser.parse(lsInput));
      String message = Throwables.getRootCause(e).getMessage();
      assertTrue(message.contains("/meta"), message);
    }
  }

  @Test
  public void testXercesHtmlMissesDoctype() throws Exception {
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
