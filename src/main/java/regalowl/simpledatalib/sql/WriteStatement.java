package regalowl.simpledatalib.sql;

import java.io.File;
import java.util.ArrayList;

import regalowl.simpledatalib.CommonFunctions;
import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.file.ErrorWriter;


public class WriteStatement extends BasicStatement {

	private int writeFailures;
	
	public WriteStatement(String statement, SimpleDataLib sdl) {
		super(statement, sdl);
		this.writeFailures = 0;
		convert();
	}
	
	private void convert() {
		if (statement == null) {return;}
		if (sdl.getSQLManager().useMySQL()) {
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
				sdl.getSQLManager().getSQLWrite().addToQueue(statement);
			} else {
				if (sdl.getSQLManager().getSQLWrite().logWriteErrors()) {
					logError(e);
				}
			}
		} catch (Exception e2) {
			sdl.getErrorWriter().writeError(e2);
		}
	}
	
	public void logError(Exception e) {
		ErrorWriter ew = sdl.getErrorWriter();
		String writeString = "SQL write failed " + writeFailures + " time(s). The failing SQL statement is in the following brackets: {{newline}}[" + statement + "]";
		if (parameters != null && parameters.size() > 0) {
			String paramList = "[";
			for (Object p:parameters) {
				if (p == null) {p = "";}
				paramList += "["+p.toString() + "], ";
			}
			paramList = paramList.substring(0, paramList.length() - 2) + "]";
			writeString += "{{newline}}Parameters: " + paramList;
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
			sdl.getErrorWriter().writeError(e2);
			return false;
		}
	}
	
	public void logStatement() {
		ErrorWriter ew = new ErrorWriter(sdl.getStoragePath() + File.separator + "SQL.log", sdl);
		ArrayList<Object> parameters = getParameters();
		if (parameters != null && parameters.size() > 0) {
			String paramList = "[";
			for (Object p:parameters) {
				if (p == null) {p = "";}
				paramList += "["+p.toString() + "], ";
			}
			paramList = paramList.substring(0, paramList.length() - 2) + "]";
			ew.writeError(null, statement + "{{newline}}Parameters: "+paramList, true);
		} else {
			ew.writeError(null, statement, true);
		}
	}

}
