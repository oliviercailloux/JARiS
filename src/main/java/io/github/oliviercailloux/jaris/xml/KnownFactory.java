package io.github.oliviercailloux.jaris.xml;

import javax.xml.transform.TransformerFactory;

public enum KnownFactory {
  JDK, XALAN, SAXON;

  private TransformerFactory factory;

  public TransformerFactory factory() throws NoClassDefFoundError {
    if (factory == null) {
      if (this == JDK) {
        return TransformerFactory.newInstance();
      }
      if (this == XALAN) {
        try {
          return new org.apache.xalan.processor.TransformerFactoryImpl();
        } catch (NoClassDefFoundError e) {
          throw new IllegalStateException("Xalan not found in classpath.", e);
        }
      }
      if (this == SAXON) {
        try {
          return new net.sf.saxon.TransformerFactoryImpl();
        } catch (NoClassDefFoundError e) {
          throw new IllegalStateException("Saxon not found in classpath.", e);
        }
      }
      throw new IllegalStateException("Unknown factory: " + this);
    }
    return factory;
  }
}
