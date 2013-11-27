package regalowl.databukkit;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.scheduler.BukkitTask;

public class SQLWrite {

	private DataBukkit dab;
	private ConcurrentHashMap<Long, String> buffer = new ConcurrentHashMap<Long, String>();
	private CopyOnWriteArrayList<String> writeStatements = new CopyOnWriteArrayList<String>();
	private BukkitTask writeTask;
	private AtomicBoolean writeActive = new AtomicBoolean();
	private AtomicLong bufferCounter = new AtomicLong();
	private AtomicLong processNext = new AtomicLong();
	private AtomicBoolean logWriteErrors = new AtomicBoolean();;
    private Queue<DatabaseConnection> connections = new LinkedList<DatabaseConnection>();
    private Queue<DatabaseConnection> activeConnections = new LinkedList<DatabaseConnection>();
    private Lock connectionLock = new ReentrantLock();
    private Condition connectionAvailable = connectionLock.newCondition();
	private AtomicBoolean shutDown = new AtomicBoolean();
    private final long writeDelay = 20L;
    private AtomicBoolean logSQL = new AtomicBoolean();

	public SQLWrite(DataBukkit dabu) {
		logWriteErrors.set(true);
		this.dab = dabu;

		DatabaseConnection dc = null;
		if (dab.useMySQL()) {
			dc = new MySQLConnection(dab, false);
		} else {
			dc = new SQLiteConnection(dab, false);
		}
		returnConnection(dc);
		logSQL.set(false);
		bufferCounter.set(0);
		processNext.set(0);
		writeActive.set(false);
		shutDown.set(false);
	}
	
	
	
	
	public synchronized void returnConnection(DatabaseConnection connection) {
		connectionLock.lock();
		try {
			activeConnections.remove(connection);
			connections.add(connection);
			connectionAvailable.signal();
		} finally {
			connectionLock.unlock();
		}
	}
	private synchronized DatabaseConnection getDatabaseConnection() {
		connectionLock.lock();
		try {
			while (connections.isEmpty()) {
				try {
					connectionAvailable.await();
				} catch (InterruptedException e) {
					dab.writeError(e, null);
				}
			}
			DatabaseConnection connect = connections.remove();
			activeConnections.add(connect);
			return connect;
		} finally {
			connectionLock.unlock();
		}
	}
	
	public void executeSynchronously(String statement) {
		DatabaseConnection database = getDatabaseConnection();
		ArrayList<String> statements = new ArrayList<String>();
		statements.add(statement);
		database.write(statements, logWriteErrors.get());
	}
	public void convertExecuteSynchronously(String statement) {
		executeSynchronously(convertSQL(statement));
	}

	
	public synchronized void addToQueue(List<String> statements) {
		for (String statement : statements) {
			if (statement == null) {continue;}
			buffer.put(bufferCounter.getAndIncrement(), statement);
		}
		startWriteTask();
	}
	public void convertAddToQueue(String statement) {
		addToQueue(convertSQL(statement));
	}
	public synchronized void addToQueue(String statement) {
		if (statement == null) {return;}
		buffer.put(bufferCounter.getAndIncrement(), statement);
		startWriteTask();
	}

	private synchronized void startWriteTask() {
		if (writeActive.get() || buffer.isEmpty() || shutDown.get()) {return;}
		writeActive.set(true);
		writeTask = dab.getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(dab.getPlugin(), new Runnable() {
			public void run() {
				DatabaseConnection database = getDatabaseConnection();
				writeStatements.clear();
				while (buffer.size() > 0) {
					writeStatements.add(buffer.get(processNext.get()));
					buffer.remove(processNext.getAndIncrement());
				}
				database.write(getWriteStatements(), logWriteErrors.get());
				if (shutDown.get()) {return;}
				writeStatements.clear();
				writeActive.set(false);
				if (!buffer.isEmpty()) {startWriteTask();}
			}
		}, writeDelay);
	}

	


	private CopyOnWriteArrayList<String> getWriteStatements() {
		CopyOnWriteArrayList<String> write = new CopyOnWriteArrayList<String>();
		for(String statement:writeStatements) {
			write.add(statement);
		}
		return write;
	}

	public int getBufferSize() {
		return buffer.size();
	}

	public int getActiveThreads() {
		return 1 - connections.size();
	}

