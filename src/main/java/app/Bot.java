package app;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
		
		//TODO we need to find the dir of memebot
		pb.directory(new File("src\\main\\resources\\bot"));
		
		Process botProcess = pb.start();
		
		input =  new BufferedReader(new InputStreamReader(botProcess.getInputStream()));
		error = new BufferedReader(new InputStreamReader(botProcess.getErrorStream()));
		output = new BufferedWriter(new OutputStreamWriter(botProcess.getOutputStream()));


	}

	/**
	 *
	 * @return console output of the bot as an inputStream
	 */
	public BufferedReader getBotOutput() {
		return input;
	}

	/**
	 * 
	 * @return error output of the bot as an inputstream
	 */
	public BufferedReader getBotErrorOutput() {
		return error;
	}

	/**
	 * 
	 * @return console input of the bot, used to write to the bots console
	 */
	public BufferedWriter getBotConsoleInput() {
		return output;
	}

}
