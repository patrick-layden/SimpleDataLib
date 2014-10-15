package regalowl.databukkit.event;

import java.util.concurrent.CopyOnWriteArrayList;


public class EventHandler {

	private CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	
    public synchronized void registerListener(Listener listener) {
    	listeners.add(listener);
    }
    public synchronized void unRegisterListener(Listener listener) {
    	if (listeners.contains(listener)) {
    		listeners.remove(listener);
    	}
    }
    
	public void fireDisableEvent() {
		for (Listener listener : listeners) {
			if (listener instanceof DisableRequestListener) {
				DisableRequestListener l = (DisableRequestListener) listener;
				l.onDisable();
			}
		}
	}
	
	public void fireLogEvent(String entry, Exception e, LogLevel level) {
		for (Listener listener : listeners) {
			if (listener instanceof LogListener) {
				LogListener l = (LogListener) listener;
				l.onLogMessage(entry, e, level);
			}
		}
	}
	
}
