package regalowl.simpledatalib.sql;


public class Field {

	private String name;
	private FieldType type;
	private boolean primaryKey;
	private boolean autoIncrement;
	private boolean hasDefault;
	private String defaultValue;
	private boolean hasFieldSize;
	private int fieldSize;
	private boolean notNull;
	private boolean useMySQL;
	private boolean unique;
	
	public Field(String name, FieldType type) {
		this.name = name;
		this.type = type;
		this.primaryKey = false;
		this.autoIncrement = false;
		this.hasDefault = false;
		this.hasFieldSize = false;
		this.notNull = false;
		this.useMySQL = false;
		this.unique = false;
	}
	
	public void setPrimaryKey() {
		if (!unique) {
			primaryKey = true;
		}
	}
	
	public void setAutoIncrement() {
		if (type.canAutoIncrement()) {
			autoIncrement = true;
		}
	}
	
	public void setUnique() {
		if (!primaryKey) {
			unique = true;
		}
	}
	
	public void setDefault(String defaultValue) {
		if (type.canHaveDefault()) {
			this.hasDefault = true;
			this.defaultValue = defaultValue;
		}
	}
	
	public void setFieldSize(int fieldSize) {
		this.hasFieldSize = true;
		this.fieldSize = fieldSize;
	}
	
	public void setNotNull() {
		notNull = true;
	}
	
	public void setUseMySQL() {
		useMySQL = true;
	}
	
	private void checkTypeCompatibility() {
		if (type.equals(FieldType.LONGTEXT) && !useMySQL) {
			type = FieldType.TEXT;
		}
	}
	
	public String getString() {
		checkTypeCompatibility();
		String fieldString = name + " " + type.toString();
		if (hasFieldSize) {
			fieldString += "(" + fieldSize + ")";
		}
		if (notNull) {
			fieldString += " NOT NULL";
		}
		if (hasDefault) {
			fieldString += " DEFAULT '" + defaultValue + "'";
		}
		if (primaryKey) {
			fieldString += " PRIMARY KEY";
		}
		if (unique) {
			fieldString += " UNIQUE";
		}
		if (autoIncrement) {
			if (useMySQL) {
				fieldString += " AUTO_INCREMENT";
			} else {
				fieldString += " AUTOINCREMENT";
			}

		}
		return fieldString;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (autoIncrement ? 1231 : 1237);
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + fieldSize;
		result = prime * result + (hasDefault ? 1231 : 1237);
		result = prime * result + (hasFieldSize ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (notNull ? 1231 : 1237);
		result = prime * result + (primaryKey ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (useMySQL ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Field other = (Field) obj;
		if (autoIncrement != other.autoIncrement)
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (fieldSize != other.fieldSize)
			return false;
		if (hasDefault != other.hasDefault)
			return false;
		if (hasFieldSize != other.hasFieldSize)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (notNull != other.notNull)
			return false;
		if (primaryKey != other.primaryKey)
			return false;
		if (type != other.type)
			return false;
		if (useMySQL != other.useMySQL)
			return false;
		return true;
	}
	
}
