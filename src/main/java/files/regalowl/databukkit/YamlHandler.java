package regalowl.databukkit;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class YamlHandler {
    private Logger log;
    private Plugin plugin;
    private BukkitTask saveTask;
    private Long saveInterval;
    private String currentFC;
    private ArrayList<String> brokenFiles = new ArrayList<String>();
    private ConcurrentHashMap<String, FileConfiguration> configs = new ConcurrentHashMap<String, FileConfiguration>();
    private ConcurrentHashMap<String, File> files = new ConcurrentHashMap<String, File>();
    
    YamlHandler(Plugin plugin) {
    	this.plugin = plugin;
    	log = Logger.getLogger("Minecraft");
    }

    public void registerFileConfiguration(String file) {
    	File configFile = new File(plugin.getDataFolder(), file + ".yml");
    	files.put(file, configFile);
    	checkFile(configFile);
    	FileConfiguration fileConfiguration = new YamlConfiguration();
    	loadFile(configFile, fileConfiguration);
    	configs.put(file, fileConfiguration);
    }
    
    public void unRegisterFileConfiguration(String file) {
    	if (configs.containsKey(file)) {
    		saveYaml(file);
    		configs.remove(file);
    		files.remove(file);
    	}
    }
    
	public void saveYaml(String fileConfiguration){
		try {
			if (configs.containsKey(fileConfiguration) && !brokenFiles.contains(configs.get(fileConfiguration).getName())) {
				FileConfiguration saveFile = configs.get(fileConfiguration);
				saveFile.save(files.get(fileConfiguration));
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
	
	
	public void startSaveTask(long interval) {
		this.saveInterval = interval;
		if (saveTask != null) {saveTask.cancel();}
		saveTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				saveYamls();
			}
		}, saveInterval, saveInterval);
	}
	
	public void stopSaveTask() {
		if (saveTask != null) {saveTask.cancel();}
	}
	
	public long getSaveInterval() {
		return saveInterval;
	}
	
	public void shutDown() {
		stopSaveTask();
		saveYamls();
	}

	public void registerDefault(String path, Object def) {
		if (!gFC(currentFC).isSet(path)) {
			gFC(currentFC).set(path, def);
			try {
				gFC(currentFC).save(files.get(currentFC));
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}
	public void set(String path, Object value) {
		gFC(currentFC).set(path, value);
	}
	public void setCurrentFileConfiguration(String fileConfiguration) {
		if (getFileConfiguration(fileConfiguration) != null) {
			currentFC = fileConfiguration;
		} else {
			currentFC = null;
		}
	}
	public String gS(String path) {
		if (currentFC == null) {return null;}
		return gFC(currentFC).getString(path);
	}
	public int gI(String path) {
		if (currentFC == null) {return -1;}
		return gFC(currentFC).getInt(path);
	}
	public double gD(String path) {
		if (currentFC == null) {return -1;}
		return gFC(currentFC).getDouble(path);
	}
	public boolean gB(String path) {
		if (currentFC == null) {return false;}
		return gFC(currentFC).getBoolean(path);
	}
	
	public void sS(String path, String value) {
		if (currentFC == null) {return;}
		gFC(currentFC).set(path, value);
	}
	public void sI(String path, int value) {
		if (currentFC == null) {return;}
		gFC(currentFC).set(path, value);
	}
	public void sD(String path, double value) {
		if (currentFC == null) {return;}
		gFC(currentFC).set(path, value);
	}
	public void sB(String path, boolean value) {
		if (currentFC == null) {return;}
		gFC(currentFC).set(path, value);
	}
	
	public QuickFileConfiguration getQuickFileConfiguration(String name) {
		FileConfiguration fc = gFC(name);
		if (fc != null) {
			return new QuickFileConfiguration(fc);
		} else {
			return null;
		}
	}
	public QuickFileConfiguration gQFC(String name) {
		return getQuickFileConfiguration(name);
	}
	
	
	public void copyFromJar(String name) {
		File configFile = new File(plugin.getDataFolder(), name + ".yml");
	    if(!configFile.exists()){
	    	configFile.getParentFile().mkdirs();
	        copy(plugin.getClass().getResourceAsStream("/"+name+".yml"), configFile);
	    }
	}
	
	public void deleteConfigFile(String name) {
		try {
			File configFile = new File(plugin.getDataFolder(), name + ".yml");
			configFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
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


    private void loadFile(File file, FileConfiguration fileConfiguration) {
        try {
        	fileConfiguration.load(file);
        } catch (Exception e) {
        	brokenFiles.add(fileConfiguration.getName());
            e.printStackTrace();
	    	log.severe("[DataBukkit["+plugin.getName()+"]]Bad "+file.getName()+" file.");
        }
    }
    
    public boolean brokenFile() {
    	if (brokenFiles.size() > 0) {
    		return true;
    	}
    	return false;
    }

}
