package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Optional;

/**
 * Either just a local name (in which case its namespace name has no value), or an
 * <a href="https://www.w3.org/TR/REC-xml-names/#dt-expname">expanded name</a>, thus consisting in a
 * namespace name and a local name.
 * <p>
 * The namespace name, if present, is an absolute URI.
 * </p>
 */
public class XmlName {
  public static XmlName expandedName(URI namespace, String localName) {
    return new XmlName(Optional.of(namespace), localName);
  }

  public static XmlName localName(String localName) {
    return new XmlName(Optional.empty(), localName);
  }

  private final Optional<URI> namespace;
  private final String localName;

  private XmlName(Optional<URI> namespace, String localName) {
    this.namespace = checkNotNull(namespace);
    this.localName = checkNotNull(localName);
  }

  /**
   * Returns this xml name as a string, using the
   * <a href="https://docstore.mik.ua/orelly/xml/xmlnut/ch04_02.htm">{URI}localName form</a>.
   *
   * @return a string representing this xml name.
   */
  public String asFullName() {
    return namespace.map(n -> "{" + n.toString() + "}").orElse("") + localName;
  }
}
