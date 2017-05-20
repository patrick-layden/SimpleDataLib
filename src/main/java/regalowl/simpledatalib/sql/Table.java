package regalowl.simpledatalib.sql;

import java.util.ArrayList;
import java.util.Iterator;

import regalowl.simpledatalib.SimpleDataLib;

public class Table {
	
	private transient SQLWrite sw;
	
	private String name;
	private SimpleDataLib sdl;
	private ArrayList<Field> fields = new ArrayList<Field>();
	private boolean hasCompositeKey;
	private ArrayList<Field> compositeKey = new ArrayList<Field>();
	
	public Table(String name, SimpleDataLib sdl) {
		this.name = name;
		this.sdl = sdl;
		this.hasCompositeKey = false;
		this.sw = sdl.getSQLManager().getSQLWrite();
	}
	
	/**
	 * Loads the table structure from the database if it exists.
	 */
	public boolean loadTable() {
		TableLoader tl = new TableLoader(name, sdl);
		if (!tl.tableExists()) return false;
		setFields(tl.getFields());
		if (tl.hasCompositeKey()) setCompositeKey(tl.getCompositeKey());
		return true;
	}
	
	public void loadTableFromString(String createString) {
		TableLoader tl = new TableLoader(name, sdl, createString);
		setFields(tl.getFields());
		if (tl.hasCompositeKey()) setCompositeKey(tl.getCompositeKey());
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
		if (sdl.getSQLManager().useMySQL()) {
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
		if (sdl.getSQLManager().useMySQL()) {
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
	
	
	public void setFields(ArrayList<Field> fields) {
		this.fields.clear();
		this.fields.addAll(fields);
	}
	
	/**
	 * Sets the composite key for this table.
	 * @param compositeKey An ArrayList of the Field objects that comprise the composite key.
	 */
	public void setCompositeKey(ArrayList<Field> compositeKey) {
		this.compositeKey.clear();
		this.compositeKey.addAll(compositeKey);
		hasCompositeKey = true;
	}
	
	/**
	 * Saves this table to the database asynchronously
	 */
	public void saveAsync() {
		sw.addToQueue(getCreateStatement(fields, false));
	}
	/**
	 * Adds the table creation statement to the synchronous SQL Write queue.  The table will be saved whenever the queue is written.
	 */
	public void save() {
		boolean state = sw.writeSync();
		sw.writeSync(true);
		sw.addToQueue(getCreateStatement(fields, false));
		sw.writeSync(state);
	}
	/**
	 * Synchronously saves the table to the database immediately.
	 */
	public void saveNow() {
		boolean state = sw.writeSync();
		sw.writeSync(true);
		sw.addToQueue(getCreateStatement(fields, false));
		sw.writeSyncQueue();
		sw.writeSync(state);
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
	 * @return A comma separated list of the table's Field names.
	 */
	public String getFieldNameString() {
		return getFieldNameString(fields);
	}
	
	/**
	 * @param fieldArrayList An ArrayList of Field objects.
	 * @return A comma separated list of the Field names.
	 */
	public String getFieldNameString(ArrayList<Field> fieldArrayList) {
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
		boolean state = sw.writeSync();
		sw.writeSync(true);
		sw.addToQueue(getCreateStatement(newFields, true));
		sw.writeSyncQueue();
		sw.addToQueue("INSERT INTO " + name + "_temp (" + getFieldNameString(fields) + ") SELECT " + getFieldNameString(fields) + " FROM " + name);
		sw.addToQueue("DROP TABLE " + name );
		sw.addToQueue(getCreateStatement(newFields, false));
		sw.writeSyncQueue();
		sw.addToQueue("INSERT INTO " + name + "(" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(newFields) + " FROM " + name + "_temp");
		sw.addToQueue("DROP TABLE " + name + "_temp");
		sw.writeSyncQueue();
		fields = newFields;
		sw.writeSync(state);
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
		boolean state = sw.writeSync();
		sw.writeSync(true);
		sw.addToQueue(getCreateStatement(newFields, true));
		sw.writeSyncQueue();
		sw.addToQueue("INSERT INTO " + name + "_temp (" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(newFields) + " FROM " + name);
		sw.addToQueue("DROP TABLE " + name );
		sw.addToQueue(getCreateStatement(newFields, false));
		sw.writeSyncQueue();
		sw.addToQueue("INSERT INTO " + name + "(" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(newFields) + " FROM " + name + "_temp");
		sw.addToQueue("DROP TABLE " + name + "_temp");
		sw.writeSyncQueue();
		fields = newFields;
		sw.writeSync(state);
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
		boolean state = sw.writeSync();
		sw.writeSync(true);
		sw.addToQueue(getCreateStatement(newFields, true));
		sw.writeSyncQueue();
		sw.addToQueue("INSERT INTO " + name + "_temp (" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(fields) + " FROM " + name);
		sw.addToQueue("DROP TABLE " + name );
		sw.addToQueue(getCreateStatement(newFields, false));
		sw.writeSyncQueue();
		sw.addToQueue("INSERT INTO " + name + "(" + getFieldNameString(newFields) + ") SELECT " + getFieldNameString(newFields) + " FROM " + name + "_temp");
		sw.addToQueue("DROP TABLE " + name + "_temp");
		sw.writeSyncQueue();
		fields = newFields;
		sw.writeSync(state);
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
