package com.carddbcreator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.magichat.Card;
import com.magichat.Expansion;
import com.magichat.CardDbUtil;

public class MainDatabaseTools {
	/*
	 * IMPORTANT NOTES TO RUN THIS PROGRAM: In order to run this you need to
	 * first Open Oracle (download the latest version if necessary):
	 * /Users/mpalmacci/Documents/workspace/cockatrice_mac_20120630 **********
	 * The default location for the Cards database is at:
	 * /Users/mpalmacci/Library
	 * /Application/Support/Cockatrice/Cockatrice/cards.xml
	 * ****************************** This setting lives in the Cockatrice
	 * Preferences In Oracle, select File/Download Sets Information... Select
	 * the sets you want, then import all the Cards - Go to the Cards Database
	 * Location and add it to the directory:
	 * /Users/mpalmacci/Documents/workspace/CardDBCreator
	 * 
	 * REPLACE ALL single quotation marks with two single quotation marks
	 * 
	 * REPLACE ALL &quot; with \"
	 * 
	 * REPLACE ALL * with \*
	 * 
	 * =================********************=================********************
	 * 
	 * For SubType List: Go to http://gatherer.wizards.com/Pages/Advanced.aspx.
	 * Right-click on Subtypes field - Select "Inspect Elements" Find the
	 * section that lists the SubTypes - Currently starts with 'Advisor'
	 * 
	 * Copy and paste that into an XML document. Just make sure you have all the
	 * "<a href"s in that section pasted
	 * 
	 * Try to clean up the XML document as best you can - XML Documents needs to
	 * end every tag started - Attempt to remove all unnecessary tags (Keep the
	 * "<a href"s!)
	 * 
	 * Replace all the single quotation marks with two single quotation marks
	 * 
	 * Make sure the charVal will not have an \n or a new line in it
	 * 
	 * 
	 * =================********************=================********************
	 * 
	 * Enhancements needed:
	 * 
	 * Utilize Multi-Threading to handle multiple actions at a time (which
	 * ones???) - See if you can utilize the code in CardDbUtil for
	 * getAllExpansions, and getCardIds, etc.
	 */

	private static String ANDROID_TABLE_NAME = "android_metadata";

