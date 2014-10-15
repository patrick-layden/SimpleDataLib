package regalowl.databukkit.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import regalowl.databukkit.DataBukkit;

public class ErrorWriter {

	private DataBukkit dab;
	private String path;
	private String text;
	private String error;

	public ErrorWriter(String path, DataBukkit dab) {
		this.dab = dab;
		this.path = path;
	}
	
	public void writeError(Exception e, String text, boolean sync) {
		this.error = dab.getCommonFunctions().getErrorString(e);
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
			bw.write(dab.getName()+ "["+dab.getCommonFunctions().getTimeStamp()+"]");
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
	
	private class Writer implements Runnable {
		@Override
		public void run() {
			write();
		}
	}
	
}
