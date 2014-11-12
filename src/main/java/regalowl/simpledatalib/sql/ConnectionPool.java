package regalowl.simpledatalib.sql;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import regalowl.simpledatalib.SimpleDataLib;

public class ConnectionPool {
	private SimpleDataLib sdl;
	
    private Queue<DatabaseConnection> connections = new LinkedList<DatabaseConnection>();
    private Queue<DatabaseConnection> activeConnections = new LinkedList<DatabaseConnection>();
    private Lock connectionLock = new ReentrantLock();
    private Condition connectionAvailable = connectionLock.newCondition();
    
    public ConnectionPool(SimpleDataLib sdl, int connectionCount) {
    	this.sdl = sdl;
    	for (int i = 0; i < connectionCount; i++) {
    		returnConnection(new DatabaseConnection(sdl, false));
    	}
    }
    
	public int getActiveConnections() {
		return activeConnections.size();
	}
    
    
	public void returnConnection(DatabaseConnection connection) {
		connectionLock.lock();
		try {
			activeConnections.remove(connection);
			connections.add(connection);
			connectionAvailable.signal();
		} finally {
			connectionLock.unlock();
		}
	}
	public DatabaseConnection getDatabaseConnection() {
		connectionLock.lock();
		try {
			while (connections.isEmpty()) {
				try {
					connectionAvailable.await();
				} catch (InterruptedException e) {
					sdl.getErrorWriter().writeError(e, null);
				}
			}
			DatabaseConnection connect = connections.remove();
			activeConnections.add(connect);
			return connect;
		} finally {
			connectionLock.unlock();
		}
	}
	
	public void shutDown() {
		while (!connections.isEmpty()){
			DatabaseConnection dc = connections.remove();
			dc.lock();
			dc.closeConnection();
		}
		while (!activeConnections.isEmpty()){
			DatabaseConnection dc = activeConnections.remove();
			dc.lock();
			dc.closeConnection();
		}
	}
}
