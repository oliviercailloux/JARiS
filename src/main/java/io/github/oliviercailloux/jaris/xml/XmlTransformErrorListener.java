package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines some exceptions as grave, according to their severity. Logs non-grave exceptions and
 * throws grave exceptions.
 */
class XmlTransformErrorListener implements ErrorListener {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformErrorListener.class);

  public static final XmlTransformErrorListener WARNING_NOT_GRAVE_ERROR_LISTENER =
      XmlTransformErrorListener.withWarningNotGrave();

  public static final XmlTransformErrorListener EVERYTHING_GRAVE_ERROR_LISTENER =
      XmlTransformErrorListener.withEverythingGrave();

  private ImmutableSet<Severity> graveSeverities;

  private static enum Severity {
    WARNING("Warning"), ERROR("Error"), FATAL("Fatal error"), THROWN("Exception");

    private String stringForm;

    private Severity(String stringForm) {
      this.stringForm = checkNotNull(stringForm);
    }

    public String asStringForm() {
      return stringForm;
    }
  }

  private static XmlTransformErrorListener withEverythingGrave() {
    return new XmlTransformErrorListener(ImmutableSet.copyOf(Severity.values()));
  }

  private static XmlTransformErrorListener withWarningNotGrave() {
    return new XmlTransformErrorListener(
        ImmutableSet.of(Severity.ERROR, Severity.FATAL, Severity.THROWN));
  }

  private XmlTransformErrorListener(Set<Severity> graveSeverities) {
    this.graveSeverities = ImmutableSet.copyOf(graveSeverities);

  }

  @Override
  public void warning(TransformerException exception) throws TransformerException {
    enact(exception, Severity.WARNING);
  }

  @Override
  public void error(TransformerException exception) throws TransformerException {
    enact(exception, Severity.ERROR);
  }

  @Override
  public void fatalError(TransformerException exception) throws TransformerException {
    enact(exception, Severity.FATAL);
  }

  private void enact(TransformerException exception, Severity severity)
      throws TransformerException {
    if (graveSeverities.contains(severity)) {
      throw exception;
    }
    LOGGER.debug(severity.asStringForm() + " while processing.", exception);
  }
}
