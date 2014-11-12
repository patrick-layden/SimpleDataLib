package regalowl.simpledatalib.event;

public class Event {
	private boolean cancelled;
	private boolean firedSuccessfully;
	
	public Event() {
		cancelled = false;
		firedSuccessfully = false;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void cancel() {
		this.cancelled = true;
	}
	
	public boolean firedSuccessfully() {
		return firedSuccessfully;
	}
	
	public void setFiredSuccessfully() {
		firedSuccessfully = true;
	}
}
