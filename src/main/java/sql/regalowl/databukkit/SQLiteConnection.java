package regalowl.databukkit;

import java.sql.DriverManager;

public class SQLiteConnection extends DatabaseConnection {

	private String sqlitePath;
	
	SQLiteConnection(DataBukkit dab, boolean override) {
		super(dab, override);
		sqlitePath = dab.getSQLitePath();
		openConnection();
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
