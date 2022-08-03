package io.github.oliviercailloux.jaris.xml;

import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;

class SchemaHelperTests {

  @Test
  void testValidDocBook() throws Exception {
    final StreamSource docBookSchema =
        new StreamSource("https://cdn.docbook.org/schema/5.0.1/xsd/docbook.xsd");
    final StreamSource docBook = new StreamSource(
        XmlTransformerTests.class.getResource("docbook simple article.xml").toString());
    final ConformityChecker conformityChecker =
        SchemaHelper.schemaHelper().getConformityChecker(docBookSchema);
    conformityChecker.verifyValid(docBook);
  }
}
