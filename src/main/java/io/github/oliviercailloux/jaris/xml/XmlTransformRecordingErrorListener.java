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
 * Defines some exceptions as grave, according to their severity. Always logs non-grave
 * exceptions. Logs grave exceptions iff there is more than one, in which case, logs all
 * exceptions in the order of encounter (so instances have memory).
 */
class XmlTransformRecordingErrorListener implements ErrorListener {
  @SuppressWarnings("unused")
  private static final Logger LOGGER =
      LoggerFactory.getLogger(XmlTransformRecordingErrorListener.class);

  public static final XmlTransformRecordingErrorListener WARNING_NOT_GRAVE_ERROR_LISTENER =
      XmlTransformRecordingErrorListener.withWarningNotGrave();

  public static final XmlTransformRecordingErrorListener EVERYTHING_GRAVE_ERROR_LISTENER =
      XmlTransformRecordingErrorListener.withEverythingGrave();

  /**
   * Logs incoming exceptions in order; except that if there is a unique grave exception that is
   * also the last exception received, it is not logged.
   */
  private static class ExceptionsRecorder {
    private final ImmutableSet<XmlTransformRecordingErrorListener.Severity> graveSeverities;
    private Optional<XmlTransformRecordingErrorListener.QualifiedTransformerException> firstGraveException;
    private final Set<XmlTransformRecordingErrorListener.QualifiedTransformerException> allNonThrownExceptions =
        new LinkedHashSet<>();

    public ExceptionsRecorder(Set<XmlTransformRecordingErrorListener.Severity> graveSeverities) {
      this.graveSeverities = ImmutableSet.copyOf(graveSeverities);
      reset();
    }

    /**
     * Logs any primal exception iff it exists and has not been logged yet, and logs the given
     * exception unless it is primal. Unless the given exception has severity THROWN and had been
     * seen already under a non-thrown severity.
     *
     * @param exception the exception to consider in supplement of any previous one
     */
    public void record(XmlTransformRecordingErrorListener.QualifiedTransformerException exception) {
      checkNotNull(exception);
      if (exception.severity == Severity.THROWN) {
        if (allNonThrownExceptions.stream().map(QualifiedTransformerException::getException)
            .anyMatch(e -> e.equals(exception.exception))) {
          LOGGER.debug("Skipping already seen exception {}.",
              exception.exception.getLocalizedMessage());
          return;
        }
      } else {
        allNonThrownExceptions.add(exception);
      }

      if (isGrave(exception)) {
        recordGrave(exception);
      } else {
        log(exception);
      }
    }

    public boolean isGrave(XmlTransformRecordingErrorListener.QualifiedTransformerException exception) {
      return graveSeverities.contains(exception.severity);
    }

    /**
     * Records or logs exceptions.
     * <p>
     * A grave exception recorded by this object is said to be <i>primal</i> iff it is the first
     * grave exception ever received; <i>secundary</i> iff it is the second grave exception ever
     * received; <i>supplemental</i> otherwise.
     * </p>
     * <ul>
     * <li>Records this exception iff it is primal.</li>
     * <li>Logs the primal grave exception (if any) iff this one is secundary; equivalently, logs
     * the primal exception iff it was recorded previously but had not yet been logged.</li>
     * <li>Then, logs this exception iff it is not primal.</li>
     * </ul>
     *
     * @param exception the exception to record or log.
     */
    private void recordGrave(XmlTransformRecordingErrorListener.QualifiedTransformerException exception) {
      final boolean isPrimal = firstGraveException.isEmpty();
      firstGraveException = Optional.of(firstGraveException.orElse(exception));
      if (!isPrimal) {
        log(exception);
      }
    }

    private void log(XmlTransformRecordingErrorListener.QualifiedTransformerException exception) {
      firstGraveException.ifPresent(QualifiedTransformerException::ensureLogged);
      exception.ensureLogged();
    }

    public boolean hasGraveException() {
      return firstGraveException.isPresent();
    }

    public XmlTransformRecordingErrorListener.QualifiedTransformerException getFirstGraveException() {
      return firstGraveException.orElseThrow();
    }

    public void reset() {
      firstGraveException = Optional.empty();
    }
  }

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

  private static class QualifiedTransformerException {
    private final TransformerException exception;
    private final XmlTransformRecordingErrorListener.Severity severity;
    private boolean hasBeenLogged;

    public QualifiedTransformerException(TransformerException exception, XmlTransformRecordingErrorListener.Severity severity) {
      this.exception = checkNotNull(exception);
      this.severity = checkNotNull(severity);
      hasBeenLogged = false;
    }

    public TransformerException getException() {
      return exception;
    }

    @SuppressWarnings("unused")
    public XmlTransformRecordingErrorListener.Severity getSeverity() {
      return severity;
    }

    @SuppressWarnings("unused")
    public boolean hasBeenLogged() {
      return hasBeenLogged;
    }

    @SuppressWarnings("unused")
    public void log() {
      checkState(!hasBeenLogged);
      ensureLogged();
    }

    public void ensureLogged() {
      LOGGER.debug(severity.asStringForm() + " while processing.", exception);
      hasBeenLogged = true;
    }
  }

  private final XmlTransformRecordingErrorListener.ExceptionsRecorder recorder;

  private static XmlTransformRecordingErrorListener withEverythingGrave() {
    return new XmlTransformRecordingErrorListener(ImmutableSet.copyOf(Severity.values()));
  }

  private static XmlTransformRecordingErrorListener withWarningNotGrave() {
    return new XmlTransformRecordingErrorListener(
        ImmutableSet.of(Severity.ERROR, Severity.FATAL, Severity.THROWN));
  }

  private XmlTransformRecordingErrorListener(Set<XmlTransformRecordingErrorListener.Severity> graveSeverities) {
    this.recorder = new ExceptionsRecorder(graveSeverities);
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

  public void thrown(TransformerException exception) {
    final XmlTransformRecordingErrorListener.QualifiedTransformerException xExc =
        new QualifiedTransformerException(exception, Severity.THROWN);
    recorder.record(xExc);
  }

  private void enact(TransformerException exception, XmlTransformRecordingErrorListener.Severity severity)
      throws TransformerException {
    final XmlTransformRecordingErrorListener.QualifiedTransformerException xExc =
        new QualifiedTransformerException(exception, severity);
    recorder.record(xExc);
    if (recorder.isGrave(xExc)) {
      throw exception;
    }
  }

  public void throwFirstGraveAsXmlException() throws XmlException {
    if (recorder.hasGraveException()) {
      throw new XmlException("Could not transform the provided document.",
          recorder.getFirstGraveException().exception);
    }
  }

  public void reset() {
    recorder.reset();
  }
}