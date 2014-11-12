package regalowl.simpledatalib.sql;

import java.util.ArrayList;

import regalowl.simpledatalib.CommonFunctions;
import regalowl.simpledatalib.DataBukkit;
import regalowl.simpledatalib.file.ErrorWriter;


public class WriteStatement extends BasicStatement {

	private int writeFailures;
	
	public WriteStatement(String statement, DataBukkit dab) {
		super(statement, dab);
		this.writeFailures = 0;
		convert();
	}
	
	private void convert() {
		if (statement == null) {return;}
		if (dab.getSQLManager().useMySQL()) {
			if (statement.contains("datetime('NOW', 'localtime')")) {
				statement = statement.replace("datetime('NOW', 'localtime')", "NOW()");
			}
			if (statement.contains("AUTOINCREMENT")) {
				statement = statement.replace("AUTOINCREMENT", "AUTO_INCREMENT");
			}
			if (statement.contains("autoincrement")) {
				statement = statement.replace("autoincrement", "auto_increment");
			}
		} else {
			if (statement.contains("NOW()")) {
				statement = statement.replace("NOW()", "datetime('NOW', 'localtime')");
			}
			if (statement.contains("AUTO_INCREMENT")) {
				statement = statement.replace("AUTO_INCREMENT", "AUTOINCREMENT");
			}
			if (statement.contains("auto_increment")) {
				statement = statement.replace("auto_increment", "autoincrement");
			}
		}
	}
	
	public void writeFailed(Exception e) {
		try {
			writeFailures++;
			if (retry(e)) {
				dab.getSQLManager().getSQLWrite().addToQueue(statement);
			} else {
				if (dab.getSQLManager().getSQLWrite().logWriteErrors()) {
					logError(e);
				}
			}
		} catch (Exception e2) {
			dab.writeError(e2);
		}
	}
	
	public void logError(Exception e) {
		ErrorWriter ew = dab.getErrorWriter();
		String writeString = "SQL write failed " + writeFailures + " time(s). The failing SQL statement is in the following brackets: %n[" + statement + "]";
		if (parameters != null && parameters.size() > 0) {
			String paramList = "[";
			for (Object p:parameters) {
				if (p == null) {p = "";}
				paramList += "["+p.toString() + "], ";
			}
			paramList = paramList.substring(0, paramList.length() - 2) + "]";
			writeString += "%nParameters: " + paramList;
		}
		ew.writeError(e, writeString, true);
	}
	
	public int failCount() {
		return writeFailures;
	}
	
	public boolean retry(Exception e) {
		try {
			String message = CommonFunctions.getErrorString(e).toLowerCase();
			if (writeFailures > 1) {
				return false;
			}
			if (message.contains("sqlite_busy") || message.contains("database is locked")) {
				return true;
			}
			return false;
		} catch (Exception e2) {
			dab.writeError(e2);
			return false;
		}
	}
	
	public void logStatement() {
		ErrorWriter ew = new ErrorWriter(dab.getStoragePath() + "SQL.log", dab);
		ArrayList<Object> parameters = getParameters();
		if (parameters != null && parameters.size() > 0) {
			String paramList = "[";
			for (Object p:parameters) {
				if (p == null) {p = "";}
				paramList += "["+p.toString() + "], ";
			}
			paramList = paramList.substring(0, paramList.length() - 2) + "]";
			ew.writeError(null, statement.replace("%n", "[new line]") + "%nParameters: "+paramList.replace("%n", "[new line]"), true);
		} else {
			ew.writeError(null, statement.replace("%n", "[new line]"), true);
		}
	}

}
