/**
 * A few helper classes to deal with XML, using the
 * <a href= "https://github.com/oliviercailloux/java-course/blob/master/DOM.adoc">Document Object
 * Model</a>, XSLT, or schema based validation.
 * <p>
 * The primary intended usage is with XML documents that are supposed to be valid, such as those in
 * the class path of a software or sent by web services. As a result, this class adopts the simplest
 * possible approach to deal with badly formed documents, by sending unchecked exceptions upon
 * encounter, to simplify usage while still failing fast.
 * </p>
 * <p>
 * The API in this package focuses on simplicity and validity of the documents that are produced.
 * </p>
 * <p>
 * As the focus is on simplicity (over flexibility), its use is appropriate if you need to do only
 * simple things with your documents, do not need much flexibility, and control the origin of the
 * documents (so do not need flexible error management).
 * </p>
 * <p>
 * The public API of the classes in this package favors
 * {@link javax.xml.transform.stream.StreamSource} (from {@code javax.xml.transform}) to
 * {@link org.xml.sax.InputSource} (from {@code org.xml.sax}). Both classes come from the
 * {@code java.xml} module, and their APIs are almost identical, the only differences being that
 * {@code StreamSource} is part of a hierarchy (as it implements
 * {@link javax.xml.transform.Source}), which makes it nicer to use in this context; and that
 * {@code InputSource} has an “encoding” parameter, which we do not need. See also
 * <a href="https://stackoverflow.com/q/69194590">SO</a>.
 * </p>
 */
package io.github.oliviercailloux.jaris.xml;
