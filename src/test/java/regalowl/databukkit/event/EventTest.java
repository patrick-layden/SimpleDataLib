package regalowl.databukkit.event;


import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import regalowl.databukkit.DataBukkit;
import regalowl.databukkit.events.LogEvent;
import regalowl.databukkit.events.LogLevel;

public class EventTest {
	
	private DataBukkit db;
	private String errorMessage;
	
	@Before
	public void init() {
		db = new DataBukkit("test");
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
