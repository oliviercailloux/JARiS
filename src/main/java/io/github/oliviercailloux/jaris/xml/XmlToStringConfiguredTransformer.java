package io.github.oliviercailloux.jaris.xml;

import javax.xml.transform.Source;

/**
 * A transformer configured with a given stylesheet, ready to transform conforming documents.
 */
public interface XmlToStringConfiguredTransformer extends XmlConfiguredTransformer {

  /**
   * Transforms the provided document and returns the result as a string.
   *
   * @param document the document to transform
   * @return the resulting transformation
   * @throws XmlException iff an error occurs when transforming the document.
   */
  public String transform(Source document) throws XmlException;
}
