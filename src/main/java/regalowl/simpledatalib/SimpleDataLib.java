package regalowl.simpledatalib;

import java.io.File;

import regalowl.simpledatalib.event.EventPublisher;
import regalowl.simpledatalib.event.SDLEventListener;
import regalowl.simpledatalib.file.ErrorWriter;
import regalowl.simpledatalib.file.FileTools;
import regalowl.simpledatalib.file.YamlHandler;
import regalowl.simpledatalib.sql.SQLManager;

public class SimpleDataLib {

	private EventPublisher ep;
	private YamlHandler yh;
	private FileTools ft;
	private ErrorWriter ew;
	private SQLManager sm;
	
	private String name;
	private String storagePath;
	private boolean shutdown;
	private boolean debug;
	
	public SimpleDataLib(String name) {
		this.name = name;
		ft = new FileTools(this);
		this.storagePath = ft.getJarPath() + File.separator + name;
		ft.makeFolder(storagePath);
		ep = new EventPublisher();
		yh = new YamlHandler(this);
		ew = new ErrorWriter(getErrorFilePath(), this);
		shutdown = false;
		debug = false;
	}
	
	public void initialize() {
		sm = new SQLManager(this);
	}
	

	public void shutDown() {
		if (!shutdown) {
			sm.shutDown();
			if (yh != null) {yh.shutDown();}
			shutdown = true;
		}
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}
	public String getName() {
		return name;
	}
	public boolean debugEnabled() {
		return debug;
	}
	public boolean isDisabled() {
		return shutdown;
	}
	public String getStoragePath() {
		return storagePath;
	}
	public String getErrorFilePath() {
		return storagePath + File.separator + "errors.log";
	}

	
	public void registerListener(SDLEventListener l) {
		ep.registerListener(l);
	}
	public void setDebug(boolean state) {
		this.debug = state;
	}


	public SQLManager getSQLManager() {
		return sm;
	}
	public YamlHandler getYamlHandler() {
		return yh;
	}
	public FileTools getFileTools() {
		return ft;
	}
	public ErrorWriter getErrorWriter() {
		return ew;
	}
	public EventPublisher getEventPublisher() {
		return ep;
	}


}
