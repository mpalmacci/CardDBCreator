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

	public static final String KEY_EXPANSION_ROWID = "_id";
	public static final String KEY_EXPANSION_NAME = "expansion_name";
	public static final String KEY_EXPANSION_SHORTNAME = "expansion_shortname";

	public static final String KEY_CARD_ROWID = "_id";
	public static final String KEY_CARD_NAME = "card_name";
	public static final String KEY_CARD_DEFAULT_EXPANSION = "card_def_expansion";
	public static final String KEY_CARD_DEFAULT_PICURL = "card_def_picurl";
	public static final String KEY_CARD_ISBLUE = "card_isblue";
	public static final String KEY_CARD_ISBLACK = "card_isblack";
	public static final String KEY_CARD_ISWHITE = "card_iswhite";
	public static final String KEY_CARD_ISGREEN = "card_isgreen";
	public static final String KEY_CARD_ISRED = "card_isred";
	public static final String KEY_CARD_MANACOST = "card_manacost";
	public static final String KEY_CARD_CMC = "card_cmc";
	public static final String KEY_CARD_TYPE = "card_type";
	public static final String KEY_CARD_SUBTYPES = "card_subtypes";
	public static final String KEY_CARD_POWER = "card_power";
	public static final String KEY_CARD_TOUGHNESS = "card_toughness";
	public static final String KEY_CARD_TEXT = "card_text";

	public static final String KEY_EXPANSION_PIC_ROWID = "_id";
	public static final String KEY_EXPANSION_PIC_CARD_ID = "expansion_pic_card_id";
	public static final String KEY_EXPANSION_PIC_EXPANSION_ID = "expansion_pic_exp_id";
	public static final String KEY_EXPANSION_PIC_PICURL = "expansion_pic_picurl";

	private static final String DATABASE_TABLE_ALLEXPANSIONS = "Expansions";
	private static final String DATABASE_TABLE_ALLCARDS = "Cards";
	private static final String DATABASE_TABLE_EXPANSION_PIC = "ExpansionPics";

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
				+ DATABASE_TABLE_ALLEXPANSIONS + " (" + KEY_EXPANSION_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_EXPANSION_NAME
				+ " TEXT NOT NULL, " + KEY_EXPANSION_SHORTNAME
				+ " TEXT NOT NULL);";
		String sCreateCardTable = "CREATE TABLE " + DATABASE_TABLE_ALLCARDS
				+ " (" + KEY_CARD_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_CARD_NAME
				+ " TEXT NOT NULL, " + KEY_CARD_DEFAULT_EXPANSION
				+ " INTEGER NOT NULL, " + KEY_CARD_DEFAULT_PICURL
				+ " TEXT NOT NULL, " + KEY_CARD_ISBLUE + " INTEGER NOT NULL, "
				+ KEY_CARD_ISBLACK + " INTEGER NOT NULL, " + KEY_CARD_ISWHITE
				+ " INTEGER NOT NULL, " + KEY_CARD_ISGREEN
				+ " INTEGER NOT NULL, " + KEY_CARD_ISRED
				+ " INTEGER NOT NULL, " + KEY_CARD_MANACOST + " TEXT, "
				+ KEY_CARD_CMC + " INTEGER NOT NULL, " + KEY_CARD_TYPE
				+ " TEXT NOT NULL, " + KEY_CARD_SUBTYPES + " TEXT, "
				+ KEY_CARD_POWER + " INTEGER, " + KEY_CARD_TOUGHNESS
				+ " INTEGER, " + KEY_CARD_TEXT + " TEXT, FOREIGN KEY("
				+ KEY_CARD_DEFAULT_EXPANSION + ") REFERENCES "
				+ DATABASE_TABLE_ALLEXPANSIONS + "(" + KEY_EXPANSION_ROWID
				+ "));";
		String sCreateExpansionPicTable = "CREATE TABLE "
				+ DATABASE_TABLE_EXPANSION_PIC + " (" + KEY_EXPANSION_PIC_ROWID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_EXPANSION_PIC_CARD_ID + " INTEGER NOT NULL, "
				+ KEY_EXPANSION_PIC_EXPANSION_ID + " INTEGER NOT NULL, "
				+ KEY_EXPANSION_PIC_PICURL + " TEXT NOT NULL, FOREIGN KEY("
				+ KEY_EXPANSION_PIC_CARD_ID + ") REFERENCES "
				+ DATABASE_TABLE_ALLCARDS + "(" + KEY_CARD_ROWID
				+ "), FOREIGN KEY(" + KEY_EXPANSION_PIC_EXPANSION_ID
				+ ") REFERENCES " + DATABASE_TABLE_ALLEXPANSIONS + "("
				+ KEY_EXPANSION_ROWID + "));";

		try {
			PreparedStatement dropExpansions = conn
					.prepareStatement(dropTable(DATABASE_TABLE_ALLEXPANSIONS));
			PreparedStatement dropCards = conn
					.prepareStatement(dropTable(DATABASE_TABLE_ALLCARDS));
			PreparedStatement dropPics = conn
					.prepareStatement(dropTable(DATABASE_TABLE_EXPANSION_PIC));

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
					+ DATABASE_TABLE_ALLEXPANSIONS + "(" + KEY_EXPANSION_NAME
					+ ", " + KEY_EXPANSION_SHORTNAME + ") VALUES ('"
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

		int iBlue, iBlack, iWhite, iGreen, iRed;
		for (Card c : allCards) {
			iBlue = c.isBlue() ? 1 : 0;
			iBlack = c.isBlack() ? 1 : 0;
			iWhite = c.isWhite() ? 1 : 0;
			iGreen = c.isGreen() ? 1 : 0;
			iRed = c.isRed() ? 1 : 0;

			String sInsertCard = "";

			if (c.getCardType().contains("Creature")) {
				sInsertCard = "INSERT INTO "
						+ DATABASE_TABLE_ALLCARDS
						+ " ("
						+ KEY_CARD_NAME
						+ ", "
						+ KEY_CARD_DEFAULT_EXPANSION
						+ ", "
						+ KEY_CARD_DEFAULT_PICURL
						+ ", "
						+ KEY_CARD_ISBLACK
						+ ", "
						+ KEY_CARD_ISBLUE
						+ ", "
						+ KEY_CARD_ISWHITE
						+ ", "
						+ KEY_CARD_ISGREEN
						+ ", "
						+ KEY_CARD_ISRED
						+ ", "
						+ KEY_CARD_MANACOST
						+ ", "
						+ KEY_CARD_CMC
						+ ", "
						+ KEY_CARD_TYPE
						+ ", "
						+ KEY_CARD_SUBTYPES
						+ ", "
						+ KEY_CARD_POWER
						+ ", "
						+ KEY_CARD_TOUGHNESS
						+ ", "
						+ KEY_CARD_TEXT
						+ ") VALUES ('"
						+ c.getName()
						+ "', "
						+ getExpansionId(
								c.getDefaultExpansion().getShortName(), conn)
						+ ", '" + c.getDefaultPicURL().toString() + "', "
						+ iBlack + ", " + iBlue + ", " + iWhite + ", " + iGreen
						+ ", " + iRed + ", '" + c.getManaCost() + "', "
						+ c.getCMC() + ", '" + c.getCardType() + "', '"
						+ c.getCardSubType() + "', '" + c.getPower() + "', '"
						+ c.getToughness() + "', '" + c.getText() + "')";
			} else if (!c.getCardType().contains("Land")
					&& !c.getCardType().contains("Scheme")) {
				sInsertCard = "INSERT INTO "
						+ DATABASE_TABLE_ALLCARDS
						+ " ("
						+ KEY_CARD_NAME
						+ ", "
						+ KEY_CARD_DEFAULT_EXPANSION
						+ ", "
						+ KEY_CARD_DEFAULT_PICURL
						+ ", "
						+ KEY_CARD_ISBLACK
						+ ", "
						+ KEY_CARD_ISBLUE
						+ ", "
						+ KEY_CARD_ISWHITE
						+ ", "
						+ KEY_CARD_ISGREEN
						+ ", "
						+ KEY_CARD_ISRED
						+ ", "
						+ KEY_CARD_MANACOST
						+ ", "
						+ KEY_CARD_CMC
						+ ", "
						+ KEY_CARD_TYPE
						+ ", "
						+ KEY_CARD_SUBTYPES
						+ ", "
						+ KEY_CARD_TEXT
						+ ") VALUES ('"
						+ c.getName()
						+ "', "
						+ getExpansionId(
								c.getDefaultExpansion().getShortName(), conn)
						+ ", '" + c.getDefaultPicURL().toString() + "', "
						+ iBlack + ", " + iBlue + ", " + iWhite + ", " + iGreen
						+ ", " + iRed + ", '" + c.getManaCost() + "', "
						+ c.getCMC() + ", '" + c.getCardType() + "', '"
						+ c.getCardSubType() + "', '" + c.getText() + "')";
			} else {
				sInsertCard = "INSERT INTO "
						+ DATABASE_TABLE_ALLCARDS
						+ " ("
						+ KEY_CARD_NAME
						+ ", "
						+ KEY_CARD_DEFAULT_EXPANSION
						+ ", "
						+ KEY_CARD_DEFAULT_PICURL
						+ ", "
						+ KEY_CARD_ISBLACK
						+ ", "
						+ KEY_CARD_ISBLUE
						+ ", "
						+ KEY_CARD_ISWHITE
						+ ", "
						+ KEY_CARD_ISGREEN
						+ ", "
						+ KEY_CARD_ISRED
						+ ", "
						+ KEY_CARD_CMC
						+ ", "
						+ KEY_CARD_TYPE
						+ ", "
						+ KEY_CARD_SUBTYPES
						+ ", "
						+ KEY_CARD_TEXT
						+ ") VALUES ('"
						+ c.getName()
						+ "', "
						+ getExpansionId(
								c.getDefaultExpansion().getShortName(), conn)
						+ ", '" + c.getDefaultPicURL().toString()
						+ "', 0, 0, 0, 0, 0, 0, '" + c.getCardType() + "', '"
						+ c.getCardSubType() + "', '" + c.getText() + "')";
			}
			try {
				PreparedStatement insertCard = conn
						.prepareStatement(sInsertCard);
				insertCard.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			int cardId = getCardId(c.getName(), conn);

			String sInsertExpansionPic;

			for (Expansion cs : c.getAllExpansions()) {
				sInsertExpansionPic = "INSERT INTO "
						+ DATABASE_TABLE_EXPANSION_PIC + " ("
						+ KEY_EXPANSION_PIC_EXPANSION_ID + ", "
						+ KEY_EXPANSION_PIC_CARD_ID + ", "
						+ KEY_EXPANSION_PIC_PICURL + ") VALUES ('"
						+ getExpansionId(cs.getShortName(), conn) + "', "
						+ cardId + ", '"
						+ c.getExpansionImages().get(cs).toString() + "')";
				try {
					PreparedStatement insertExpansionPic = conn
							.prepareStatement(sInsertExpansionPic);
					insertExpansionPic.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Done setting up Cards and Pictures.");
	}

	private static List<Expansion> getAllExpansions(Connection conn) {
		List<Expansion> allExpansions = new ArrayList<Expansion>();

		String sSelectAllExpansions = "SELECT * FROM "
				+ DATABASE_TABLE_ALLEXPANSIONS;

		Expansion exp;
		try {
			PreparedStatement selectAllExpansions = conn
					.prepareStatement(sSelectAllExpansions);
			ResultSet expRS = selectAllExpansions.executeQuery();
			while (expRS.next()) {
				exp = new Expansion(expRS.getInt(KEY_EXPANSION_ROWID),
						expRS.getString(KEY_EXPANSION_NAME),
						expRS.getString(KEY_EXPANSION_SHORTNAME));

				allExpansions.add(exp);
			}
			expRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Collections.sort(allExpansions);

		return allExpansions;
	}

	private static List<Card> getAllCards(Connection conn) {
		List<Card> allCards = new ArrayList<Card>();

		String sSelectAllCards = "SELECT * FROM " + DATABASE_TABLE_ALLCARDS;
		String sSelectExpansionPics = "SELECT * FROM "
				+ DATABASE_TABLE_ALLEXPANSIONS;

		Card c;
		try {
			PreparedStatement selectAllCards = conn
					.prepareStatement(sSelectAllCards);
			ResultSet cRS = selectAllCards.executeQuery();
			while (cRS.next()) {
				PreparedStatement selectExpansionPics = conn
						.prepareStatement(sSelectExpansionPics);
				ResultSet expRS = selectExpansionPics.executeQuery();
				List<Expansion> cardExpansions = new ArrayList<Expansion>();
				while (expRS.next()) {
					cardExpansions.add(new Expansion(expRS
							.getInt(KEY_EXPANSION_ROWID), expRS
							.getString(KEY_EXPANSION_NAME), expRS
							.getString(KEY_EXPANSION_SHORTNAME)));
				}
				expRS.close();

				c = new Card(cRS.getInt(KEY_CARD_ROWID),
						cRS.getString(KEY_CARD_NAME), cardExpansions);

				allCards.add(c);
			}
			cRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Collections.sort(allCards);

		return allCards;
	}

	private static int getCardId(String name, Connection conn) {
		int cardId = 0;

		String sSelectCardByName = "SELECT * FROM " + DATABASE_TABLE_ALLCARDS
				+ " WHERE " + KEY_CARD_NAME + " = '" + name + "'";

		try {
			PreparedStatement selectCardByName = conn
					.prepareStatement(sSelectCardByName);
			ResultSet cRS = selectCardByName.executeQuery();

			// TODO Check that there is just one ID that has returned
			cardId = cRS.getInt(KEY_CARD_ROWID);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return cardId;
	}

	private static int getExpansionId(String shortName, Connection conn) {
		int expId = 0;

		String sSelectExpBySName = "SELECT * FROM "
				+ DATABASE_TABLE_ALLEXPANSIONS + " WHERE "
				+ KEY_EXPANSION_SHORTNAME + " = '" + shortName + "'";

		try {
			PreparedStatement selectExpBySName = conn
					.prepareStatement(sSelectExpBySName);
			ResultSet cRS = selectExpBySName.executeQuery();

			// TODO Check that there is just one ID that has returned
			expId = cRS.getInt(KEY_CARD_ROWID);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return expId;
	}

	private static String dropTable(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName + ";";
	}
}
