package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringWriter;
import java.util.function.Consumer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.s9api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

class XmlConfiguredTransformerSaxonPedanticImpl implements XmlConfiguredTransformer {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlConfiguredTransformerImpl.class);

  static XmlConfiguredTransformerSaxonPedanticImpl using(TransformerImpl transformer) {
    return new XmlConfiguredTransformerSaxonPedanticImpl(transformer);
  }

  private final TransformerImpl transformer;

  private XmlConfiguredTransformerSaxonPedanticImpl(TransformerImpl transformer) {
    this.transformer = checkNotNull(transformer);
    checkArgument(transformer.getErrorListener() instanceof XmlTransformErrorListener);
  }

  @Override
  public void transform(Source document, Result result) throws XmlException {
    checkNotNull(document);
    checkArgument(!document.isEmpty());
    checkNotNull(result);

    LOGGER.debug("Transforming document using transformer {}.", transformer);
    try {
      transformer.transform(document, result);
    } catch (TransformerException e) {
      throw new XmlException("Error while transforming document.", e);
    }
    SaxonMessageHandler myHandler =
        (SaxonMessageHandler) transformer.getUnderlyingController().getMessageHandler();
    if (myHandler.hasBeenCalled()) {
      throw new XmlException("Error while transforming document.");
    }
  }
}
