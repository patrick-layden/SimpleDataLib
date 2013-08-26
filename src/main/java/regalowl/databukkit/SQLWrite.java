package regalowl.databukkit;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.scheduler.BukkitTask;

public class SQLWrite {

	private DataBukkit dab;
	private ConcurrentHashMap<Integer, String> buffer = new ConcurrentHashMap<Integer, String>();
	CopyOnWriteArrayList<String> writeStatements = new CopyOnWriteArrayList<String>();
	private BukkitTask writeTask;
	private boolean writeActive;
	private AtomicInteger bufferCounter = new AtomicInteger();
	private AtomicInteger processNext = new AtomicInteger();
	private DatabaseConnection database;

	public SQLWrite(DataBukkit dab) {
		this.dab = dab;
		bufferCounter.set(0);
		processNext.set(0);
		if (dab.useMySQL()) {
			database = new MySQLConnection(dab);
		} else {
			database = new SQLiteConnection(dab);
		}
		writeActive = false;
	}


	public void executeSQL(List<String> statements) {
		for (String statement : statements) {
			buffer.put(bufferCounter.getAndIncrement(), statement);
		}
		startWrite();
	}

	public void executeSQL(String statement) {
		buffer.put(bufferCounter.getAndIncrement(), statement);
		startWrite();
	}

	private void startWrite() {
		if (writeActive) {return;}
		writeActive = true;
		writeTask = dab.getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(dab.getPlugin(), new Runnable() {
			public void run() {
				if (database.inUse()) {return;}
				while (buffer.size() > 0) {
					writeStatements.add(buffer.get(processNext.get()));
					buffer.remove(processNext.getAndIncrement());
				}
				if (writeStatements.size() > 0) {
					database.aSyncWrite(getWriteStatements());
				}
				writeStatements.clear();
				if (buffer.size() == 0) {
					cancelWrite();
					return;
				}
			}
		}, 0L, 1L);
	}
	
	private CopyOnWriteArrayList<String> getWriteStatements() {
		CopyOnWriteArrayList<String> write = new CopyOnWriteArrayList<String>();
		for(String statement:writeStatements) {
			write.add(statement);
		}
		return write;
	}

	private void cancelWrite() {
		try {
			writeTask.cancel();
			writeActive = false;
		} catch (Exception e) {
			writeActive = false;
		}
	}


	public int getBufferSize() {
		return buffer.size();
	}

	public int getActiveThreads() {
		int activeThreads = 0;
		if (database.inUse()) {
			activeThreads++;
		}

		return activeThreads;
	}

	public ArrayList<String> getBuffer() {
		ArrayList<String> abuffer = new ArrayList<String>();
		for (String item : buffer.values()) {
			abuffer.add(item);
		}
		return abuffer;
	}

	public void shutDown() {
		cancelWrite();
		writeActive = true;

		List<String> statements = database.closeConnection();
		for (String statement : statements) {
			buffer.put(bufferCounter.getAndIncrement(), statement);
		}
		for (String statement : writeStatements) {
			buffer.put(bufferCounter.getAndIncrement(), statement);
		}
		saveBuffer();
	}


	private void saveBuffer() {
		if (buffer.size() > 0) {
			if (dab.useMySQL()) {
				database = new MySQLConnection(dab);
			} else {
				database = new SQLiteConnection(dab);
			}
			writeStatements.clear();
			for (String s:buffer.values()) {
				writeStatements.add(s);
			}
			database.syncWrite(getWriteStatements());
			buffer.clear();
			writeStatements.clear();
		}
	}

}
