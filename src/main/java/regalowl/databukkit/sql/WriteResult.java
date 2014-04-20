package regalowl.databukkit.sql;

import java.util.ArrayList;
import java.util.List;

public class WriteResult {
	private boolean successful;
	private WriteStatement failedSQL;
	private Exception exception;
	private List<WriteStatement> remainingSQL = new ArrayList<WriteStatement>();
	private List<WriteStatement> successfulSQL = new ArrayList<WriteStatement>();
	
	public WriteResult(boolean success, List<WriteStatement> successful) {
		this.successful = success;
		this.successfulSQL.addAll(successful);
	}
	
	public WriteResult(boolean success, List<WriteStatement> successful, WriteStatement failedStatement, Exception error, List<WriteStatement> remaining) {
		this.successful = success;
		this.failedSQL = failedStatement;
		this.exception = error;
		if (remaining != null) {
			for (WriteStatement ws:remaining) {
				this.remainingSQL.add(ws);
			}
		}
		if (successful != null) {
			for (WriteStatement ws:successful) {
				this.successfulSQL.add(ws);
			}
		}
	}

	public boolean wasSuccessful() {
		return successful;
	}
	public WriteStatement getFailedSQL() {
		return failedSQL;
	}
	public Exception getException() {
		return exception;
	}
	public List<WriteStatement> getRemainingSQL() {
		return remainingSQL;
	}
	public List<WriteStatement> getSuccessfulSQL() {
		return successfulSQL;
	}
	
}
