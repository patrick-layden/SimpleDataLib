package regalowl.databukkit.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import regalowl.databukkit.DataBukkit;

public class ErrorWriter {

	private DataBukkit dab;
	private String path;
	private String text;
	private String error;
	private Plugin plugin;

	public ErrorWriter(String path, DataBukkit dab) {
		this.dab = dab;
		this.path = path;
		this.plugin = dab.getPlugin();
	}
	
	public void writeError(Exception e, String text, boolean sync) {
		if (!plugin.isEnabled() && e != null && sync == false) {
			sync = true;
		}
		this.error = dab.getCommonFunctions().getErrorString(e);
		this.text = text;
		if (!sync) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {public void run() {write();}});
		} else {
			write();
		}
	}
	
	private void write() {
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.newLine();
			bw.newLine();
			bw.write(plugin.getName()+"["+plugin.getDescription().getVersion()+"], "+ Bukkit.getServer().getName()+
					"["+Bukkit.getServer().getVersion()+"], ["+dab.getCommonFunctions().getTimeStamp()+"]");
			bw.newLine();
			if (text != null) {
				bw.write(String.format(text));
				bw.newLine();
			}
			if (error != null) {bw.write(String.format(error));}
			bw.newLine();
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