	public ArrayList<String> getBuffer() {
		ArrayList<String> abuffer = new ArrayList<String>();
		for (String item : buffer.values()) {
			abuffer.add(item);
		}
		return abuffer;
	}

	public void shutDown() {
		shutDown.set(true);
		if (writeTask != null) {writeTask.cancel();}
		writeActive.set(true);
		while (!connections.isEmpty()){
			connections.remove().closeConnection();
		}
		while (!activeConnections.isEmpty()){
			activeConnections.remove().closeConnection();
		}
		addToQueue(writeStatements);
		saveBuffer();
	}
	private void saveBuffer() {
		if (buffer.size() > 0) {
			dab.getLogger().info("[DataBukkit["+dab.getPlugin().getName()+"]]Saving the remaining SQL queue: ["+buffer.size()+" statements].  Please wait.");
			DatabaseConnection database = null;
			if (dab.useMySQL()) {
				database = new MySQLConnection(dab, true);
			} else {
				database = new SQLiteConnection(dab, true);
			}
			writeStatements.clear();
			for (String s:buffer.values()) {
				writeStatements.add(s);
			}
			database.write(getWriteStatements(), logWriteErrors.get());
			buffer.clear();
			writeStatements.clear();
			dab.getLogger().info("[DataBukkit["+dab.getPlugin().getName()+"]]SQL queue save complete.");
		}
	}
	public AtomicBoolean shutdownStatus() {
		return shutDown;
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
		for (String value:values.values()) {
			statement += quoteValue(value) + ", ";
		}
		statement = statement.substring(0, statement.length() - 2);
		statement += ")";		
		addToQueue(statement);
	}
	/**
	 * 
	 * @param table: Name of table
	 * @param values: HashMap<Name of field, String form of value>
	 * 
	 */
	public void performUpdate(String table, HashMap<String, String> values, HashMap<String, String> conditions) {
		String statement = "UPDATE " + table + " SET ";
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			String value = values.get(field);
			statement += field + " = " + quoteValue(value) + ", ";
		}
		statement = statement.substring(0, statement.length() - 2);
		statement += " WHERE ";
		it = conditions.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			String condition = conditions.get(field);
			statement += field + " = " + quoteValue(condition) + " AND ";
		}
		statement = statement.substring(0, statement.length() - 5);
		addToQueue(statement);
	}
	
	public void performDelete(String table, HashMap<String, String> conditions) {
		String statement = "DELETE FROM " + table + " WHERE ";
		Iterator<String> it = conditions.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			String condition = conditions.get(field);
			statement += field + " = " + quoteValue(condition) + " AND ";
		}
		statement = statement.substring(0, statement.length() - 5);
		addToQueue(statement);
	}
	
	public String quoteValue(String value) {
		String valueTest = value.replaceAll("[^()]", "");
		if (valueTest.equalsIgnoreCase("()")) {
			return convertSQL(value);
		} else {
			return "'" + convertSQL(value) + "'";
		}
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
	
	public void setErrorLogging(boolean state) {
		logWriteErrors.set(state);
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
	
	public void logSQL(String statement) {
		new ErrorWriter(null, statement, dab.getPluginFolderPath() + "SQL.log", dab.getPlugin());
	}
	/**
	 * This method will call the method of your choice in the class of your choice (the class of the object) after the current write operation is complete.
	 * 
	 * @param object The object that holds the method you want to call.
	 * @param method The name of the method you want to call.
	 */
	public void afterWrite(Object object, String method) {
		new AfterWrite(object, method);
	}
	private class AfterWrite {
		private String m;
		private Object o;
		private BukkitTask afterWriteTask;
		AfterWrite(Object object, String method) {
			this.o = object;
			this.m = method;
			afterWriteTask = dab.getPlugin().getServer().getScheduler().runTaskTimer(dab.getPlugin(), new Runnable() {
				public void run() {
					if (!writeActive.get() && buffer.size() == 0) {
						try {
							Method meth = o.getClass().getMethod(m);
							meth.invoke(o.getClass().newInstance());
						} catch (Exception e) {
							dab.writeError(e, null);
						} finally {
							afterWriteTask.cancel();
						}
						return;
					}
				}
			}, 1L, 1L);
		}
	}

}
