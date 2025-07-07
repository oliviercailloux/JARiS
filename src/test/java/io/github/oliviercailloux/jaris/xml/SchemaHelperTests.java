package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.VerifyException;
import io.github.oliviercailloux.docbook.DocBookResources;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;

class SchemaHelperTests {

  @Test
  void testValidDocBook() throws Exception {
    final StreamSource docBook =
        new StreamSource(XmlTransformerTests.class.getResource("DocBook/Simple.xml").toString());

    final SchemaHelper schemaHelper = SchemaHelper.schemaHelper();
    final Schema docBookSchema = schemaHelper.asSchema(DocBookResources.XSD_5_0_URI);
    final ConformityChecker conformityChecker = schemaHelper.conformityChecker(docBookSchema);

    conformityChecker.verifyValid(docBook);
  }

  @Test
  void testInvalidDocBook() throws Exception {
    final StreamSource docBook =
        new StreamSource(XmlTransformerTests.class.getResource("DocBook/Invalid.xml").toString());

    final SchemaHelper schemaHelper = SchemaHelper.schemaHelper();
    final Schema docBookSchema = schemaHelper.asSchema(DocBookResources.XSD_5_0_URI);
    final ConformityChecker conformityChecker = schemaHelper.conformityChecker(docBookSchema);

    assertThrows(VerifyException.class, () -> conformityChecker.verifyValid(docBook));
  }
}
