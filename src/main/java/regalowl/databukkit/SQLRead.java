package regalowl.databukkit;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class SQLRead {

	private DataBukkit dab;
	private int threadlimit;

    private Queue<DatabaseConnection> connections = new LinkedList<DatabaseConnection>();
    private Lock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();

    
	public SQLRead(DataBukkit dabu) {
		this.dab = dabu;
		threadlimit = 1;
		for (int i = 0; i < threadlimit; i++) {
			dab.getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(dab.getPlugin(), new Runnable() {
	    		public void run() {
	    			DatabaseConnection dc = null;
	    			if (dab.useMySQL()) {
	    				dc = new MySQLConnection(dab);
	    			} else {
		    			dc = new SQLiteConnection(dab);
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
	public QueryResult aSyncSelect(String select) {
		return getDatabaseConnection().read(select);
	}
    
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getStringList(String statement) {
		ArrayList<String> data = new ArrayList<String>();
		QueryResult result = getDatabaseConnection().read(statement);
		while (result.next()) {
			data.add(result.getString(1));
		}
		return data;
	}
	/**
	 * 
	 * This method should be called asynchronously to prevent lag
	 * @return ArrayList<Double>
	 */
	public ArrayList<Double> getDoubleList(String statement) {
		ArrayList<Double> data = new ArrayList<Double>();
		QueryResult result = getDatabaseConnection().read(statement);
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
		ArrayList<Integer> data = new ArrayList<Integer>();
		QueryResult result = getDatabaseConnection().read(statement);
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
		ArrayList<Long> data = new ArrayList<Long>();
		QueryResult result = getDatabaseConnection().read(statement);
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
		ArrayList<Float> data = new ArrayList<Float>();
		QueryResult result = getDatabaseConnection().read(statement);
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
		ArrayList<Boolean> data = new ArrayList<Boolean>();
		QueryResult result = getDatabaseConnection().read(statement);
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
		QueryResult result = getDatabaseConnection().read(statement);
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
		QueryResult result = getDatabaseConnection().read(statement);
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
	public String getString(String statement) {
		QueryResult result = getDatabaseConnection().read(statement);
		String data = null;
		if (result.next()) {
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
		QueryResult result = getDatabaseConnection().read(statement);
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
		QueryResult result = getDatabaseConnection().read(statement);
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
		QueryResult result = getDatabaseConnection().read(statement);
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
		QueryResult result = getDatabaseConnection().read("SELECT COUNT(*) FROM " + table);
		result.next();
		int rowcount = result.getInt(1);
		result.close();
		return rowcount;
	}
	
	

	
	public int getActiveReadConnections() {
		return (threadlimit - connections.size());
	}
	
	
	public void shutDown() {
		for (DatabaseConnection dc:connections) {
			dc.closeConnection();
		}
	}

}


