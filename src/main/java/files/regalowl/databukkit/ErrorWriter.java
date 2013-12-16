package regalowl.databukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.bukkit.plugin.Plugin;

public class ErrorWriter {


	
	private String path;
	private String text;
	private String error;
	private Plugin plugin;

	
	ErrorWriter(Exception e, String text, String path, Plugin pl, boolean sync) {
		try {
			this.error = getErrorString(e);
			this.text = text;
			this.path = path;
			this.plugin = pl;
			if (path == null) {return;}
			if (!sync) {
				plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
					public void run() {
						write();
					}
				}, 0L);
			} else {
				write();
			}
		} catch (Exception e2) {
			plugin.getLogger().info("Tried to log error to file but was unable.  The stacktrace for the logged error is as follows: " + getErrorString(e));
		}
	}
	
	
	public void write() {
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.newLine();
			bw.newLine();
			if (text != null) {bw.write(text);}
			bw.newLine();
			if (error != null) {bw.write(error);}
			bw.newLine();
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getErrorString(Exception e) {
		if (e == null) {return null;}
		StringWriter error = new StringWriter();
		e.printStackTrace(new PrintWriter(error));
		return error.toString();
	}
	

}
