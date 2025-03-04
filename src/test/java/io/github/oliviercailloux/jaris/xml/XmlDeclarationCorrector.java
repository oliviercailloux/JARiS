package io.github.oliviercailloux.jaris.xml;

public class XmlDeclarationCorrector {
  /** Corrects a bug in JDK and XALAN identity transformers: the XML declaration does not end with a new line. */
  public static String terminateXmlDeclaration(String xml) {
    return xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
  }
}
