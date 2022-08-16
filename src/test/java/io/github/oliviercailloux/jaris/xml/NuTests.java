package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import nu.validator.client.EmbeddedValidator;
import nu.validator.messages.GnuMessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.messages.TextMessageEmitter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.source.SourceCode;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class NuTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(NuTests.class);

  private static final String MSG_SUCCESS = "Document checking completed. No errors found.";
  private static final String MSG_FAIL = "Document checking completed.";
  private static final String EXTENSION_ERROR =
      "File was not checked. Files must have .html, .xhtml, .htm, or .xht extensions.";

  @Test
  void testEmbeddedValidatorAccepts() throws Exception {
    final Path html = Path.of(getClass().getResource("simple and valid.html").toURI());
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
  void testUnwrappedRejects() throws Exception {
    final Path path = Path.of(getClass().getResource("invalid.html").toURI());
    final boolean asciiQuotes = false;
    final boolean detectLanguages = false;
    final boolean forceHtml = false;
    final boolean loadEntities = false;
    final boolean noStream = false;
    final SimpleDocumentValidator validator =
        new SimpleDocumentValidator(true, false, !detectLanguages);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    boolean showSource = true;
    boolean batchMode = true;
    final GnuMessageEmitter emitter = new GnuMessageEmitter(out, asciiQuotes);
    final MessageEmitterAdapter adapter = new MessageEmitterAdapter(null, validator.getSourceCode(),
        showSource, new ImageCollector(validator.getSourceCode()), 0, batchMode, emitter);
    adapter.setErrorsOnly(false);
    adapter.setHtml(true);
    adapter.start(null);
    final org.xml.sax.ErrorHandler errorHandler = adapter;
    try {
      validator.setUpMainSchema(EmbeddedValidator.SCHEMA_URL, new SystemErrErrorHandler());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    validator.setUpValidatorAndParsers(errorHandler, noStream, loadEntities);

    final AtomicBoolean used = new AtomicBoolean(false);
    if (!used.compareAndSet(false, true)) {
      throw new IllegalStateException("OneOffValidator instances are not reusable");
    }
    try {
      if (Files.notExists(path) || !Files.isReadable(path)) {
        errorHandler
            .warning(new SAXParseException("File not found.", null, path.toString(), -1, -1));
      } else if (isXhtml(path.toFile())) {
        if (forceHtml) {
          validator.checkHtmlFile(path.toFile(), true);
        } else {
          validator.checkXmlFile(path.toFile());
        }
      } else if (isHtml(path.toFile())) {
        validator.checkHtmlFile(path.toFile(), true);
      } else {
        errorHandler.warning(new SAXParseException(EXTENSION_ERROR, null, path.toString(), -1, -1));
      }
    } catch (SAXException e) {
      errorHandler.warning(new SAXParseException(e.getMessage(), null, path.toString(), -1, -1));
    }

    adapter.end(MSG_SUCCESS, MSG_FAIL, "");
    final String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
    LOGGER.info("Rejected invalid: {}.", output);
    assertFalse(output.isEmpty());
  }

  /**
   * https://gist.github.com/vincent-zurczak/23e0f626eaafab96cb32
   */
  @Test
  void testVZ() throws Exception {
    final Path path = Path.of(getClass().getResource("invalid.html").toURI());

    InputStream in = new ByteArrayInputStream(htmlContent.getBytes("UTF-8"));
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    SourceCode sourceCode = new SourceCode();
    ImageCollector imageCollector = new ImageCollector(sourceCode);
    boolean showSource = false;
    MessageEmitter emitter = new TextMessageEmitter(out, false);
    MessageEmitterAdapter errorHandler =
        new MessageEmitterAdapter(sourceCode, showSource, imageCollector, 0, false, emitter);
    errorHandler.setErrorsOnly(true);

    SimpleDocumentValidator validator = new SimpleDocumentValidator();
    validator.setUpMainSchema("http://s.validator.nu/html5-rdfalite.rnc",
        new SystemErrErrorHandler());
    validator.setUpValidatorAndParsers(errorHandler, true, false);
    validator.checkHtmlInputSource(new InputSource(in));

    return 0 == errorHandler.getErrors();

    final String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
    LOGGER.info("Rejected invalid: {}.", output);
    assertFalse(output.isEmpty());
  }

  private boolean isXhtml(File file) {
    String name = file.getName();
    return name.endsWith(".xhtml") || name.endsWith(".xht");
  }

  private boolean isHtml(File file) {
    String name = file.getName();
    return name.endsWith(".html") || name.endsWith(".htm");
  }
}
