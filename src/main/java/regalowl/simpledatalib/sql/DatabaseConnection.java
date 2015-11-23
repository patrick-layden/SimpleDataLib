package regalowl.simpledatalib.sql;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.events.LogEvent;
import regalowl.simpledatalib.events.LogLevel;
import regalowl.simpledatalib.events.ShutdownEvent;
import regalowl.simpledatalib.sql.WriteResult.WriteResultType;



public class DatabaseConnection {

	private SimpleDataLib sdl;
	private Connection connection;
    private AtomicBoolean readOnly = new AtomicBoolean();
    private AtomicBoolean lock = new AtomicBoolean();
    private AtomicBoolean ignoreWrites = new AtomicBoolean();
    
	public DatabaseConnection(SimpleDataLib sdl, boolean readOnly) {
		this.lock.set(false);
		this.sdl = sdl;
		this.readOnly.set(readOnly);
		ignoreWrites.set(false);
	}

	public synchronized WriteResult write(List<WriteStatement> statements) {
		if (statements == null || statements.size() == 0) return new WriteResult(WriteResultType.EMPTY);
		if (lock.get()) return new WriteResult(WriteResultType.DISABLED);
		if (ignoreWrites.get()) return new WriteResult(WriteResultType.SUCCESS);
		WriteStatement currentStatement = null;
		PreparedStatement preparedStatement = null;
		try {
			prepareConnection();
			for (WriteStatement statement : statements) {
				currentStatement = statement;
				preparedStatement = connection.prepareStatement(currentStatement.getStatement());
				currentStatement.applyParameters(preparedStatement);
				preparedStatement.executeUpdate();
			}
			if (lock.get()) {
				connection.rollback();
				return new WriteResult(WriteResultType.DISABLED);
			} else {
				connection.commit();
				WriteResult result = new WriteResult(WriteResultType.SUCCESS);
				result.setSuccessful(statements);
				return result;
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
				statements.remove(currentStatement);
				WriteResult result = new WriteResult(WriteResultType.ERROR);
				result.setFailedStatement(currentStatement);
				result.setRemaining(statements);
				result.setException(e);
				return result;
			} catch (SQLException e1) {
				sdl.getErrorWriter().writeError(e1, "Rollback failed.");
				statements.remove(currentStatement);
				WriteResult result = new WriteResult(WriteResultType.ERROR);
				result.setRemaining(statements);
				result.setException(e1);
				return result;
			}
		} finally {
			try {
				if (preparedStatement != null) preparedStatement.close();
			} catch (SQLException e) {
				sdl.getErrorWriter().writeError(e);
			}
		}
	}
	
	
	public synchronized WriteResult writeWithoutTransaction(WriteStatement statement) {
		if (statement == null) return new WriteResult(WriteResultType.EMPTY);
		if (lock.get()) return new WriteResult(WriteResultType.DISABLED);
		if (ignoreWrites.get()) return new WriteResult(WriteResultType.SUCCESS);
		try {
			prepareConnection();
			connection.setAutoCommit(true);
			Statement state = connection.createStatement();
			state.execute(statement.getStatement());
			state.close();
			WriteResult result = new WriteResult(WriteResultType.SUCCESS);
			result.addSuccessful(statement);
			return result;
		} catch (SQLException e) {
			WriteResult result = new WriteResult(WriteResultType.ERROR);
			result.setFailedStatement(statement);
			result.setException(e);
			return result;
		}
	}
	
	public synchronized QueryResult read(BasicStatement statement) {
		QueryResult qr = new QueryResult();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			prepareConnection();
			preparedStatement = connection.prepareStatement(statement.getStatement());
			statement.applyParameters(preparedStatement);
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				qr.addColumnName(rsmd.getColumnLabel(i));
			}
			while (resultSet.next()) {
				for (int i = 1; i <= columnCount; i++) {
					qr.addData(i, resultSet.getString(i));
				}
			}
			return qr;
		} catch (SQLException e) {
			qr.setException(e, statement.getStatement());
			return qr;
		} finally {
			try {
				if (preparedStatement != null) preparedStatement.close();
				if (resultSet != null) resultSet.close();
			} catch (SQLException e) {
				sdl.getErrorWriter().writeError(e);
			}
		}
	}
	
	public void setIgnoreWrites(boolean state) {
		ignoreWrites.set(state);
	}

	public void lock() {
		lock.set(true);
	}
	public void unlock() {
		lock.set(false);
	}

	public synchronized void prepareConnection() {
		if (!isValid()) {fixConnection();}
	}
	
	private synchronized boolean isValid() {
		try {
			if (connection == null || connection.isClosed()) return false;
			if (!readOnly.get() && connection.isReadOnly()) return false;
			if (!readOnly.get()) {
				try {
					connection.setAutoCommit(false);
				} catch (SQLException se) {
					return false;
				}
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	private synchronized void fixConnection() {
		closeConnection();
		openConnection();
		if (isValid()) {return;}
		if (readOnly.get()) {
			sdl.getEventPublisher().fireEvent(new LogEvent("[" + sdl.getName() + "]Fatal database connection error. " 
		+ "Make sure your database is unlocked and readable in order to use this plugin." + " Disabling " 
					+ sdl.getName() + ".", null, LogLevel.SEVERE));
		} else {
			sdl.getEventPublisher().fireEvent(new LogEvent("[" + sdl.getName() + "]Fatal database connection error. " 
		+ "Make sure your database is unlocked and writeable in order to use this plugin." + " Disabling " + sdl.getName() + ".", null, LogLevel.SEVERE));
		}
		sdl.getEventPublisher().fireEvent(new ShutdownEvent());
	}
	
	public synchronized void openConnection() {
		if (sdl.getSQLManager().useMySQL()) {
			try {
				String username = sdl.getSQLManager().getUsername();
				String password = sdl.getSQLManager().getPassword();
				int port = sdl.getSQLManager().getPort();
				String host = sdl.getSQLManager().getHost();
				String database = sdl.getSQLManager().getDatabase();
				connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
				connection.setReadOnly(readOnly.get());
			} catch (Exception e) {
				sdl.getErrorWriter().writeError(e, "Database connection error.");
			}
		} else {
			try {
				String sqlitePath = sdl.getSQLManager().getSQLitePath();
				Class.forName("org.sqlite.JDBC");
				connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
				connection.setReadOnly(readOnly.get());
			} catch (Exception e) {
				sdl.getErrorWriter().writeError(e, "Database connection error.");
			}
		}
	}
	
	public synchronized void closeConnection() {
		if (connection == null) return;
		try {
			if (connection.getAutoCommit() == false) connection.rollback();
		} catch (SQLException e) {}
		try {
			if (!connection.isClosed()) connection.close();
		} catch (Exception e) {
			sdl.getErrorWriter().writeError(e, "Connection failed to close.");
		}
	}

}
