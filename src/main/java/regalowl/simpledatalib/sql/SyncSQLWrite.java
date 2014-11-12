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
	
	public SyncSQLWrite(SimpleDataLib sdl, ConnectionPool pool) {
		this.sdl = sdl;
		this.pool = pool;
		this.sw = sdl.getSQLManager().getSQLWrite();
	}
	
	public int getQueueSize() {
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
	
	public synchronized void queue(List<WriteStatement> statements) {
		for (WriteStatement statement:statements) {
			if (statement != null) {
				queue.add(statement);
			}
		}
	}
	public synchronized void queue(WriteStatement statement) {
		if (statement != null) {
			queue.add(statement);
		}
	}
	public synchronized void queue(String statement) {
		if (statement != null) {
			queue(new WriteStatement(statement, sdl));
		}
	}
	

	public synchronized void queueInsert(String table, HashMap<String, String> values) {
		queue(sw.getInsertStatement(table, values));
	}
	public synchronized void queueUpdate(String table, HashMap<String, String> values, HashMap<String, String> conditions) {
		queue(sw.getUpdateStatement(table, values, conditions));
	}
	public synchronized void queueDelete(String table, HashMap<String, String> conditions) {
		queue(sw.getDeleteStatement(table, conditions));
	}
	
	
}
