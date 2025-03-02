package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Helper for creating schemas and validating documents.
 * </p>
 * <p>
 * Instances of this class fail fast (throwing an exception) if encountering a warning, an error or
 * a fatalError upon reading a schema or validating a document.
 * </p>
 */
public class SchemaHelper {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaHelper.class);

  /**
   * Provides an instance of a schema helper, and initializes a schema factory using
   * {@link SchemaFactory#newInstance(String)} that reads W3C XML Schema 1.0.
   *
   * @return a schema helper instance.
   */
  public static SchemaHelper schemaHelper() {
    final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    return schemaHelper(factory);
  }

  /**
   * Provides an instance of a schema helper that will use the provided factory.
   *
   * @param factory the factory to be used by the returned schema helper.
   * @return a schema helper instance.
   */
  public static SchemaHelper schemaHelper(SchemaFactory factory) {
    factory.setErrorHandler(SchemaHelper.THROWING_ERROR_HANDLER);
    LOGGER.info("Using factory {}.", factory);
    return new SchemaHelper(factory);
  }

  private static final class LoggingOrThrowingErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException exception) {
      LOGGER.debug("Warning while processing.", exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXParseException {
      throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXParseException {
      throw exception;
    }
  }

  private static final class ThrowingErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException exception) throws SAXParseException {
      throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXParseException {
      throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXParseException {
      throw exception;
    }
  }

  static final ErrorHandler LOGGING_OR_THROWING_ERROR_HANDLER = new LoggingOrThrowingErrorHandler();
  private static final ErrorHandler THROWING_ERROR_HANDLER = new ThrowingErrorHandler();
  private final SchemaFactory factory;

  private SchemaHelper(SchemaFactory tf) {
    this.factory = checkNotNull(tf);
  }

  public Schema asSchema(ByteSource schemaSource) throws XmlException, IOException {
    final Schema asSchema;
    try (InputStream is = schemaSource.openStream()) {
      asSchema = factory.newSchema(new StreamSource(is));
    } catch (SAXException e) {
      throw new XmlException("While parsing schema.", e);
    }
    return asSchema;
  }

  public Schema asSchema(CharSource schemaSource) throws XmlException, IOException {
    final Schema asSchema;
    try (Reader r = schemaSource.openBufferedStream()) {
      asSchema = factory.newSchema(new StreamSource(r));
    } catch (SAXException e) {
      throw new XmlException("While parsing schema.", e);
    }
    return asSchema;
  }

  /**
   * Produces the schema corresponding to the given source, or throws.
   *
   * @param schemaSource the source
   * @return the corresponding schema
   * @throws XmlException iff an error is produced while parsing the schema.
   */
  public Schema asSchema(URI schemaSource) throws XmlException {
    return asSchema(new StreamSource(schemaSource.toString()));
  }

  /**
   * Produces the schema corresponding to the given source, or throws.
   *
   * @param schemaSource the source
   * @return the corresponding schema
   * @throws XmlException iff an error is produced while parsing the schema.
   */
  public Schema asSchema(Source schemaSource) throws XmlException {
    final Schema asSchema;
    try {
      asSchema = factory.newSchema(schemaSource);
    } catch (SAXException e) {
      throw new XmlException("While parsing schema.", e);
    }
    return asSchema;
  }

  /**
   * Returns a conformity checker that will validate documents against the provided schema.
   *
   * @param schema the schema to use for validating documents.
   * @return a conformity checker.
   * @throws XmlException iff the provided schema cannot be interpreted.
   */
  public ConformityChecker conformityChecker(Schema schema) throws XmlException {
    return ConformityChecker.withSchema(schema);
  }
}
