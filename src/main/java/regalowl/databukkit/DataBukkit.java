package regalowl.databukkit;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import regalowl.databukkit.file.ErrorWriter;
import regalowl.databukkit.file.FileTools;
import regalowl.databukkit.file.YamlHandler;
import regalowl.databukkit.sql.ConnectionPool;
import regalowl.databukkit.sql.SQLRead;
import regalowl.databukkit.sql.SQLWrite;
import regalowl.databukkit.sql.SyncSQLWrite;
import regalowl.databukkit.sql.Table;

public class DataBukkit {

	private Plugin plugin;
	private boolean useMySql;
	private SQLWrite sw;
	private SyncSQLWrite ssw;
	private SQLRead sr;
	private ConnectionPool pool;
	private ArrayList<Table> tables = new ArrayList<Table>();
	private Logger log;
	private YamlHandler yh;
	private CommonFunctions cf;
	private FileTools ft;
	private ErrorWriter ew;
	private boolean debug;
	
	private boolean dataBaseExists;
	private String host;
	private String database;
	private String username;
	private String password;
	private int port;
	
	private boolean shutdown;


	public DataBukkit(Plugin plugin) {
		initialize(plugin);
	}
	
	public void initialize(Plugin plugin) {
		this.plugin = plugin;
		log = Logger.getLogger("Minecraft");
		yh = new YamlHandler(plugin);
		cf = new CommonFunctions();
		ft = new FileTools(plugin);
		ew = new ErrorWriter(getErrorFilePath(), this);
		useMySql = false;
		dataBaseExists = false;
		shutdown = false;
		debug = false;
	}
	
	public void setDebug(boolean state) {
		this.debug = state;
	}
	
	public void enableMySQL(String host, String database, String username, String password, int port) {
		this.host = host;
		this.database = database;
		this.username = username;
		this.password = password;
		this.port = port;
		useMySql = true;
	}
	
	
	public void createDatabase() {
		if (shutdown) {return;}
		boolean databaseOk = false;
		if (useMySql) {
			databaseOk = checkMySQL();
			if (!databaseOk) {
				databaseOk = checkSQLLite();
				log.severe("[DataBukkit["+plugin.getName()+"]]MySQL connection failed, defaulting to SQLite.");
				useMySql = false;
			}
		} else {
			databaseOk = checkSQLLite();
		}
		if (databaseOk) {
			pool = new ConnectionPool(this, 1);
			sw = new SQLWrite(this, pool);
			ssw = new SyncSQLWrite(this, pool);
			sr = new SQLRead(this, pool);
			dataBaseExists = true;
		} else {
			log.severe("-----------------------------------------------------");
			log.severe("[DataBukkit["+plugin.getName()+"]]Database connection failed. Disabling "+plugin.getName()+".");
			log.severe("-----------------------------------------------------");
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}
	

	
	
	public String getSQLitePath() {
		return getPluginFolderPath() + plugin.getName() + ".db";
	}
	
	public String getPluginFolderPath() {
		String pluginFolder = getJarPath() + "plugins" + File.separator + plugin.getName();
		makeFolder(pluginFolder);
		return pluginFolder + File.separator;
	}
	
	public String getErrorFilePath() {
		return getPluginFolderPath() + "errors.log";
	}

	public String getJarPath() {
		String path = DataBukkitPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String serverpath = "";
		try {
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			serverpath = decodedPath.substring(0, decodedPath.lastIndexOf("plugins"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		if (serverpath.startsWith("file:")) {
			serverpath = serverpath.replace("file:", "");
		}
		return serverpath;
	}
	
	public void makeFolder(String path) {
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdir();
		}
	}
	
	private boolean checkSQLLite() {
		String path = getSQLitePath();
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connect = DriverManager.getConnection("jdbc:sqlite:" + path);
			Statement state = connect.createStatement();
			state.execute("DROP TABLE IF EXISTS dbtest");
			state.execute("CREATE TABLE IF NOT EXISTS dbtest (TEST VARCHAR)");
			state.execute("DROP TABLE IF EXISTS dbtest");
			state.close();
			connect.close();
			return true;
		} catch (Exception e) {
			if (debug) {
				writeError(e, "[DataBukkit Debug Message] SQLite check failed.");
			}
			return false;
		}
	}
	
	private boolean checkMySQL() {
		try {
			Connection connect = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
			Statement state = connect.createStatement();
			state.execute("DROP TABLE IF EXISTS dbtest");
			state.execute("CREATE TABLE IF NOT EXISTS dbtest (TEST VARCHAR(255))");
			state.execute("DROP TABLE IF EXISTS dbtest");
			state.close();
			connect.close();
			return true;
		} catch (Exception e) {
			if (debug) {
				writeError(e, "[DataBukkit Debug Message] MySQL check failed.");
			}
			return false;
		}
	}
	
	public Table generateTable(String name) {
		return new Table(name, this);
	}
	
	public Table addTable(String name) {
		Table t = new Table(name, this);
		tables.add(t);
		return t;
	}
	
	public Table getTable(String name) {
		for (Table t:tables) {
			if (name.equalsIgnoreCase(t.getName())) {
				return t;
			}
		}
		return null;
	}
	
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
	
	public YamlHandler getYamlHandler() {
		return yh;
	}
	
	public CommonFunctions getCommonFunctions() {
		return cf;
	}
	
	public FileTools getFileTools() {
		return ft;
	}
	
	public ErrorWriter getErrorWriter() {
		return ew;
	}

	public boolean useMySQL() {
		return useMySql;
	}
	
	public void writeError(Exception e, String info) {
		ew.writeError(e, info, false);
	}
	public void writeError(Exception e) {
		ew.writeError(e, null, false);
	}
	public void writeError(String info) {
		ew.writeError(null, info, false);
	}
	
	public void shutDown() {
		if (!shutdown) {
			if (sw != null) {sw.shutDown();}
			if (sr != null) {sr.shutDown();}
			if (yh != null) {yh.shutDown();}
			shutdown = true;
		}
	}
	
	public Logger getLogger() {
		return log;
	}
	
	public boolean dataBaseExists() {
		return dataBaseExists;
	}
	public Plugin getPlugin() {
		return plugin;
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
}
