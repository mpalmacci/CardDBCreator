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

import com.magichat.Card;
import com.magichat.Expansion;

public class SAXDataParser {

	SAXCardsActivityHandler saxC_AH = new SAXCardsActivityHandler();

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
			File cardXml = new File("/Users/mpalmacci/Documents/workspace/CardDBCreator", "cards.xml");
			istream = new FileInputStream(cardXml);

			XMLReader xr = setupSaxXR();
			xr.setContentHandler(saxC_AH);

			xr.parse(new InputSource(istream));
		} catch (SAXException saxE) {
			saxE.printStackTrace();
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
	}

	protected List<Expansion> getAllExpansions() {
		return saxC_AH.getAllExpansions();
	}

	protected List<Card> getAllCards() {
		return saxC_AH.getAllCards();
	}
}