package regalowl.simpledatalib;

import regalowl.simpledatalib.CommonFunctions;
import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.event.EventHandler;
import regalowl.simpledatalib.events.LogEvent;

public class TestLogger {
	public TestLogger(SimpleDataLib db) {
		db.registerListener(this);
	}
	
	@EventHandler
	public void onLogMessage(LogEvent event) {
		if (event.getMessage() != null) System.out.println(event.getMessage());
		if (event.getException() != null) System.out.println(CommonFunctions.getErrorString(event.getException()));
	}
}