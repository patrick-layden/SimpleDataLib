package regalowl.databukkit;

public class WriteStatement {

	private String statement;
	private int writeFailures;
	
	public WriteStatement(String statement) {
		this.statement = statement;
		this.writeFailures = 0;
	}
	public String getStatement() {
		return statement;
	}
	
	public void writeFailed(Exception e) {
		writeFailures++;
	}
	public int failCount() {
		return writeFailures;
	}
	
}
