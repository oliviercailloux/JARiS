package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

/**
 * A transformer configured with a given stylesheet, ready to transform conforming documents.
 */
public interface XmlTransformer {

  /**
   * Transforms the provided document.
   *
   * @param document the document to transform
   * @param result where the result will be held
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   */
  public void transform(Source document, Result result) throws XmlException;

  public default void bytesToBytes(ByteSource document, ByteSink result) throws XmlException, IOException {
    try (InputStream is = document.openBufferedStream(); OutputStream os = result.openBufferedStream()) {
      transform(new StreamSource(is), new StreamResult(os));
    }
  }

  public default byte[] bytesToBytes(ByteSource document) throws XmlException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (InputStream is = document.openBufferedStream()) {
      transform(new StreamSource(is), new StreamResult(out));
    }
    return out.toByteArray();
  }

  /**
   * Transforms the provided document and returns the result as a string.
   *
   * @param document the document to transform
   * @return the transformed content as a string
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   */
  @Deprecated
  public default String transform(Source document) throws XmlException {
    checkArgument(!document.isEmpty());

    final StringWriter resultWriter = new StringWriter();
    final StreamResult result = new StreamResult(resultWriter);

    transform(document, result);

    return resultWriter.toString();
  }

  public default Document bytesToDom(ByteSource document) throws XmlException, IOException {
    DOMResult result = new DOMResult();
    try (InputStream is = document.openBufferedStream()) {
      transform(new StreamSource(is), result);
    }
    return (Document) result.getNode();
  }

  public default void bytesToResult(ByteSource document, Result result) throws XmlException, IOException {
    try (InputStream is = document.openBufferedStream()) {
      transform(new StreamSource(is), result);
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
  public default String transform(ByteSource document) throws XmlException, IOException {
    try (InputStream is = document.openBufferedStream()) {
      return transform(new StreamSource(is));
    }
  }

  public default byte[] charsToBytes(CharSource document) throws XmlException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (Reader r = document.openBufferedStream()) {
      transform(new StreamSource(r), new StreamResult(out));
    }
    return out.toByteArray();
  }

  public default void charsToChars(CharSource document, CharSink result) throws XmlException, IOException {
    try (Reader r = document.openBufferedStream(); Writer w = result.openBufferedStream()) {
      transform(new StreamSource(r), new StreamResult(w));
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
    try (Reader r = document.openBufferedStream()) {
      return transform(new StreamSource(r));
    }
  }

  public default Document charsToDom(CharSource document) throws XmlException, IOException {
    DOMResult result = new DOMResult();
    try (Reader r = document.openBufferedStream()) {
      transform(new StreamSource(r), result);
    }
    return (Document) result.getNode();
  }
}
