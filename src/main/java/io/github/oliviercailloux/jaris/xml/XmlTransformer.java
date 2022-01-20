package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableSet;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * <p>
 * Instances of this class make a best effort to log warnings and to fail fast (throwing an
 * exception) if an error or a fatalError is raised during the parsing of the schema or of the
 * document to transform.
 * </p>
 */
public class XmlTransformer {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformer.class);

  public static final String FACTORY_PROPERTY = "javax.xml.transform.TransformerFactory";

  /**
   * Provides a transformer instance using the TransformerFactory builtin system-default
   * implementation.
   *
   * @return a transformer instance.
   */
  public static XmlTransformer usingSystemDefaultFactory() {
    final TransformerFactory factory = TransformerFactory.newDefaultInstance();
    return usingFactory(factory);
  }

  /**
   * Provides a transformer instance using the TransformerFactory found using the
   * <a href="../../../module-summary.html#LookupMechanism">JAXP Lookup Mechanism</a>, thus,
   * equivalent to the one obtained with {@link TransformerFactory#newInstance()}.
   * <p>
   * The system property that determines which Factory implementation to create is
   * {@link #FACTORY_PROPERTY}.
   * </p>
   *
   * @return a transformer instance.
   */
  public static XmlTransformer usingFoundFactory() {
    final TransformerFactory factory = TransformerFactory.newInstance();
    return usingFactory(factory);
  }

  /**
   * Provides a transformer instance using the provided factory.
   *
   * @param factory the factory to use.
   * @return a transformer instance.
   */
  public static XmlTransformer usingFactory(TransformerFactory factory) {
    return generalTransformer(factory, RecordingErrorListener.WARNING_NOT_GRAVE_ERROR_LISTENER);
  }

  /**
   * Provides a transformer instance using the TransformerFactory builtin system-default
   * implementation.
   * <p>
   * The returned transformer throws exceptions upon encountering warnings (as well as errors).
   * </p>
   *
   * @return a transformer instance.
   */
  public static XmlTransformer pedanticTransformer() {
    final TransformerFactory factory = TransformerFactory.newDefaultInstance();
    return generalTransformer(factory, RecordingErrorListener.EVERYTHING_GRAVE_ERROR_LISTENER);
  }

  /**
   * Provides a transformer instance using the provided factory.
   * <p>
   * The returned transformer throws exceptions upon encountering warnings (as well as errors).
   * </p>
   *
   * @param factory the factory to use.
   * @return a transformer instance.
   */
  public static XmlTransformer pedanticTransformer(TransformerFactory factory) {
    return generalTransformer(factory, RecordingErrorListener.EVERYTHING_GRAVE_ERROR_LISTENER);
  }

  private static XmlTransformer generalTransformer(TransformerFactory factory,
      RecordingErrorListener errorListener) {
    factory.setErrorListener(errorListener);
    /*
     * https://www.saxonica.com/html/documentation/configuration/config-features.html;
     * https://stackoverflow.com/a/4699749.
     *
     * The default implementation (from Apache Xalan) seems to have a bug preventing it from using
     * the provided error listener, see https://stackoverflow.com/a/21209904/.
     */
    try {
      factory.setAttribute("http://saxon.sf.net/feature/messageEmitterClass",
          "net.sf.saxon.serialize.MessageWarner");
    } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
      LOGGER.debug("saxon messageEmitterClass attribute not supported, not set");
    }
    LOGGER.info("Using factory {}.", factory);
    return new XmlTransformer(factory);
  }

  /**
   * Defines some exceptions as grave, according to their severity. Always logs non-grave
   * exceptions. Logs grave exceptions iff there is more than one, in which case, logs all
   * exceptions in the order of encounter (so instances have memory).
   */
  private static class RecordingErrorListener implements ErrorListener {
    public static final RecordingErrorListener WARNING_NOT_GRAVE_ERROR_LISTENER =
        RecordingErrorListener.withWarningNotGrave();

    public static final RecordingErrorListener EVERYTHING_GRAVE_ERROR_LISTENER =
        RecordingErrorListener.withEverythingGrave();

    /**
     * Logs incoming exceptions in order; except that if there is a unique grave exception that is
     * also the last exception received, it is not logged.
     */
    private static class ExceptionsRecorder {
      private final ImmutableSet<Severity> graveSeverities;
      private Optional<QualifiedTransformerException> firstGraveException;
      private final Set<QualifiedTransformerException> allNonThrownExceptions = new LinkedHashSet<>();

      public ExceptionsRecorder(Set<Severity> graveSeverities) {
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
      public void record(QualifiedTransformerException exception) {
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

      public boolean isGrave(QualifiedTransformerException exception) {
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
      private void recordGrave(QualifiedTransformerException exception) {
        final boolean isPrimal = firstGraveException.isEmpty();
        firstGraveException = Optional.of(firstGraveException.orElse(exception));
        if (!isPrimal) {
          log(exception);
        }
      }

      private void log(QualifiedTransformerException exception) {
        firstGraveException.ifPresent(QualifiedTransformerException::ensureLogged);
        exception.ensureLogged();
      }

      public boolean hasGraveException() {
        return firstGraveException.isPresent();
      }

      public QualifiedTransformerException getFirstGraveException() {
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
      private final Severity severity;
      private boolean hasBeenLogged;

      public QualifiedTransformerException(TransformerException exception, Severity severity) {
        this.exception = checkNotNull(exception);
        this.severity = checkNotNull(severity);
        hasBeenLogged = false;
      }

      public TransformerException getException() {
        return exception;
      }

      @SuppressWarnings("unused")
      public Severity getSeverity() {
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

    private final ExceptionsRecorder recorder;

    private static RecordingErrorListener withEverythingGrave() {
      return new RecordingErrorListener(ImmutableSet.copyOf(Severity.values()));
    }

    private static RecordingErrorListener withWarningNotGrave() {
      return new RecordingErrorListener(
          ImmutableSet.of(Severity.ERROR, Severity.FATAL, Severity.THROWN));
    }

    private RecordingErrorListener(Set<Severity> graveSeverities) {
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
      final QualifiedTransformerException xExc = new QualifiedTransformerException(exception, Severity.THROWN);
      recorder.record(xExc);
    }

    private void enact(TransformerException exception, Severity severity)
        throws TransformerException {
      final QualifiedTransformerException xExc = new QualifiedTransformerException(exception, severity);
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

  private final TransformerFactory factory;

  private XmlTransformer(TransformerFactory tf) {
    this.factory = checkNotNull(tf);
    checkArgument(factory.getErrorListener() instanceof RecordingErrorListener);
  }

  /**
   * Transforms (internal use).
   *
   * @param document not empty
   * @param result the result holder
   * @param stylesheet may be empty
   */
  private void transformInternal(Source document, Result result, Source stylesheet) {
    checkNotNull(document);
    checkArgument(!document.isEmpty());
    checkNotNull(stylesheet);
    checkNotNull(result);

    final RecordingErrorListener recordingErrorListener =
        (RecordingErrorListener) factory.getErrorListener();
    recordingErrorListener.reset();

    final javax.xml.transform.Transformer transformer;
    try {
      LOGGER.debug("Obtaining transformer from stylesheet {}.", stylesheet);
      if (stylesheet.isEmpty()) {
        transformer = factory.newTransformer();
      } else {
        transformer = factory.newTransformer(stylesheet);
        LOGGER.debug("Obtained transformer from stylesheet {}.", stylesheet);
      }
    } catch (TransformerConfigurationException e) {
      throw new XmlException("Could not parse the provided stylesheet.", e);
    }
    transformer.setErrorListener(factory.getErrorListener());
    LOGGER.info("Using transformer {}.", transformer);
    try {
      transformer.transform(document, result);
    } catch (TransformerException e) {
      recordingErrorListener.thrown(e);
    }

    /*
     * The spec is unclear about whether the error listener throwing should fail processing; and by
     * experiment, it seems that throwing when warning() goes unnoticed. So let’s crash it manually
     * according to the severity level demanded for, rather than in the catch block (which may not
     * be triggered).
     */
    recordingErrorListener.throwFirstGraveAsXmlException();
    /*
     * We want to log everything; and throw the first grave exception (grave meaning any or at least
     * error, depending on pedantism). We avoid doing both logging and throwing when there is only
     * one grave exc and it is the last exc seen (so the ordering is clear); but when a grave exc is
     * followed by another one, we want to log both in order for the ordering to be visible in the
     * logs, even though this leads to double treatment (logging and throwing).
     *
     * We observed that saxonica may send two fatal errors for one terminal message: the message
     * itself, and a second one, “Processing terminated by xsl:message at line 237 in chunker.xsl”.
     * Hence our desire to throw the first grave exception (which is more informative), and log
     * everything (so that the complete order of exceptions is visible). We observed that Saxonica
     * terminates with the latter exception (appearing in our catch block) instead of the former, so
     * we must override this.
     *
     * In pedantic mode, everything is grave. Otherwise, errors and supplementary (the one thrown
     * that ends the process) are grave.
     */
  }

  /**
   * Transforms the provided document, using the provided stylesheet, and returns the result as a
   * string.
   *
   * @param document the document to transform
   * @param stylesheet the stylesheet that indicates the transform to perform
   * @return the resulting transformation
   * @throws XmlException iff an error occurs when parsing the stylesheet or when transforming the
   *         document.
   */
  public String transform(Source document, Source stylesheet) throws XmlException {
    checkArgument(!document.isEmpty());
    checkArgument(!stylesheet.isEmpty());

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);

    transformInternal(document, result, stylesheet);

    return resultWriter.toString();
  }

  /**
   * Transforms the provided document, using the “identity” transform.
   *
   * @param document the document to transform
   * @param result where the result will be held
   * @throws XmlException If an error occurs when transforming the document.
   */
  public void transform(Source document, Result result) throws XmlException {
    transformInternal(document, result, new SAXSource());
  }

  /**
   * Not ready.
   *
   * @param document the document
   * @throws TransformerException iff shit happens
   */
  @SuppressWarnings("unused")
  private String transformToString(Document document)
      throws TransformerConfigurationException, TransformerException {
    final StringWriter writer = new StringWriter();

    final javax.xml.transform.Transformer transformer = factory.newTransformer();
    // transformer.setErrorListener(…);

    /* Doesn’t seem to take these properties into account. */
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
    // "2");

    transformer.transform(new DOMSource(document), new StreamResult(writer));
    return writer.toString();
  }
}
