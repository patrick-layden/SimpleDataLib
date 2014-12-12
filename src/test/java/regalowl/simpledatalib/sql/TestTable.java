package regalowl.simpledatalib.sql;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.TestLogger;
import regalowl.simpledatalib.sql.Field;
import regalowl.simpledatalib.sql.FieldType;
import regalowl.simpledatalib.sql.SQLManager;
import regalowl.simpledatalib.sql.Table;

public class TestTable {
	@Test
	public void testLoadTableFromString() {
		SimpleDataLib db = new SimpleDataLib("test");
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
		//System.out.println(fs.getCreateStatement());
		//System.out.println(t.getCreateStatement());
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
		//System.out.println(fs.getCreateStatement());
		//System.out.println(t.getCreateStatement());
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
		//System.out.println(fs.getCreateStatement());
		//System.out.println(t.getCreateStatement());
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
		//System.out.println(fs.getCreateStatement());
		//System.out.println(t.getCreateStatement());
		assertTrue(fs.equals(t));
		
		
		fs = sm.generateTable("test5");
		createStatement = "CREATE TABLE `test5` (`NAME` varchar(100) NOT NULL,`DISPLAY_NAME` varchar(255) DEFAULT NULL,"
				+ "`COMPONENTS` varchar(1000) DEFAULT NULL,PRIMARY KEY (`NAME`)) ENGINE=InnoDB DEFAULT CHARSET=latin1";
		fs.loadTableFromString(createStatement);
		//db.getEventPublisher().fireEvent(new LogEvent("[SimpleDataLib["+db.getName()+"]]"+fs.getCreateStatement(), null, LogLevel.ERROR));
		t = sm.generateTable("test5");
		f = t.addField("NAME", FieldType.VARCHAR);f.setFieldSize(100);f.setNotNull();f.setPrimaryKey();
		f = t.addField("DISPLAY_NAME", FieldType.VARCHAR);f.setFieldSize(255);
		f = t.addField("COMPONENTS", FieldType.VARCHAR);f.setFieldSize(1000);
		//db.getEventPublisher().fireEvent(new LogEvent("[SimpleDataLib["+db.getName()+"]]"+t.getCreateStatement(), null, LogLevel.ERROR));
		//System.out.println(fs.getCreateStatement());
		//System.out.println(t.getCreateStatement());
		assertTrue(fs.equals(t));
		
		
		fs = sm.generateTable("test6");
		createStatement = "CREATE TABLE test6 (fa_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "fa_name TEXT  NOT NULL default '', "
				+ "fa_archive_name TEXT  default '', "
				+ "fa_storage_group BLOB,"
				+ "fa_storage_key BLOB default '', "
				+ "fa_deleted_user INTEGER,"
				+ "fa_deleted_timestamp BLOB default '', "
				+ "fa_deleted_reason text, "
				+ "fa_size INTEGER  default 0, "
				+ "fa_width INTEGER default 0, "
				+ "fa_height INTEGER default 0, "
				+ "fa_metadata BLOB, "
				+ "fa_bits INTEGER default 0, "
				+ "fa_media_type TEXT default NULL, "
				+ "fa_major_mime TEXT default \"\"unknown\"\","
				+ " fa_minor_mime BLOB default \"\"unknown\"\", "
				+ "fa_description BLOB,"
				+ "fa_user INTEGER  default 0, "
				+ "fa_user_text TEXT ,"
				+ "fa_timestamp BLOB default '',"
				+ "fa_deleted INTEGER  NOT NULL default 0, "
				+ "fa_sha1 BLOB NOT NULL default '')";
		fs.loadTableFromString(createStatement);
		t = sm.generateTable("test6");
		f = t.addField("fa_id", FieldType.INTEGER);f.setNotNull();f.setPrimaryKey();f.setAutoIncrement();
		f = t.addField("fa_name", FieldType.TEXT);f.setNotNull();f.setDefault("");
		f = t.addField("fa_archive_name", FieldType.TEXT);f.setDefault("");
		f = t.addField("fa_storage_group", FieldType.BLOB);
		f = t.addField("fa_storage_key", FieldType.BLOB);f.setDefault("");
		f = t.addField("fa_deleted_user", FieldType.INTEGER);
		f = t.addField("fa_deleted_timestamp", FieldType.BLOB);f.setDefault("");
		f = t.addField("fa_deleted_reason", FieldType.TEXT);
		f = t.addField("fa_size", FieldType.INTEGER);f.setDefault("0");
		f = t.addField("fa_width", FieldType.INTEGER);f.setDefault("0");
		f = t.addField("fa_height", FieldType.INTEGER);f.setDefault("0");
		f = t.addField("fa_metadata", FieldType.BLOB);
		f = t.addField("fa_bits", FieldType.INTEGER);f.setDefault("0");
		f = t.addField("fa_media_type", FieldType.TEXT);f.setDefault(null);
		f = t.addField("fa_major_mime", FieldType.TEXT);f.setDefault("unknown");
		f = t.addField("fa_minor_mime", FieldType.BLOB);f.setDefault("unknown");
		f = t.addField("fa_description", FieldType.BLOB);
		f = t.addField("fa_user", FieldType.INTEGER);f.setDefault("0");
		f = t.addField("fa_user_text", FieldType.TEXT);
		f = t.addField("fa_timestamp", FieldType.BLOB);f.setDefault("");
		f = t.addField("fa_deleted", FieldType.INTEGER);f.setNotNull();f.setDefault("0");
		f = t.addField("fa_sha1", FieldType.BLOB);f.setNotNull();f.setDefault("");
		//System.out.println(fs.getCreateStatement());
		//System.out.println(t.getCreateStatement());
		assertTrue(fs.equals(t));
		
		fs = sm.generateTable("test7");
		createStatement = "CREATE TABLE `test7` (`ID` int(11) NOT NULL AUTO_INCREMENT,"
				+ "`NAME` varchar(255) DEFAULT NULL,`UUID` varchar(255) DEFAULT NULL,`ECONOMY` tinytext,"
				+ "`BALANCE` double NOT NULL DEFAULT '0',`X` double NOT NULL DEFAULT '0',`Y` double NOT NULL DEFAULT '0',"
				+ "`Z` double NOT NULL DEFAULT '0',`WORLD` tinytext NOT NULL,`HASH` varchar(255) NOT NULL DEFAULT '',"
				+ "`SALT` varchar(255) NOT NULL DEFAULT '',PRIMARY KEY (`ID`),UNIQUE KEY `NAME` (`NAME`), "
				+ "UNIQUE KEY `UUID` (`UUID`)) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1";
		fs.loadTableFromString(createStatement);
		t = sm.generateTable("test7");
		f = t.addField("ID", FieldType.INTEGER);f.setNotNull();f.setPrimaryKey();f.setAutoIncrement();
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
		//System.out.println(fs.getCreateStatement());
		//System.out.println(t.getCreateStatement());
		assertTrue(fs.equals(t));
		
		
		fs = sm.generateTable("test8");
		createStatement = "CREATE TABLE test8 (NAME VARCHAR(100) NOT NULL PRIMARY KEY, BALANCE DOUBLE NOT NULL DEFAULT '0', OWNERS VARCHAR(255), MEMBERS VARCHAR(255))";
		fs.loadTableFromString(createStatement);
		t = sm.generateTable("test8");
		f = t.addField("NAME", FieldType.VARCHAR);f.setFieldSize(100);f.setNotNull();f.setPrimaryKey();
		f = t.addField("BALANCE", FieldType.DOUBLE);f.setNotNull();f.setDefault("0");
		f = t.addField("OWNERS", FieldType.VARCHAR);f.setFieldSize(255);
		f = t.addField("MEMBERS", FieldType.VARCHAR);f.setFieldSize(255);
		assertTrue(fs.equals(t));
		
		
		fs = sm.generateTable("test9");
		createStatement = "CREATE TABLE test9 (ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, OBJECT TINYTEXT, ECONOMY TINYTEXT, TIME DATETIME, PRICE DOUBLE)";
		fs.loadTableFromString(createStatement);
		t = sm.generateTable("test9");
		f = t.addField("ID", FieldType.INTEGER);f.setNotNull();f.setPrimaryKey();f.setAutoIncrement();
		f = t.addField("OBJECT", FieldType.TINYTEXT);
		f = t.addField("ECONOMY", FieldType.TINYTEXT);
		f = t.addField("TIME", FieldType.DATETIME);
		f = t.addField("PRICE", FieldType.DOUBLE);
		assertTrue(fs.equals(t));
		
		//System.out.println(debug.replace("{{newline}}", System.getProperty("line.separator")));
		
	}
}
