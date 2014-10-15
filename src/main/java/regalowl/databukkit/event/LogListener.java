package regalowl.databukkit.event;

public interface LogListener extends Listener {
	public void onLogMessage(String entry, Exception e, LogLevel level);
}
