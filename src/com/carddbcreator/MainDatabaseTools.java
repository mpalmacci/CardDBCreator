package com.carddbcreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.magichat.Card;
import com.magichat.Expansion;
import com.magichat.MagicHatDbHelper;

public class MainDatabaseTools {
	/*
	 * IMPORTANT NOTES TO RUN THIS PROGRAM: In order to run this you need to
	 * first Open Oracle (download the latest version if necessary):
	 * /Users/mpalmacci
	 * /Documents/workspace/cockatrice_20120702/cockatrice/cockatrice_mac_20120630
	 * The default location for the Cards database is at:
	 * /Users/mpalmacci/Library/Application
	 * Support/Cockatrice/Cockatrice/cards.xml This setting lives in the
	 * Cockatrice Preferences In Oracle, select File/Download Sets
	 * Information... Select the sets you want, then import all the Cards Go to
	 * the Cards Database Location and add it to the directory:
	 * /Users/mpalmacci/Documents/workspace/CardDBCreator
	 * 
	 * REPLACE ALL single quotation marks with two single quotation marks
	 * 
	 * REPLACE ALL &quot; with \"
	 * 
	 * REPLACE ALL * with \*
	 */
	// private static final MagicHatDbHelper cardDb = new MagicHatDbHelper();

	public static void main(String[] args) {

		try {
			SQLiteDb db = new SQLiteDb();
			Connection conn = db.getConnection();
			createSchema(conn);
			setupCards(conn);
			db.closeConnection();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private static void createSchema(Connection conn) throws SQLException {
		// which will produce a legitimate Url for SqlLite JDBC :
		// jdbc:sqlite:card.db
		String sCreateExpansionTable = "CREATE TABLE "
				+ MagicHatDbHelper.DB_TABLE_ALLEXPANSIONS + " ("
				+ MagicHatDbHelper.KEY_EXPANSION_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ MagicHatDbHelper.KEY_EXPANSION_NAME + " TEXT NOT NULL, "
				+ MagicHatDbHelper.KEY_EXPANSION_SHORTNAME + " TEXT NOT NULL);";
		String sCreateCardTable = "CREATE TABLE "
				+ MagicHatDbHelper.DB_TABLE_ALLCARDS + " ("
				+ MagicHatDbHelper.KEY_CARD_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ MagicHatDbHelper.KEY_CARD_NAME + " TEXT NOT NULL, "
				+ MagicHatDbHelper.KEY_CARD_ISBLUE + " INTEGER NOT NULL, "
				+ MagicHatDbHelper.KEY_CARD_ISBLACK + " INTEGER NOT NULL, "
				+ MagicHatDbHelper.KEY_CARD_ISWHITE + " INTEGER NOT NULL, "
				+ MagicHatDbHelper.KEY_CARD_ISGREEN + " INTEGER NOT NULL, "
				+ MagicHatDbHelper.KEY_CARD_ISRED + " INTEGER NOT NULL, "
				+ MagicHatDbHelper.KEY_CARD_MANACOST + " TEXT, "
				+ MagicHatDbHelper.KEY_CARD_CMC + " INTEGER NOT NULL, "
				+ MagicHatDbHelper.KEY_CARD_TYPE + " TEXT NOT NULL, "
				+ MagicHatDbHelper.KEY_CARD_SUBTYPES + " TEXT, "
				+ MagicHatDbHelper.KEY_CARD_POWER + " INTEGER, "
				+ MagicHatDbHelper.KEY_CARD_TOUGHNESS + " INTEGER, "
				+ MagicHatDbHelper.KEY_CARD_RARITY + " TEXT, "
				+ MagicHatDbHelper.KEY_CARD_TEXT + " TEXT);";
		String sCreateExpansionPicTable = "CREATE TABLE "
				+ MagicHatDbHelper.DB_TABLE_REL_CARD_EXP + " ("
				+ MagicHatDbHelper.KEY_REL_CARD_EXP_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ MagicHatDbHelper.KEY_REL_CARD_ID + " INTEGER NOT NULL, "
				+ MagicHatDbHelper.KEY_REL_EXP_ID + " INTEGER NOT NULL, "
				+ MagicHatDbHelper.KEY_REL_PIC_URL
				+ " TEXT NOT NULL, FOREIGN KEY(" + MagicHatDbHelper.KEY_REL_CARD_ID
				+ ") REFERENCES " + MagicHatDbHelper.DB_TABLE_ALLCARDS + "("
				+ MagicHatDbHelper.KEY_CARD_ROWID + "), FOREIGN KEY("
				+ MagicHatDbHelper.KEY_REL_EXP_ID + ") REFERENCES "
				+ MagicHatDbHelper.DB_TABLE_ALLEXPANSIONS + "("
				+ MagicHatDbHelper.KEY_EXPANSION_ROWID + "));";

		try {
			PreparedStatement dropExpansions = conn
					.prepareStatement(dropTable(MagicHatDbHelper.DB_TABLE_ALLEXPANSIONS));
			PreparedStatement dropCards = conn
					.prepareStatement(dropTable(MagicHatDbHelper.DB_TABLE_ALLCARDS));
			PreparedStatement dropPics = conn
					.prepareStatement(dropTable(MagicHatDbHelper.DB_TABLE_REL_CARD_EXP));

			dropExpansions.execute();
			dropCards.execute();
			dropPics.execute();

			PreparedStatement createExpansionTable = conn
					.prepareStatement(sCreateExpansionTable);
			PreparedStatement createCardTable = conn
					.prepareStatement(sCreateCardTable);
			PreparedStatement createExpansionPicTable = conn
					.prepareStatement(sCreateExpansionPicTable);

			createExpansionTable.execute();
			createCardTable.execute();
			createExpansionPicTable.execute();
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

		for (Expansion cs : allExpansions) {
			String sInsertExpansion = "INSERT INTO "
					+ MagicHatDbHelper.DB_TABLE_ALLEXPANSIONS + "("
					+ MagicHatDbHelper.KEY_EXPANSION_NAME + ", "
					+ MagicHatDbHelper.KEY_EXPANSION_SHORTNAME + ") VALUES ('"
					+ cs.getName() + "', '" + cs.getShortName() + "');";

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

		int iBlue, iBlack, iWhite, iGreen, iRed, cardId;
		String sInsertCardExpansionRel = new String(), sInsertCard = new String();
		for (Card c : allCards) {
			iBlue = c.isBlue() ? 1 : 0;
			iBlack = c.isBlack() ? 1 : 0;
			iWhite = c.isWhite() ? 1 : 0;
			iGreen = c.isGreen() ? 1 : 0;
			iRed = c.isRed() ? 1 : 0;

			sInsertCard = new String();

			if (c.getCardType().contains("Creature")) {
				sInsertCard = "INSERT INTO "
						+ MagicHatDbHelper.DB_TABLE_ALLCARDS + " ("
						+ MagicHatDbHelper.KEY_CARD_NAME + ", "
						+ MagicHatDbHelper.KEY_CARD_ISBLACK + ", "
						+ MagicHatDbHelper.KEY_CARD_ISBLUE + ", "
						+ MagicHatDbHelper.KEY_CARD_ISWHITE + ", "
						+ MagicHatDbHelper.KEY_CARD_ISGREEN + ", "
						+ MagicHatDbHelper.KEY_CARD_ISRED + ", "
						+ MagicHatDbHelper.KEY_CARD_MANACOST + ", "
						+ MagicHatDbHelper.KEY_CARD_CMC + ", "
						+ MagicHatDbHelper.KEY_CARD_TYPE + ", "
						+ MagicHatDbHelper.KEY_CARD_SUBTYPES + ", "
						+ MagicHatDbHelper.KEY_CARD_POWER + ", "
						+ MagicHatDbHelper.KEY_CARD_TOUGHNESS + ", "
						+ MagicHatDbHelper.KEY_CARD_TEXT + ") VALUES ('"
						+ c.getName() + "', " + iBlack + ", " + iBlue + ", "
						+ iWhite + ", " + iGreen + ", " + iRed + ", '"
						+ c.getManaCost() + "', " + c.getCMC() + ", '"
						+ c.getCardType() + "', '" + c.getCardSubType()
						+ "', '" + c.getPower() + "', '" + c.getToughness()
						+ "', '" + c.getText() + "')";
			} else if (!c.getCardType().contains("Land")
					&& !c.getCardType().contains("Scheme")) {
				sInsertCard = "INSERT INTO "
						+ MagicHatDbHelper.DB_TABLE_ALLCARDS + " ("
						+ MagicHatDbHelper.KEY_CARD_NAME + ", "
						+ MagicHatDbHelper.KEY_CARD_ISBLACK + ", "
						+ MagicHatDbHelper.KEY_CARD_ISBLUE + ", "
						+ MagicHatDbHelper.KEY_CARD_ISWHITE + ", "
						+ MagicHatDbHelper.KEY_CARD_ISGREEN + ", "
						+ MagicHatDbHelper.KEY_CARD_ISRED + ", "
						+ MagicHatDbHelper.KEY_CARD_MANACOST + ", "
						+ MagicHatDbHelper.KEY_CARD_CMC + ", "
						+ MagicHatDbHelper.KEY_CARD_TYPE + ", "
						+ MagicHatDbHelper.KEY_CARD_SUBTYPES + ", "
						+ MagicHatDbHelper.KEY_CARD_TEXT + ") VALUES ('"
						+ c.getName() + "', " + iBlack + ", " + iBlue + ", "
						+ iWhite + ", " + iGreen + ", " + iRed + ", '"
						+ c.getManaCost() + "', " + c.getCMC() + ", '"
						+ c.getCardType() + "', '" + c.getCardSubType()
						+ "', '" + c.getText() + "')";
			} else {
				sInsertCard = "INSERT INTO "
						+ MagicHatDbHelper.DB_TABLE_ALLCARDS + " ("
						+ MagicHatDbHelper.KEY_CARD_NAME + ", "
						+ MagicHatDbHelper.KEY_CARD_ISBLACK + ", "
						+ MagicHatDbHelper.KEY_CARD_ISBLUE + ", "
						+ MagicHatDbHelper.KEY_CARD_ISWHITE + ", "
						+ MagicHatDbHelper.KEY_CARD_ISGREEN + ", "
						+ MagicHatDbHelper.KEY_CARD_ISRED + ", "
						+ MagicHatDbHelper.KEY_CARD_CMC + ", "
						+ MagicHatDbHelper.KEY_CARD_TYPE + ", "
						+ MagicHatDbHelper.KEY_CARD_SUBTYPES + ", "
						+ MagicHatDbHelper.KEY_CARD_TEXT + ") VALUES ('"
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

			cardId = getCardId(c.getName(), conn);

			sInsertCardExpansionRel = new String();

			for (Expansion cs : c.getAllExpansions()) {
				sInsertCardExpansionRel = "INSERT INTO "
						+ MagicHatDbHelper.DB_TABLE_REL_CARD_EXP + " ("
						+ MagicHatDbHelper.KEY_REL_EXP_ID + ", "
						+ MagicHatDbHelper.KEY_REL_CARD_ID + ", "
						+ MagicHatDbHelper.KEY_REL_PIC_URL + ") VALUES ('"
						+ getExpansionId(cs.getShortName(), conn) + "', "
						+ cardId + ", '"
						+ c.getExpansionImages().get(cs).toString() + "')";
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
		System.out.println("Done setting up Cards.");
	}

	private static List<Expansion> getAllExpansions(Connection conn) {
		List<Expansion> allExpansions = new ArrayList<Expansion>();

		String sSelectAllExpansions = "SELECT * FROM "
				+ MagicHatDbHelper.DB_TABLE_ALLEXPANSIONS;

		Expansion exp;
		try {
			PreparedStatement selectAllExpansions = conn
					.prepareStatement(sSelectAllExpansions);
			ResultSet expRS = selectAllExpansions.executeQuery();
			while (expRS.next()) {
				exp = new Expansion(
						expRS.getInt(MagicHatDbHelper.KEY_EXPANSION_ROWID),
						expRS.getString(MagicHatDbHelper.KEY_EXPANSION_NAME),
						expRS.getString(MagicHatDbHelper.KEY_EXPANSION_SHORTNAME));

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

		String sCount = "SELECT count(*) FROM "
				+ MagicHatDbHelper.DB_TABLE_ALLCARDS + " WHERE "
				+ MagicHatDbHelper.KEY_CARD_NAME + " = '" + name + "'";

		String sSelectCardByName = "SELECT * FROM "
				+ MagicHatDbHelper.DB_TABLE_ALLCARDS + " WHERE "
				+ MagicHatDbHelper.KEY_CARD_NAME + " = '" + name + "'";

		try {
			PreparedStatement countPS = conn.prepareStatement(sCount);
			ResultSet countRS = countPS.executeQuery();
			countRS.next();

			if (countRS.getInt(1) == 1) {
				PreparedStatement selectCardByName = conn
						.prepareStatement(sSelectCardByName);
				ResultSet cRS = selectCardByName.executeQuery();

				cardId = cRS.getInt(MagicHatDbHelper.KEY_CARD_ROWID);
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
				+ MagicHatDbHelper.DB_TABLE_ALLEXPANSIONS + " WHERE "
				+ MagicHatDbHelper.KEY_EXPANSION_SHORTNAME + " = '" + shortName
				+ "'";
		String sSelectExpBySName = "SELECT * FROM "
				+ MagicHatDbHelper.DB_TABLE_ALLEXPANSIONS + " WHERE "
				+ MagicHatDbHelper.KEY_EXPANSION_SHORTNAME + " = '" + shortName
				+ "'";

		try {
			PreparedStatement countPS = conn.prepareStatement(sCount);
			ResultSet countRS = countPS.executeQuery();
			countRS.next();

			if (countRS.getInt(1) == 1) {
				PreparedStatement selectExpBySName = conn
						.prepareStatement(sSelectExpBySName);
				ResultSet cRS = selectExpBySName.executeQuery();

				expId = cRS.getInt(MagicHatDbHelper.KEY_EXPANSION_ROWID);
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
