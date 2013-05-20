package com.carddbcreator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.magichat.cards.Card;
import com.magichat.cards.Expansion;

public class SAXCardsActivityHandler extends DefaultHandler {
	List<Expansion> allExpansions = new ArrayList<Expansion>();
	List<Card> allCards = new ArrayList<Card>();
	Card c;
	Expansion exp;

	String charVal = "";
	boolean isExp = true;
	boolean isText = false;

	// Expansion data types
	String shortName = "", name = "";

	// Card data types
	String manaCost = "", type = "", pt = "", text = "";
	List<String> colors = new ArrayList<String>();
	List<Expansion> expansions = new ArrayList<Expansion>();
	List<URL> picURL = new ArrayList<URL>();
	HashMap<Expansion, URL> setImages;

	public List<Expansion> getAllExpansions() {
		return allExpansions;
	}

	public List<Card> getAllCards() {
		return allCards;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("cards")) {
			isExp = false;
		}
		if (qName.equals("set") && !isExp) {
			try {
				picURL.add(new URL(attributes.getValue("picURL")));
			} catch (MalformedURLException urlEx) {
				urlEx.printStackTrace();
			}
		}
		if (qName.equals("text")) {
			isText = true;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		charVal = new String(ch, start, length);
		
		// This is to compile all the Text values into one
		// SAX has a limit to the amount of characters in 'ch'
		if (isText) {
			text = text + charVal;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (isExp) {
			if (qName.equals("name")) {
				shortName = charVal;
			} else if (qName.equals("longname")) {
				name = charVal;

				exp = new Expansion(name, shortName);
				allExpansions.add(exp);
			}
		} else {
			if (qName.equals("name")) {
				shortName = charVal;
			} else if (qName.equals("set")) {
				for (Expansion expan : allExpansions) {
					if (expan.getShortName().equals(charVal)) {
						expansions.add(expan);
						break;
					}
				}
			} else if (qName.equals("color")) {
				colors.add(charVal);
			} else if (qName.equals("manacost")) {
				manaCost = charVal;
			} else if (qName.equals("type")) {
				type = charVal;
			} else if (qName.equals("pt")) {
				pt = charVal;
			} else if (qName.equals("text")) {
				c = new Card(shortName, expansions, picURL, colors, manaCost,
						type, pt, text);
				allCards.add(c);

				expansions = new ArrayList<Expansion>();
				picURL = new ArrayList<URL>();
				colors = new ArrayList<String>();
				isText = false;
				text = new String();
			}
		}
	}
}