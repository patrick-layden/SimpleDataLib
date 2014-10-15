package regalowl.databukkit.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;

import regalowl.databukkit.DataBukkit;
import regalowl.databukkit.event.LogLevel;

public class SQLManager {
	
	private DataBukkit db;
	
	private boolean dataBaseExists;
	private String host;
	private String database;
	private String username;
	private String password;
	private int port;
	
	private boolean useMySql;
	private SQLWrite sw;
	private SyncSQLWrite ssw;
	private SQLRead sr;
	private ConnectionPool pool;
	private int connectionPoolSize = 1;
	private ArrayList<Table> tables = new ArrayList<Table>();
	
	
	public SQLManager(DataBukkit db) {
		this.db = db;
		useMySql = false;
		dataBaseExists = false;
	}
	/**
	 * Shuts down the database and writes any remaining SQL statements.
	 */
	public void shutDown() {
		if (!db.isDisabled()) {
			if (sw != null) {sw.shutDown();}
			if (sr != null) {sr.shutDown();}
		}
	}
	/**
	 * Sets the database type to MySQL and uses the provided connection data.
	 */
	public void enableMySQL(String host, String database, String username, String password, int port) {
		this.host = host;
		this.database = database;
		this.username = username;
		this.password = password;
		this.port = port;
		useMySql = true;
	}
	/**
	 * Sets up a MySQL or SQLite database.  The database can be read via SQLRead, and written via SQLWrite or SyncSQLWrite.
	 */
	public void createDatabase() {
		if (db.isDisabled()) {return;}
		boolean databaseOk = false;
		if (useMySql) {
			databaseOk = checkMySQL();
			if (!databaseOk) {
				databaseOk = checkSQLLite();
				db.getEventHandler().fireLogEvent("[DataBukkit["+db.getName()+"]]MySQL connection failed, defaulting to SQLite.", null, LogLevel.ERROR);
				useMySql = false;
			}
		} else {
			databaseOk = checkSQLLite();
		}
		if (databaseOk) {
			pool = new ConnectionPool(db, connectionPoolSize);
			sw = new SQLWrite(db, pool);
			ssw = new SyncSQLWrite(db, pool);
			sr = new SQLRead(db, pool);
			dataBaseExists = true;
		} else {
			db.getEventHandler().fireLogEvent("[DataBukkit["+db.getName()+"]]Database connection failed. Attempting to disable "+db.getName()+".", null, LogLevel.ERROR);
			db.getEventHandler().fireDisableEvent();
		}
	}
	private boolean checkSQLLite() {
		String path = getSQLitePath();
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connect = DriverManager.getConnection("jdbc:sqlite:" + path);
			Statement state = connect.createStatement();
			state.execute("DROP TABLE IF EXISTS dbtest12343432");
			state.execute("CREATE TABLE IF NOT EXISTS dbtest12343432 (TEST VARCHAR)");
			state.execute("DROP TABLE IF EXISTS dbtest12343432");
			state.close();
			connect.close();
			return true;
		} catch (Exception e) {
			if (db.debugEnabled()) {
				db.getEventHandler().fireLogEvent("[DataBukkit["+db.getName()+"]] SQLite check failed.", e, LogLevel.ERROR);
				db.writeError(e, "[DataBukkit Debug Message] SQLite check failed.");
			}
			return false;
		}
	}
	private boolean checkMySQL() {
		try {
			Connection connect = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
			Statement state = connect.createStatement();
			state.execute("DROP TABLE IF EXISTS dbtest12343432");
			state.execute("CREATE TABLE IF NOT EXISTS dbtest12343432 (TEST VARCHAR(255))");
			state.execute("DROP TABLE IF EXISTS dbtest12343432");
			state.close();
			connect.close();
			return true;
		} catch (Exception e) {
			if (db.debugEnabled()) {
				db.getEventHandler().fireLogEvent("[DataBukkit["+db.getName()+"]] MySQL check failed.", e, LogLevel.ERROR);
				db.writeError(e, "[DataBukkit Debug Message] MySQL check failed.");
			}
			return false;
		}
	}
	
	/**
	 * Sets the number of database connections to use.  If using SQLite this is capped at 1.
	 */
	public void setConnectionPoolSize(int size) {
		if (!useMySql) return;
		this.connectionPoolSize = size;
	}
	/**
	 * MySQL is enabled if true.
	 */
	public boolean useMySQL() {
		return useMySql;
	} 
	/**
	 * A working database is ready to be used if true.
	 */
	public boolean dataBaseExists() {
		return dataBaseExists;
	}
	public String getHost() {
		return host;
	}
	public String getDatabase() {
		return database;
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public int getPort() {
		return port;
	}
	
	/**
	 * Generates a new Table object which represents a SQL table.
	 */
	public Table generateTable(String name) {
		return new Table(name, db);
	}
	/**
	 * Generates a new Table object and adds it to the stored list of tables.
	 */
	public Table addTable(String name) {
		Table t = new Table(name, db);
		tables.add(t);
		return t;
	}
	/**
	 * If the SQLManager has the specified table object in storage it will return it.
	 */
	public Table getTable(String name) {
		for (Table t:tables) {
			if (name.equalsIgnoreCase(t.getName())) {
				return t;
			}
		}
		return null;
	}
	/**
	 * Writes all stored tables to the database.
	 */
	public void saveTables() {
		for (Table t:tables) {
			t.save();
		}
		ssw.writeQueue();
	}
	
	
	public SQLWrite getSQLWrite() {
		return sw;
	}
	public SyncSQLWrite getSyncSQLWrite() {
		return ssw;
	}
	public SQLRead getSQLRead() {
		return sr;
	}

	/**
	 * Returns the path to the SQLite database file.
	 */
	public String getSQLitePath() {
		return db.getStoragePath() + File.separator + db.getName() + ".db";
	}
}
