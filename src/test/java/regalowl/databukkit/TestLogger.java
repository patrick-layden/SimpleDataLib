package regalowl.databukkit;

import regalowl.databukkit.event.EventHandler;
import regalowl.databukkit.events.LogEvent;

public class TestLogger {
	private DataBukkit db;
	public TestLogger(DataBukkit db) {
		this.db = db;
		db.registerListener(this);
	}
	
	@EventHandler
	public void onLogMessage(LogEvent event) {
		if (event.getMessage() != null) System.out.println(event.getMessage());
		if (event.getException() != null) System.out.println(db.getCommonFunctions().getErrorString(event.getException()));
	}
}