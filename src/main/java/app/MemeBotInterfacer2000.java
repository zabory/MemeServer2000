package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONException;
import org.json.JSONObject;

import dataStructures.MemeBotMsg2000;

/**
 * An interfacer between the memeBot and the main thread.
 * Also contains and hosts the process for the bot.
 * @author Ben Shabowski
 * @version 2000
 * @since 2000
 */
public class MemeBotInterfacer2000 {
	
	//input from main thread
	private Queue<MemeBotMsg2000> input;
	//output to main thread
	private Queue<MemeBotMsg2000> output;
	
	//input from bot
	private BufferedReader botInput;
	//error input from bot
	private BufferedReader botErrorInput;
	//output to bot
	private BufferedWriter botOutput;
	
	//actual bot
	private MemeBot2000 bot;
	
	public MemeBotInterfacer2000(LinkedBlockingQueue<MemeBotMsg2000> input) {
		this.input = input;
		output = new LinkedBlockingQueue<MemeBotMsg2000>();
		
		try {
			//launch bot
			bot = new MemeBot2000();
			
			//get bot streams
			botInput = bot.getBotOutput();
			botErrorInput = bot.getBotErrorOutput();
			botOutput = bot.getBotConsoleInput();
			
			//start all threads
			new BotInputThread().start();
			new ErrorInputThread().start();
			new BotOutputThread().start();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not start bot, exiting program very ungracefully");
			System.exit(1);
		}
	}
	
	/**
	 * Kills the bot
	 */
	public void killBot() {
		bot.kill();
	}
	
	/**
	 * Class to handle out to the bot's input thread
	 * @author Ben Shabowski
	 * @version 2000
	 * @since 2000
	 */
	private class BotOutputThread extends Thread {
		public void run() {
			while(true) {
				try {
					if(input.size() > 0) {
						botOutput.write(input.poll().toJSON().toString() + "\n");
						botOutput.flush();
					}
				} catch (IOException e) {
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
					output.add(new MemeBotMsg2000(new JSONObject(botInput.readLine())));
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
			String error = "";
			String input = "";
			while(input != null) {
				try {
					input = botErrorInput.readLine();
					if(input != null) {
						error += input + "\n";
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			output.add(new MemeBotMsg2000().body("ERROR:\n" + error));
		}
	}

	public Queue<MemeBotMsg2000> getOutput() {
		return output;
	}
	
	
	
	
}