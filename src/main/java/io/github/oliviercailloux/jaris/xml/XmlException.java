package io.github.oliviercailloux.jaris.xml;

/**
 * A runtime exception indicating an unexpected exception relating to XML treatment, supposed to
 * be generally not worth catching.
 */
@SuppressWarnings("serial")
public class XmlException extends RuntimeException {
  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public XmlException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause. (A {@code null} value is permitted, and indicates that the cause is
   *        nonexistent or unknown.)
   */
  public XmlException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause and a detail message of
   * {@code (cause==null ? null : cause.toString())} (which typically contains the class and
   * detail message of {@code cause}). This constructor is useful for XML exceptions that are
   * little more than wrappers for other throwables.
   *
   * @param cause the cause. (A {@code null} value is permitted, and indicates that the cause is
   *        nonexistent or unknown.)
   */
  public XmlException(Throwable cause) {
    super(cause);
  }
}