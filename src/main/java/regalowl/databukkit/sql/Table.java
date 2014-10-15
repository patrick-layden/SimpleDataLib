package regalowl.databukkit.sql;

import java.util.ArrayList;
import java.util.Iterator;

import regalowl.databukkit.DataBukkit;

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
	
	public String getName() {
		return name;
	}
	
	
	public Field addField(String name, FieldType type) {
		Field newField = new Field(name, type);
		if (dab.getSQLManager().useMySQL()) {
			newField.setUseMySQL();
		}
		fields.add(newField);
		return newField;
	}
	
	public Field generateField(String name, FieldType type) {
		Field newField = new Field(name, type);
		if (dab.getSQLManager().useMySQL()) {
			newField.setUseMySQL();
		}
		return newField;
	}
	
	public Field getField(String name) {
		for (Field f:fields) {
			if (f.getName().equalsIgnoreCase(name)) {
				return f;
			}
		}
		return null;
	}
	
	
	public void setCompositeKey(ArrayList<Field> compositeKey) {
		this.compositeKey.addAll(compositeKey);
		hasCompositeKey = true;
	}
	
	public void saveAsync() {
		dab.getSQLManager().getSQLWrite().addToQueue(getCreateStatement(fields, false));
	}
	public void save() {
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(fields, false));
	}
	public void saveNow() {
		dab.getSQLManager().getSyncSQLWrite().queue(getCreateStatement(fields, false));
		dab.getSQLManager().getSyncSQLWrite().writeQueue();
	}

	private String getCompositeKeyString() {
		String key = "PRIMARY KEY(";
		Iterator<Field> it = compositeKey.iterator();
		while (it.hasNext()) {
		    Field f = it.next();
		    if (!it.hasNext()) {
		    	key += f.getName() + ")";
		    } else {
		    	key += f.getName() + ", ";
		    }
		}
		return key;
	}
	
	public void addField(Field field, Field afterField) {
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
	
	public void removeField(Field field) {
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
	
	public void alterField(Field oldField, Field newField) {
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
	
	
}
