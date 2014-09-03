package regalowl.databukkit.sql;

public enum FieldType {
	
	VARCHAR(false, true), 
	INTEGER(true, true), 
	TEXT(false, false), 
	DATETIME(false, false), 
	TINYTEXT(false, false), 
	DOUBLE(false, true);


    private FieldType(boolean canAutoIncrement, boolean canHaveDefault){
        this.canAutoIncrement = canAutoIncrement;
        this.canHaveDefault = canHaveDefault;
    }

    private final boolean canAutoIncrement;
    public boolean canAutoIncrement(){return canAutoIncrement;}
    private final boolean canHaveDefault;
    public boolean canHaveDefault(){return canHaveDefault;}
    
}
