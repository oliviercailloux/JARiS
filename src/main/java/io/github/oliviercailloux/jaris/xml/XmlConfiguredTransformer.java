package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * A transformer configured with a given stylesheet, ready to transform conforming documents.
 */
public interface XmlConfiguredTransformer {

  /**
   * Transforms the provided document.
   *
   * @param document the document to transform
   * @param result where the result will be held
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   */
  public void transform(Source document, Result result) throws XmlException;

  /**
   * Transforms the provided document and returns the result as a string.
   *
   * @param document the document to transform
   * @return the transformed content as a string
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   */
  public default String transform(Source document) throws XmlException {
    checkArgument(!document.isEmpty());

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);

    transform(document, result);

    return resultWriter.toString();
  }

  /**
   * Transforms the provided document and returns the result as a string.
   *
   * @param document the document to transform
   * @return the transformed content as a string
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   */
  public default String transform(ByteSource document) throws XmlException, IOException {
    try (InputStream is = document.openStream()) {
      return transform(new StreamSource(is));
    }
  }

  /**
   * Transforms the provided document and returns the result as a string.
   *
   * @param document the document to transform
   * @return the transformed content as a string
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   */
  public default String transform(CharSource document) throws XmlException, IOException {
    try (Reader r = document.openStream()) {
      return transform(new StreamSource(r));
    }
  }
}
