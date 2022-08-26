package io.github.oliviercailloux.jaris.xml;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;

class SchemaHelperTests {

  @Test
  void testValidDocBook() throws Exception {
    final StreamSource docBook = new StreamSource(
        XmlTransformerTests.class.getResource("docbook simple article.xml").toString());
    final StreamSource docBookSchemaSource =
        new StreamSource("https://cdn.docbook.org/schema/5.0.1/xsd/docbook.xsd");

    final SchemaHelper schemaHelper = SchemaHelper.schemaHelper();
    final Schema docBookSchema = schemaHelper.asSchema(docBookSchemaSource);
    final ConformityChecker conformityChecker = schemaHelper.conformityChecker(docBookSchema);

    conformityChecker.verifyValid(docBook);
  }
}
