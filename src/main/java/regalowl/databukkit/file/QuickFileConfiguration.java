package regalowl.databukkit.file;

import org.bukkit.configuration.file.FileConfiguration;

public class QuickFileConfiguration {
	private FileConfiguration fc;
	
	public QuickFileConfiguration(FileConfiguration fc) {
		this.fc = fc;
	}
	
	public FileConfiguration getFileConfiguration() {
		return fc;
	}
	
	public void set(String path, Object value) {
		fc.set(path, value);
	}
	
	public String gS(String path) {
		return fc.getString(path);
	}
	public int gI(String path) {
		return fc.getInt(path);
	}
	public double gD(String path) {
		return fc.getDouble(path);
	}
	public boolean gB(String path) {
		return fc.getBoolean(path);
	}
	
	public void sS(String path, String value) {
		fc.set(path, value);
	}
	public void sI(String path, int value) {
		fc.set(path, value);
	}
	public void sD(String path, double value) {
		fc.set(path, value);
	}
	public void sB(String path, boolean value) {
		fc.set(path, value);
	}
}
