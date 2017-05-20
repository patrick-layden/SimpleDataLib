package regalowl.simpledatalib.sql;

import java.util.ArrayList;

import regalowl.simpledatalib.CommonFunctions;
import regalowl.simpledatalib.SimpleDataLib;

public class TableLoader {
	
	private String name;
	private SimpleDataLib sdl;
	private String createString;
	private boolean tableExists;
	private String s;
	private Field f;
	private String debugMessage = "";
	
	private ArrayList<Field> fields = new ArrayList<Field>();
	private boolean hasCompositeKey;
	private ArrayList<Field> compositeKey = new ArrayList<Field>();
	
	public TableLoader(String name, SimpleDataLib sdl) {
		this.name = name;
		this.sdl = sdl;
		this.tableExists = tableExistsInDB();
		if (!tableExists) return;
		this.createString = getCreateStatementFromDB();
		try {
			loadTableFromString();
		} catch (Exception e) {
			sdl.getErrorWriter().writeError(e, debugMessage);
			//System.out.println(debugMessage.replace("{{newline}}", System.getProperty("line.separator")));	
			//e.printStackTrace();
		}
	}
	
	public TableLoader(String name, SimpleDataLib sdl, String createString) {
		this.name = name;
		this.sdl = sdl;
		this.createString = createString;
		try {
			loadTableFromString();
		} catch (Exception e) {
			sdl.getErrorWriter().writeError(e, debugMessage);
			//System.out.println(debugMessage.replace("{{newline}}", System.getProperty("line.separator")));	
			//e.printStackTrace();
		}
	}
	
	public String getDebugMessage() {
		return debugMessage;
	}
	
	public ArrayList<Field> getFields() {
		return fields;
	}
	public boolean hasCompositeKey() {
		return hasCompositeKey;
	}
	public ArrayList<Field> getCompositeKey() {
		return compositeKey;
	}
	public String getCreateStatement() {
		return createString;
	}
	public Table getTable() {
		Table t = new Table(name, sdl);
		t.setFields(fields);
		if (hasCompositeKey) t.setCompositeKey(compositeKey);
		return t;
	}
	
	/**
	 * Returns a SQL create statement for this table from the database.
	 */
	private String getCreateStatementFromDB() {
		if (sdl.getSQLManager().useMySQL()) {
			QueryResult qr = sdl.getSQLManager().getSQLRead().select("SHOW CREATE TABLE " + name);
			if (qr.next()) return qr.getString(2);
		} else {
			QueryResult qr = sdl.getSQLManager().getSQLRead().select("SELECT sql FROM sqlite_master WHERE tbl_name = '"+name+"'");
			if (qr.next()) return qr.getString(1);
		}
		return null;
	}
	

	private boolean tableExistsInDB() {
		if (sdl.getSQLManager().useMySQL()) {
			QueryResult qr = sdl.getSQLManager().getSQLRead().select("SHOW TABLES LIKE '" + name + "'");
			if (qr.next()) return true;
		} else {
			QueryResult qr = sdl.getSQLManager().getSQLRead().select("SELECT sql FROM sqlite_master WHERE tbl_name = '"+name+"'");
			if (qr.next()) return true;
		}
		return false;
	}
	
	/**
	 * Loads all Table data from the given SQL create statement.
	 */
	private void loadTableFromString() {
		this.s = createString;
		appendDebug("Initial Input: " + createString);
		s = s.replaceAll("DEFAULT NULL", "");
		s = s.replaceAll("[\n\r]", "");
		s = s.replace(",", ", ");
		s = s.replaceAll(" +", " ");
		s = s.replace("`", "");
		s = s.replace("\"\"", "'");
		s = s.substring(s.indexOf("(") + 1, s.lastIndexOf(")")).trim();
		appendDebug("Processed Input: " + s);
		int counter = 0;
		while (s.replace(" ", "").length() > 0) {
			if (counter > 1000) break;
			while (hasNextProperty());
			if (s.replace(" ", "").length() == 0) break;
			boolean hasFieldSize = false;
			int fieldSize = 0;
			String fieldName = getBefore(s, " ");
			s = getAfter(s, " ");
			appendDebug("[NAME]"+s);
			String typeString = "";
			if (s.contains(" ")) {
				typeString = getBefore(s, " ");
			} else {
				typeString = s;
			}
			if (typeString.contains(",")) typeString = getBefore(s, ",");
			FieldType fieldType = null;
			if (typeString.contains("(")) {
				fieldType = FieldType.fromString(getBefore(typeString, "("));
				if (!fieldType.equals(FieldType.INTEGER)) {
					hasFieldSize = true;
					fieldSize = Integer.parseInt(getBetween(typeString, "(", ")"));
				}
			} else {
				fieldType = FieldType.fromString(typeString);
			}
			if (fieldType == null) appendDebug("Field null from input: " + typeString);
			f = new Field(fieldName, fieldType);
			if (hasFieldSize) f.setFieldSize(fieldSize);
			if (s.contains(" ")) {
				s = getAfter(s, " ");
			} else {
				s = "";
			}
			appendDebug("[TYPE]"+s);
			fields.add(f);
			counter++;
		}
	}
	
