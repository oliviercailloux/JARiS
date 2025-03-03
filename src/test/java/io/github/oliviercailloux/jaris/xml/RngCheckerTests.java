package io.github.oliviercailloux.jaris.xml;

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
import org.junitpioneer.jupiter.RestoreSystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RngCheckerTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(RngCheckerTests.class);

  @Test
  public void testAutoFactoryFails() throws Exception {
    assertThrows(IllegalArgumentException.class,
        () -> SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI));
  }

  @RestoreSystemProperties
  @Test
  public void testAutoFactoryExplicit() throws Exception {
    System.setProperty(SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI,
        XMLSyntaxSchemaFactory.class.getName());
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
    assertNotNull(schemaFactory);
  }

  @Test
  public void testDocbookSimple() throws Exception {
    final SchemaFactory factory = new XMLSyntaxSchemaFactory();
    final SchemaHelper helper = SchemaHelper.schemaHelper(factory);
    final ByteSource schemaSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("DocBook/docbook.rng"));
    final Schema compiledSchema = helper.asSchema(schemaSource);
    final ByteSource documentSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("DocBook/Simple.xml"));
    helper.conformityChecker(compiledSchema).verifyValid(documentSource);
  }

  @Test
  public void testDocbookInvalid() throws Exception {
    final SchemaFactory factory = new XMLSyntaxSchemaFactory();
    final SchemaHelper helper = SchemaHelper.schemaHelper(factory);
    final ByteSource schemaSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("DocBook/docbook.rng"));
    final Schema compiledSchema = helper.asSchema(schemaSource);
    final ByteSource documentSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("DocBook/Invalid.xml"));
    assertThrows(VerifyException.class,
        () -> helper.conformityChecker(compiledSchema).verifyValid(documentSource));
  }

  @Test
  public void testDocbookComplex() throws Exception {
    final SchemaFactory factory = new XMLSyntaxSchemaFactory();
    final SchemaHelper helper = SchemaHelper.schemaHelper(factory);
    final ByteSource schemaSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("DocBook/docbook.rng"));
    final Schema compiledSchema = helper.asSchema(schemaSource);
    final ByteSource documentSource =
        Resources.asByteSource(RngCheckerTests.class.getResource("DocBook/Howto.xml"));
    helper.conformityChecker(compiledSchema).verifyValid(documentSource);
  }
}
