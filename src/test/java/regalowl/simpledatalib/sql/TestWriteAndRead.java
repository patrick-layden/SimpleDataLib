package regalowl.simpledatalib.sql;

import static org.junit.Assert.*;

import org.junit.Test;

import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.TestLogger;
import regalowl.simpledatalib.sql.Field;
import regalowl.simpledatalib.sql.FieldType;
import regalowl.simpledatalib.sql.SQLManager;
import regalowl.simpledatalib.sql.Table;

public class TestWriteAndRead {
	@Test
	public void testCreateShutdownAndLoad() {
		SimpleDataLib sdl = new SimpleDataLib("test");
		sdl.initialize();
		sdl.setDebug(true);
		new TestLogger(sdl);
		SQLManager sm = sdl.getSQLManager();
		sm.setWriteTaskInterval(1L);
		sm.createDatabase();
		
		SQLWrite sw = sm.getSQLWrite();
		Table t = sm.addTable("hyperconomy_object_data");
		Field f = t.addField("ID", FieldType.INTEGER);f.setPrimaryKey();f.setAutoIncrement();
		f = t.addField("DATA", FieldType.TEXT);
		sm.saveTables();
		sw.writeSync(true);
		String statement = "DELETE FROM hyperconomy_object_data";
		sw.addToQueue(statement);
		sw.writeSyncQueue();
		sw.writeSync(false);
		
		
		statement = "INSERT INTO hyperconomy_object_data (ID, DATA) VALUES ('" + 1 + "', ?)";
		WriteStatement ws = new WriteStatement(statement, sdl);
		ws.addParameter("test");
		sw.addToQueue(ws);
		
		
		sm.shutDown();
		
		
		sdl.initialize();
		sdl.setDebug(true);
		new TestLogger(sdl);
		sm = sdl.getSQLManager();
		sm.createDatabase();
		SQLRead sr = sm.getSQLRead();
		QueryResult qr  = sr.select("SELECT * FROM hyperconomy_object_data WHERE ID = '1'");
		qr.next();
		assertTrue(qr.getString("DATA").equals("test"));
	}
}
