package regalowl.databukkit;



import java.sql.DriverManager;

public class MySQLConnection extends DatabaseConnection {
	
	private String username;
	private String password;
	private int port;
	private String host;
	private String database;
	
	/**
	 * Creates and opens a new MySQL connection.
	 * @param dab The DataBukkit object.
	 * @param readOnly Whether or not this connection should be read only.
	 * @param override Whether or not to enable the shutdown override for this connection.  
	 * If enabled this connection can be used to write while a plugin is being shut down.
	 */
	MySQLConnection(DataBukkit dab, boolean readOnly, boolean override) {
		super(dab, readOnly, override);
		username = dab.getUsername();
		password = dab.getPassword();
		port = dab.getPort();
		host = dab.getHost();
		database = dab.getDatabase();
		openConnection();
	}

	protected void openConnection() {
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
			connection.setReadOnly(readOnly.get());
			if (!isValid()) {
				//TODO
			}
		} catch (Exception e) {
			dab.writeError(e, "Database connection error.");
		}
	}

	
}