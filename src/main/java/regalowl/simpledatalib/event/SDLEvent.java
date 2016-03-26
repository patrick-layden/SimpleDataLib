package regalowl.simpledatalib.event;

public class SDLEvent {
	private boolean cancelled;
	private boolean firedSuccessfully;
	
	public SDLEvent() {
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
