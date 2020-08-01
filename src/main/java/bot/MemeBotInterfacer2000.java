package bot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import app.MemeConfigLoader3000;
import org.json.JSONException;
import org.json.JSONObject;

import datastructures.MemeBotMsg2000;

/**
 * An interfacer between the memeBot and the main thread.
 * Also contains and hosts the process for the bot.
 * @author Ben Shabowski
 * @version 2000
 * @since 2000
 */
public class MemeBotInterfacer2000 {
	
	//input from main thread
	private BlockingQueue<MemeBotMsg2000> input;
	//output to main thread
	private BlockingQueue<MemeBotMsg2000> output;
	
	//input from bot
	private BufferedReader botInput;
	//error input from bot
	private BufferedReader botErrorInput;
	//output to bot
	private BufferedWriter botOutput;
	
	//actual bot
	private MemeBot2000 bot;
	
	public MemeBotInterfacer2000(MemeConfigLoader3000 config, BlockingQueue<MemeBotMsg2000> botInputQ, BlockingQueue<MemeBotMsg2000> botOutputQ) {
				
		this.input = botInputQ;
		this.output = botOutputQ;
		
		//launch bot
		bot = new MemeBot2000(config);
		
		//get bot streams
		botInput = bot.getBotOutput();
		botErrorInput = bot.getBotErrorOutput();
		botOutput = bot.getBotConsoleInput();
		
		//start all threads
		new BotInputThread().start();
		new ErrorInputThread().start();
		new BotOutputThread().start();
	}

	
	/**
	 * Kills the bot
	 */
	public void killBot() {
		bot.kill();
	}
	
	/**
	 * Class to handle out to the bot's input thread
	 * 
	 * @author Ben Shabowski
	 * @version 2000
	 * @since 2000
	 */
	private class BotOutputThread extends Thread {
		public void run() {
			while (true) {
				try {
					Thread.sleep(250);
					String in = input.take().toJSON().toString() + "\n";
					
					botOutput.write(in);
					botOutput.flush();

				} catch (IOException | InterruptedException e) {
					System.out.println(e);
				}
			}
		}
	}
	
	/**
	 * Class to handle input from the bot's output thread. 
	 * @author Ben Shabowski
	 * @version 2000
	 * @since 2000
	 */
	private class BotInputThread extends Thread {
		public void run() {
			while (true) {
				try {
					String input = botInput.readLine();					
					JSONObject in;
					
					if(input.charAt(0) == '{') {
						in = new JSONObject(input);
					}else {
						in = new JSONObject();
						in.put("body", input);
						in.put("command", "print");
					}
					
					output.add(new MemeBotMsg2000(in));
				} catch (JSONException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Class to handle input from the bot's error output thread
	 * @author Ben Shabowski
	 * @version 2000
	 * @since 2000
	 */
	private class ErrorInputThread extends Thread{
		public void run() {
			String input = "";
			while(input != null) {
				try {
					input = botErrorInput.readLine();
					System.out.println(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Queue<MemeBotMsg2000> getOutput() {
		return output;
	}
	
	
	
	
}
