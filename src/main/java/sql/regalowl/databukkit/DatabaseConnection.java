package regalowl.databukkit;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;



public abstract class DatabaseConnection {

	protected DataBukkit dab;
	protected DatabaseConnection dc;
	protected Connection connection;
	protected ArrayList<WriteStatement> statements = new ArrayList<WriteStatement>();
	protected WriteStatement currentStatement;
	protected PreparedStatement preparedStatement;
	protected AtomicBoolean logReadErrors = new AtomicBoolean();
	
    protected AtomicBoolean shutDownOverride = new AtomicBoolean();
    protected AtomicBoolean readOnly = new AtomicBoolean();
    
	DatabaseConnection(DataBukkit dab, boolean readOnly, boolean override) {
		dc = this;
		this.dab = dab;
		this.shutDownOverride.set(override);
		this.readOnly.set(readOnly);
	}
	
	
	public synchronized void write(List<WriteStatement> sql, boolean logErrors) {
		try {
			currentStatement = null;
			if (!isWriteable()) {fixConnection();}
			for (WriteStatement cs : sql) {statements.add(cs);}
			if (statements.size() == 0) {return;}
			for (WriteStatement statement : statements) {
				currentStatement = statement;
				if (dab.getSQLWrite().logSQL()) {dab.getSQLWrite().logSQL(currentStatement);}
				preparedStatement = connection.prepareStatement(currentStatement.getStatement());
				preparedStatement.executeUpdate();
			}
			if (dab.getSQLWrite().shutdownStatus().get() && !shutDownOverride.get()) {
				connection.rollback();
				dab.getSQLWrite().addWriteStatementsToQueue(statements);
			} else {
				connection.commit();
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
				statements.remove(currentStatement);
				currentStatement.writeFailed(e, logErrors);
				dab.getSQLWrite().addWriteStatementsToQueue(statements);
			} catch (SQLException e1) {
				dab.writeError(e, "Rollback failed.  Cannot recover. Data loss may have occurred.");
			}
		} finally {
			if (!dab.getSQLWrite().shutdownStatus().get()) {
				statements.clear();
			}
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				dab.writeError(e);
			}
			dab.getSQLWrite().returnConnection(dc);
		}
	}
	
	
	/**
	 * This function should be run asynchronously to prevent slowing the main thread.
	 * @param statement
	 * @return QueryResult
	 */
	public synchronized QueryResult read(String statement, boolean logErrors) {
		logReadErrors.set(logErrors);
		QueryResult qr = new QueryResult();
		try {
			if (!isValid()) {fixConnection();}
			Statement state = connection.createStatement();
			ResultSet resultSet = state.executeQuery(statement);
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
			resultSet.close();
			state.close();
			statement = null;
			return qr;
		} catch (SQLException e) {
			if (logReadErrors.get()) {
				dab.writeError(e, "The failed SQL statement is in the following brackets: [" + statement + "]");
			}
			return qr;
		} finally {
			dab.getSQLRead().returnConnection(dc);
		}
	}
	
	public boolean isValid() {
		try {
			if (connection == null || connection.isClosed()) {
				return false;
			}
			if (!readOnly.get() && connection.isReadOnly()) {
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	public boolean isWriteable() {
		if (!isValid()) {return false;}
		try {
			connection.setAutoCommit(false);
		} catch (SQLException se) {
			return false;
		}
		return true;
	}
	public void fixConnection() {
		closeConnection();
		openConnection();
		if (readOnly.get()) {
			if (!isValid()) {
				dab.getLogger().severe("-----------------------------------------------------");
				dab.getLogger().severe("[DataBukkit["+dab.getPlugin().getName()+"]]Fatal database connection error. "
						+ "Make sure your database is unlocked and readable in order to use this plugin."
						+ " Disabling "+dab.getPlugin().getName()+".");
				dab.getLogger().severe("-----------------------------------------------------");
				dab.getPlugin().getPluginLoader().disablePlugin(dab.getPlugin());
			}
		} else {
			if (!isWriteable()) {
				dab.getLogger().severe("-----------------------------------------------------");
				dab.getLogger().severe("[DataBukkit["+dab.getPlugin().getName()+"]]Fatal database connection error. "
						+ "Make sure your database is unlocked and writeable in order to use this plugin."
						+ " Disabling "+dab.getPlugin().getName()+".");
				dab.getLogger().severe("-----------------------------------------------------");
				dab.getPlugin().getPluginLoader().disablePlugin(dab.getPlugin());
			}
		}
	}
	protected abstract void openConnection();
	public synchronized void closeConnection() {
		try {
			if (!connection.isClosed()) {
				connection.close();
			}
		} catch (Exception e) {
			dab.writeError(e, "Connection failed to close.");
		}
	}


	
}
