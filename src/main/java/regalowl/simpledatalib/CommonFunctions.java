package regalowl.simpledatalib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class CommonFunctions {

	/**
	 * @param input A double value.
	 * @return The input value rounded to two decimal places.
	 */
	public static double twoDecimals(double input) {
		return round(input, 2);
	}
	/**
	 * @param input A double value.
	 * @param decimals The number of decimal places to round to.
	 * @return The rounded double value.
	 */
	public static double round(double input, int decimals) {
		BigDecimal result = round(new BigDecimal(String.valueOf(input)), decimals);
		return result.doubleValue();
	}
	/**
	 * @param input A BigDecimal value.
	 * @param decimals The number of decimal places to round to.
	 * @return The rounded BigDecimal value.
	 */
	public static BigDecimal round(BigDecimal input, int decimals) {
		BigDecimal factor = new BigDecimal(String.valueOf(Math.pow(10, decimals)));
		return new BigDecimal(input.multiply(factor).toBigInteger()).divide(factor);
	}
	/**
	 * @param input A BigDecimal value.
	 * @param decimals The number of decimal places to round to.
	 * @return The rounded BigDecimal value in String form.
	 */
	public static String roundString(BigDecimal input, int decimals) {
		return round(input, decimals).toPlainString();
	}
	/**
	 * @param input A double value.
	 * @param decimals The number of decimal places to round to.
	 * @return The rounded double value in String format.
	 */
	public static String roundString(double input, int decimals) {
		return round(new BigDecimal(String.valueOf(input)), decimals).toPlainString();
	}
	/**
	 * @return A time stamp.
	 */
	public static String getTimeStamp() {
		 Date date = new Date();
		return new Timestamp(date.getTime()).toString();
	}
	/**
	 * @param e An exception.
	 * @return The exception converted to a String.
	 */
	public static String getErrorString(Exception e) {
		if (e == null) {return null;}
		StringWriter error = new StringWriter();
		e.printStackTrace(new PrintWriter(error));
		return error.toString();
	}
	
	
	
	/**
	 * @param a delimited string
	 * @param the delimiter used to delimit the string
	 * @return An ArrayList containing the delimitted elements in the given string
	 */
	public static ArrayList<String> explode(String string, String delimiter) {
		ArrayList<String> array = new ArrayList<String>();
		if (string == null || delimiter == null || !string.contains(delimiter)) return array;
		if (string.indexOf(delimiter) == 0) string = string.substring(1, string.length());
		if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(delimiter)) string += delimiter;
		while (string.contains(delimiter)) {
			array.add(string.substring(0, string.indexOf(delimiter)));
			if (string.indexOf(delimiter) == string.lastIndexOf(delimiter)) break;
			string = string.substring(string.indexOf(delimiter) + 1, string.length());
		}
		return array;
	}
	/**
	 * @param a List
	 * @param delimiter
	 * @return A string containing the elements in the array with elements separated by the specified delimitter
	 */
	public static String implode(List<String> array, String delimiter) {
		if (array == null || delimiter == null) return "";
		String string = "";
		for (String cs : array) {
			string += cs + delimiter;
		}
		return string;
	}
	
	
	
	/**
	 * @param String A string using commas as delimitters
	 * @return ArrayList containing the comma separated elements in the given string
	 */
	public static ArrayList<String> explode(String string) {
		ArrayList<String> array = new ArrayList<String>();
		if (string == null || string.length() == 0) return array;
		if (string.equals(",")) {
			array.add("");
			return array;
		}
		if (!string.contains(",")) {
			array.add(string);
			return array;
		}
		int nestLevel = getNestLevel(string);
		String comma = "["+nestLevel+"]";
		if (string.indexOf(",") == 0) {
			array.add(""); //leading comma means empty string as first entry
			string = string.substring(1, string.length());
		}
		if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(",")) {string += ",";}
		while (string.contains(",")) {
			array.add(string.substring(0, string.indexOf(",")).replace(comma, ","));
			if (string.indexOf(",") == string.lastIndexOf(",")) {break;}
			string = string.substring(string.indexOf(",") + 1, string.length());
		}
		return array;
	}
	/**
	 * @param List
	 * @return A string containing the comma separated values of the given list
	 */
	public static String implode(List<String> array) {
		if (array == null) {return "";}
		int nestLevel = getNestLevel(array.toString()) + 1;
		String comma = "["+nestLevel+"]";
		String string = "";
		for (String cs:array) {
			string += cs.replace(",", comma) + ",";
		}
		return string;
	}

	
	
	
	/**
	 * @param string 
	 * A string generated by the CommonFunctions implodeMap() function.  This method cannot deserialize data generated from other sources.
	 * @return HashMap<String,String> that was previously serialized by the implodeMap() function.
	 */
	public static HashMap<String,String> explodeMap(String string) {
		HashMap<String,String> map = new HashMap<String,String>();
		if (string == null || !string.contains(",")) {return map;}
		int nestLevel = getNestLevel(string);
		String comma = "["+nestLevel+"]";
		String semicolon = "{"+nestLevel+"}";
		if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(";")) {string += ";";}
		while (string.contains(";")) {
			String mapEntry = string.substring(0, string.indexOf(";"));
			String mapKey = mapEntry.substring(0, mapEntry.indexOf(",")).replace(comma, ",").replace(semicolon, ";");
			String mapValue = mapEntry.substring(mapEntry.indexOf(",") + 1, mapEntry.length()).replace(comma, ",").replace(semicolon, ";");
			map.put(mapKey, mapValue);
			if (string.indexOf(";") == string.lastIndexOf(";")) {break;}
			string = string.substring(string.indexOf(";") + 1, string.length());
		}
		return map;
	}
	/**
	 * @param map
	 * A Map with String key and value.  The map may contain an unlimited number of maps within the map.  The map can contain any character.  This function supports
	 * unlimited nesting.
	 * @return A string with the supplied Map's data.  Can only be returned to a Map with the CommonFunctions explodeMap() function.
	 */
	public static String implodeMap(Map<String,String> map) {
		if (map == null) {return "";}
		int nestLevel = getNestLevel(map.toString()) + 1;
		String comma = "["+nestLevel+"]";
		String semicolon = "{"+nestLevel+"}";
		String string = "";
		for (Map.Entry<String,String> entry : map.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) continue;
		    String key = entry.getKey().replace(",", comma).replace(";", semicolon);
		    String value = entry.getValue().replace(",", comma).replace(";", semicolon);
		    string += (key + "," + value + ";");
		}
		return string;
	}

	public static int getNestLevel(String string) {
		int nestLevel = -1;
		boolean inBracket = false;
		String bString = "";
		for (char c:string.toCharArray()) {
			if (c == '{') {
				bString = "";
				inBracket = true;
				continue;
			} else if (c == '}') {
				try {
					int cLvl = Integer.parseInt(bString);
					if (cLvl > nestLevel) nestLevel = cLvl;
				} catch (NumberFormatException e) {}
				bString = "";
				inBracket = false;
			}
			if (inBracket) bString += c;
		}
		inBracket = false;
		for (char c:string.toCharArray()) {
			if (c == '[') {
				bString = "";
				inBracket = true;
				continue;
			} else if (c == ']') {
				try {
					int cLvl = Integer.parseInt(bString);
					if (cLvl > nestLevel) nestLevel = cLvl;
				} catch (NumberFormatException e) {}
				bString = "";
				inBracket = false;
			}
			if (inBracket) bString += c;
		}
		return nestLevel;
	}
	
	
	
	public static <T> ArrayList<String> convertToStringArrayList(List<T> list) {
		ArrayList<String> newArrayList = new ArrayList<String>();
		for (T t:list) {
			newArrayList.add(t.toString());
		}
		return newArrayList;
	}
	public static ArrayList<Integer> convertToIntArrayList(List<String> list) {
		ArrayList<Integer> newArrayList = new ArrayList<Integer>();
		for (String s:list) {
		    try {
		    	newArrayList.add(Integer.parseInt(s));
		    } catch (Exception e) {}
		}
		return newArrayList;
	}
	public static ArrayList<Double> convertToDoubleArrayList(List<String> list) {
		ArrayList<Double> newArrayList = new ArrayList<Double>();
		for (String s:list) {
		    try {
		    	newArrayList.add(Double.parseDouble(s));
		    } catch (Exception e) {}
		}
		return newArrayList;
	}
	
	public static <T> HashMap<String,String> convertToStringMap(Map<String,T> map) {
		HashMap<String,String> newMap = new HashMap<String,String>();
		for (Map.Entry<String,T> entry : map.entrySet()) {
		    String key = entry.getKey();
		    T value = entry.getValue();
		    newMap.put(key, value.toString());
		}
		return newMap;
	}
	public static HashMap<String,Integer> convertToIntMap(Map<String,String> map) {
		HashMap<String,Integer> newMap = new HashMap<String,Integer>();
		for (Map.Entry<String,String> entry : map.entrySet()) {
		    String key = entry.getKey();
		    try {
		    	Integer value = Integer.parseInt(entry.getValue());
		    	newMap.put(key, value);
		    } catch (Exception e) {}
		}
		return newMap;
	}
	public static HashMap<String,Double> convertToDoubleMap(Map<String,String> map) {
		HashMap<String,Double> newMap = new HashMap<String,Double>();
		for (Map.Entry<String,String> entry : map.entrySet()) {
		    String key = entry.getKey();
		    try {
		    	Double value = Double.parseDouble(entry.getValue());
		    	newMap.put(key, value);
		    } catch (Exception e) {}
		}
		return newMap;
	}
	
	
	public static Object createObjectFromBase64(String base64String) throws IOException, ClassNotFoundException {
		byte[] data = Base64Coder.decode(base64String);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	public static String convertObjectToBase64(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return new String(Base64Coder.encode(baos.toByteArray()));
	}

	

	
}
