package regalowl.simpledatalib.sql;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import regalowl.simpledatalib.SimpleDataLib;


public class SQLRead {

	private SimpleDataLib sdl;
	private AtomicBoolean logReadErrors = new AtomicBoolean();
	private ConnectionPool pool;
    
	public SQLRead(SimpleDataLib sdl, ConnectionPool pool) {
		this.sdl = sdl;
		logReadErrors.set(true);
		this.pool = pool;
	}

	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(BasicStatement select) {
		DatabaseConnection dbc = pool.getDatabaseConnection();
		QueryResult qr = dbc.read(select);
		if (!qr.successful() && logReadErrors.get()) {
			sdl.getErrorWriter().writeError(qr.getException(), "The failed SQL statement is in the following brackets: [" + qr.getFailedSQL() + "]");
		}
		pool.returnConnection(dbc);
		return qr;
	}
	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(String statement, ArrayList<Object> parameters) {
		BasicStatement bs = new BasicStatement(statement, sdl);
		if (parameters != null) {
			for (Object param:parameters) {
				bs.addParameter(param);
			}
		}
		return select(bs);
	}

	
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return QueryResult
	 */
	public QueryResult select(String select) {
		return select(new BasicStatement(select, sdl));
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		if (conditions != null && conditions.size() > 0) {
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				bs.addParameter(conditions.get(fld));
			}
		}
		return select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		if (conditions != null && conditions.size() > 0) {
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				bs.addParameter(conditions.get(fld));
			}
		}
		ArrayList<String> data = new ArrayList<String>();
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		ArrayList<Double> data = new ArrayList<Double>();
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		ArrayList<Integer> data = new ArrayList<Integer>();
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		ArrayList<Long> data = new ArrayList<Long>();
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		ArrayList<Float> data = new ArrayList<Float>();
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		ArrayList<Boolean> data = new ArrayList<Boolean>();
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		if (conditions != null && conditions.size() > 0) {
			Iterator<String> it = conditions.keySet().iterator();
			while (it.hasNext()) {
				String fld = it.next();
				bs.addParameter(conditions.get(fld));
			}
		}
		String data = null;
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement(statement, sdl);
		QueryResult result = select(bs);
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
		BasicStatement bs = new BasicStatement("SELECT COUNT(*) FROM " + table, sdl);
		QueryResult result = select(bs);
		result.next();
		int rowcount = result.getInt(1);
		result.close();
		return rowcount;
	}

	
	public int getActiveReadConnections() {
		return pool.getActiveConnections();
	}
	
	
	public void setErrorLogging(boolean state) {
		logReadErrors.set(state);
	}

}


