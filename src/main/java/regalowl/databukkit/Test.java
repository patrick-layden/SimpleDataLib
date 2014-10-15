package regalowl.databukkit;

import regalowl.databukkit.sql.Field;
import regalowl.databukkit.sql.FieldType;
import regalowl.databukkit.sql.SQLManager;
import regalowl.databukkit.sql.Table;

public class Test  {

	public static void main (String[] args) {
		/*
		DataBukkit db = new DataBukkit("test");
		db.initialize();
		db.setDebug(true);
		new TestLogger(db);
		SQLManager sm = db.getSQLManager();
		sm.enableMySQL("192.168.100.35", "testuser", "testuser", "test123", 3306);
		sm.createDatabase();
		Table t = sm.addTable("hyperconomy_players");
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
		/*
		db = new DataBukkit("test");
		db.initialize();
		sm = db.getSQLManager();
		sm.enableMySQL("localhost", "testuser", "testuser", "test123", 3306);
		sm.createDatabase();
		Table ta = sm.addTable("hyperconomy_players");
		ta.loadTable();
		*/
		
	}




}
