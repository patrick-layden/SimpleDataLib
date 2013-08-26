package regalowl.databukkit;

import java.sql.DriverManager;

public class SQLiteConnection extends DatabaseConnection {

	private String sqlitePath;
	
	SQLiteConnection(DataBukkit dab) {
		super(dab);
		sqlitePath = dab.getSQLitePath();
		openConnection();
		inUse = false;
	}

	
	protected void openConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
		} catch (Exception e) {
			try {
				Class.forName("org.sqlite.JDBC");
				connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
			} catch (Exception e2) {
				dab.writeError(e2, "Fatal database connection error.");
				return;
			}
		}
	}


}
