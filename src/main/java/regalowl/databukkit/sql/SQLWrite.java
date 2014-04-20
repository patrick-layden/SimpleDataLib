package regalowl.databukkit.sql;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.scheduler.BukkitRunnable;

import regalowl.databukkit.DataBukkit;

public class SQLWrite {

	private DataBukkit dab;
	private ConnectionPool pool;
	
	private ConcurrentHashMap<Long, WriteStatement> buffer = new ConcurrentHashMap<Long, WriteStatement>();
	private AtomicLong bufferCounter = new AtomicLong();
	private AtomicLong processNext = new AtomicLong();
	
	private AtomicBoolean logWriteErrors = new AtomicBoolean();;
    private AtomicBoolean logSQL = new AtomicBoolean();
    
	private AtomicBoolean writeActive = new AtomicBoolean();
    private WriteTask writeTask;
    private final long writeTaskInterval = 600L;
    
	public SQLWrite(DataBukkit dab, ConnectionPool pool) {
		this.dab = dab;
		this.pool = pool;
		logWriteErrors.set(true);
		logSQL.set(false);
		bufferCounter.set(0);
		processNext.set(0);
		writeActive.set(false);
		writeTask = new WriteTask();
		writeTask.runTaskTimerAsynchronously(dab.getPlugin(), writeTaskInterval, writeTaskInterval);
	}
	
	

	public synchronized void addToQueue(WriteStatement statement) {
		if (statement == null) {return;}
		buffer.put(bufferCounter.getAndIncrement(), statement);
	}
	public synchronized void addWriteStatementsToQueue(List<WriteStatement> statements) {
		if (statements == null) {return;}
		for (WriteStatement statement : statements) {
			addToQueue(statement);
		}
	}
	public synchronized void addToQueue(String statement) {
		if (statement == null) {return;}
		addToQueue(new WriteStatement(statement, dab));
	}
	public synchronized void addToQueue(List<String> statements) {
		if (statements == null) {return;}
		for (String statement : statements) {
			addToQueue(statement);
		}
	}
	public synchronized void convertAddToQueue(String statement) {
		if (statement == null) {return;}
		addToQueue(convertSQL(statement));
	}

	public synchronized void addToQueue(String statement, ArrayList<Object> parameters) {
		if (statement == null) {return;}
		WriteStatement ws = new WriteStatement(statement, dab);
		for (Object param:parameters) {
			ws.addParameter(param);
		}
		addToQueue(ws);
	}


	
    private class WriteTask extends BukkitRunnable {
    	private boolean stop;
    	private ArrayList<WriteStatement> writeArray;
    	private DatabaseConnection database;
    	
    	public WriteTask() {
    		this.stop = false;
    	}
    	
		public void run() {
			if (stop) {return;}
			if (buffer.size() == 0) {return;}
			writeActive.set(true);
			database = pool.getDatabaseConnection();
			writeArray = new ArrayList<WriteStatement>();
			while (buffer.size() > 0) {
				writeArray.add(buffer.get(processNext.get()));
				buffer.remove(processNext.getAndIncrement());
			}
			write(writeArray);
			pool.returnConnection(database);
			writeActive.set(false);
		}

		private void write(ArrayList<WriteStatement> writeArray) {
			WriteResult result = database.write(writeArray);
			if (result.wasSuccessful()) {
					if (logSQL.get() && result.getSuccessfulSQL() != null && !result.getSuccessfulSQL().isEmpty()) {
						for (WriteStatement ws:result.getSuccessfulSQL()) {
							ws.logStatement();
						}
					}
			} else {
				if (logWriteErrors.get()) {
					result.getFailedSQL().writeFailed(result.getException());
				}
				if (result.getRemainingSQL() != null && !result.getRemainingSQL().isEmpty()) {
					addWriteStatementsToQueue(result.getRemainingSQL());
				}
			}
		}
		
		public void stop() {
			this.stop = true;
		}
		
		public ArrayList<WriteStatement> getActiveStatements() {
			return writeArray;
		}
    }
	
	


	public int getBufferSize() {
		return buffer.size();
	}



