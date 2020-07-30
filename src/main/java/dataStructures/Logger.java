package dataStructures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Logger {
	
	public enum level{
		INFO,
		WARNING,
		ERROR,
		FATAL
	}
	
	private File outputFile;
	private PrintWriter output;
	private String logName;
	private long messageCount;
	private boolean consoleOutput;
	
	public Logger() {
		this("latest");
	}
	
	public Logger(String loggerFileName) {
		consoleOutput = true;
		messageCount = 0L;
		logName = loggerFileName;
		// file name
		outputFile = new File("logs\\" + logName + ".txt");
		// if it already exists, rename it to something else
		if (outputFile.exists()) {
			String newName = "";
			try {
				// get date of log file creation
				Scanner input = new Scanner(outputFile);
				newName = input.nextLine().replace("Log created at ", "");
				newName = (logName.equals("latest") ? "log-" : logName + "-") + newName;
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			outputFile.renameTo(new File("logs\\" + newName + ".txt"));
		}

		// create file
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
		messageCount++;
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(" MM/dd HH:mm:ss ");
		
		String tabCount = logLevel == level.WARNING ? "\t" : "\t\t";
		
		String outputMessage = "[" + logLevel + "]" + sdf.format(currentDate) + tabCount + message;
		
		if(consoleOutput) System.out.println(outputMessage);
		
		output.println(outputMessage);
		output.flush();
	}
	
	public String getLogName() {
		return logName;
	}
	
	public long getMessageCount() {
		return messageCount;
	}
	
	public void setConsoleOutput(boolean cpo) {
		consoleOutput = cpo;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	public void close() {
		output.close();
	}
}
