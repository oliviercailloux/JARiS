package io.github.oliviercailloux.jaris.xml;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
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

  public default void bytesToResult(ByteSource document, Result result) throws XmlException, IOException {
    try (InputStream is = document.openBufferedStream()) {
      transform(new StreamSource(is), result);
    }
  }

  public default byte[] charsToBytes(CharSource document) throws XmlException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (Reader r = document.openBufferedStream()) {
      transform(new StreamSource(r), new StreamResult(out));
    }
    return out.toByteArray();
  }

}
