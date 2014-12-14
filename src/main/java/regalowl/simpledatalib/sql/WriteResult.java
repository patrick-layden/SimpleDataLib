package regalowl.simpledatalib.sql;

import java.util.ArrayList;
import java.util.List;

public class WriteResult {
	
	private WriteResultType type;
	private WriteStatement failedSQL;
	private Exception exception;
	private List<WriteStatement> remainingSQL = new ArrayList<WriteStatement>();
	private List<WriteStatement> successfulSQL = new ArrayList<WriteStatement>();
	
	public enum WriteResultType {SUCCESS, EMPTY, ERROR, DISABLED;}

	public WriteResult() {}
	public WriteResult(WriteResultType type) {
		this.type = type;
	}
	
	public void setType(WriteResultType type) {
		this.type = type;
	}
	public WriteResultType getStatus() {
		return type;
	}
	
	public void setSuccessful(List<WriteStatement> successful) {
		this.successfulSQL.clear();
		this.successfulSQL.addAll(successful);
	}
	public void addSuccessful(WriteStatement statement) {
		successfulSQL.add(statement);
	}
	public List<WriteStatement> getSuccessfulSQL() {
		return successfulSQL;
	}
	public boolean hasSuccessfulSQL() {
		return (successfulSQL != null && !successfulSQL.isEmpty()) ? true:false;
	}
	
	public void setRemaining(List<WriteStatement> remaining) {
		this.remainingSQL.clear();
		this.remainingSQL.addAll(remaining);
	}
	public void addRemaining(WriteStatement statement) {
		remainingSQL.add(statement);
	}
	public List<WriteStatement> getRemainingSQL() {
		return remainingSQL;
	}
	public boolean hasRemainingSQL() {
		return (remainingSQL != null && !remainingSQL.isEmpty()) ? true:false;
	}
	
	public void setException(Exception e) {
		this.exception = e;
	}
	public Exception getException() {
		return exception;
	}
	public boolean hasException() {
		return (exception != null) ? true:false;
	}
	
	public void setFailedStatement(WriteStatement failed) {
		this.failedSQL = failed;
	}
	public WriteStatement getFailedStatement() {
		return failedSQL;
	}
	public boolean hasFailedStatement() {
		return (failedSQL != null) ? true:false;
	}
	
}
