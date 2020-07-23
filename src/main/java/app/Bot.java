package app;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Bot {
	
	private BufferedReader input;
	private BufferedReader error;
	private BufferedWriter output;
	
	public Bot() throws IOException {
		
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("cmd.exe", "/c", "node MemeBot2000.js");
		
		Process botProcess = pb.start();
		
		input =  new BufferedReader(new InputStreamReader(botProcess.getInputStream()));
		error = new BufferedReader(new InputStreamReader(botProcess.getErrorStream()));
		output = new BufferedWriter(new OutputStreamWriter(botProcess.getOutputStream()));
	}

	/**
	 *
	 * @return console output of the bot as an inputStream
	 */
	public BufferedReader getInput() {
		return input;
	}

	/**
	 * 
	 * @return error output of the bot as an inputstream
	 */
	public BufferedReader getError() {
		return error;
	}

	/**
	 * 
	 * @return console input of the bot, used to write to the bots console
	 */
	public BufferedWriter getOutput() {
		return output;
	}

}
