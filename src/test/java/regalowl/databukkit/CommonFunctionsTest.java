package regalowl.databukkit;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;


public class CommonFunctionsTest {
	@Test
	public void testHashMapSerialization() {
		HashMap<String,String> data = new HashMap<String,String>();
		data.put("key1[[[C]]][[C]],", "data1");
		data.put("key2", "da$%$%Fd[C]fesdta2");
		data.put("key3", "d$%FDFD,,,ata3");
		String serialized = CommonFunctions.implodeMap(data);
		HashMap<String,String> data2 = CommonFunctions.explodeMap(serialized);
		assertTrue(data.equals(data2));
	}
	
	@Test
	public void testListSerialization() {
		ArrayList<String> data = new ArrayList<String>();
		data.add("str,,,i,ng1");
		data.add("st[[[C]]]ring2");
		data.add("st][[[C][[[ring3");
		String serialized = CommonFunctions.implode(data);
		ArrayList<String> data2 = CommonFunctions.explode(serialized);
		assertTrue(data.equals(data2));
	}
}
