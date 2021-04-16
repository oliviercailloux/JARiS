module io.github.oliviercailloux.jaris {
  exports io.github.oliviercailloux.jaris.credentials;
  exports io.github.oliviercailloux.jaris.exceptions;
  exports io.github.oliviercailloux.jaris.xml;

  exports io.github.oliviercailloux.jaris.collections;
  exports io.github.oliviercailloux.jaris.io;

  requires org.slf4j;
  requires com.google.common;
  requires java.sql;
  requires java.xml;
}
