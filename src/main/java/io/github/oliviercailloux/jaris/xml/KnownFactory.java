package io.github.oliviercailloux.jaris.xml;

import javax.xml.transform.TransformerFactory;

public enum KnownFactory {
  JDK, XALAN, SAXON;

  private TransformerFactory factory;

  public TransformerFactory factory() throws NoClassDefFoundError {
    if (factory == null) {
      if (this == JDK) {
        return TransformerFactory.newDefaultInstance();
      }
      if (this == XALAN) {
        return new org.apache.xalan.processor.TransformerFactoryImpl();
      }
      if (this == SAXON) {
        return new net.sf.saxon.TransformerFactoryImpl();
      }
      throw new IllegalStateException("Unknown factory: " + this);
    }
    return factory;
  }
}
