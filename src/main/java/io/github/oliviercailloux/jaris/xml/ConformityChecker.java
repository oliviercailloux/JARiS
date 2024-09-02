package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.VerifyException;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * <p>
 * Helper for validating documents.
 * </p>
 * <p>
 * Instances of this class fail fast (throwing an exception) if encountering a warning, an error or
 * a fatalError upon validating a document.
 * </p>
 */
public class ConformityChecker {

  static ConformityChecker withSchema(Schema schema) {
    return new ConformityChecker(schema);
  }

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(ConformityChecker.class);

  private final Schema schema;

  private ConformityChecker(Schema schema) {
    this.schema = checkNotNull(schema);
  }

  public void verifyValid(ByteSource document) throws VerifyException, XmlException, IOException {
    try (InputStream is = document.openStream()) {
      verifyValid(new StreamSource(is));
    }
  }

  public void verifyValid(CharSource document) throws VerifyException, XmlException, IOException {
    try (Reader r = document.openStream()) {
      verifyValid(new StreamSource(r));
    }
  }

  /**
   * Throws an exception iff the provided document is invalid.
   *
   * @param document the document to validate.
   * @throws VerifyException iff the document is invalid, equivalently, iff a warning, error or
   *         fatalError is encountered while validating the provided document
   * @throws XmlException if the Source is an XML artifact that the implementation cannot validate
   *         (for example, a processing instruction)
   * @throws IOException if the validator is processing a javax.xml.transform.sax.SAXSource and the
   *         underlying org.xml.sax.XMLReader throws an IOException.
   */
  public void verifyValid(Source document) throws VerifyException, XmlException, IOException {
    checkState(schema != null, "Schema not set.");
    final javax.xml.validation.Validator validator = schema.newValidator();
    try {
      validator.validate(document);
    } catch (IllegalArgumentException e) {
      throw new XmlException(e);
    } catch (SAXException e) {
      throw new VerifyException(e);
    }
  }
}
