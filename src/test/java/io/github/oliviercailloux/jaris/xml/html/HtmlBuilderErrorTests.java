package io.github.oliviercailloux.jaris.xml.html;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import io.github.oliviercailloux.jaris.xml.Resourcer;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class HtmlBuilderErrorTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(HtmlBuilderErrorTests.class);

  private static final class ThrowingErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException exception) throws SAXParseException {
      LOGGER.info("Warning while processing.", exception);
      throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXParseException {
      LOGGER.info("Fatal while processing.", exception);
      throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXParseException {
      LOGGER.info("Error while processing.", exception);
      throw exception;
    }
  }

  @Test
  public void testHtmlIso() throws Exception {
    URL resource = Resources.getResource(Resourcer.class, "Html/Encoding ISO.html");
    String uri = resource.toString();
    HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
    builder.setErrorHandler(new ThrowingErrorHandler());
    // logs no warning, does not throw
    builder.parse(uri);
    builder.setErrorHandler(new ThrowingErrorHandler());
    // logs a warning then throws
    builder.parse(uri);
  }
}
