package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

/**
 * A transformer configured with a given stylesheet, ready to transform conforming documents.
 */
public interface XmlConfiguredTransformer {

  /**
   * Transforms the provided document.
   *
   * @param document the document to transform
   * @param result where the result will be held
   * @throws XmlException iff an error occurs when transforming the document.
   */
  public void transform(Source document, Result result) throws XmlException;

  /**
   * Transforms the provided document and returns the result as a string.
   *
   * @param document the document to transform
   * @return the transformed content as a string
   * @throws XmlException iff an error occurs when transforming the document.
   */
  public default String transform(Source document) throws XmlException {
    checkArgument(!document.isEmpty());

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);

    transform(document, result);

    return resultWriter.toString();
  }
}
