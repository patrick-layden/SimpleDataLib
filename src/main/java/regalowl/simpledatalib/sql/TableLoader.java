package regalowl.simpledatalib.sql;

import java.util.ArrayList;

import regalowl.simpledatalib.CommonFunctions;
import regalowl.simpledatalib.SimpleDataLib;

public class TableLoader {
	
	private String name;
	private SimpleDataLib sdl;
	private String createString;
	private String s;
	private Field f;
	
	private ArrayList<Field> fields = new ArrayList<Field>();
	private boolean hasCompositeKey;
	private ArrayList<Field> compositeKey = new ArrayList<Field>();
	
	public TableLoader(String name, SimpleDataLib sdl) {
		this.name = name;
		this.sdl = sdl;
		this.createString = getCreateStatementFromDB();
		loadTableFromString();
	}
	
	public TableLoader(String name, SimpleDataLib sdl, String createString) {
		this.name = name;
		this.sdl = sdl;
		this.createString = createString;
		loadTableFromString();
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
	
	/**
	 * Loads all Table data from the given SQL create statement.
	 */
	private void loadTableFromString() {
		this.s = createString;
		s = s.replaceAll("[\n\r]", "");
		s = s.replaceAll(" +", " ");
		s = s.replace("`", "");
		s = s.replace("\"\"", "'");
		s = s.substring(s.indexOf("(") + 1, s.lastIndexOf(")")).trim();
		System.out.println("start: "+s);
		int counter = 0;
		while (s.length() > 0) {
			if (counter > 1000) {System.out.println("runaway: " + s);break;}
			while (hasNextProperty());
			boolean hasFieldSize = false;
			int fieldSize = 0;
			String fieldName = getBefore(s, " ");
			s = getAfter(s, " ");
			System.out.println("field name: "+s);
			String typeString = getBefore(s, " ");
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
			f = new Field(fieldName, fieldType);
			if (hasFieldSize) f.setFieldSize(fieldSize);
			s = getAfter(s, " ");
			System.out.println("field type: "+s);
			f.setFieldSize(fieldSize);
			fields.add(f);
			counter++;
		}
	}
	
	private boolean hasNextProperty() {
		if (s.toUpperCase().startsWith(" ")) {
			s = getAfter(s, 1);
			System.out.println("space: " + s);
		}
		if (s.toUpperCase().startsWith(", ")) {
			s = getAfter(s, 2);
			System.out.println("comma: " + s);
			return false;
		}
		if (s.toUpperCase().startsWith(",")) {
			s = getAfter(s, 1);
			System.out.println("comma: " + s);
			return false;
		}
		if (s.toUpperCase().startsWith("PRIMARY KEY(")) {
			s = getAfter(s, 12);
			handleCompositeKey();
			System.out.println("composite: " + s);
		}
		if (s.toUpperCase().startsWith("PRIMARY KEY (")) {
			s = getAfter(s, 13);
			handleCompositeKey();
			System.out.println("composite: " + s);
		}
		if (s.toUpperCase().startsWith("NOT NULL")) {
			f.setNotNull();
			s = getAfter(s, 8);
			System.out.println("not null: " + s);
			return true;
		}
		if (s.toUpperCase().startsWith("PRIMARY KEY")) {
			f.setPrimaryKey();
			s = getAfter(s, 11);
			System.out.println("primary: " + s);
			return true;
		}
		if (s.toUpperCase().startsWith("UNIQUE")) {
			f.setUnique();
			s = getAfter(s, 6);
			System.out.println("unique: " + s);
			return true;
		}
		if (s.toUpperCase().startsWith("AUTO_INCREMENT")) {
			f.setAutoIncrement();
			s = getAfter(s, 14);
			System.out.println("increment: " + s);
			return true;
		}
		if (s.toUpperCase().startsWith("AUTOINCREMENT")) {
			f.setAutoIncrement();
			s = getAfter(s, 13);
			System.out.println("increment: " + s);
			return true;
		}
		if (s.toUpperCase().startsWith("DEFAULT")) {
			s = getAfter(s, 7);
			String defaultValue = "";
			if (s.startsWith("'")) {
				defaultValue = getBetween(s, "'", "'");
				s = getAfter(s, defaultValue + "'");
				System.out.println("default: " + s);
			} else if (s.startsWith(" '")) {
				defaultValue = getBetween(s, " '", "'");
				s = getAfter(s, defaultValue + "'");
				System.out.println("default: " + s);
			} else {
				defaultValue = getBefore(s, " ");
				s = getAfter(s, " ");
				System.out.println("default: " + s);
			}
			f.setDefault(defaultValue);
			return true;
		}

		return false;
	}
	
	private void handleCompositeKey() {
		String keyString = getBefore(s, ")");
		s = getAfter(s, keyString + ")");
		System.out.println(s);
		keyString = keyString.replace(", ", ",");
		ArrayList<String> primaryKey = CommonFunctions.explode(keyString, ",");
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
}
