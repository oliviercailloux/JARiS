package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlUtilsTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtilsTests.class);

	@Test
	void testToString() throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final Document document = builder.newDocument();

		final Element html = document.createElementNS(XmlUtils.XHTML_NAME_SPACE, "html");
		html.setAttribute("lang", "en");
		document.appendChild(html);
		final Element head = document.createElementNS(XmlUtils.XHTML_NAME_SPACE, "head");
		html.appendChild(head);
		final Element meta = document.createElementNS(XmlUtils.XHTML_NAME_SPACE, "meta");
		meta.setAttribute("http-equiv", "Content-type");
		meta.setAttribute("content", "text/html; charset=utf-8");
		head.appendChild(meta);
		final Element body = document.createElementNS(XmlUtils.XHTML_NAME_SPACE, "body");
		html.appendChild(body);
		final String result = XmlUtils.toString(document);
		LOGGER.info("Got: {}.", result);

		final String expected = Files.readString(Path.of(getClass().getResource("simple.html").toURI()));
		assertEquals(expected, result);
	}
}
