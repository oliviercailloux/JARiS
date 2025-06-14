package io.github.oliviercailloux.jaris.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

class SaxErrorHandlers {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(SaxErrorHandlers.class);

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
  static final ErrorHandler THROWING_ERROR_HANDLER = new ThrowingErrorHandler();
}
