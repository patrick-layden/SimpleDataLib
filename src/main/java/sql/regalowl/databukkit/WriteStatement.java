package regalowl.databukkit;


public class WriteStatement extends BasicStatement {

	private int writeFailures;
	
	public WriteStatement(String statement, DataBukkit dab) {
		super(statement, dab);
		this.writeFailures = 0;
	}

	
	public void writeFailed(Exception e, boolean logError) {
		try {
			writeFailures++;
			if (retry(e)) {
				dab.getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(dab.getPlugin(), new Runnable() {
					public void run() {
						dab.getSQLWrite().addToQueue(statement);
					}
				}, 60L);
			} else {
				if (logError) {
					dab.writeError(e, "SQL write failed "+writeFailures+" time(s).  The failing SQL statement is in the following brackets: [" + statement + "]");
				}
			}
		} catch (Exception e2) {
			dab.writeError(e2);
		}
	}
	public int failCount() {
		return writeFailures;
	}
	public boolean retry(Exception e) {
		try {
			String message = dab.getCommonFunctions().getErrorString(e).toLowerCase();
			if (writeFailures > 3) {
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

	
}
