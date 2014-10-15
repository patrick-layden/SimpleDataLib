package regalowl.databukkit;

import java.io.File;

import regalowl.databukkit.event.EventHandler;
import regalowl.databukkit.event.Listener;
import regalowl.databukkit.file.ErrorWriter;
import regalowl.databukkit.file.FileTools;
import regalowl.databukkit.file.YamlHandler;
import regalowl.databukkit.sql.Field;
import regalowl.databukkit.sql.FieldType;
import regalowl.databukkit.sql.SQLManager;
import regalowl.databukkit.sql.Table;

public class DataBukkit {

	private String name;
	private String storagePath;
	private EventHandler eh;
	private YamlHandler yh;
	private CommonFunctions cf;
	private FileTools ft;
	private ErrorWriter ew;
	private boolean debug;
	private SQLManager sm;
	private boolean shutdown;


	public static void main (String[] args) {
		DataBukkit db = new DataBukkit("test");
		db.initialize();
		SQLManager sm = db.getSQLManager();
		Table t = db.getSQLManager().addTable("hyperconomy_players");
		Field f = t.addField("ID", FieldType.INTEGER);f.setNotNull();f.setPrimaryKey();f.setAutoIncrement();
		f = t.addField("NAME", FieldType.VARCHAR);f.setFieldSize(255);f.setUnique();
		f = t.addField("UUID", FieldType.VARCHAR);f.setFieldSize(255);f.setUnique();
		f = t.addField("ECONOMY", FieldType.TINYTEXT);
		f = t.addField("BALANCE", FieldType.DOUBLE);f.setNotNull();f.setDefault("0");
		f = t.addField("X", FieldType.DOUBLE);f.setNotNull();f.setDefault("0");
		f = t.addField("Y", FieldType.DOUBLE);f.setNotNull();f.setDefault("0");
		f = t.addField("Z", FieldType.DOUBLE);f.setNotNull();f.setDefault("0");
		f = t.addField("WORLD", FieldType.TINYTEXT);f.setNotNull();
		f = t.addField("HASH", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();f.setDefault("");
		f = t.addField("SALT", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();f.setDefault("");
		sm.saveTables();
		db.shutDown();
		db = new DataBukkit("test");
		db.initialize();
		sm = db.getSQLManager();
		sm.createDatabase();
		Table ta = sm.addTable("hyperconomy_players");
		ta.loadTable();
	}
	
	public DataBukkit(String name) {
		this.name = name;
	}
	
	public void initialize() {
		ft = new FileTools(this);
		this.storagePath = ft.getJarPath() + File.separator + name;
		eh = new EventHandler();
		yh = new YamlHandler(this);
		cf = new CommonFunctions();
		ew = new ErrorWriter(getErrorFilePath(), this);
		sm = new SQLManager(this);
		shutdown = false;
		debug = false;
	}
	
	public void shutDown() {
		if (!shutdown) {
			sm.shutDown();
			if (yh != null) {yh.shutDown();}
			shutdown = true;
		}
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}
	public String getName() {
		return name;
	}
	public boolean debugEnabled() {
		return debug;
	}
	public boolean isDisabled() {
		return shutdown;
	}
	public String getStoragePath() {
		return storagePath;
	}
	public String getErrorFilePath() {
		return storagePath + File.separator + "errors.log";
	}

	
	public void registerListener(Listener l) {
		eh.registerListener(l);
	}
	public void setDebug(boolean state) {
		this.debug = state;
	}


	public SQLManager getSQLManager() {
		return sm;
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
	public EventHandler getEventHandler() {
		return eh;
	}
	
	//TODO move to errorwriter
	public void writeError(Exception e, String info) {
		ew.writeError(e, info, false);
	}
	public void writeError(Exception e) {
		ew.writeError(e, null, false);
	}
	public void writeError(String info) {
		ew.writeError(null, info, false);
	}
	

}
