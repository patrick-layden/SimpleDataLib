package regalowl.simpledatalib;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import regalowl.simpledatalib.CommonFunctions;


public class CommonFunctionsTest {
	
	@Test
	public void testGetNestingLevel() {
		int lvl = CommonFunctions.getNestLevel("{2}");
		assertTrue(lvl == 2);
		lvl = CommonFunctions.getNestLevel("[111]");
		assertTrue(lvl == 111);
		lvl = CommonFunctions.getNestLevel("{{{}]][7][]{123}{}");
		assertTrue(lvl == 123);
		lvl = CommonFunctions.getNestLevel("{{{}]][0701][]{123}{}");
		assertTrue(lvl == 701);
	}
	
	@Test
	public void testHashMapSerialization() {
		HashMap<String,String> data = new HashMap<String,String>();
		data.put("key1", "data1");
		data.put("key2", "data2");
		data.put("k[9]ey,,,3", "d{{6}ata3");
		String serialized = CommonFunctions.implodeMap(data);
		//System.out.println(serialized);
		HashMap<String,String> data2 = CommonFunctions.explodeMap(serialized);
		//System.out.println(data2.toString());
		assertTrue(data.equals(data2));
	}
	
	@Test
	public void testListSerialization() {
		ArrayList<String> data = new ArrayList<String>();
		data.add("stri{{22}{}[7]ng1");
		data.add("string2");
		data.add("string3");
		String serialized = CommonFunctions.implode(data);
		//System.out.println(serialized);
		ArrayList<String> data2 = CommonFunctions.explode(serialized);
		//System.out.println(data2.toString());
		assertTrue(data.equals(data2));
	}
}
