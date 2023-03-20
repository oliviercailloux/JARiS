package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

class XmlConfiguredTransformerImpl implements XmlConfiguredTransformer {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlConfiguredTransformerImpl.class);

  static XmlConfiguredTransformerImpl using(Transformer transformer) {
    return new XmlConfiguredTransformerImpl(transformer);
  }

  private final Transformer transformer;

  private XmlConfiguredTransformerImpl(Transformer transformer) {
    this.transformer = checkNotNull(transformer);
    checkArgument(transformer.getErrorListener() instanceof XmlTransformRecordingErrorListener);
  }

  @Override
  public void transform(Source document, Result result) throws XmlException {
    checkNotNull(document);
    checkArgument(!document.isEmpty());
    checkNotNull(result);

    final XmlTransformRecordingErrorListener recordingErrorListener =
        (XmlTransformRecordingErrorListener) transformer.getErrorListener();
    recordingErrorListener.reset();

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
   * Not ready.
   *
   * @param document the document
   * @throws TransformerException iff shit happens
   */
  @SuppressWarnings("unused")
  private String transformToString(Document document)
      throws TransformerConfigurationException, TransformerException {
    final StringWriter writer = new StringWriter();

    /* Doesn’t seem to take these properties into account. */
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
    // "2");

    transformer.transform(new DOMSource(document), new StreamResult(writer));
    return writer.toString();
  }
}
