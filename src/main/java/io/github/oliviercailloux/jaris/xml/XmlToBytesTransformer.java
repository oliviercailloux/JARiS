package io.github.oliviercailloux.jaris.xml;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public interface XmlToBytesTransformer {

  /**
   * Transforms the provided document.
   *
   * @param document the document to transform
   * @param result where the result will be held
   * @throws XmlException iff an error occurs when transforming the document. Wraps a
   *         {@link TransformerException}.
   * @throws IOException iff an I/O error occurs while reading the document or writing the result.
   *         Not thrown if the source is a DOMSource or a StreamSource reading from a
   *         ByteArrayInputStream or from a StringReader and the result is a DOMResult or a
   *         StreamResult writing to a ByteArrayOutputStream or to a StringWriter.
   */
  public void sourceToResult(Source document, Result result) throws XmlException, IOException;

  public default void sourceToBytes(Source document, ByteSink result)
      throws XmlException, IOException {
    try (OutputStream os = result.openBufferedStream()) {
      sourceToResult(document, new StreamResult(os));
    }
  }

  public default byte[] sourceToBytes(Source document) throws XmlException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    sourceToResult(document, new StreamResult(out));
    return out.toByteArray();
  }

  public default void bytesToBytes(ByteSource document, ByteSink result)
      throws XmlException, IOException {
    try (InputStream is = document.openBufferedStream();
        OutputStream os = result.openBufferedStream()) {
      sourceToResult(new StreamSource(is), new StreamResult(os));
    }
  }

  public default byte[] bytesToBytes(byte[] document) throws XmlException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      sourceToResult(new StreamSource(new ByteArrayInputStream(document)), new StreamResult(out));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return out.toByteArray();
  }

  public default byte[] bytesToBytes(ByteSource document) throws XmlException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (InputStream is = document.openBufferedStream()) {
      sourceToResult(new StreamSource(is), new StreamResult(out));
    }
    return out.toByteArray();
  }

  public default void bytesToResult(ByteSource document, Result result)
      throws XmlException, IOException {
    try (InputStream is = document.openBufferedStream()) {
      sourceToResult(new StreamSource(is), result);
    }
  }

  public default byte[] charsToBytes(String document) throws XmlException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (StringReader r = new StringReader(document)) {
      sourceToResult(new StreamSource(r), new StreamResult(out));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return out.toByteArray();
  }

  public default byte[] charsToBytes(CharSource document) throws XmlException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (Reader r = document.openBufferedStream()) {
      sourceToResult(new StreamSource(r), new StreamResult(out));
    }
    return out.toByteArray();
  }
}
