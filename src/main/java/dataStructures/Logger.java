package dataStructures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Logger {
	
	enum level{
		INFO,
		WARNING,
		ERROR,
		FATAL
	}
	
	private File outputFile;
	private PrintWriter output;
	
	public Logger() {
		//file name
		outputFile = new File("logs\\currentLog.txt");
		//if it already exists, rename it to something else
		if(outputFile.exists()) {
			String logFileCreationDate = "";
			try {
				//get date of log file creation
				Scanner input = new Scanner(outputFile);
				logFileCreationDate = input.nextLine().replace("Log created at ", "");
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			outputFile.renameTo(new File("logs\\" + logFileCreationDate + ".txt"));
		}
		
		//create file
		try {
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			output = new PrintWriter(outputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-HH-mm");
		
		output.println("Log created at " + sdf.format(currentDate));
		output.flush();
		
	}	
	
	
	public void println(String message) {
		println(level.INFO, message);
	}
	
	public void println(level logLevel, String message) {
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(" MM/dd HH:mm:ss ");
		String outputMessage = "[" + logLevel + "]" + sdf.format(currentDate) + "\t" + message;
		
		System.out.println(outputMessage);
		
		output.println(outputMessage);
		output.flush();
	}
}
