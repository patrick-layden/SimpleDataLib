package regalowl.simpledatalib;

import regalowl.simpledatalib.CommonFunctions;
import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.event.SDLEvent;
import regalowl.simpledatalib.event.SDLEventListener;
import regalowl.simpledatalib.events.LogEvent;

public class TestLogger implements SDLEventListener {
	public TestLogger(SimpleDataLib db) {
		db.registerListener(this);
	}
	
	@Override
	public void handleSDLEvent(SDLEvent event) {
		if (event instanceof LogEvent) {
			LogEvent levent = (LogEvent)event;
			if (levent.getMessage() != null) System.out.println(levent.getMessage());
			if (levent.getException() != null) System.out.println(CommonFunctions.getErrorString(levent.getException()));
		}
	}
}