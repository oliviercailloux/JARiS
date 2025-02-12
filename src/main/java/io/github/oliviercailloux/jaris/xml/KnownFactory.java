package io.github.oliviercailloux.jaris.xml;

import javax.xml.transform.TransformerFactory;

public enum KnownFactory {
  JDK,
  XALAN,
  SAXON;

  private TransformerFactory factory;

  public TransformerFactory factory() {
    /* Need dynamic loading to avoid crashing when Xalan or Saxon is not depended upon. */
    if (factory == null) {
      factory = switch (this) {
        case JDK -> TransformerFactory.newDefaultInstance();
        case XALAN -> new org.apache.xalan.processor.TransformerFactoryImpl();
        case SAXON -> new net.sf.saxon.TransformerFactoryImpl();
      };
    }
    return factory;
  }
}
