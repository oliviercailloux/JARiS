package io.github.oliviercailloux.jaris.xml;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringWriter;
import java.util.AbstractList;
import java.util.RandomAccess;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.google.common.collect.ImmutableList;

/**
 * A few helper methods to deal with XML, especially using the <a href=
 * "https://github.com/oliviercailloux/java-course/blob/master/DOM.adoc">Document
 * Object Model</a>.
 */
public class XmlUtils {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);

	public static final String XHTML_NAME_SPACE = "http://www.w3.org/1999/xhtml";

	private XmlUtils() {
	}

	private static class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
		private final NodeList delegate;

		NodeListWrapper(NodeList l) {
			delegate = l;
		}

		@Override
		public Node get(int index) {
			return delegate.item(index);
		}

		@Override
		public int size() {
			return delegate.getLength();
		}
	}

	private static class NodeListToElementsWrapper extends AbstractList<Element> implements RandomAccess {
		private final NodeList delegate;

		NodeListToElementsWrapper(NodeList l) {
			delegate = l;
		}

		@Override
		public Element get(int index) {
			return (Element) delegate.item(index);
		}

		@Override
		public int size() {
			return delegate.getLength();
		}
	}

	/**
	 * Returns a copy of the given list of nodes, using a proper generic collection.
	 */
	public static ImmutableList<Node> toList(NodeList nodes) {
		return ImmutableList.copyOf(new NodeListWrapper(nodes));
	}

	/**
	 * Returns a copy of the given list of nodes as a list of elements, using a
	 * proper generic collection.
	 *
	 * @throws ClassCastException if some node in the provided list cannot be cast
	 *                            to an element.
	 */
	public static ImmutableList<Element> toElements(NodeList nodes) throws ClassCastException {
		return ImmutableList.copyOf(new NodeListToElementsWrapper(nodes));
	}

	/**
	 * Returns the node type, its local name, its namespace, its value, and its
	 * name.
	 */
	public static String toDebugString(Node node) {
		return String.format("Node type %s, Local %s, NS %s, Value %s, Name %s.", node.getNodeType(),
				node.getLocalName(), node.getNamespaceURI(), node.getNodeValue(), node.getNodeName());
	}

	/**
	 * Returns a pretty-printed textual representation of the node.
	 */
	public static String toString(Node node) {
		checkNotNull(node);
		final DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
			throw new IllegalStateException(e);
		}
		final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
		final LSSerializer ser = impl.createLSSerializer();
		ser.getDomConfig().setParameter("format-pretty-print", true);
		final StringWriter writer = new StringWriter();
		final LSOutput output = impl.createLSOutput();
		output.setCharacterStream(writer);
		ser.write(node, output);
		/**
		 * See <a href="https://bugs.openjdk.java.net/browse/JDK-7150637">7150637</a>
		 * and <a href="https://bugs.openjdk.java.net/browse/JDK-8054115">8054115 -
		 * LSSerializer remove a '\n' following the xml declaration</a>. I tried to file
		 * a bug about this as well in July 2020.
		 */
		return writer.toString();
	}

	static String toStringUsingTransformer(Document document) {
		final StringWriter writer = new StringWriter();

		TransformerFactory tf = TransformerFactory.newDefaultInstance();
		final Transformer transformer;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new IllegalStateException(e);
		}
		/** Doesn’t seem to take these properties into account. */
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
//		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		try {
			transformer.transform(new DOMSource(document), new StreamResult(writer));
		} catch (TransformerException e) {
			throw new IllegalStateException(e);
		}

		return writer.toString();
	}
}
