package regalowl.databukkit.sql;

import java.util.ArrayList;
import java.util.Iterator;

import regalowl.databukkit.DataBukkit;
import regalowl.databukkit.events.LogEvent;
import regalowl.databukkit.events.LogLevel;

public class Table {
	
	private String name;
	private DataBukkit dab;
	private ArrayList<Field> fields = new ArrayList<Field>();
	private boolean hasCompositeKey;
	private ArrayList<Field> compositeKey = new ArrayList<Field>();
	
	public Table(String name, DataBukkit dab) {
		this.name = name;
		this.dab = dab;
		this.hasCompositeKey = false;
	}
	
	/**
	 * Loads the table structure from the database if it exists.  Returns true if successful.
	 */
	public boolean loadTable() {
		String createStatement = getCreateStatementFromDB();
		if (createStatement != null) {
			try {
				loadTableFromString(createStatement);
				return true;
			} catch (Exception e) {
				dab.getEventPublisher().fireEvent(new LogEvent("[DataBukkit["+dab.getName()+"]]Failed to load table from database: ["+name+"]", e, LogLevel.ERROR));
				return false;
			}
		}
		dab.getEventPublisher().fireEvent(new LogEvent("[DataBukkit["+dab.getName()+"]]Table doesn't exist in database: ["+name+"]", null, LogLevel.ERROR));
		return false;
	}
	
	/**
	 * Loads all Table data from the given SQL create statement.
	 */
	public void loadTableFromString(String createString) {
		createString = createString.substring(createString.indexOf("(") + 1, createString.lastIndexOf(")")).trim();
		createString = createString.replaceAll("[\n\r]", "");
		createString = createString.replace("`", "");
		if (createString.contains("PRIMARY KEY(") || createString.contains("PRIMARY KEY (")) {//if composite key section has spaces, remove them.
			int pKeyIndex = createString.indexOf("PRIMARY KEY");
			String pKeyString = createString.substring(pKeyIndex, createString.indexOf(")", pKeyIndex) + 1);
			String newPKeyString = pKeyString.replace(", ", ",");
			createString = createString.replace(pKeyString, newPKeyString);
		}
		ArrayList<String> fieldStrings = dab.getCommonFunctions().explode(createString, ", ");
		ArrayList<String> primaryKey = new ArrayList<String>();
		for (String fString:fieldStrings) {
			fString = fString.trim();
			if (fString.toUpperCase().startsWith("PRIMARY KEY")) {
				//dab.getEventHandler().fireLogEvent("[DataBukkit["+dab.getName()+"]]"+fString, null, LogLevel.ERROR);
				String keyList = fString.substring(fString.indexOf("(") + 1, fString.lastIndexOf(")")).replace(" ", "");
				primaryKey = dab.getCommonFunctions().explode(keyList, ",");
				continue;
			}
			String fName = fString.substring(0, fString.indexOf(" "));
			fString = fString.substring(fString.indexOf(" ") + 1, fString.length());
			String fType = "";
			if (fString.contains(" ")) {
				fType = fString.substring(0, fString.indexOf(" "));
			} else {
				fType = fString;
			}
			boolean hasFieldSize = false;
			int fieldSize = 0;
			FieldType ft = null;
			if (fType.contains("(")) {
				ft = FieldType.fromString(fType.substring(0, fType.indexOf("(")));
				if (!ft.equals(FieldType.INTEGER)) {
					hasFieldSize = true;
					fieldSize = Integer.parseInt(fType.substring(fType.indexOf("(") + 1, fType.lastIndexOf(")")));
				}
			} else {
				ft = FieldType.fromString(fType);
			}

			Field f = new Field(fName, ft);
			if (hasFieldSize) {
				f.setFieldSize(fieldSize);
			}
			fString = fString.substring(fString.indexOf(" ") + 1, fString.length());
			fString = fString.toUpperCase();
			if (fString.contains("NOT NULL")) {
				f.setNotNull();
			}
			if (fString.contains("PRIMARY KEY")) {
				f.setPrimaryKey();
			}
			if (fString.contains("UNIQUE")) {
				f.setUnique();
			}
			if (fString.contains("AUTO_INCREMENT") || fString.contains("AUTOINCREMENT")) {
				f.setAutoIncrement();
			}
			if (fString.contains("DEFAULT")) {
				int defaultValueIndex = fString.indexOf("DEFAULT '") + 9;
				String defaultValue = fString.substring(defaultValueIndex, fString.indexOf("'", defaultValueIndex));
				f.setDefault(defaultValue);
			}
			fields.add(f);
		}
		if (primaryKey.size() > 1) {
			for (String n:primaryKey) {
				Field f = getField(n);
				compositeKey.add(f);
			}
			hasCompositeKey = true;
		}
	}
	
