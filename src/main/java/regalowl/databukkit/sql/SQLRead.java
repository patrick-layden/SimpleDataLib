package regalowl.databukkit.sql;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import regalowl.databukkit.DataBukkit;


public class SQLRead {

	private DataBukkit dab;
	private int threadlimit;
	private AtomicBoolean logReadErrors = new AtomicBoolean();
    private Queue<DatabaseConnection> connections = new LinkedList<DatabaseConnection>();
    private Lock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();

    
	public SQLRead(DataBukkit dabu) {
		this.dab = dabu;
		logReadErrors.set(true);
		threadlimit = 1;
		for (int i = 0; i < threadlimit; i++) {
			dab.getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(dab.getPlugin(), new Runnable() {
	    		public void run() {
	    			DatabaseConnection dc = null;
	    			if (dab.useMySQL()) {
	    				dc = new MySQLConnection(dab, true, false);
	    			} else {
		    			dc = new SQLiteConnection(dab, true, false);
	    			}
	    			returnConnection(dc);
	    		}
	    	}, i);
		}
	}

	public void returnConnection(DatabaseConnection connection) {
		lock.lock();
		try {
			while (connections.size() == threadlimit) {
				try {
					notFull.await();
				} catch (InterruptedException e) {
					dab.writeError(e, null);
				}
			}
			connections.add(connection);
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	private DatabaseConnection getDatabaseConnection() {
		lock.lock();
		try {
			while (connections.isEmpty()) {
				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					dab.writeError(e, null);
				}
			}
			DatabaseConnection connect = connections.remove();
			notFull.signal();
			return connect;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(String statement, ArrayList<Object> parameters) {
		BasicStatement bs = new BasicStatement(statement, dab);
		for (Object param:parameters) {
			bs.addParameter(param);
		}
		return getDatabaseConnection().read(bs, logReadErrors.get());
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(BasicStatement select) {
		return getDatabaseConnection().read(select, logReadErrors.get());
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(String select) {
		BasicStatement bs = new BasicStatement(select, dab);
		return getDatabaseConnection().read(bs, logReadErrors.get());
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(String table, ArrayList<String> fields, HashMap<String, String> conditions) {
		String statement = "SELECT ";
		for (String field: fields) {
			statement += field + ", ";
		}
		statement = statement.substring(0, statement.length() - 2);
		statement += " FROM " + table;
		if (conditions != null && conditions.size() > 0) {
			statement += " WHERE ";
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				statement += fld + " = ? AND ";
			}
			statement = statement.substring(0, statement.length() - 5);
		}
		BasicStatement bs = new BasicStatement(statement, dab);
		if (conditions != null && conditions.size() > 0) {
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				bs.addParameter(conditions.get(fld));
			}
		}
		return getDatabaseConnection().read(bs, logReadErrors.get());
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(String table, String field, HashMap<String, String> conditions) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(field);
		return select(table, fields, conditions);
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(String table, HashMap<String, String> conditions) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("*");
		return select(table, fields, conditions);
	}

	
	/**
	 * This method will perform a query asynchronously, and then synchronously call the method of your choice in the class of your choice with the QueryResult as the parameter.
	 * This method does not need to be called asynchronously but the performance may be less than other methods due to reflection.
	 * 
	 * @param object The object that holds the method you want to call.
	 * @param method The name of the method you want to call.
	 * @param query The SQL select query to perform.
	 */
	public void syncRead(Object object, String method, BasicStatement query, Object args) {
		new SyncRead(object, method, query, args);
	}
	/**
	 * This method will perform a query asynchronously, and then synchronously call the method of your choice in the class of your choice with the QueryResult as the parameter.
	 * This method does not need to be called asynchronously but the performance may be less than other methods due to reflection.
	 * 
	 * @param object The object that holds the method you want to call.
	 * @param method The name of the method you want to call.
	 * @param query The SQL select query to perform.
	 */
	public void syncRead(Object object, String method, String query, Object args) {
		BasicStatement bs = new BasicStatement(query, dab);
		new SyncRead(object, method, bs, args);
	}
	private class SyncRead {
		private String m;
		private BasicStatement q;
		private Object o;
		private QueryResult qr;
		private Object args;
		SyncRead(Object object, String method, BasicStatement query, Object arguments) {
			this.o = object;
			this.m = method;
			this.q = query;
			this.args = arguments;
			dab.getPlugin().getServer().getScheduler().runTaskAsynchronously(dab.getPlugin(), new Runnable() {
				public void run() {
					qr = getDatabaseConnection().read(q, logReadErrors.get());
					qr.setAdditionalData(args);
					dab.getPlugin().getServer().getScheduler().runTask(dab.getPlugin(), new Runnable() {
						public void run() {
							try {
								Method meth = o.getClass().getMethod(m, QueryResult.class);
								meth.invoke(o.getClass().newInstance(), qr);
							} catch (Exception e) {
								dab.writeError(e, null);
							}
						}
					});
				}
			});
		}
	}
    
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getStringList(String table, String field, HashMap<String, String> conditions) {
		String statement = "SELECT " + field + " FROM " + table;
		if (conditions != null && conditions.size() > 0) {
			statement += " WHERE ";
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				statement += fld + " = ? AND ";
			}
			statement = statement.substring(0, statement.length() - 5);
		}
		BasicStatement bs = new BasicStatement(statement, dab);
		if (conditions != null && conditions.size() > 0) {
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				bs.addParameter(conditions.get(fld));
			}
		}
		ArrayList<String> data = new ArrayList<String>();
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		while (result.next()) {
			data.add(result.getString(1));
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return ArrayList<Double>
	 */
	public ArrayList<Double> getDoubleList(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		ArrayList<Double> data = new ArrayList<Double>();
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		while (result.next()) {
			data.add(result.getDouble(1));
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return ArrayList<Integer>
	 */
	public ArrayList<Integer> getIntList(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		ArrayList<Integer> data = new ArrayList<Integer>();
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		while (result.next()) {
			data.add(result.getInt(1));
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return ArrayList<Long>
	 */
	public ArrayList<Long> getLongList(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		ArrayList<Long> data = new ArrayList<Long>();
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		while (result.next()) {
			data.add(result.getLong(1));
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return ArrayList<Flost>
	 */
	public ArrayList<Float> getFloatList(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		ArrayList<Float> data = new ArrayList<Float>();
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		while (result.next()) {
			data.add(result.getFloat(1));
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return ArrayList<Boolean>
	 */
	public ArrayList<Boolean> getBooleanList(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		ArrayList<Boolean> data = new ArrayList<Boolean>();
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		while (result.next()) {
			data.add(result.getBoolean(1));
		}
		result.close();
		return data;
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return Integer
	 */
	public Integer getInt(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		Integer data = null;
		if (result.next()) {
			data = result.getInt(1);
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return Boolean
	 */
	public Boolean getBoolean(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		Boolean data = null;
		if (result.next()) {
			data = result.getBoolean(1);
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return String
	 */
	public String getString(String table, String field, HashMap<String, String> conditions) {
		String statement = "SELECT " + field + " FROM " + table;
		if (conditions != null && conditions.size() > 0) {
			statement += " WHERE ";
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				statement += fld + " = ? AND ";
			}
			statement = statement.substring(0, statement.length() - 5);
		}
		BasicStatement bs = new BasicStatement(statement, dab);
		if (conditions != null && conditions.size() > 0) {
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				bs.addParameter(conditions.get(fld));
			}
		}
		String data = null;
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		while (result.next()) {
			data = result.getString(1);
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return Double
	 */
	public Double getDouble(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		Double data = null;
		if (result.next()) {
			data = result.getDouble(1);
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return Long
	 */
	public Long getLong(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		Long data = null;
		if (result.next()) {
			data = result.getLong(1);
		}
		result.close();
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return Float
	 */
	public Float getFloat(String statement) {
		BasicStatement bs = new BasicStatement(statement, dab);
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		Float data = null;
		if (result.next()) {
			data = result.getFloat(1);
		}
		result.close();
		return data;
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return Integer
	 */
	public int countTableEntries(String table) {
		BasicStatement bs = new BasicStatement("SELECT COUNT(*) FROM " + table, dab);
		QueryResult result = getDatabaseConnection().read(bs, logReadErrors.get());
		result.next();
		int rowcount = result.getInt(1);
		result.close();
		return rowcount;
	}
	
	

	
	public int getActiveReadConnections() {
		return (threadlimit - connections.size());
	}
	
	
	public void shutDown() {
		try {
			for (DatabaseConnection dc:connections) {
				dc.closeConnection();
			}
		} catch (Exception e) {}
		connections.clear();
	}
	
	public void setErrorLogging(boolean state) {
		logReadErrors.set(state);
	}

}


