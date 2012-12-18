package com.carddbcreator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLiteDb {
	private String sDriver = "";
	private String sUrl = null;
	private int iTimeout = 30;
	private Connection conn = null;
	private Statement statement = null;

	public SQLiteDb() throws Exception {
		sDriver = "org.sqlite.JDBC";
		sUrl = "jdbc:sqlite:/Users/mpalmacci/Documents/workspace/TheMagicHat/assets/cards.db";
		setConnection();
		setStatement();
	}

	private void setConnection() throws Exception {
		Class.forName(sDriver);
		conn = DriverManager.getConnection(sUrl);
	}

	public Connection getConnection() {
		return conn;
	}

	public void setStatement() throws Exception {
		if (conn == null) {
			setConnection();
		}
		statement = conn.createStatement();
		statement.setQueryTimeout(iTimeout); // set timeout to 30 sec.
	}

	public void closeConnection() {
		try {
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
