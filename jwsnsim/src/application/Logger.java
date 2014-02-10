package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger{

	BufferedWriter out = null;
	String logFile = null;
	
	public Logger(String logFile) {					
		createLogFile(logFile);
	}
		
	private void createLogFile(String logFile){		
		try {
			out = new BufferedWriter(new FileWriter(new File(logFile)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void log(String data){				
		try {
			out.append(data);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void close(){
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
