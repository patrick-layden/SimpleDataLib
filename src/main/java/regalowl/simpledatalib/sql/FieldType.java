package regalowl.simpledatalib.sql;

public enum FieldType {
	
	VARCHAR(false, true), 
	INTEGER(true, true), 
	TEXT(false, false), 
	DATETIME(false, false), 
	TINYTEXT(false, false), 
	LONGTEXT(false, false), 
	DOUBLE(false, true),
	BLOB(false, false);

    private FieldType(boolean canAutoIncrement, boolean canHaveDefault){
        this.canAutoIncrement = canAutoIncrement;
        this.canHaveDefault = canHaveDefault;
    }

    private final boolean canAutoIncrement;
    public boolean canAutoIncrement(){return canAutoIncrement;}
    private final boolean canHaveDefault;
    public boolean canHaveDefault(){return canHaveDefault;}
    
    public static FieldType fromString(String type) {
    	type = type.toUpperCase();
    	if (type.contains("VARCHAR")) {
    		return VARCHAR;
    	} else if (type.equals("INTEGER") || type.equals("INT")) {
    		return INTEGER;
    	} else if (type.equals("TEXT")) {
    		return TEXT;
    	} else if (type.equals("DATETIME")) {
    		return DATETIME;
    	} else if (type.equals("TINYTEXT")) {
    		return TINYTEXT;
    	} else if (type.equals("LONGTEXT")) {
    		return LONGTEXT;
    	} else if (type.equals("VARCHAR")) {
    		return VARCHAR;
    	} else if (type.equals("DOUBLE")) {
    		return DOUBLE;
    	} else if (type.equals("BLOB")) {
    		return BLOB;
    	}
    	return null;
    }
    
}
