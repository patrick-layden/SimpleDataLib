package regalowl.simpledatalib.sql;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import regalowl.simpledatalib.DataBukkit;
import regalowl.simpledatalib.TestLogger;
import regalowl.simpledatalib.sql.Field;
import regalowl.simpledatalib.sql.FieldType;
import regalowl.simpledatalib.sql.SQLManager;
import regalowl.simpledatalib.sql.Table;

public class TestTable {
	@Test
	public void testLoadTableFromString() {
		DataBukkit db = new DataBukkit("test");
		db.initialize();
		db.setDebug(true);
		new TestLogger(db);
		SQLManager sm = db.getSQLManager();
		Table fs = sm.generateTable("test");
		String createStatement = "CREATE TABLE test "
				+ "(ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "NAME VARCHAR(255) UNIQUE, "
				+ "UUID VARCHAR(255) UNIQUE, "
				+ "ECONOMY TINYTEXT, "
				+ "BALANCE DOUBLE NOT NULL DEFAULT '0', "
				+ "X DOUBLE NOT NULL DEFAULT '0', "
				+ "Y DOUBLE NOT NULL DEFAULT '0', "
				+ "Z DOUBLE NOT NULL DEFAULT '0', "
				+ "WORLD TINYTEXT NOT NULL, "
				+ "HASH VARCHAR(255) NOT NULL DEFAULT '', "
				+ "SALT VARCHAR(255) NOT NULL DEFAULT '')";
		fs.loadTableFromString(createStatement);
		Table t = sm.generateTable("test");
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
		assertTrue(fs.equals(t));

		fs = sm.generateTable("test2");
		createStatement = "CREATE TABLE `test2` ("
				+ "`SHOP` varchar(100) NOT NULL, "
				+ "`HYPEROBJECT` varchar(100) NOT NULL, "
				+ "`QUANTITY` double NOT NULL, "
				+ "`SELL_PRICE` double NOT NULL, "
				+ "`BUY_PRICE` double NOT NULL, "
				+ "`MAX_STOCK` int(11) NOT NULL DEFAULT '1000000', "
				+ "`STATUS` varchar(255) NOT NULL, "
				+ "PRIMARY KEY (`SHOP`,`HYPEROBJECT`) ) "
				+ "ENGINE=InnoDB DEFAULT CHARSET=latin1";
		fs.loadTableFromString(createStatement);
		t = sm.generateTable("test2");
		ArrayList<Field> compositeKey = new ArrayList<Field>();
		f = t.addField("SHOP", FieldType.VARCHAR);f.setFieldSize(100);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("HYPEROBJECT", FieldType.VARCHAR);f.setFieldSize(100);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("QUANTITY", FieldType.DOUBLE);f.setNotNull();
		f = t.addField("SELL_PRICE", FieldType.DOUBLE);f.setNotNull();
		f = t.addField("BUY_PRICE", FieldType.DOUBLE);f.setNotNull();
		f = t.addField("MAX_STOCK", FieldType.INTEGER);f.setNotNull();f.setDefault("1000000");
		f = t.addField("STATUS", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		t.setCompositeKey(compositeKey);
		assertTrue(fs.equals(t));
		
		fs = sm.generateTable("test3");
		createStatement = "CREATE TABLE `hyperconomy_info_signs` ( "
				+ "`WORLD` varchar(100) NOT NULL, "
				+ "`X` int(11) NOT NULL, "
				+ "`Y` int(11) NOT NULL, "
				+ "`Z` int(11) NOT NULL, "
				+ "`HYPEROBJECT` varchar(255) NOT NULL, "
				+ "`TYPE` varchar(255) NOT NULL, "
				+ "`MULTIPLIER` int(11) NOT NULL, "
				+ "`ECONOMY` varchar(255) NOT NULL, "
				+ "`ECLASS` varchar(255) NOT NULL, "
				+ "PRIMARY KEY (`WORLD`,`X`,`Y`,`Z`)) "
				+ "ENGINE=InnoDB DEFAULT CHARSET=latin1";
		fs.loadTableFromString(createStatement);
		t = sm.generateTable("test3");
		compositeKey = new ArrayList<Field>();
		f = t.addField("WORLD", FieldType.VARCHAR);f.setFieldSize(100);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("X", FieldType.INTEGER);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("Y", FieldType.INTEGER);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("Z", FieldType.INTEGER);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("HYPEROBJECT", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		f = t.addField("TYPE", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		f = t.addField("MULTIPLIER", FieldType.INTEGER);f.setNotNull();
		f = t.addField("ECONOMY", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		f = t.addField("ECLASS", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		t.setCompositeKey(compositeKey);
		assertTrue(fs.equals(t));
		
		
		
		fs = sm.generateTable("test4");
		createStatement = "CREATE TABLE test4 ("
				+ "WORLD VARCHAR(100) NOT NULL, "
				+ "X INTEGER NOT NULL, "
				+ "Y INTEGER NOT NULL, "
				+ "Z INTEGER NOT NULL, "
				+ "HYPEROBJECT VARCHAR(255) NOT NULL, "
				+ "TYPE VARCHAR(255) NOT NULL, "
				+ "MULTIPLIER INTEGER NOT NULL, "
				+ "ECONOMY VARCHAR(255) NOT NULL, "
				+ "ECLASS VARCHAR(255) NOT NULL, "
				+ "PRIMARY KEY(WORLD, X, Y, Z))";
		fs.loadTableFromString(createStatement);
		t = sm.generateTable("test4");
		compositeKey = new ArrayList<Field>();
		f = t.addField("WORLD", FieldType.VARCHAR);f.setFieldSize(100);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("X", FieldType.INTEGER);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("Y", FieldType.INTEGER);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("Z", FieldType.INTEGER);f.setNotNull();
		compositeKey.add(f);
		f = t.addField("HYPEROBJECT", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		f = t.addField("TYPE", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		f = t.addField("MULTIPLIER", FieldType.INTEGER);f.setNotNull();
		f = t.addField("ECONOMY", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		f = t.addField("ECLASS", FieldType.VARCHAR);f.setFieldSize(255);f.setNotNull();
		t.setCompositeKey(compositeKey);
		assertTrue(fs.equals(t));
		
		
		//db.getEventHandler().fireEvent(new LogEvent("[DataBukkit["+db.getName()+"]]"+fs.getCreateStatement(), null, LogLevel.ERROR));
		
	}
}
