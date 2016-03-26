package regalowl.simpledatalib.event;


import java.util.concurrent.CopyOnWriteArrayList;



public class EventPublisher {

	private CopyOnWriteArrayList<SDLEventListener> listeners = new CopyOnWriteArrayList<SDLEventListener>();
	
    public void registerListener(SDLEventListener listener) {
    	if (!listeners.contains(listener)) listeners.add(listener);
    }
    
    public void unRegisterListener(SDLEventListener listener) {
    	listeners.remove(listener);
    }
    
    public void unRegisterAllListeners() {
    	listeners.clear();
    }

	public SDLEvent fireEvent(SDLEvent event) {
		for (SDLEventListener listener:listeners) {
			listener.handleSDLEvent(event);
			event.setFiredSuccessfully();
		}
		return event;
    }
	
}
