package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.VerifyException;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RngCheckerTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(RngCheckerTests.class);
  
  static final String RELAXNG_FACTORY_PROPERTY = "javax.xml.validation.SchemaFactory:http://relaxng.org/ns/structure/1.0";
  static final String RELAXNG_FACTORY_VALUE = "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory";

  @Test
  public void testAutoFactoryFails() throws Exception {
    assertThrows(IllegalArgumentException.class,
        () -> SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI));
  }

  @SetSystemProperty(key = RELAXNG_FACTORY_PROPERTY, value = RELAXNG_FACTORY_VALUE)
  @Test
  public void testAutoFactoryExplicit() throws Exception {
    assertEquals(SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI, RELAXNG_FACTORY_PROPERTY);
    assertEquals(XMLSyntaxSchemaFactory.class.getName(), RELAXNG_FACTORY_VALUE);
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
    assertNotNull(schemaFactory);
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
