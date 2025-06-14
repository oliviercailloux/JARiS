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
    final CharSource style = charSource("Article/To text.xsl");
    final CharSource input = charSource("Article/Two authors.xml");
    final String expected = charSource("Article/Two authors.txt").read();
    XmlTransformerFactory f = XmlTransformerFactory.usingFactory(KnownFactory.JDK.factory());
    assertEquals(expected, f.pedantic().usingStylesheet(style).charsToChars(input));
  }

  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  public void testMissingFactory(KnownFactory factory) throws Exception {
    assertThrows(ClassNotFoundException.class,
        () -> XmlTransformerFactory.usingFactory(factory.factory()));
  }
}
