package regalowl.simpledatalib.file;



import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import regalowl.simpledatalib.SimpleDataLib;


public class YamlHandler {
    private SimpleDataLib sdl;
    private Timer timer;
    private Long saveInterval;
    private ArrayList<String> brokenFiles = new ArrayList<String>();
    private ConcurrentHashMap<String, FileConfiguration> configs = new ConcurrentHashMap<String, FileConfiguration>();
    
    public YamlHandler(SimpleDataLib sdl) {
    	this.sdl = sdl;
    }

    public void registerFileConfiguration(String file) {
    	File configFile = new File(sdl.getStoragePath(), file + ".yml");
    	checkFile(configFile);
    	FileConfiguration fileConfiguration = new FileConfiguration(sdl, configFile);
    	fileConfiguration.load();
    	configs.put(file, fileConfiguration);
    }
    
    public void unRegisterFileConfiguration(String file) {
    	if (configs.containsKey(file)) {
    		saveYaml(file);
    		configs.remove(file);
    	}
    }
    
	public void saveYaml(String fileConfiguration){
		try {
			if (configs.containsKey(fileConfiguration) && !brokenFiles.contains(fileConfiguration)) {
				FileConfiguration saveFile = configs.get(fileConfiguration);
				saveFile.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	public void saveYamls() {
		for (String key:configs.keySet()) {
			saveYaml(key);
		}
    }

	public FileConfiguration getFileConfiguration(String fileConfiguration){
		if (configs.containsKey(fileConfiguration)) {
			return configs.get(fileConfiguration);
		} else {
			return null;
		}
	}
	public FileConfiguration gFC(String fileConfiguration){
		if (configs.containsKey(fileConfiguration)) {
			return configs.get(fileConfiguration);
		} else {
			return null;
		}
	}
	
	/**
	 * @param interval in milliseconds
	 */
	public void startSaveTask(long interval) {
		this.saveInterval = interval;
		if (timer != null) {timer.cancel();}
		timer = new Timer();
		timer.schedule(new SaveTask(), saveInterval, saveInterval);
	}
	public void stopSaveTask() {
		if (timer != null) {timer.cancel();}
	}
	private class SaveTask extends TimerTask {
		@Override
		public void run() {
			saveYamls();
		}
	}
	
	public long getSaveInterval() {
		return saveInterval;
	}
	
	public void shutDown() {
		stopSaveTask();
		saveYamls();
	}

	public void copyFromJar(String name) {
		File configFile = new File(sdl.getStoragePath(), name + ".yml");
	    if(!configFile.exists()){
	    	configFile.getParentFile().mkdirs();
	        sdl.getFileTools().copyFileFromJar(name+".yml", sdl.getStoragePath() + File.separator + name + ".yml");
	    }
	}

	private void checkFile(File file) {
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public boolean brokenFile() {
    	if (brokenFiles.size() > 0) {
    		return true;
    	}
    	return false;
    }
    

}