	private boolean hasNextProperty() {
		if (s.toUpperCase().startsWith(" ")) {
			s = getAfter(s, 1);
			appendDebug("[SPACE]"+s);
		} else if (s.toUpperCase().startsWith(", ")) {
			s = getAfter(s, 2);
			appendDebug("[COMMA]"+s);
		} else if (s.toUpperCase().startsWith(",")) {
			s = getAfter(s, 1);
			appendDebug("[COMMA]"+s);
		} else if (s.toUpperCase().startsWith("PRIMARY KEY(")) {
			s = getAfter(s, 12);
			handleCompositeKey();
			appendDebug("[COMPOSITE KEY]"+s);
		} else if (s.toUpperCase().startsWith("PRIMARY KEY (")) {
			s = getAfter(s, 13);
			handleCompositeKey();
			appendDebug("[COMPOSITE KEY]"+s);
		} else if (s.toUpperCase().startsWith("UNIQUE KEY")) {
			s = getAfter(s, "(");
			handleUniqueKey();
			appendDebug("[UNIQUE KEY]"+s);
		} else if (s.toUpperCase().startsWith("NOT NULL")) {
			f.setNotNull();
			s = getAfter(s, 8);
			appendDebug("[NOT NULL]"+s);
		} else if (s.toUpperCase().startsWith("PRIMARY KEY")) {
			f.setPrimaryKey();
			s = getAfter(s, 11);
			appendDebug("[PRIMARY KEY]"+s);
		} else if (s.toUpperCase().startsWith("UNIQUE")) {
			f.setUnique();
			s = getAfter(s, 6);
			appendDebug("[UNIQUE]"+s);
		} else if (s.toUpperCase().startsWith("AUTO_INCREMENT")) {
			f.setAutoIncrement();
			s = getAfter(s, 14);
			appendDebug("[AUTOINCREMENT]"+s);
		} else if (s.toUpperCase().startsWith("AUTOINCREMENT")) {
			f.setAutoIncrement();
			s = getAfter(s, 13);
			appendDebug("[AUTOINCREMENT]"+s);
		} else if (s.toUpperCase().startsWith("DEFAULT")) {
			s = getAfter(s, 8);
			String defaultValue = "";
			if (s.startsWith("''")) {
				s = getAfter(s, "''");
			} else if (s.startsWith("'")) {
				defaultValue = getBetween(s, "'", "'");
				s = getAfter(s, defaultValue + "'");
			} else {
				defaultValue = getBefore(s, " ");
				if (defaultValue.endsWith(",")) defaultValue = getBefore(s, ",");
				s = getAfter(s, " ");
			}
			appendDebug("[DEFAULT]"+s);
			f.setDefault(defaultValue);
		} else {
			return false;
		}
		return true;
	}
	
	private void handleCompositeKey() {
		String keyString = getBefore(s, ")");
		s = getAfter(s, keyString + ")");
		keyString = keyString.replace(", ", ",");
		ArrayList<String> primaryKey = CommonFunctions.explode(keyString);
		appendDebug("Composite Key Explode Input: " + keyString);
		appendDebug("Composite Key Array: " + primaryKey.toString());
		if (primaryKey.size() > 1) {
			for (String n:primaryKey) {
				Field f = getField(n);
				compositeKey.add(f);
			}
			hasCompositeKey = true;
		} else if (primaryKey.size() == 1) {
			Field f = getField(primaryKey.get(0));
			f.setPrimaryKey();
		}
	}
	
	//TODO handle composite unique keys - just copy primary key
	private void handleUniqueKey() {
		String keyString = getBefore(s, ")");
		s = getAfter(s, keyString + ")");
		Field f = getField(keyString);
		f.setUnique();
	}
	
	
	private String getBefore(String string, String sequence) {
		return string.substring(0, string.indexOf(sequence));
	}
	private String getAfter(String string, String sequence) {
		return string.substring(string.indexOf(sequence) + sequence.length(), string.length());
	}
	private String getAfter(String string, int characters) {
		return string.substring(characters, string.length());
	}
	private String getBetween(String string, String startSequence, String endSequence) {
		return string.substring(string.indexOf(startSequence) + startSequence.length(), string.indexOf(endSequence, string.indexOf(startSequence) + startSequence.length()));
	}
	
	private Field getField(String name) {
		for (Field f:fields) {
			if (f.getName().equalsIgnoreCase(name)) {
				return f;
			}
		}
		return null;
	}
	
	private void appendDebug(String message) {
		debugMessage += message + "{{newline}}";
	}
	
	public boolean tableExists() {
		return this.tableExists;
	}
}
