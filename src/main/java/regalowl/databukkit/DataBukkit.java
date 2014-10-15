package regalowl.databukkit;

import java.io.File;

import regalowl.databukkit.event.EventHandler;
import regalowl.databukkit.event.Listener;
import regalowl.databukkit.file.ErrorWriter;
import regalowl.databukkit.file.FileTools;
import regalowl.databukkit.file.YamlHandler;
import regalowl.databukkit.sql.SQLManager;

public class DataBukkit {

	private String name;
	private String storagePath;
	private EventHandler eh;
	private YamlHandler yh;
	private CommonFunctions cf;
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
		eh = new EventHandler();
		yh = new YamlHandler(this);
		cf = new CommonFunctions();
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

	
	public void registerListener(Listener l) {
		eh.registerListener(l);
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
	public CommonFunctions getCommonFunctions() {
		return cf;
	}
	public FileTools getFileTools() {
		return ft;
	}
	public ErrorWriter getErrorWriter() {
		return ew;
	}
	public EventHandler getEventHandler() {
		return eh;
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