	public synchronized void shutDown() {
		writeTask.stop();
		addWriteStatementsToQueue(writeTask.getActiveStatements());
		if (writeTask != null) {writeTask.cancel();}
		pool.shutDown();
		saveBuffer();
	}
	private void saveBuffer() {
		if (buffer.size() == 0) {return;}
		dab.getLogger().info("[DataBukkit[" + dab.getPlugin().getName() + "]]Saving the remaining SQL queue: [" + buffer.size() + " statements].  Please wait.");
		DatabaseConnection database = new DatabaseConnection(dab, false);
		ArrayList<WriteStatement> writeArray = new ArrayList<WriteStatement>();
		while (buffer.size() > 0) {
			writeArray.add(buffer.get(processNext.get()));
			buffer.remove(processNext.getAndIncrement());
		}
		WriteResult result = database.write(writeArray);
		if (result.wasSuccessful()) {
			if (logSQL.get() && result.getSuccessfulSQL() != null && !result.getSuccessfulSQL().isEmpty()) {
				for (WriteStatement ws : result.getSuccessfulSQL()) {
					ws.logStatement();
				}
			}
		} else {
			dab.getLogger().severe("[DataBukkit[" + dab.getPlugin().getName() + "]]A database error occurred while shutting down.  Attempting to save remaining data... This may take longer than usual.");
			if (logWriteErrors.get()) {
				result.getFailedSQL().logError(result.getException());
			}
			if (result.getRemainingSQL() != null && !result.getRemainingSQL().isEmpty()) {
				for (WriteStatement ws : result.getRemainingSQL()) {
					ArrayList<WriteStatement> statement = new ArrayList<WriteStatement>();
					statement.add(ws);
					WriteResult r = database.write(statement);
					if (r.wasSuccessful()) {
						if (logSQL.get() && r.getSuccessfulSQL() != null && !r.getSuccessfulSQL().isEmpty()) {
							r.getSuccessfulSQL().get(0).logStatement();
						}
					} else {
						if (logWriteErrors.get()) {
							r.getFailedSQL().logError(r.getException());
						}
					}
				}
			}
		}
		buffer.clear();
		dab.getLogger().info("[DataBukkit[" + dab.getPlugin().getName() + "]]SQL queue save complete.");

	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void createSqlTable(String name, ArrayList<String> fields) {
		String statement = "CREATE TABLE IF NOT EXISTS " + name + " (";
		for (int i=0; i < fields.size(); i++) {
			String field = convertSQL(fields.get(i));
			if (i < (fields.size() - 1)) {
				statement += field + ", ";
			} else {
				statement += field + ")";
			}
		}
		addToQueue(statement);
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
			ws.addParameter(convertSQL(value));
		}
		addToQueue(ws);
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
		addToQueue(ws);
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
		addToQueue(ws);
	}
	
	
	
	
	

	public String convertSQL(String statement) {
		if (statement == null) {return statement;}
		if (dab.useMySQL()) {
			statement = statement.replace("datetime('NOW', 'localtime')", "NOW()");
			statement = statement.replace("AUTOINCREMENT", "AUTO_INCREMENT");
			statement = statement.replace("autoincrement", "auto_increment");
		} else {
			statement = statement.replace("NOW()", "datetime('NOW', 'localtime')");
			statement = statement.replace("AUTO_INCREMENT", "AUTOINCREMENT");
			statement = statement.replace("auto_increment", "autoincrement");
		}
		return statement;
	}
	public String longText(String statement) {
		if (dab.useMySQL()) {
			statement = statement.replace(" TEXT ", " LONGTEXT ");
		} else {
			statement = statement.replace(" LONGTEXT ", " TEXT ");
		}
		return statement;
	}
	
	
	
	
	public void setErrorLogging(boolean state) {
		logWriteErrors.set(state);
	}
	public boolean logWriteErrors() {
		return logWriteErrors.get();
	}
	
	public boolean writeActive() {
		return writeActive.get();
	}
	
	public void setLogSQL(boolean state) {
		logSQL.set(state);
	}
	public boolean logSQL() {
		return logSQL.get();
	}

}
