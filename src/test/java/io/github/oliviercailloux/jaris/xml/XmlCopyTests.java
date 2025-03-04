package io.github.oliviercailloux.jaris.xml;

import static io.github.oliviercailloux.jaris.xml.Resourcer.charSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.io.CharSource;
import io.github.oliviercailloux.jaris.xml.XmlTransformerFactory.OutputProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.w3c.dom.Document;

public class XmlCopyTests {

  @Test
  void testJdk() throws Exception {
    final CharSource source = charSource("Article ns/Title two authors.xml");
    XmlTransformer copier =
        XmlTransformerFactory.usingFactory(KnownFactory.JDK.factory()).usingEmptyStylesheet();
    String directResult = copier.charsToChars(source);
    String patched =
        XmlDeclarationCorrector.terminateXmlDeclaration(directResult).replaceAll("\\h+\n", "");
    assertEquals(source.read(), patched);
  }

  @Test
  void testJdkNoStartIndent() throws Exception {
    final CharSource source = charSource("Article ns/Title two authors.xml");
    String textOneLine = Resourcer.titleTwoAuthorsOneLine();
    XmlTransformer copier =
        XmlTransformerFactory.usingFactory(KnownFactory.JDK.factory()).usingEmptyStylesheet();
    String resultFromOneLine = copier.charsToChars(CharSource.wrap(textOneLine));
    assertEquals(source.read(), XmlDeclarationCorrector.terminateXmlDeclaration(resultFromOneLine));
  }

  @Test
  void testXalan() throws Exception {
    final CharSource source = charSource("Article ns/Title two authors.xml");
    XmlTransformer copier =
        XmlTransformerFactory.usingFactory(KnownFactory.XALAN.factory()).usingEmptyStylesheet();
    String directResult = copier.charsToChars(source);
    assertEquals(source.read(), XmlDeclarationCorrector.terminateXmlDeclaration(directResult));
  }

  @Test
  void testSaxon() throws Exception {
    final CharSource source = charSource("Article ns/Title two authors.xml");
    XmlTransformer copier =
        XmlTransformerFactory.usingFactory(KnownFactory.SAXON.factory()).usingEmptyStylesheet();
    String directResult = copier.charsToChars(source);
    assertEquals(source.read().replaceAll("    ", "   "), directResult);
  }

  @ParameterizedTest
  @EnumSource(names = {"XALAN", "SAXON"})
  void testIndependentFromStartIndent(KnownFactory factory) throws Exception {
    final CharSource source = charSource("Article ns/Title two authors.xml");
    String textOneLine = Resourcer.titleTwoAuthorsOneLine();
    XmlTransformer copier =
        XmlTransformerFactory.usingFactory(factory.factory()).usingEmptyStylesheet();
    String resultFromOriginal = copier.charsToChars(source);
    String resultFromOneLine = copier.charsToChars(CharSource.wrap(textOneLine));
    assertEquals(resultFromOriginal, resultFromOneLine);
  }

  @ParameterizedTest
  @EnumSource
  void testNotPretty(KnownFactory factory) throws Exception {
    String textOneLine = Resourcer.titleTwoAuthorsOneLine();
    XmlTransformer copier = XmlTransformerFactory.usingFactory(factory.factory())
        .usingEmptyStylesheet(OutputProperties.noIndent());
    String directResult = copier.charsToChars(CharSource.wrap(textOneLine));
    assertEquals(textOneLine, directResult);
  }

  @ParameterizedTest
  @EnumSource
  void testThroughDom(KnownFactory factory) throws Exception {
    final CharSource source = charSource("Article ns/Title two authors.xml");
    XmlTransformer copier =
        XmlTransformerFactory.usingFactory(factory.factory()).usingEmptyStylesheet();
    Document docCopy = copier.charsToDom(source);
    assertEquals(source.read(), DomHelper.domHelper().toString(docCopy));
  }

  @ParameterizedTest
  @EnumSource
  void testThroughDomIndependentFromStartIndent(KnownFactory factory) throws Exception {
    final CharSource source = charSource("Article ns/Title two authors.xml");
    String textOneLine = Resourcer.titleTwoAuthorsOneLine();
    XmlTransformer copier =
        XmlTransformerFactory.usingFactory(factory.factory()).usingEmptyStylesheet();
    Document copyFromOriginal = copier.charsToDom(source);
    Document copyFromOneLine = copier.charsToDom(CharSource.wrap(textOneLine));
    DomHelper helper = DomHelper.domHelper();
    assertEquals(helper.toString(copyFromOriginal), helper.toString(copyFromOneLine));
  }
}
