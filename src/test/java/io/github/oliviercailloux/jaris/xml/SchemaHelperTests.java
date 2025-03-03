package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.VerifyException;
import java.net.URI;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;

class SchemaHelperTests {

  @Test
  void testValidDocBook() throws Exception {
    final StreamSource docBook = new StreamSource(
        XmlTransformerTests.class.getResource("DocBook/Simple.xml").toString());
    final URI docBookSchemaSource =
        URI.create("https://cdn.docbook.org/schema/5.0.1/xsd/docbook.xsd");

    final SchemaHelper schemaHelper = SchemaHelper.schemaHelper();
    final Schema docBookSchema = schemaHelper.asSchema(docBookSchemaSource);
    final ConformityChecker conformityChecker = schemaHelper.conformityChecker(docBookSchema);

    conformityChecker.verifyValid(docBook);
  }

  @Test
  void testInvalidDocBook() throws Exception {
    final StreamSource docBook = new StreamSource(
        XmlTransformerTests.class.getResource("DocBook/Invalid.xml").toString());
    final URI docBookSchemaSource =
        URI.create("https://cdn.docbook.org/schema/5.0.1/xsd/docbook.xsd");

    final SchemaHelper schemaHelper = SchemaHelper.schemaHelper();
    final Schema docBookSchema = schemaHelper.asSchema(docBookSchemaSource);
    final ConformityChecker conformityChecker = schemaHelper.conformityChecker(docBookSchema);

    assertThrows(VerifyException.class, () -> conformityChecker.verifyValid(docBook));
  }
}
