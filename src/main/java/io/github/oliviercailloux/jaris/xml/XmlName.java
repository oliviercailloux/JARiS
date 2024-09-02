package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Optional;
import javax.xml.namespace.QName;

/**
 * Either just a local name (in which case its namespace name has no value), or an
 * <a href="https://www.w3.org/TR/REC-xml-names/#dt-expname">expanded name</a>, thus consisting in a
 * namespace name and a local name.
 * <p>
 * The namespace name, if present, is an absolute URI.
 * </p>
 * @see QName
 */
public record XmlName (Optional<URI> namespace, String localName) {
  /**
   * Returns an expanded name representing the given namespace and local name.
   *
   * @param namespace the namespace; an absolute URI
   * @param localName the local name
   * @return an expanded name
   */
  public static XmlName expandedName(URI namespace, String localName) {
    return new XmlName(Optional.of(namespace), localName);
  }

  /**
   * Returns an xml name representing the given local name, without namespace.
   *
   * @param localName the local name
   * @return an xml name
   */
  public static XmlName localName(String localName) {
    return new XmlName(Optional.empty(), localName);
  }

  public XmlName {
    checkNotNull(namespace);
    namespace.ifPresent(n -> checkArgument(n.isAbsolute()));
    checkNotNull(localName);
  }

  public QName toQName() {
    return new QName(namespace.map(Object::toString).orElse(null), localName);
  }

  /**
   * Returns this xml name as a string, using the
   * <a href="https://docstore.mik.ua/orelly/xml/xmlnut/ch04_02.htm">{URI}localName</a> form.
   *
   * @return a string representing this xml name.
   */
  public String asFullName() {
    return namespace.map(n -> "{" + n.toString() + "}").orElse("") + localName;
  }
}
