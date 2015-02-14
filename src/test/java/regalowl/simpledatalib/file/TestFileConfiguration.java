package regalowl.simpledatalib.file;

import static org.junit.Assert.*;

import org.junit.Test;

import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.TestLogger;


public class TestFileConfiguration {
	@Test
	public void testLoadTableFromString() {
	
		
		SimpleDataLib sdl = new SimpleDataLib("test");
		sdl.initialize();
		sdl.setDebug(true);
		new TestLogger(sdl);
		
		FileConfiguration cfg = new FileConfiguration(sdl);
		cfg.set("key1", "value1");
		String result = cfg.getString("key1");
		assertTrue(result.equals("value1"));
		
		cfg.set("key2.skey1", "value2");
		result = cfg.getString("key2.skey1");
		assertTrue(result.equals("value2"));
		
		cfg.set("key3.skey1.skey2", "value3");
		result = cfg.getString("key3.skey1.skey2");
		assertTrue(result.equals("value3"));
		
		cfg.set("key3.skey1.skey2", "value4");
		result = cfg.getString("key3.skey1.skey2");
		assertTrue(result.equals("value4"));
		
		cfg.set("key4.skey2.skey3.skey4", true);
		boolean b = cfg.getBoolean("key4.skey2.skey3.skey4");
		assertTrue(b);
		
		//System.out.println(debug.replace("{{newline}}", System.getProperty("line.separator")));
		
	}
}
