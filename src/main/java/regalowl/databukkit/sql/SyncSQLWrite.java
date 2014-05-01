package regalowl.databukkit.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import regalowl.databukkit.DataBukkit;

public class SyncSQLWrite {
	
	private DataBukkit dab;
	private SQLWrite sw;
	private ConnectionPool pool;
	
	public SyncSQLWrite(DataBukkit dab, ConnectionPool pool) {
		this.dab = dab;
		this.pool = pool;
		this.sw = dab.getSQLWrite();
	}
	
	public void execute(List<WriteStatement> statements) {
		if (statements == null) {return;}
		DatabaseConnection database = pool.getDatabaseConnection();
		WriteResult result = database.write(statements);
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
	}
	public void execute(WriteStatement statement) {
		if (statement == null) {return;}
		ArrayList<WriteStatement> statements = new ArrayList<WriteStatement>();
		statements.add(statement);
		execute(statements);
	}
	public void execute(String statement) {
		if (statement == null) {return;}
		ArrayList<WriteStatement> statements = new ArrayList<WriteStatement>();
		statements.add(new WriteStatement(statement, dab));
		execute(statements);
	}
	public void convertExecute(String statement) {
		if (statement == null) {return;}
		execute(sw.convertSQL(statement));
	}
	
	

	
	public void createSqlTable(String name, ArrayList<String> fields) {
		String statement = "CREATE TABLE IF NOT EXISTS " + name + " (";
		for (int i=0; i < fields.size(); i++) {
			String field = dab.getSQLWrite().convertSQL(fields.get(i));
			if (i < (fields.size() - 1)) {
				statement += field + ", ";
			} else {
				statement += field + ")";
			}
		}
		execute(statement);
	}
	public void performInsert(String table, HashMap<String, String> values) {
		String statement = "INSERT INTO " + table + " (";
		for (String field:values.keySet()) {
			statement += field + ", ";
		}
		statement = statement.substring(0, statement.length() - 2);
		statement += ") VALUES (";
		for (int i=0; i<values.size(); i++) {
			statement +=  "?,";
		}
		statement = statement.substring(0, statement.length() - 1);
		statement += ")";
		WriteStatement ws = new WriteStatement(statement, dab);
		for (String value:values.values()) {
			ws.addParameter(sw.convertSQL(value));
		}
		execute(ws);
	}
	public void performUpdate(String table, HashMap<String, String> values, HashMap<String, String> conditions) {
		String statement = "UPDATE " + table + " SET ";
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()) {
			statement += it.next() + " = ?, ";
		}
		statement = statement.substring(0, statement.length() - 2);
		statement += " WHERE ";
		it = conditions.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			statement += field + " = ? AND ";
		}
		statement = statement.substring(0, statement.length() - 5);
		WriteStatement ws = new WriteStatement(statement, dab);
		it = values.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			ws.addParameter(values.get(field));
		}
		it = conditions.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			ws.addParameter(conditions.get(field));
		}
		execute(ws);
	}
	
	public void performDelete(String table, HashMap<String, String> conditions) {
		String statement = "DELETE FROM " + table + " WHERE ";
		Iterator<String> it = conditions.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			statement += field + " = ? AND ";
		}
		statement = statement.substring(0, statement.length() - 5);
		WriteStatement ws = new WriteStatement(statement, dab);
		
		it = conditions.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			ws.addParameter(conditions.get(field));
		}
		execute(ws);
	}
	
	
}
