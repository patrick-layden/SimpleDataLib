package regalowl.databukkit.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import regalowl.databukkit.DataBukkit;
import regalowl.databukkit.events.LogEvent;
import regalowl.databukkit.events.LogLevel;

public class FileConfiguration {
	
	private DataBukkit db;
	private ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<String, Object>();
	private Yaml yaml;
	private File file;
	private boolean broken;
	
    public FileConfiguration(DataBukkit db, File file) {
    	this.db = db;
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(new SafeConstructor(), new Representer(), options);
        this.file = file;
        broken = false;
    }
    
    
	@SuppressWarnings("unchecked")
	public void load() {
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			Object input = yaml.load(new UnicodeReader(stream));
			if (input != null) {
				HashMap<String, Object> iData = (HashMap<String, Object>) input;
				data.putAll(iData);
			}
		} catch (Exception e) {
			broken = true;
			db.getEventPublisher().fireEvent(new LogEvent("", e, LogLevel.ERROR));
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {}
		}
	}

	public void save() {
		if (broken) return;
		FileOutputStream stream = null;
		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		try {
			stream = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
			HashMap<String, Object> oData = new HashMap<String, Object>();
			oData.putAll(data);
			yaml.dump(oData, writer);
		} catch (Exception e) {
			db.getEventPublisher().fireEvent(new LogEvent("", e, LogLevel.ERROR));
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {}
		}
	}
	
	public Set<String> getKeys() {
		//TODO
		return null;
	}
	
	
	public Set<String> getTopLevelKeys() {
		return data.keySet();
	}
	
	public String getName() {
		return file.getName();
	}
	
	@SuppressWarnings("unchecked")
	private Object getData(String key) {
		if (key == null || key.equals("")) return null;
		if (key.contains(".")) {
			String[] keys = key.split(Pattern.quote("."));
			if (!data.containsKey(keys[0])) return null;
			HashMap<String, Object> nestedData = (HashMap<String, Object>) data.get(keys[0]);
			Object o = null;
			for (int i = 1; i < keys.length; i++) {
				o = nestedData.get(keys[i]);
				if (o instanceof HashMap) {
					nestedData = (HashMap<String, Object>)o;
				}
			}
			return o;
		} else {
			return data.get(key);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void set(String key, Object value) {
		if (key == null || key.equals("")) return;
		if (key.contains(".")) {
			String[] keys = key.split(Pattern.quote("."));
			if (!data.containsKey(keys[0])) return;
			HashMap<String, Object> nestedData = (HashMap<String, Object>) data.get(keys[0]);
			Object o = null;
			for (int i = 1; i < keys.length; i++) {
				o = nestedData.get(keys[i]);
				if (o instanceof HashMap) {
					nestedData = (HashMap<String, Object>)o;
				}
			}
			if (value == null) {
				nestedData.remove(keys[keys.length-1]);
			} else {
				nestedData.put(keys[keys.length-1], value);
			}
		} else {
			if (value == null) {
				data.remove(key);
			} else {
				data.put(key, value);
			}
		}
	}
	
	public boolean isSet(String path) {
		if (getData(path) != null) return true;
		return false;
	}
	
	public String getString(String path) {
		if (getData(path) instanceof String) {
			return (String) getData(path);
		}
		return null;
	}
	
	public synchronized int getInt(String path) {
		return convertInteger(getData(path));
	}
	
	public long getLong(String path) {
		return convertLong(getData(path));
	}
	
	public double getDouble(String path) {
		return convertDouble(getData(path));
	}
	
	public boolean getBoolean(String path) {
		Object o = getData(path);
		if (o == null) {
			return false;
		} else if (o instanceof Boolean) {
			return (Boolean)o;
		} else {
			return false;
		}
	}
	
	
	private Integer convertInteger(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Byte) {
			return (int)(Byte)o;
		} else if (o instanceof Integer) {
			return (Integer)o;
		} else if (o instanceof Double) {
			return (int)(double)(Double)o;
		} else if (o instanceof Float) {
			return (int)(float)(Float)o;
		} else if (o instanceof Long) {
			return (int)(long)(Long)o;
		} else {
			return null;
		}
	}

	private Double convertDouble(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Float) {
			return (double)(Float)o;
		} else if (o instanceof Double) {
			return (Double)o;
		} else if (o instanceof Byte) {
			return (double)(Byte)o;
		} else if (o instanceof Integer) {
			return (double)(Integer)o;
		} else if (o instanceof Long) {
			return (double)(Long)o;
		} else {
			return null;
		}
	}
	
	private Long convertLong(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Float) {
			return (long)(float)(Float)o;
		} else if (o instanceof Double) {
			return (long)(double)(Double)o;
		} else if (o instanceof Byte) {
			return (long)(Byte)o;
		} else if (o instanceof Integer) {
			return (long)(Integer)o;
		} else if (o instanceof Long) {
			return (long)(Long)o;
		} else {
			return null;
		}
	}

}
