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
	public void testArrayListSerialization() {
		ArrayList<String> data = new ArrayList<String>();
		data.add("stri{{22}{}[7]ng1");
		data.add("string2");
		data.add("string3");
		String serialized = CommonFunctions.implode(data);
		//System.out.println(serialized);
		ArrayList<String> data2 = CommonFunctions.explode(serialized);
		//System.out.println(data2.toString());
		assertTrue(data.equals(data2));
		
		data = new ArrayList<String>();
		data.add(",");
		serialized = CommonFunctions.implode(data);
		data2 = CommonFunctions.explode(serialized);
		assertTrue(data.equals(data2));
		
		data = new ArrayList<String>();
		data.add("");
		data.add("1");
		data.add("two");
		data.add("");
		data.add("four");
		data.add("");
		serialized = CommonFunctions.implode(data);
		data2 = CommonFunctions.explode(serialized);
		System.out.println(data2.toString());
		assertTrue(data.equals(data2));
	}
	
	@Test
	public void testIntArrayListSerialization() {
		ArrayList<Integer> data = new ArrayList<Integer>();
		data.add(23);
		data.add(1);
		data.add(79797);
		String serialized = CommonFunctions.implode(CommonFunctions.convertToStringArrayList(data));
		ArrayList<Integer> data2 = CommonFunctions.convertToIntArrayList(CommonFunctions.explode(serialized));
		assertTrue(data.equals(data2));
	}
	
	@Test
	public void testIntMapSerialization() {
		HashMap<String,Integer> data = new HashMap<String,Integer>();
		data.put("key1", 45);
		data.put("key2", 9999);
		data.put("k[9]ey,,,3", -999);
		String serialized = CommonFunctions.implodeMap(CommonFunctions.convertToStringMap(data));
		HashMap<String,Integer> data2 = CommonFunctions.convertToIntMap(CommonFunctions.explodeMap(serialized));
		assertTrue(data.equals(data2));
	}
}
