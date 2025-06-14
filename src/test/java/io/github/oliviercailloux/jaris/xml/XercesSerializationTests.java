package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Verify.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class XercesSerializationTests {
  @Test
  void testSerialize() throws Exception {
    DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
    final DOMImplementation implXml = registry.getDOMImplementation("XML");
    final Document doc = implXml.createDocument(null, "Root", null);
    final DOMImplementationLS implLs = (DOMImplementationLS) registry.getDOMImplementation("LS");
    LSSerializer ser = implLs.createLSSerializer();
    ser.getDomConfig().setParameter("format-pretty-print", true);
    ser.getDomConfig().setParameter("xml-declaration", true);
    final StringWriter writer = new StringWriter();
    final LSOutput output = implLs.createLSOutput();
    output.setEncoding(StandardCharsets.UTF_8.name());
    output.setCharacterStream(writer);
    boolean res = ser.write(doc, output);
    verify(res, "Write failed");
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<Root/>\n", writer.toString());
  }
}
