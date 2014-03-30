package regalowl.databukkit;


import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import regalowl.databukkit.file.YamlHandler;


public class DataBukkitPlugin extends JavaPlugin {
	public static DataBukkitPlugin dataBukkit;
	private ArrayList<DataBukkit> dataBukkits = new ArrayList<DataBukkit>();
	private YamlHandler y;
	private FileConfiguration config;
	private boolean useMySql;

	@Override
	public void onEnable() {
		dataBukkit = this;
		y = new YamlHandler(this);
		y.registerFileConfiguration("config");
		config = y.getFileConfiguration("config");
		boolean save = false;
		if (!config.isSet("use-mysql")) {config.set("use-mysql", false);save=true;};
		if (!config.isSet("host")) {config.set("host", "localhost");save=true;};
		if (!config.isSet("database")) {config.set("database", "not_set");save=true;};
		if (!config.isSet("username")) {config.set("username", "not_set");save=true;};
		if (!config.isSet("password")) {config.set("password", "not_set");save=true;};
		if (!config.isSet("port")) {config.set("port", 3306);save=true;};
		if (save) {y.saveYamls();}
		useMySql = config.getBoolean("use-mysql");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
		for (int i=0; i < dataBukkits.size(); i++) {
			dataBukkits.get(i).shutDown();
		}
	}


	public FileConfiguration config() {
		return config;
	}
	
	public DataBukkit getDataBukkit(Plugin plugin) {
		DataBukkit db = new DataBukkit(plugin);
		dataBukkits.add(db);
		if (useMySql) {
			y.setCurrentFileConfiguration("config");
			db.enableMySQL(y.gS("host"), y.gS("database"), y.gS("username"), y.gS("password"), y.gI("port"));
		}
		return db;
	}


	

}
