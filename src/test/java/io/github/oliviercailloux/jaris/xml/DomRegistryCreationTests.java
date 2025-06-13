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
  void testCreate() throws Exception {
    DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
    final DOMImplementationList implLss = registry.getDOMImplementationList("LS");
    assertEquals(4, implLss.getLength());
    final DOMImplementation implLs0 = implLss.item(0);
    assertEquals("com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl", implLs0.getClass().getName());
    final DOMImplementation implLs1 = implLss.item(1);
    assertEquals("com.sun.org.apache.xerces.internal.dom.DOMImplementationImpl", implLs1.getClass().getName());
    final DOMImplementation implLs2 = implLss.item(2);
    assertEquals("com.sun.org.apache.xerces.internal.dom.PSVIDOMImplementationImpl", implLs2.getClass().getName());
    final DOMImplementation implLs3 = implLss.item(3);
    assertEquals("com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl", implLs3.getClass().getName());
  }
}
