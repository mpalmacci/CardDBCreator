package com.carddbcreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SAXSubTypeDataParser {
	SAXSubTypeActivityHandler saxST_AH = new SAXSubTypeActivityHandler();

	private XMLReader setupSaxXR() {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			return sp.getXMLReader();
		} catch (SAXException saxE) {
			saxE.printStackTrace();
		} catch (ParserConfigurationException pcE) {
			pcE.printStackTrace();
		}

		return null;
	}

	protected void parseCardXml() {
		InputStream istream = null;

		try {
			File subTypeXml = new File(
					"/Users/mpalmacci/Documents/workspace/CardDBCreator",
					"subtype.xml");
			istream = new FileInputStream(subTypeXml);

			XMLReader xr = setupSaxXR();
			xr.setContentHandler(saxST_AH);

			xr.parse(new InputSource(istream));
		} catch (SAXException saxE) {
			saxE.printStackTrace();
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
	}

	protected List<String> getAllSubTypes() {
		return saxST_AH.getAllSubTypes();
	}
}
