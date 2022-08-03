package io.github.oliviercailloux.jaris.xml;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * A transformer configured with a given stylesheet, ready to transform conforming documents.
 */
interface XmlSimpleConfiguredTransformer {

  /**
   * Transforms the provided document.
   *
   * @param document the document to transform
   * @param result where the result will be held
   * @throws XmlException iff an error occurs when transforming the document.
   */
  public void transform(Source document, Result result) throws XmlException;
}
