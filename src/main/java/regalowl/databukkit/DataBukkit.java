package regalowl.databukkit;

import java.io.File;

import regalowl.databukkit.event.EventPublisher;
import regalowl.databukkit.file.ErrorWriter;
import regalowl.databukkit.file.FileTools;
import regalowl.databukkit.file.YamlHandler;
import regalowl.databukkit.sql.SQLManager;

public class DataBukkit {

	private String name;
	private String storagePath;
	private EventPublisher ep;
	private YamlHandler yh;
	private FileTools ft;
	private ErrorWriter ew;
	private boolean debug;
	private SQLManager sm;
	private boolean shutdown;

	
	public DataBukkit(String name) {
		this.name = name;
	}
	
	public void initialize() {
		ft = new FileTools(this);
		this.storagePath = ft.getJarPath() + File.separator + name;
		ep = new EventPublisher();
		yh = new YamlHandler(this);
		ew = new ErrorWriter(getErrorFilePath(), this);
		sm = new SQLManager(this);
		shutdown = false;
		debug = false;
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

	
	public void registerListener(Object l) {
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
	
	//TODO move to errorwriter
	public void writeError(Exception e, String info) {
		ew.writeError(e, info, false);
	}
	public void writeError(Exception e) {
		ew.writeError(e, null, false);
	}
	public void writeError(String info) {
		ew.writeError(null, info, false);
	}
	

}
