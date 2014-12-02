package regalowl.simpledatalib.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;





import regalowl.simpledatalib.CommonFunctions;
import regalowl.simpledatalib.SimpleDataLib;

public class ErrorWriter {

	private SimpleDataLib sdl;
	private String path;
	private String text;
	private String error;

	public ErrorWriter(String path, SimpleDataLib sdl) {
		this.sdl = sdl;
		this.path = path;
	}
	
	
	public void writeError(Exception e, String info) {
		writeError(e, info, false);
	}
	public void writeError(Exception e) {
		writeError(e, null, false);
	}
	public void writeError(String info) {
		writeError(null, info, false);
	}	
	public void writeError(Exception e, String text, boolean sync) {
		this.error = CommonFunctions.getErrorString(e);
		this.text = text;
		if (!sync) {
			new Thread(new Writer()).start();
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
			bw.write(sdl.getName()+ "["+CommonFunctions.getTimeStamp()+"]");
			bw.newLine();
			if (text != null) {
				bw.write(text.replace("{{newline}}", System.getProperty("line.separator")));
				bw.newLine();
			}
			if (error != null) {bw.write(error);}
			bw.newLine();
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class Writer implements Runnable {
		@Override
		public void run() {
			write();
		}
	}
	
}
