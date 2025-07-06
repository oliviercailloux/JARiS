package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import net.sf.saxon.jaxp.TransformerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class XmlTransformerSaxonPedanticImpl implements XmlTransformer {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlTransformerSaxonPedanticImpl.class);

  static XmlTransformerSaxonPedanticImpl using(TransformerImpl transformer) {
    return new XmlTransformerSaxonPedanticImpl(transformer);
  }

  private final TransformerImpl transformer;

  private XmlTransformerSaxonPedanticImpl(TransformerImpl transformer) {
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
    SaxonMessageHandler myHandler =
        (SaxonMessageHandler) transformer.getUnderlyingController().getMessageHandler();
    if (myHandler.hasBeenCalled()) {
      throw new XmlException("Error while transforming document.");
    }
  }
}
