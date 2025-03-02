package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xml.utils.URI;
import org.w3c.dom.Document;

/**
 * A transformer configured with a given stylesheet, ready to transform conforming documents.
 */
public interface XmlTransformer extends XmlToBytesTransformer {

  /**
   * Transforms the provided document and returns the result as a string.
   *
   * @param document the document to transform
   * @return the transformed content as a string
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   */
  public default String bytesToChars(ByteSource document) throws XmlException, IOException {
    try (InputStream is = document.openBufferedStream()) {
      return sourceToChars(new StreamSource(is));
    }
  }

  public default Document bytesToDom(ByteSource document) throws XmlException, IOException {
    DOMResult result = new DOMResult();
    try (InputStream is = document.openBufferedStream()) {
      sourceToResult(new StreamSource(is), result);
    }
    return (Document) result.getNode();
  }

  public default void charsToChars(CharSource document, CharSink result)
      throws XmlException, IOException {
    try (Reader r = document.openBufferedStream(); Writer w = result.openBufferedStream()) {
      sourceToResult(new StreamSource(r), new StreamResult(w));
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
  public default String charsToChars(CharSource document) throws XmlException, IOException {
    try (Reader r = document.openBufferedStream()) {
      return sourceToChars(new StreamSource(r));
    }
  }

  public default Document charsToDom(CharSource document) throws XmlException, IOException {
    DOMResult result = new DOMResult();
    try (Reader r = document.openBufferedStream()) {
      sourceToResult(new StreamSource(r), result);
    }
    return (Document) result.getNode();
  }

  /**
   * Transforms the provided document and returns the result as a string.
   *
   * @param document the document to transform
   * @return the transformed content as a string
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   */
  public default String sourceToChars(URI document) throws XmlException {
    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);

    sourceToResult(new StreamSource(document.toString()), result);

    return resultWriter.toString();
  }

  public default void sourceToChars(URI document, CharSink result)
      throws XmlException, IOException {
    try (Writer w = result.openBufferedStream()) {
      sourceToResult(new StreamSource(document.toString()), new StreamResult(w));
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
  public default String sourceToChars(Source document) throws XmlException {
    checkArgument(!document.isEmpty());

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);

    sourceToResult(document, result);

    return resultWriter.toString();
  }

  public default void sourceToChars(Source document, CharSink result)
      throws XmlException, IOException {
    try (Writer w = result.openBufferedStream()) {
      sourceToResult(document, new StreamResult(w));
    }
  }

  public default Document sourceToDom(URI document) throws XmlException {
    DOMResult result = new DOMResult();
    sourceToResult(new StreamSource(document.toString()), result);
    return (Document) result.getNode();
  }

  public default Document sourceToDom(Source document) throws XmlException {
    DOMResult result = new DOMResult();
    sourceToResult(document, result);
    return (Document) result.getNode();
  }
}
