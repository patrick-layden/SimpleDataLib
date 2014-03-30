package regalowl.databukkit.sql;

import java.sql.DriverManager;

import regalowl.databukkit.DataBukkit;

public class SQLiteConnection extends DatabaseConnection {

	private String sqlitePath;
	
	/**
	 * Creates and opens a new SQLite connection.
	 * @param dab The DataBukkit object.
	 * @param readOnly Whether or not this connection should be read only.
	 * @param override Whether or not to enable the shutdown override for this connection.  
	 * If enabled this connection can be used to write while a plugin is being shut down.
	 */
	SQLiteConnection(DataBukkit dab, boolean readOnly, boolean override) {
		super(dab, readOnly, override);
		sqlitePath = dab.getSQLitePath();
		openConnection();
	}

	
	protected void openConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
			connection.setReadOnly(readOnly.get());
			if (!isValid()) {
				//TODO
			}
		} catch (Exception e) {
			dab.writeError(e, "Database connection error.");
		}
	}


}
