package regalowl.databukkit.sql;

import java.util.ArrayList;

import regalowl.databukkit.DataBukkit;
import regalowl.databukkit.file.ErrorWriter;


public class WriteStatement extends BasicStatement {

	private int writeFailures;
	
	public WriteStatement(String statement, DataBukkit dab) {
		super(statement, dab);
		this.writeFailures = 0;
	}

	
	public void writeFailed(Exception e) {
		try {
			writeFailures++;
			if (retry(e)) {
				dab.getSQLWrite().addToQueue(statement);
			} else {
				if (dab.getSQLWrite().logWriteErrors()) {
					logError(e);
				}
			}
		} catch (Exception e2) {
			dab.writeError(e2);
		}
	}
	
	public void logError(Exception e) {
		ErrorWriter ew = dab.getErrorWriter();
		ew.writeError(e, "SQL write failed " + writeFailures + " time(s). The failing SQL statement is in the following brackets: [" + statement + "]", true);
	}
	
	public int failCount() {
		return writeFailures;
	}
	
	public boolean retry(Exception e) {
		try {
			String message = dab.getCommonFunctions().getErrorString(e).toLowerCase();
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
		ErrorWriter ew = new ErrorWriter(dab.getPluginFolderPath() + "SQL.log", dab);
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
