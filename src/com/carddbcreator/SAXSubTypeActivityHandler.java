package com.carddbcreator;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXSubTypeActivityHandler extends DefaultHandler {
	List<String> allSubTypes = new ArrayList<String>();

	String charVal = "";

	public List<String> getAllSubTypes() {
		return allSubTypes;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		charVal = new String(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("a")) {
			allSubTypes.add(charVal);
		}
	}
}