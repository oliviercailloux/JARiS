package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.VerifyException;
import java.io.IOException;
import javax.xml.transform.Source;
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
