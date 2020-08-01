package datastructures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class MemeLogger3000 {
	
	public enum level{
		INFO,		// Info about whats happening
		WARNING,	// May have detected an issue
		ERROR,		// An issue has occurred, but system stays alive
		FATAL		// An issue has occurred and the system will have to restart or crash
	}
	
	private File outputFile;
	private PrintWriter output;
	private String logName;
	private long messageCount;
	private boolean consoleOutput;
	
	/**
	 * Creates a logger with file name of 'latest'
	 */
	public MemeLogger3000() {
		this("latest");
	}
	
	/**
	 * Creates a logger with a specific file name
	 * @param loggerFileName
	 */
	public MemeLogger3000(String loggerFileName) {
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
	
	/**
	 * Prints a message with level INFO
	 * @param message
	 */
	public void println(String message) {
		println(level.INFO, message);
	}
	
	/**
	 * Prints a message with a specific log level
	 * @param logLevel
	 * @param message
	 */
	public void println(level logLevel, String message) {
		String classTag = "";
		int spaceFactor = 24;
		for(StackTraceElement element : Thread.currentThread().getStackTrace()){
			if(!element.getClassName().equals(Thread.currentThread().getStackTrace()[0].getClassName())) {
				String className = element.getClassName().replaceAll(".*\\.", "");
				classTag = " ".repeat((spaceFactor - className.length())%2==0 ? (spaceFactor - className.length())/2 : ((spaceFactor - className.length())/2)+1)
						+ className + " ".repeat((spaceFactor - className.length())/2);
			}
		}

		messageCount++;
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(" MM/dd HH:mm:ss ");
		
		String tabCount = logLevel == level.WARNING ? "\t" : "\t\t";
		
		String outputMessage = "[ " + logLevel + " ] "
							+ "[ " + classTag + " ] "
							+ "[" + sdf.format(currentDate) + "]"
							+ tabCount + message;
		
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
	
	/**
	 * Closes output stream to file
	 */
	public void close() {
		output.close();
	}
}
