package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import nu.validator.client.EmbeddedValidator;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class NuTests {
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

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(NuTests.class);

  @Test
  void testEmbeddedValidatorAccepts() throws Exception {
    final Path html = Path.of(getClass().getResource("simple and valid.html").toURI());
    final EmbeddedValidator validator = new EmbeddedValidator();
    validator.setOutputFormat(EmbeddedValidator.OutputFormat.GNU);
    final String output = validator.validate(html);
    assertTrue(output.isEmpty(), output);
  }

  @Test
  void testEmbeddedValidatorAcceptsXhtml() throws Exception {
    final Path html = Path.of(getClass().getResource("simple.xhtml").toURI());
    final EmbeddedValidator validator = new EmbeddedValidator();
    validator.setOutputFormat(EmbeddedValidator.OutputFormat.GNU);
    final String output = validator.validate(html);
    assertTrue(output.isEmpty(), output);
  }

  @Test
  void testEmbeddedValidatorRejects() throws Exception {
    final Path html = Path.of(getClass().getResource("invalid.html").toURI());
    final EmbeddedValidator validator = new EmbeddedValidator();
    validator.setOutputFormat(EmbeddedValidator.OutputFormat.GNU);
    final String output = validator.validate(html);
    LOGGER.info("Rejected invalid: {}.", output);
    assertFalse(output.isEmpty());
  }

  @Test
  void testUnwrappedAccepts() throws Exception {
    final Path path = Path.of(getClass().getResource("simple and valid.html").toURI());
    final boolean detectLanguages = false;
    final boolean loadEntities = false;
    final boolean noStream = false;
    final SimpleDocumentValidator validator =
        new SimpleDocumentValidator(true, false, !detectLanguages);
    final org.xml.sax.ErrorHandler errorHandler = new ThrowingErrorHandler();
    try {
      validator.setUpMainSchema(EmbeddedValidator.SCHEMA_URL, new SystemErrErrorHandler());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    validator.setUpValidatorAndParsers(errorHandler, noStream, loadEntities);

    assertDoesNotThrow(() -> validator.checkHtmlFile(path.toFile(), true));

  }

  @Test
  void testUnwrappedRejects() throws Exception {
    final Path path = Path.of(getClass().getResource("invalid.html").toURI());
    final boolean detectLanguages = false;
    final boolean loadEntities = false;
    final boolean noStream = false;
    final SimpleDocumentValidator validator =
        new SimpleDocumentValidator(true, false, !detectLanguages);
    final org.xml.sax.ErrorHandler errorHandler = new ThrowingErrorHandler();
    try {
      validator.setUpMainSchema(EmbeddedValidator.SCHEMA_URL, new SystemErrErrorHandler());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    validator.setUpValidatorAndParsers(errorHandler, noStream, loadEntities);

    assertThrows(SAXException.class, () -> validator.checkHtmlFile(path.toFile(), true));

  }
}