	public static void main(String[] args) {

		// Set the arguments to equal "Install" to trigger a new install or
		// "Update" to update a list of expansions
		switch (args[0]) {
		case "Install":
			try {
				SQLiteDb db = new SQLiteDb();
				Connection conn = db.getConnection();
				createSchema(conn);
				setupCards(conn);
				setupSubTypes(conn);
				setupAndroid(conn);
				db.closeConnection();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			break;
		case "Update":
			try {
				Class.forName("org.sqlite.JDBC");
				Connection conn = DriverManager
						.getConnection("jdbc:sqlite:/Users/mpalmacci/Documents/workspace/TheMagicHat/assets/cards.db");
				addNewCards(conn);
				conn.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			break;
		}
	}

	private static void createSchema(Connection conn) throws SQLException {
		// which will produce a legitimate Url for SqlLite JDBC :
		// jdbc:sqlite:card.db
		String sCreateExpansionTable = "CREATE TABLE "
				+ CardDbUtil.DB_TABLE_ALLEXPANSIONS + " ("
				+ CardDbUtil.KEY_EXPANSION_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CardDbUtil.KEY_EXPANSION_NAME + " TEXT NOT NULL, "
				+ CardDbUtil.KEY_EXPANSION_SHORTNAME + " TEXT NOT NULL);";
		String sCreateCardTable = "CREATE TABLE "
				+ CardDbUtil.DB_TABLE_ALLCARDS + " ("
				+ CardDbUtil.KEY_CARD_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CardDbUtil.KEY_CARD_NAME + " TEXT NOT NULL, "
				+ CardDbUtil.KEY_CARD_ISBLUE + " INTEGER NOT NULL, "
				+ CardDbUtil.KEY_CARD_ISBLACK + " INTEGER NOT NULL, "
				+ CardDbUtil.KEY_CARD_ISWHITE + " INTEGER NOT NULL, "
				+ CardDbUtil.KEY_CARD_ISGREEN + " INTEGER NOT NULL, "
				+ CardDbUtil.KEY_CARD_ISRED + " INTEGER NOT NULL, "
				+ CardDbUtil.KEY_CARD_MANACOST + " TEXT, "
				+ CardDbUtil.KEY_CARD_CMC + " INTEGER NOT NULL, "
				+ CardDbUtil.KEY_CARD_TYPE + " TEXT NOT NULL, "
				+ CardDbUtil.KEY_CARD_SUBTYPES + " TEXT, "
				+ CardDbUtil.KEY_CARD_POWER + " INTEGER, "
				+ CardDbUtil.KEY_CARD_TOUGHNESS + " INTEGER, "
				+ CardDbUtil.KEY_CARD_RARITY + " TEXT, "
				+ CardDbUtil.KEY_CARD_TEXT + " TEXT);";
		String sCreateExpansionPicTable = "CREATE TABLE "
				+ CardDbUtil.DB_TABLE_REL_CARD_EXP + " ("
				+ CardDbUtil.KEY_REL_CARD_EXP_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CardDbUtil.KEY_REL_CARD_ID + " INTEGER NOT NULL, "
				+ CardDbUtil.KEY_REL_EXP_ID + " INTEGER NOT NULL, "
				+ CardDbUtil.KEY_REL_PIC_URL + " TEXT NOT NULL, FOREIGN KEY("
				+ CardDbUtil.KEY_REL_CARD_ID + ") REFERENCES "
				+ CardDbUtil.DB_TABLE_ALLCARDS + "("
				+ CardDbUtil.KEY_CARD_ROWID + "), FOREIGN KEY("
				+ CardDbUtil.KEY_REL_EXP_ID + ") REFERENCES "
				+ CardDbUtil.DB_TABLE_ALLEXPANSIONS + "("
				+ CardDbUtil.KEY_EXPANSION_ROWID + "));";
		String sCreateSubTypeTable = "CREATE TABLE "
				+ CardDbUtil.DB_TABLE_SUB_TYPES + " ("
				+ CardDbUtil.KEY_SUB_TYPE_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CardDbUtil.KEY_SUB_TYPE_NAME + " TEXT NOT NULL);";

		try {
			PreparedStatement dropExpansions = conn
					.prepareStatement(dropTable(CardDbUtil.DB_TABLE_ALLEXPANSIONS));
			PreparedStatement dropCards = conn
					.prepareStatement(dropTable(CardDbUtil.DB_TABLE_ALLCARDS));
			PreparedStatement dropPics = conn
					.prepareStatement(dropTable(CardDbUtil.DB_TABLE_REL_CARD_EXP));
			PreparedStatement dropSubType = conn
					.prepareStatement(dropTable(CardDbUtil.DB_TABLE_SUB_TYPES));
			PreparedStatement dropDroid = conn
					.prepareStatement(dropTable(ANDROID_TABLE_NAME));

			dropExpansions.execute();
			dropCards.execute();
			dropPics.execute();
			dropSubType.execute();
			dropDroid.execute();

			PreparedStatement createExpansionTable = conn
					.prepareStatement(sCreateExpansionTable);
			PreparedStatement createCardTable = conn
					.prepareStatement(sCreateCardTable);
			PreparedStatement createExpansionPicTable = conn
					.prepareStatement(sCreateExpansionPicTable);
			PreparedStatement createSubTypeTable = conn
					.prepareStatement(sCreateSubTypeTable);

			createExpansionTable.execute();
			createCardTable.execute();
			createExpansionPicTable.execute();
			createSubTypeTable.execute();
		} catch (SQLException exc) {
			exc.printStackTrace();
		}
	}

	private static void setupCards(Connection conn) throws SQLException {
		List<Expansion> allExpansions = new ArrayList<Expansion>();
		List<Card> allCards = new ArrayList<Card>();

		SAXDataParser sdp = new SAXDataParser();
		try {
			sdp.parseCardXml();
		} catch (Exception e) {
			e.printStackTrace();
		}

		allExpansions = sdp.getAllExpansions();

		for (Expansion exp : allExpansions) {
			String sInsertExpansion = "INSERT INTO "
					+ CardDbUtil.DB_TABLE_ALLEXPANSIONS + "("
					+ CardDbUtil.KEY_EXPANSION_NAME + ", "
					+ CardDbUtil.KEY_EXPANSION_SHORTNAME + ") VALUES ('"
					+ exp.getName() + "', '" + exp.getShortName() + "');";

			try {
				PreparedStatement insertExpansion = conn
						.prepareStatement(sInsertExpansion);
				insertExpansion.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done setting up Expansions.");

		allExpansions = getAllExpansions(conn);
		allCards = sdp.getAllCards();

		int iBlue, iBlack, iWhite, iGreen, iRed;
		String sInsertCard = new String();
		for (Card c : allCards) {
			iBlue = c.isBlue() ? 1 : 0;
			iBlack = c.isBlack() ? 1 : 0;
			iWhite = c.isWhite() ? 1 : 0;
			iGreen = c.isGreen() ? 1 : 0;
			iRed = c.isRed() ? 1 : 0;

			sInsertCard = new String();

			if (c.getCardType().contains("Creature")) {
				sInsertCard = "INSERT INTO " + CardDbUtil.DB_TABLE_ALLCARDS
						+ " (" + CardDbUtil.KEY_CARD_NAME + ", "
						+ CardDbUtil.KEY_CARD_ISBLACK + ", "
						+ CardDbUtil.KEY_CARD_ISBLUE + ", "
						+ CardDbUtil.KEY_CARD_ISWHITE + ", "
						+ CardDbUtil.KEY_CARD_ISGREEN + ", "
						+ CardDbUtil.KEY_CARD_ISRED + ", "
						+ CardDbUtil.KEY_CARD_MANACOST + ", "
						+ CardDbUtil.KEY_CARD_CMC + ", "
						+ CardDbUtil.KEY_CARD_TYPE + ", "
						+ CardDbUtil.KEY_CARD_SUBTYPES + ", "
						+ CardDbUtil.KEY_CARD_POWER + ", "
						+ CardDbUtil.KEY_CARD_TOUGHNESS + ", "
						+ CardDbUtil.KEY_CARD_TEXT + ") VALUES ('"
						+ c.getName() + "', " + iBlack + ", " + iBlue + ", "
						+ iWhite + ", " + iGreen + ", " + iRed + ", '"
						+ c.getManaCost() + "', " + c.getCMC() + ", '"
						+ c.getCardType() + "', '" + c.getCardSubType()
						+ "', '" + c.getPower() + "', '" + c.getToughness()
						+ "', '" + c.getText() + "')";
			} else if (!c.getCardType().contains("Land")
					&& !c.getCardType().contains("Scheme")) {
				sInsertCard = "INSERT INTO " + CardDbUtil.DB_TABLE_ALLCARDS
						+ " (" + CardDbUtil.KEY_CARD_NAME + ", "
						+ CardDbUtil.KEY_CARD_ISBLACK + ", "
						+ CardDbUtil.KEY_CARD_ISBLUE + ", "
						+ CardDbUtil.KEY_CARD_ISWHITE + ", "
						+ CardDbUtil.KEY_CARD_ISGREEN + ", "
						+ CardDbUtil.KEY_CARD_ISRED + ", "
						+ CardDbUtil.KEY_CARD_MANACOST + ", "
						+ CardDbUtil.KEY_CARD_CMC + ", "
						+ CardDbUtil.KEY_CARD_TYPE + ", "
						+ CardDbUtil.KEY_CARD_SUBTYPES + ", "
						+ CardDbUtil.KEY_CARD_TEXT + ") VALUES ('"
						+ c.getName() + "', " + iBlack + ", " + iBlue + ", "
						+ iWhite + ", " + iGreen + ", " + iRed + ", '"
						+ c.getManaCost() + "', " + c.getCMC() + ", '"
						+ c.getCardType() + "', '" + c.getCardSubType()
						+ "', '" + c.getText() + "')";
			} else {
				sInsertCard = "INSERT INTO " + CardDbUtil.DB_TABLE_ALLCARDS
						+ " (" + CardDbUtil.KEY_CARD_NAME + ", "
						+ CardDbUtil.KEY_CARD_ISBLACK + ", "
						+ CardDbUtil.KEY_CARD_ISBLUE + ", "
						+ CardDbUtil.KEY_CARD_ISWHITE + ", "
						+ CardDbUtil.KEY_CARD_ISGREEN + ", "
						+ CardDbUtil.KEY_CARD_ISRED + ", "
						+ CardDbUtil.KEY_CARD_CMC + ", "
						+ CardDbUtil.KEY_CARD_TYPE + ", "
						+ CardDbUtil.KEY_CARD_SUBTYPES + ", "
						+ CardDbUtil.KEY_CARD_TEXT + ") VALUES ('"
						+ c.getName() + "', 0, 0, 0, 0, 0, 0, '"
						+ c.getCardType() + "', '" + c.getCardSubType()
						+ "', '" + c.getText() + "')";
			}
			try {
				PreparedStatement insertCard = conn
						.prepareStatement(sInsertCard);
				insertCard.execute();
				insertCard.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			insertCardExpansionRelationship(c, conn);
		}
		System.out.println("Done setting up Cards.");
	}

	private static void setupSubTypes(Connection conn) {
		String sCreateSubTypeTable = "CREATE TABLE "
				+ CardDbUtil.DB_TABLE_SUB_TYPES + " ("
				+ CardDbUtil.KEY_SUB_TYPE_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CardDbUtil.KEY_SUB_TYPE_NAME + " TEXT NOT NULL);";
		try {
			PreparedStatement dropSubType = conn
					.prepareStatement(dropTable(CardDbUtil.DB_TABLE_SUB_TYPES));
			dropSubType.execute();
			PreparedStatement createSubTypeTable = conn
					.prepareStatement(sCreateSubTypeTable);
			createSubTypeTable.execute();
		} catch (SQLException exc) {
			exc.printStackTrace();
		}

		List<String> allSubTypes = new ArrayList<String>();

		SAXSubTypeDataParser sSTdp = new SAXSubTypeDataParser();
		try {
			sSTdp.parseCardXml();
		} catch (Exception e) {
			e.printStackTrace();
		}

		allSubTypes = sSTdp.getAllSubTypes();

		String sInsertSubType;
		for (String subType : allSubTypes) {
			sInsertSubType = "INSERT INTO " + CardDbUtil.DB_TABLE_SUB_TYPES
					+ " (" + CardDbUtil.KEY_SUB_TYPE_NAME + ") VALUES ('"
					+ subType + "')";
			try {
				PreparedStatement insertSubType = conn
						.prepareStatement(sInsertSubType);
				insertSubType.execute();
				insertSubType.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done setting up SubTypes.");
	}

	private static void setupAndroid(Connection conn) {
		String sCreateAndroidTable = "CREATE TABLE \"" + ANDROID_TABLE_NAME
				+ "\" (\"locale\" TEXT DEFAULT 'en_US')";
		String sInsertAndroidData = "INSERT INTO \"" + ANDROID_TABLE_NAME
				+ "\" VALUES ('en_US')";
		try {
			PreparedStatement createAndroidTable = conn
					.prepareStatement(sCreateAndroidTable);
			createAndroidTable.execute();
			createAndroidTable.close();
			PreparedStatement insertAndroidData = conn
					.prepareStatement(sInsertAndroidData);
			insertAndroidData.execute();
			insertAndroidData.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Done setting up Android.");
	}

	private static void addNewCards(Connection conn) {
		List<Expansion> allNewExpansions = new ArrayList<Expansion>();
		List<Card> allNewCards = new ArrayList<Card>();

		SAXDataParser sdp = new SAXDataParser();
		try {
			sdp.parseCardXml();
		} catch (Exception e) {
			e.printStackTrace();
		}

		allNewExpansions = sdp.getAllExpansions();

		for (Expansion exp : allNewExpansions) {
			String sSelectExp = "SELECT " + CardDbUtil.KEY_EXPANSION_SHORTNAME
					+ " FROM " + CardDbUtil.DB_TABLE_ALLEXPANSIONS + " WHERE "
					+ CardDbUtil.KEY_EXPANSION_SHORTNAME + " = '"
					+ exp.getShortName() + "';";

			try {
				PreparedStatement selectExpansion = conn
						.prepareStatement(sSelectExp);
				ResultSet expRS = selectExpansion.executeQuery();
				if (expRS.next()) {
					selectExpansion.close();
					continue;
				}
				selectExpansion.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			String sInsertExpansion = "INSERT INTO "
					+ CardDbUtil.DB_TABLE_ALLEXPANSIONS + "("
					+ CardDbUtil.KEY_EXPANSION_NAME + ", "
					+ CardDbUtil.KEY_EXPANSION_SHORTNAME + ") VALUES ('"
					+ exp.getName() + "', '" + exp.getShortName() + "');";

			try {
				PreparedStatement insertExpansion = conn
						.prepareStatement(sInsertExpansion);
				insertExpansion.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done adding new Expansions.");

		allNewExpansions = getAllExpansions(conn);
		allNewCards = sdp.getAllCards();

		int iBlue, iBlack, iWhite, iGreen, iRed;
		String sInsertCard = new String();
		for (Card c : allNewCards) {
			String sSelectCard = "SELECT " + CardDbUtil.KEY_CARD_NAME
					+ " FROM " + CardDbUtil.DB_TABLE_ALLCARDS + " WHERE "
					+ CardDbUtil.KEY_CARD_NAME + " = '" + c.getName() + "';";

			try {
				PreparedStatement selectCard = conn
						.prepareStatement(sSelectCard);
				ResultSet cardRS = selectCard.executeQuery();
				if (cardRS.next()) {
					selectCard.close();
					insertCardExpansionRelationship(c, conn);
					continue;
				}
				selectCard.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			iBlue = c.isBlue() ? 1 : 0;
			iBlack = c.isBlack() ? 1 : 0;
			iWhite = c.isWhite() ? 1 : 0;
			iGreen = c.isGreen() ? 1 : 0;
			iRed = c.isRed() ? 1 : 0;

			sInsertCard = new String();

			if (c.getCardType().contains("Creature")) {
				sInsertCard = "INSERT INTO " + CardDbUtil.DB_TABLE_ALLCARDS
						+ " (" + CardDbUtil.KEY_CARD_NAME + ", "
						+ CardDbUtil.KEY_CARD_ISBLACK + ", "
						+ CardDbUtil.KEY_CARD_ISBLUE + ", "
						+ CardDbUtil.KEY_CARD_ISWHITE + ", "
						+ CardDbUtil.KEY_CARD_ISGREEN + ", "
						+ CardDbUtil.KEY_CARD_ISRED + ", "
						+ CardDbUtil.KEY_CARD_MANACOST + ", "
						+ CardDbUtil.KEY_CARD_CMC + ", "
						+ CardDbUtil.KEY_CARD_TYPE + ", "
						+ CardDbUtil.KEY_CARD_SUBTYPES + ", "
						+ CardDbUtil.KEY_CARD_POWER + ", "
						+ CardDbUtil.KEY_CARD_TOUGHNESS + ", "
						+ CardDbUtil.KEY_CARD_TEXT + ") VALUES ('"
						+ c.getName() + "', " + iBlack + ", " + iBlue + ", "
						+ iWhite + ", " + iGreen + ", " + iRed + ", '"
						+ c.getManaCost() + "', " + c.getCMC() + ", '"
						+ c.getCardType() + "', '" + c.getCardSubType()
						+ "', '" + c.getPower() + "', '" + c.getToughness()
						+ "', '" + c.getText() + "')";
			} else if (!c.getCardType().contains("Land")
					&& !c.getCardType().contains("Scheme")) {
				sInsertCard = "INSERT INTO " + CardDbUtil.DB_TABLE_ALLCARDS
						+ " (" + CardDbUtil.KEY_CARD_NAME + ", "
						+ CardDbUtil.KEY_CARD_ISBLACK + ", "
						+ CardDbUtil.KEY_CARD_ISBLUE + ", "
						+ CardDbUtil.KEY_CARD_ISWHITE + ", "
						+ CardDbUtil.KEY_CARD_ISGREEN + ", "
						+ CardDbUtil.KEY_CARD_ISRED + ", "
						+ CardDbUtil.KEY_CARD_MANACOST + ", "
						+ CardDbUtil.KEY_CARD_CMC + ", "
						+ CardDbUtil.KEY_CARD_TYPE + ", "
						+ CardDbUtil.KEY_CARD_SUBTYPES + ", "
						+ CardDbUtil.KEY_CARD_TEXT + ") VALUES ('"
						+ c.getName() + "', " + iBlack + ", " + iBlue + ", "
						+ iWhite + ", " + iGreen + ", " + iRed + ", '"
						+ c.getManaCost() + "', " + c.getCMC() + ", '"
						+ c.getCardType() + "', '" + c.getCardSubType()
						+ "', '" + c.getText() + "')";
			} else {
				sInsertCard = "INSERT INTO " + CardDbUtil.DB_TABLE_ALLCARDS
						+ " (" + CardDbUtil.KEY_CARD_NAME + ", "
						+ CardDbUtil.KEY_CARD_ISBLACK + ", "
						+ CardDbUtil.KEY_CARD_ISBLUE + ", "
						+ CardDbUtil.KEY_CARD_ISWHITE + ", "
						+ CardDbUtil.KEY_CARD_ISGREEN + ", "
						+ CardDbUtil.KEY_CARD_ISRED + ", "
						+ CardDbUtil.KEY_CARD_CMC + ", "
						+ CardDbUtil.KEY_CARD_TYPE + ", "
						+ CardDbUtil.KEY_CARD_SUBTYPES + ", "
						+ CardDbUtil.KEY_CARD_TEXT + ") VALUES ('"
						+ c.getName() + "', 0, 0, 0, 0, 0, 0, '"
						+ c.getCardType() + "', '" + c.getCardSubType()
						+ "', '" + c.getText() + "')";
			}
			try {
				PreparedStatement insertCard = conn
						.prepareStatement(sInsertCard);
				insertCard.execute();
				insertCard.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			insertCardExpansionRelationship(c, conn);
		}
		System.out.println("Done setting up Cards.");
	}

	private static void insertCardExpansionRelationship(Card c, Connection conn) {
		int cardId = getCardId(c.getName(), conn);

		String sInsertCardExpansionRel = new String();

		for (Expansion exp : c.getAllExpansions()) {
			String sSelectRel = "SELECT " + CardDbUtil.KEY_REL_CARD_ID
					+ " FROM " + CardDbUtil.DB_TABLE_REL_CARD_EXP + " WHERE "
					+ CardDbUtil.KEY_REL_CARD_ID + " = " + cardId + " AND "
					+ CardDbUtil.KEY_REL_EXP_ID + " = "
					+ getExpansionId(exp.getShortName(), conn) + ";";

			try {
				PreparedStatement selectRel = conn.prepareStatement(sSelectRel);
				ResultSet relRS = selectRel.executeQuery();
				if (relRS.next()) {
					selectRel.close();
					continue;
				}
				selectRel.close();
			} catch (SQLException exc) {
				exc.printStackTrace();
			}

			sInsertCardExpansionRel = "INSERT INTO "
					+ CardDbUtil.DB_TABLE_REL_CARD_EXP + " ("
					+ CardDbUtil.KEY_REL_EXP_ID + ", "
					+ CardDbUtil.KEY_REL_CARD_ID + ", "
					+ CardDbUtil.KEY_REL_PIC_URL + ") VALUES ('"
					+ getExpansionId(exp.getShortName(), conn) + "', " + cardId
					+ ", '" + c.getExpansionImages().get(exp).toString() + "')";
			try {
				PreparedStatement insertCardExpansionRel = conn
						.prepareStatement(sInsertCardExpansionRel);
				insertCardExpansionRel.execute();
				insertCardExpansionRel.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static List<Expansion> getAllExpansions(Connection conn) {
		List<Expansion> allExpansions = new ArrayList<Expansion>();

		String sSelectAllExpansions = "SELECT * FROM "
				+ CardDbUtil.DB_TABLE_ALLEXPANSIONS;

		Expansion exp;
		try {
			PreparedStatement selectAllExpansions = conn
					.prepareStatement(sSelectAllExpansions);
			ResultSet expRS = selectAllExpansions.executeQuery();
			while (expRS.next()) {
				exp = new Expansion(
						expRS.getInt(CardDbUtil.KEY_EXPANSION_ROWID),
						expRS.getString(CardDbUtil.KEY_EXPANSION_NAME),
						expRS.getString(CardDbUtil.KEY_EXPANSION_SHORTNAME));

				allExpansions.add(exp);
			}
			expRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Collections.sort(allExpansions);

		return allExpansions;
	}

	private static int getCardId(String name, Connection conn) {
		int cardId = 0;

		String sCount = "SELECT count(*) FROM " + CardDbUtil.DB_TABLE_ALLCARDS
				+ " WHERE " + CardDbUtil.KEY_CARD_NAME + " = '" + name + "'";

		String sSelectCardByName = "SELECT * FROM "
				+ CardDbUtil.DB_TABLE_ALLCARDS + " WHERE "
				+ CardDbUtil.KEY_CARD_NAME + " = '" + name + "'";

		try {
			PreparedStatement countPS = conn.prepareStatement(sCount);
			ResultSet countRS = countPS.executeQuery();
			countRS.next();

			if (countRS.getInt(1) == 1) {
				PreparedStatement selectCardByName = conn
						.prepareStatement(sSelectCardByName);
				ResultSet cRS = selectCardByName.executeQuery();

				cardId = cRS.getInt(CardDbUtil.KEY_CARD_ROWID);
			} else {
				System.out
						.println("MainDatabaseTools.getCardId: No unique card was found with name: "
								+ name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return cardId;
	}

	private static int getExpansionId(String shortName, Connection conn) {
		int expId = 0;

		String sCount = "SELECT count(*) FROM "
				+ CardDbUtil.DB_TABLE_ALLEXPANSIONS + " WHERE "
				+ CardDbUtil.KEY_EXPANSION_SHORTNAME + " = '" + shortName + "'";
		String sSelectExpBySName = "SELECT * FROM "
				+ CardDbUtil.DB_TABLE_ALLEXPANSIONS + " WHERE "
				+ CardDbUtil.KEY_EXPANSION_SHORTNAME + " = '" + shortName + "'";

		try {
			PreparedStatement countPS = conn.prepareStatement(sCount);
			ResultSet countRS = countPS.executeQuery();
			countRS.next();

			if (countRS.getInt(1) == 1) {
				PreparedStatement selectExpBySName = conn
						.prepareStatement(sSelectExpBySName);
				ResultSet cRS = selectExpBySName.executeQuery();

				expId = cRS.getInt(CardDbUtil.KEY_EXPANSION_ROWID);
			} else {
				System.out
						.println("MainDatabaseTools.getExpansionId: No unique card was found with name: "
								+ shortName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return expId;
	}

	private static String dropTable(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName + ";";
	}
}
