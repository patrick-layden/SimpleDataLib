package regalowl.databukkit.events;

import regalowl.databukkit.event.Event;


public class LogEvent extends Event {
	private String message;
	private Exception e;
	private LogLevel level;
	
	public LogEvent(String message, Exception e, LogLevel level) {
		this.message = message;
		this.e = e;
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public Exception getException() {
		return e;
	}

	public LogLevel getLevel() {
		return level;
	}


}