	/**
	 * Returns a SQL create statement for this table from the database.
	 */
	private String getCreateStatementFromDB() {
		if (dab.getSQLManager().useMySQL()) {
			QueryResult qr = dab.getSQLManager().getSQLRead().select("SHOW CREATE TABLE " + name);
			if (qr.next()) return qr.getString(2);
		} else {
			QueryResult qr = dab.getSQLManager().getSQLRead().select("SELECT sql FROM sqlite_master WHERE tbl_name = '"+name+"'");
			if (qr.next()) return qr.getString(1);
		}
		return null;
	}
	
	/**
	 * @return The name of this Table.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Creates and adds a new Field to this Table object.
	 * @param name of the new field
	 * @param type of the new field
	 * @return The new field object.
	 */
	public Field addField(String name, FieldType type) {
		Field newField = new Field(name, type);
		if (dab.getSQLManager().useMySQL()) {
			newField.setUseMySQL();
		}
		fields.add(newField);
		return newField;
	}
	
	/**
	 * Creates and returns a new Field object without adding it to this table.
	 * @param name of the new field
	 * @param type of the new field
	 * @return The new field object.
	 */
	public Field generateField(String name, FieldType type) {
		Field newField = new Field(name, type);
		if (dab.getSQLManager().useMySQL()) {
			newField.setUseMySQL();
		}
		return newField;
	}
	
	/**
	 * @param name of the field
	 * @return The Field object if it exists within this table.  If not null is returned.
	 */
	public Field getField(String name) {
		for (Field f:fields) {
			if (f.getName().equalsIgnoreCase(name)) {
				return f;
			}
		}
		return null;
	}
	
	/**
	 * @return All of the Field objects in this table.
	 */
	public ArrayList<Field> getFields() {
		return fields;
	}
	
	
	/**
	 * Sets the composite key for this table.
	 * @param compositeKey An ArrayList of the Field objects that comprise the composite key.
	 */
	public void setCompositeKey(ArrayList<Field> compositeKey) {
		this.compositeKey.addAll(compositeKey);
		hasCompositeKey = true;
	}
	
