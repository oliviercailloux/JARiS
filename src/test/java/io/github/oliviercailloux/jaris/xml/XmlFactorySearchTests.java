package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.xml.transform.TransformerFactory;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RestoreSystemProperties;

public class XmlFactorySearchTests {
  
  private static final String XALAN_FACTORY = "org.apache.xalan.processor.TransformerFactoryImpl";

  @RestoreSystemProperties
  @Test
  void testLoad() throws Exception{
    assertEquals(KnownFactory.JDK.factory().getClass(), TransformerFactory.newDefaultInstance().getClass());
    System.setProperty(XmlTransformerFactory.FACTORY_PROPERTY, XALAN_FACTORY);
    assertEquals(KnownFactory.XALAN.factory().getClass(), TransformerFactory.newInstance().getClass());
  }
}
