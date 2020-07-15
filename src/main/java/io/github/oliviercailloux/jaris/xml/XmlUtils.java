package io.github.oliviercailloux.jaris.xml;

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
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.google.common.collect.ImmutableList;

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

	public static ImmutableList<Node> toList(NodeList n) {
		return ImmutableList.copyOf(new NodeListWrapper(n));
	}

	public static ImmutableList<Element> toElements(NodeList n) {
		return ImmutableList.copyOf(new NodeListToElementsWrapper(n));
	}

	public static void logContent(NodeList childNodes) {
		for (Node node : toList(childNodes)) {
			LOGGER.info("Node type {}, Local {}, NS {}, Value {}, Name {}.", node.getNodeType(), node.getLocalName(),
					node.getNamespaceURI(), node.getNodeValue(), node.getNodeName());
		}
	}

	public static String toString(Document document) {
		final DOMImplementationLS impl = (DOMImplementationLS) document.getImplementation().getFeature("LS", "3.0");
		final LSSerializer ser = impl.createLSSerializer();
		/**
		 * But see
		 * <a href="https://bugs.openjdk.java.net/browse/JDK-7150637">7150637</a> and
		 * <a href="https://bugs.openjdk.java.net/browse/JDK-8054115">8054115</a>:
		 * LSSerializer remove a '\n' following the xml declaration
		 */
		ser.getDomConfig().setParameter("format-pretty-print", true);
		final StringWriter writer = new StringWriter();
		final LSOutput output = impl.createLSOutput();
		output.setCharacterStream(writer);
		ser.write(document, output);
		return writer.toString();
	}

	static String toStringTransformer(Document document) {
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
