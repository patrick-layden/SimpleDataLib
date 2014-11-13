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
		try {
			BigDecimal result = round(new BigDecimal(String.valueOf(input)), decimals);
			return result.doubleValue();
		} catch (Exception e) {
			return input;
		}
	}
	/**
	 * @param input A BigDecimal value.
	 * @param decimals The number of decimal places to round to.
	 * @return The rounded BigDecimal value.
	 */
	public static BigDecimal round(BigDecimal input, int decimals) {
		try {
			BigDecimal factor = new BigDecimal(String.valueOf(Math.pow(10, decimals)));
			return new BigDecimal(input.multiply(factor).toBigInteger()).divide(factor);
		} catch (Exception e) {
			return input;
		}
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
		try {
			return round(new BigDecimal(String.valueOf(input)), decimals).toPlainString();
		} catch (Exception e) {
			return input+"";
		}
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
	 * @param commalist A comma separated value string string.
	 * @return An ArrayList containing the double values from the CSV string.
	 */
	public static ArrayList<Double> doubleToArray(String commalist) {
		try {
			ArrayList<Double> array = new ArrayList<Double>();
			if (commalist.indexOf(",") == 0) {
				commalist = commalist.substring(1, commalist.length());
			}
			if (!commalist.substring(commalist.length() - 1, commalist.length()).equalsIgnoreCase(",")) {
				commalist = commalist + ",";
			}
			while (commalist.contains(",")) {
				array.add(Double.parseDouble(commalist.substring(0, commalist.indexOf(","))));
				if (commalist.indexOf(",") == commalist.lastIndexOf(",")) {
					break;
				}
				commalist = commalist.substring(commalist.indexOf(",") + 1, commalist.length());
			}
			return array;
		} catch (Exception e) {
			ArrayList<Double> array = new ArrayList<Double>();
			return array;
		}
	}
	/**
	 * @param commalist A comma separated value string string.
	 * @return An ArrayList containing the int values from the CSV string.
	 */
	public static ArrayList<Integer> intToArray(String commalist) {
		try {
			ArrayList<Integer> array = new ArrayList<Integer>();
			if (commalist.indexOf(",") == 0) {
				commalist = commalist.substring(1, commalist.length());
			}
			if (!commalist.substring(commalist.length() - 1, commalist.length()).equalsIgnoreCase(",")) {
				commalist = commalist + ",";
			}
			while (commalist.contains(",")) {
				array.add(Integer.parseInt(commalist.substring(0, commalist.indexOf(","))));
				if (commalist.indexOf(",") == commalist.lastIndexOf(",")) {
					break;
				}
				commalist = commalist.substring(commalist.indexOf(",") + 1, commalist.length());
			}
			return array;
		} catch (Exception e) {
			ArrayList<Integer> array = new ArrayList<Integer>();
			return array;
		}
	}
	/**
	 * @param array An ArrayList of Double values.
	 * @return A CSV string containing the double values.
	 */
	public static String doubleArrayToString(ArrayList<Double> array) {
		String string = "";
		int c = 0;
		while (c < array.size()) {
			string = string + array.get(c) + ",";
			c++;
		}
		return string;
	}
	/**
	 * @param array An ArrayList of Integer values.
	 * @return A CSV string containing the Integer values.
	 */
	public static String intArrayToString(ArrayList<Integer> array) {
		String string = "";
		int c = 0;
		while (c < array.size()) {
			string = string + array.get(c) + ",";
			c++;
		}
		return string;
	}
	
	
	
	
	
	public static ArrayList<String> explode(String string, String delimiter) {
		ArrayList<String> array = new ArrayList<String>();
		if (string == null || delimiter == null || !string.contains(delimiter)) {
			return array;
		}
		if (string.indexOf(delimiter) == 0) {
			string = string.substring(1, string.length());
		}
		if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(delimiter)) {
			string += delimiter;
		}
		while (string.contains(delimiter)) {
			array.add(string.substring(0, string.indexOf(delimiter)));
			if (string.indexOf(delimiter) == string.lastIndexOf(delimiter)) {
				break;
			}
			string = string.substring(string.indexOf(delimiter) + 1, string.length());
		}
		return array;
	}

	public static String implode(List<String> array, String delimiter) {
		if (array == null || delimiter == null) {
			return "";
		}
		String string = "";
		for (String cs : array) {
			string += cs + delimiter;
		}
		return string;
	}

	
	
	
	
	
	
	
	
	public static ArrayList<String> explode(String string) {
		ArrayList<String> array = new ArrayList<String>();
		if (string == null || !string.contains(",")) {return array;}
		int nestLevel = getNestLevel(string);
		String comma = "["+nestLevel+"]";
		if (string.indexOf(",") == 0) {string = string.substring(1, string.length());}
		if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(",")) {string += ",";}
		while (string.contains(",")) {
			array.add(string.substring(0, string.indexOf(",")).replace(comma, ","));
			if (string.indexOf(",") == string.lastIndexOf(",")) {break;}
			string = string.substring(string.indexOf(",") + 1, string.length());
		}
		return array;
	}
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
	public static String implodeMap(HashMap<String,String> map) {
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
			if (inBracket) {
				bString += c;
			}
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
	
	
	
	
	public static HashMap<String,Integer> explodeIntMap(String string) {
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		try {
			if (string == null || !string.contains(",")) {return map;}
			if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(";")) {string += ";";}
			while (string.contains(";")) {
				String mapEntry = string.substring(0, string.indexOf(";"));
				String mapKey = mapEntry.substring(0, mapEntry.indexOf(","));
				Integer mapValue = Integer.parseInt(mapEntry.substring(mapEntry.indexOf(",") + 1, mapEntry.length()));
				map.put(mapKey, mapValue);
				if (string.indexOf(";") == string.lastIndexOf(";")) {break;}
				string = string.substring(string.indexOf(";") + 1, string.length());
			}
			return map;
		} catch (Exception e) {
			return new HashMap<String,Integer>();
		}
		
	}
	
	public static String implodeIntMap(HashMap<String,Integer> map) {
		if (map == null) {return "";}
		String string = "";
		for (Map.Entry<String,Integer> entry : map.entrySet()) {
		    String key = entry.getKey();
		    Integer value = entry.getValue();
		    string += (key + "," + value + ";");
		}
		return string;
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
