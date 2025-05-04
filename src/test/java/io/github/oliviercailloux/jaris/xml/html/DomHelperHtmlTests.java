package io.github.oliviercailloux.jaris.xml.html;

import static io.github.oliviercailloux.jaris.xml.Resourcer.byteSource;
import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.SAXParseException;

public class DomHelperHtmlTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DomHelperHtmlTests.class);
  
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
  public void testNoHtmlFeature() throws Exception {
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
}