	/**
	 * Saves this table to the database asynchronously
	 */
	public void saveAsync() {
		dab.getSQLManager().getSQLWrite().addToQueue(getCreateStatement(fields, false));
	}
	/**
	 * Adds the table creation statement to the synchronous SQL Write queue.  The table will be saved whenever the queue is written.
	 */
	public void save() {
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(fields, false));
	}
	/**
	 * Synchronously saves the table to the database immediately.
	 */
	public void saveNow() {
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(fields, false));
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
	}

	
	/**
	 * @return The table creation String for this Table.
	 */
	public String getCreateStatement() {
		return getCreateStatement(fields, false);
	}
	/**
	 * @param fieldArrayList An ArrayList of the Fields which are a part of this table.
	 * @param temp True if this is a temporary table, false if not.
	 * @return A String table creation statement.
	 */
	private String getCreateStatement(ArrayList<Field> fieldArrayList, boolean temp) {
		String tableName = name;
		if (temp) {
			tableName += "_temp";
		}
		String statement = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
		Iterator<Field> it = fieldArrayList.iterator();
		while (it.hasNext()) {
		    Field f = it.next();
		    if (!it.hasNext()) {
		    	if (hasCompositeKey) {
			    	statement += f.getString() + ", " + getCompositeKeyString() + ")";
		    	} else {
			    	statement += f.getString() + ")";
		    	}
		    } else {
		    	statement += f.getString() + ", ";
		    }
		}
		return statement;
	}
	
	/**
	 * @param fieldArrayList An ArrayList of Field objects.
	 * @return A comma separated list of the Field names.
	 */
	private String getFieldNameString(ArrayList<Field> fieldArrayList) {
		Iterator<Field> it = fieldArrayList.iterator();
		String fieldNameString = "";
		while (it.hasNext()) {
		    Field f = it.next();
		    if (!it.hasNext()) {
		    	fieldNameString += f.getName();
		    } else {
		    	fieldNameString += f.getName() + ", ";
		    }
		}
		return fieldNameString;
	}
	
	/**
	 * @return A composite key creation String which can be used in a Table create statement.
	 */
	private String getCompositeKeyString() {
		String key = "PRIMARY KEY(";
		Iterator<Field> it = compositeKey.iterator();
		while (it.hasNext()) {
		    Field f = it.next();
		    if (!it.hasNext()) {
		    	key += f.getName() + ")";
		    } else {
		    	key += f.getName() + ",";
		    }
		}
		return key;
	}
	
	
	
	
	/**
	 * Adds the new Field to the database after the specified Field.  The Field will also be added to this object.
	 * @param field The Field to be added to the database.
	 * @param afterField The Field which will proceed the new Field in the database.  This is an existing Field.
	 */
	public void addFieldToDatabase(Field field, Field afterField) {
		ArrayList<Field> newFields = new ArrayList<Field>();
		for (Field f:fields) {
			if (f.equals(afterField)) {
				newFields.add(f);
				newFields.add(field);
			} else {
				newFields.add(f);
			}
		}
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(newFields, true));
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		dab.getSQLManager().getSyncSQLWrite().queue("INSERT INTO " + name + "_temp (" + getFieldNameString(fields) + ") SELECT " + getFieldNameString(fields) + " FROM " + name);
		dab.getSQLManager().getSyncSQLWrite().queue("DROP TABLE " + name );
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(newFields, false));
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		dab.getSQLManager().getSyncSQLWrite().queue("INSERT INTO " + name + "(" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(newFields) + " FROM " + name + "_temp");
		dab.getSQLManager().getSyncSQLWrite().queue("DROP TABLE " + name + "_temp");
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		fields = newFields;
	}
	
	/**
	 * @param field Removes the specified Field from the database and this object.
	 */
	public void removeFieldFromDatabase(Field field) {
		ArrayList<Field> newFields = new ArrayList<Field>();
		newFields.addAll(fields);
		if (newFields.contains(field)) {
			newFields.remove(field);
		}
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(newFields, true));
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		dab.getSQLManager().getSyncSQLWrite().queue("INSERT INTO " + name + "_temp (" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(newFields) + " FROM " + name);
		dab.getSQLManager().getSyncSQLWrite().queue("DROP TABLE " + name );
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(newFields, false));
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		dab.getSQLManager().getSyncSQLWrite().queue("INSERT INTO " + name + "(" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(newFields) + " FROM " + name + "_temp");
		dab.getSQLManager().getSyncSQLWrite().queue("DROP TABLE " + name + "_temp");
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		fields = newFields;
	}
	
	/**
	 * Changes the specified Field in the database to the new Field.  The Field in this object will also be changed.
	 * @param oldField The old Field.
	 * @param newField The new Field.
	 */
	public void alterFieldInDatabase(Field oldField, Field newField) {
		ArrayList<Field> newFields = new ArrayList<Field>();
		newFields.addAll(fields);
		int index = 0;
		if (newFields.contains(oldField)) {
			index = newFields.indexOf(oldField);
			newFields.set(index, newField);
		} else {
			return;
		}
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(newFields, true));
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		dab.getSQLManager().getSyncSQLWrite().queue("INSERT INTO " + name + "_temp (" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(fields) + " FROM " + name);
		dab.getSQLManager().getSyncSQLWrite().queue("DROP TABLE " + name );
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(newFields, false));
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		dab.getSQLManager().getSyncSQLWrite().queue("INSERT INTO " + name + "(" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(newFields) + " FROM " + name + "_temp");
		dab.getSQLManager().getSyncSQLWrite().queue("DROP TABLE " + name + "_temp");
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
		fields = newFields;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compositeKey == null) ? 0 : compositeKey.hashCode());
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + (hasCompositeKey ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Table other = (Table) obj;
		if (compositeKey == null) {
			if (other.compositeKey != null) return false;
		} else if (!compositeKey.equals(other.compositeKey)) {
			return false;
		}
		if (fields == null) {
			if (other.fields != null) return false;
		} else if (!fields.equals(other.fields)) {
			return false;
		}
		if (hasCompositeKey != other.hasCompositeKey)
			return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	
}
