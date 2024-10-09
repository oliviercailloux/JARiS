package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.VerifyException;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.Test;

public class RngCheckerTests {
  @Test
  public void testAutoFactoryFails() throws Exception {
    assertThrows(IllegalArgumentException.class,
        () -> SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI));
  }

  @Test
  public void testDocbookSimple() throws Exception {
    final SchemaFactory factory = new XMLSyntaxSchemaFactory();
    final SchemaHelper helper = SchemaHelper.schemaHelper(factory);
    final ByteSource schemaSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("docbook.rng"));
    final Schema compiledSchema = helper.asSchema(schemaSource);
    final ByteSource documentSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("docbook simple article.xml"));
    helper.conformityChecker(compiledSchema).verifyValid(documentSource);
  }

  @Test
  public void testDocbookInvalid() throws Exception {
    final SchemaFactory factory = new XMLSyntaxSchemaFactory();
    final SchemaHelper helper = SchemaHelper.schemaHelper(factory);
    final ByteSource schemaSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("docbook.rng"));
    final Schema compiledSchema = helper.asSchema(schemaSource);
    final ByteSource documentSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("docbook invalid.xml"));
    assertThrows(VerifyException.class,
        () -> helper.conformityChecker(compiledSchema).verifyValid(documentSource));
  }

  @Test
  public void testDocbookComplex() throws Exception {
    final SchemaFactory factory = new XMLSyntaxSchemaFactory();
    final SchemaHelper helper = SchemaHelper.schemaHelper(factory);
    final ByteSource schemaSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("docbook.rng"));
    final Schema compiledSchema = helper.asSchema(schemaSource);
    final ByteSource documentSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("docbook howto.xml"));
    helper.conformityChecker(compiledSchema).verifyValid(documentSource);
  }
}
