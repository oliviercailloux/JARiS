package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class DomRegistryCreationTests {
  @Test
  void testCreateXerces() throws Exception {
    DomHelper h = DomHelper.domHelper();
    DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
    final DOMImplementationList implLss = registry.getDOMImplementationList("LS");
    assertEquals(4, implLss.getLength());
    final DOMImplementation implLs0 = implLss.item(0);
    assertEquals(org.apache.xerces.dom.CoreDOMImplementationImpl.class, implLs0.getClass());
    final DOMImplementation implLs1 = implLss.item(1);
    assertEquals(org.apache.xerces.dom.DOMImplementationImpl.class, implLs1.getClass());
    final DOMImplementation implLs2 = implLss.item(2);
    assertEquals(org.apache.xerces.dom.PSVIDOMImplementationImpl.class, implLs2.getClass());
    final DOMImplementation implLs3 = implLss.item(3);
    assertEquals(org.apache.xerces.impl.xs.XSImplementationImpl.class, implLs3.getClass());
  }
}
