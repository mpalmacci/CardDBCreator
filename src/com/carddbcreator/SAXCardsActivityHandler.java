package com.carddbcreator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.magichat.Card;
import com.magichat.Expansion;

public class SAXCardsActivityHandler extends DefaultHandler {
	List<Expansion> allExpansions = new ArrayList<Expansion>();
	List<Card> allCards = new ArrayList<Card>();
	Card c;
	Expansion exp;

	String charVal = "";
	boolean isExp = true;

	// Expansion data types
	String shortName = "", name = "";

	// Card data types
	String manaCost = "", type = "", pt = "", text = "";
	List<String> color = new ArrayList<String>();
	List<Expansion> expansions = new ArrayList<Expansion>();
	List<URL> picURL = new ArrayList<URL>();
	HashMap<Expansion, URL> setImages;
	// i is used as the index tracker for the set input values
	// j is used for picURL in the same manner
	// k is used for color
	int power = 0, toughness = 0, i = 0, k = 0;

	public List<Expansion> getAllExpansions() {
		return allExpansions;
	}

	public List<Card> getAllCards() {
		return allCards;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (localName.equals("cards")) {
			isExp = false;
		}
		if (localName.equals("set") && !isExp) {
			try {
				picURL.add(new URL(attributes.getValue("picURL")));
			} catch (MalformedURLException urlEx) {
				urlEx.printStackTrace();
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		charVal = new String(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (isExp) {
			if (localName.equals("name")) {
				shortName = charVal;
			} else if (localName.equals("longname")) {
				name = charVal;

				exp = new Expansion(name, shortName);
				allExpansions.add(exp);
			}
		} else {
			if (localName.equals("name")) {
				shortName = charVal;
			} else if (localName.equals("set")) {
				for (Expansion expan : allExpansions) {
					if (expan.getShortName().equals(charVal)) {
						expansions.add(expan);
						break;
					}
				}
			} else if (localName.equals("color")) {
				color.add(charVal);
				k++;
			} else if (localName.equals("manacost")) {
				manaCost = charVal;
			} else if (localName.equals("type")) {
				type = charVal;
			} else if (localName.equals("pt")) {
				pt = charVal;
			} else if (localName.equals("text")) {
				text = charVal;

				c = new Card(shortName, expansions, picURL, color, manaCost, type, pt,
						text);
				allCards.add(c);

				expansions.clear();
				picURL.clear();
				color.clear();
			}
		}
	}
}
