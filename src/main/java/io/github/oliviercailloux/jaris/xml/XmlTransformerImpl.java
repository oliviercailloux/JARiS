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

class XmlTransformerImpl implements XmlTransformer {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerImpl.class);

  static XmlTransformerImpl using(Transformer transformer) {
    return new XmlTransformerImpl(transformer);
  }

  private final Transformer transformer;

  private XmlTransformerImpl(Transformer transformer) {
    this.transformer = checkNotNull(transformer);
    checkArgument(transformer.getErrorListener() instanceof XmlTransformErrorListener);
  }

  @Override
  public void sourceToResult(Source document, Result result) throws XmlException {
    checkNotNull(document);
    checkArgument(!document.isEmpty());
    checkNotNull(result);

    LOGGER.debug("Transforming document using transformer {}.", transformer);
    try {
      transformer.transform(document, result);
    } catch (TransformerException e) {
      throw new XmlException("Error while transforming document.", e);
    }
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
