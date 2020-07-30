package app;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MemeBot2000 {
	
	private BufferedReader input;
	private BufferedReader error;
	private BufferedWriter output;
	
	private Process bot;
	
	public MemeBot2000() throws IOException {
		
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("cmd.exe", "/c", "node MemeBot2000.js");
		
		pb.directory(new File("src\\main\\resources\\bot"));
		
		bot = pb.start();
		
		//Sleep thread, not like the server can do anything while the bot is booting up and connecting anywas
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		input =  new BufferedReader(new InputStreamReader(bot.getInputStream()));
		error = new BufferedReader(new InputStreamReader(bot.getErrorStream()));
		output = new BufferedWriter(new OutputStreamWriter(bot.getOutputStream()));

	}
	
	/**
	 * Kill the bot process
	 */
	public void kill() {
		bot.destroyForcibly();
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
