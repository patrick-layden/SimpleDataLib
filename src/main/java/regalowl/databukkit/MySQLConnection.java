package regalowl.databukkit;



import java.sql.DriverManager;

public class MySQLConnection extends DatabaseConnection {
	
	private String username;
	private String password;
	private int port;
	private String host;
	private String database;
	
	MySQLConnection(DataBukkit dab) {
		super(dab);
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
		} catch (Exception e) {
			try {
				connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
			} catch (Exception e2) {
				dab.writeError(e2, "Fatal database connection error.");
				return;
			}
		}
	}

	
}