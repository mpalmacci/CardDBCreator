package com.carddbcreator;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.magichat.Card;
import com.magichat.Expansion;

public class MainDatabaseTools {
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

	private static final String DATABASE_NAME = "cards.db";
	private static final String DATABASE_DRIVER = "org.sqlite.JDBC";
	private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;

	private static final String DATABASE_TABLE_ALLEXPANSIONS = "Expansions";
	private static final String DATABASE_TABLE_ALLCARDS = "Cards";
	private static final String DATABASE_TABLE_EXPANSION_PIC = "ExpansionPics";

	public static void main(String[] args) {

		try {
			SQLiteDb db = new SQLiteDb(DATABASE_DRIVER, DATABASE_URL);
			createDatabase(db);
			setupCards(db);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private static void createDatabase(SQLiteDb db) throws Exception {
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
			db.executeQry(dropTable(DATABASE_TABLE_ALLEXPANSIONS));
			db.executeQry(dropTable(DATABASE_TABLE_ALLCARDS));
			db.executeQry(dropTable(DATABASE_TABLE_EXPANSION_PIC));

			db.executeQry(sCreateExpansionTable);
			db.executeQry(sCreateCardTable);
			db.executeQry(sCreateExpansionPicTable);
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				db.closeConnection();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	private static void setupCards(SQLiteDb db) {
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
			String insertExpansion = "INSERT INTO"
					+ DATABASE_TABLE_ALLEXPANSIONS + "(" + KEY_EXPANSION_NAME
					+ ", " + KEY_EXPANSION_SHORTNAME + ") VALUES ("
					+ cs.getName() + ", " + cs.getShortName() + ");";

			try {
				db.executeQry(insertExpansion);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done setting up Expansions.");

		allExpansions = getAllExpansions(db);
		allCards = sdp.getAllCards();

		int iBlue, iBlack, iWhite, iGreen, iRed;
		for (Card c : allCards) {
			iBlue = c.isBlue() ? 1 : 0;
			iBlack = c.isBlack() ? 1 : 0;
			iWhite = c.isWhite() ? 1 : 0;
			iGreen = c.isGreen() ? 1 : 0;
			iRed = c.isRed() ? 1 : 0;

			String insertCard = "";

			if (c.getCardType().contains("Creature")) {
				insertCard = "INSERT INTO "
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
						+ ") VALUES ("
						+ c.getName()
						+ ", "
						+ getExpansionId(
								c.getDefaultExpansion().getShortName(),
								allExpansions) + ", "
						+ c.getDefaultPicURL().toString() + ", " + iBlack
						+ ", " + iBlue + ", " + iWhite + ", " + iGreen + ", "
						+ iRed + ", " + c.getManaCost() + ", " + c.getCMC()
						+ ", " + c.getCardType() + ", " + c.getCardSubTypes()
						+ ", " + c.getPower() + ", " + c.getToughness() + ", "
						+ c.getText() + ");";
			} else if (!c.getCardType().contains("Land")
					&& !c.getCardType().contains("Scheme")) {
				insertCard = "INSERT INTO "
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
						+ ") VALUES ("
						+ c.getName()
						+ ", "
						+ getExpansionId(
								c.getDefaultExpansion().getShortName(),
								allExpansions) + ", "
						+ c.getDefaultPicURL().toString() + ", " + iBlack
						+ ", " + iBlue + ", " + iWhite + ", " + iGreen + ", "
						+ iRed + ", 0, " + c.getCardType() + ", "
						+ c.getCardSubTypes() + ", " + c.getText() + ");";
			} else {
				insertCard = "INSERT INTO "
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
						+ ") VALUES ("
						+ c.getName()
						+ ", "
						+ getExpansionId(
								c.getDefaultExpansion().getShortName(),
								allExpansions) + ", "
						+ c.getDefaultPicURL().toString() + ", " + iBlack
						+ ", " + iBlue + ", " + iWhite + ", " + iGreen + ", "
						+ iRed + ", " + c.getManaCost() + ", " + c.getCMC()
						+ ", " + c.getCardType() + ", " + c.getCardSubTypes()
						+ ", " + c.getText() + ");";
			}
			try {
				db.executeQry(insertCard);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done setting up Cards.");

		allCards = getAllCards(db);

		for (Card c : allCards) {
			for (Expansion cs : c.getAllExpansions()) {
				int cardId = 0;
				for (Card cd : allCards) {
					if (cd.getName().equals(cd.getName())) {
						cardId = cd.getId();
					}
				}
				if (cardId == 0) {
					System.out
							.println("MagicHatDB.setupCards: Card Id was not found.");
				}
				String insertExpansionPic = "INSERT INTO "
						+ DATABASE_TABLE_EXPANSION_PIC + " ("
						+ KEY_EXPANSION_PIC_EXPANSION_ID + ", "
						+ KEY_EXPANSION_PIC_CARD_ID + ", "
						+ KEY_EXPANSION_PIC_PICURL + ") VALUES ("
						+ getExpansionId(cs.getShortName(), allExpansions)
						+ ", " + cardId + ", "
						+ c.getExpansionImages().get(cs).toString() + ");";
				try {
					db.executeQry(insertExpansionPic);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Done setting up Card Pictures.");
	}

	private static List<Expansion> getAllExpansions(SQLiteDb db) {
		List<Expansion> allExpansions = new ArrayList<Expansion>();

		String selectAllExpansions = "SELECT * FROM "
				+ DATABASE_TABLE_ALLEXPANSIONS;

		Expansion exp;
		try {
			ResultSet expRS = db.executeQry(selectAllExpansions);
			try {
				while (expRS.next()) {
					exp = new Expansion(expRS.getInt(KEY_EXPANSION_ROWID),
							expRS.getString(KEY_EXPANSION_NAME),
							expRS.getString(KEY_EXPANSION_SHORTNAME));

					allExpansions.add(exp);
				}
				expRS.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Collections.sort(allExpansions);

		return allExpansions;
	}

	private static List<Card> getAllCards(SQLiteDb db) {
		List<Card> allCards = new ArrayList<Card>();

		String selectAllCards = "SELECT * FROM " + DATABASE_TABLE_ALLCARDS;
		String selectExpansionPics = "SELECT * FROM "
				+ DATABASE_TABLE_EXPANSION_PIC + ", "
				+ DATABASE_TABLE_ALLEXPANSIONS + " WHERE "
				+ DATABASE_TABLE_EXPANSION_PIC + "." + KEY_EXPANSION_PIC_EXPANSION_ID
				+ " = " + DATABASE_TABLE_ALLEXPANSIONS + "."
				+ KEY_EXPANSION_ROWID + ";";

		Card c;
		try {
			ResultSet cRS = db.executeQry(selectAllCards);
			try {
				while (cRS.next()) {
					ResultSet expRS = db.executeQry(selectExpansionPics);
					List<Expansion> cardExpansions = new ArrayList<Expansion>();
					while (expRS.next()) {
						cardExpansions.add(new Expansion(expRS
								.getInt(DATABASE_TABLE_ALLEXPANSIONS + "."
										+ KEY_EXPANSION_ROWID), expRS.getString(DATABASE_TABLE_ALLEXPANSIONS + "." + KEY_EXPANSION_NAME), expRS.getString(DATABASE_TABLE_ALLEXPANSIONS + "." + KEY_EXPANSION_SHORTNAME)));
					}

					c = new Card(cRS.getInt(KEY_EXPANSION_ROWID),
							cRS.getString(KEY_EXPANSION_NAME), cardExpansions);

					allCards.add(c);
				}
				cRS.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Collections.sort(allCards);

		return allCards;
	}

	private static int getExpansionId(String shortName,
			List<Expansion> allExpansions) {
		for (Expansion exp : allExpansions) {
			if (exp.getShortName().equals(shortName)) {
				return exp.getId();
			}
		}
		System.out.println("The Expansion wasn't found in getExpansionId");
		return 0;
	}

	private static String dropTable(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName + ";";
	}
}
