package io.github.oliviercailloux.jaris.xml;
import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.CharSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class XmlTransformerWithoutOptionalIT {
  @Test
  public void testHasFactory() throws Exception {
    assertDoesNotThrow(() -> XmlTransformerFactory.usingFactory(KnownFactory.JDK.factory()));
  }
  
  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  public void testMissingFactory(KnownFactory factory) throws Exception {
    assertThrows(ClassNotFoundException.class, () -> XmlTransformerFactory.usingFactory(factory.factory()));
  }
}
