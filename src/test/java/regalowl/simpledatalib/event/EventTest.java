package regalowl.simpledatalib.event;


import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.event.Event;
import regalowl.simpledatalib.event.EventHandler;
import regalowl.simpledatalib.events.LogEvent;
import regalowl.simpledatalib.events.LogLevel;

public class EventTest {
	
	private SimpleDataLib db;
	private String errorMessage;
	
	@Before
	public void init() {
		db = new SimpleDataLib("test");
		db.initialize();
		db.setDebug(true);
		db.getEventPublisher().registerListener(this);
	}
	
	@Test
	public void testLogEvent() {
		Event e = db.getEventPublisher().fireEvent(new LogEvent("test", null, LogLevel.INFO));
		assertTrue(e.firedSuccessfully());
	}
	
	@EventHandler
	public void onLogEvent(LogEvent event) {
		errorMessage = event.getMessage();
	}
	
	@After
	public void after() {
		assertTrue(errorMessage.equals("test"));
	}
}
