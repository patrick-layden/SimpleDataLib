package regalowl.simpledatalib.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import regalowl.simpledatalib.SimpleDataLib;

public class SyncSQLWrite {
	
	private SimpleDataLib sdl;
	private SQLWrite sw;
	private ConnectionPool pool;
	private ArrayList<WriteStatement> queue = new ArrayList<WriteStatement>();
	
	public SyncSQLWrite(SimpleDataLib sdl, ConnectionPool pool, SQLWrite sw) {
		this.sdl = sdl;
		this.pool = pool;
		this.sw = sw;
	}
	
	public synchronized int getQueueSize() {
		return queue.size();
	}
	
	public synchronized void writeQueue() {
		if (queue == null || queue.isEmpty()) {return;}
		DatabaseConnection database = pool.getDatabaseConnection();
		WriteResult result = database.write(queue);
		if (result.getStatus() == WriteResultType.SUCCESS) {
			if (sw.logSQL() && result.getSuccessfulSQL() != null && !result.getSuccessfulSQL().isEmpty()) {
				for (WriteStatement ws:result.getSuccessfulSQL()) {
					ws.logStatement();
				}
			}
		} else if (result.getStatus() == WriteResultType.ERROR) {
			if (sw.logWriteErrors()) {
				result.getFailedSQL().writeFailed(result.getException());
			}
		}
		pool.returnConnection(database);
		queue.clear();
	}
	
	public synchronized void addWriteStatementsToQueue(List<WriteStatement> statements) {
		for (WriteStatement statement:statements) {
			if (statement != null) {
				queue.add(statement);
			}
		}
	}
	public synchronized void addToQueue(WriteStatement statement) {
		if (statement != null) {
			queue.add(statement);
		}
	}
	public synchronized void addToQueue(String statement) {
		if (statement != null) {
			addToQueue(new WriteStatement(statement, sdl));
		}
	}
	
	public synchronized void addToQueue(List<String> statements) {
		
	}
	
	public void addToQueue(String statement, ArrayList<Object> parameters) {
		
	}
	

	public synchronized void queueInsert(String table, HashMap<String, String> values) {
		addToQueue(sw.getInsertStatement(table, values));
	}
	public synchronized void queueUpdate(String table, HashMap<String, String> values, HashMap<String, String> conditions) {
		addToQueue(sw.getUpdateStatement(table, values, conditions));
	}
	public synchronized void queueDelete(String table, HashMap<String, String> conditions) {
		addToQueue(sw.getDeleteStatement(table, conditions));
	}
	
	
}
