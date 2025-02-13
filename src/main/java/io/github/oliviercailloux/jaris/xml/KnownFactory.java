package io.github.oliviercailloux.jaris.xml;

import com.google.common.base.VerifyException;
import java.lang.reflect.InvocationTargetException;
import javax.xml.transform.TransformerFactory;

public enum KnownFactory {
  JDK, XALAN, SAXON;

  private TransformerFactory factory;

  public TransformerFactory factory() throws ClassNotFoundException {
    if (factory == null) {
      if (this == JDK) {
        return TransformerFactory.newDefaultInstance();
      }
      if (this == XALAN) {
        try {
          return Class.forName("org.apache.xalan.processor.TransformerFactoryImpl").asSubclass(TransformerFactory.class).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          throw new ClassNotFoundException("Error while initializing.", e);
        }
      }
      if (this == SAXON) {
        try {
          return Class.forName("net.sf.saxon.TransformerFactoryImpl").asSubclass(TransformerFactory.class).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          throw new ClassNotFoundException("Error while initializing.", e);
        }
      }
      throw new VerifyException("Unknown factory: " + this);
    }
    return factory;
  }
}
