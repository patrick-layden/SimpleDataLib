package regalowl.databukkit;

import regalowl.databukkit.event.LogLevel;
import regalowl.databukkit.event.LogListener;

public class TestLogger implements LogListener {
	private DataBukkit db;
	public TestLogger(DataBukkit db) {
		this.db = db;
		db.registerListener(this);
	}
	@Override
	public void onLogMessage(String entry, Exception e, LogLevel level) {
		if (entry != null) System.out.println(entry);
		if (e != null) System.out.println(db.getCommonFunctions().getErrorString(e));
	}
}