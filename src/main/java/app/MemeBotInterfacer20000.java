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
 * An interfacer between the memeBot and the main thread
 * @author Ben Shabowski
 * @version 2000
 * @since 2000
 */
public class MemeBotInterfacer20000 extends Thread {
	
	private Queue<MemeBotMsg2000> input = new LinkedBlockingQueue<MemeBotMsg2000>();
	private Queue<MemeBotMsg2000> output = new LinkedBlockingQueue<MemeBotMsg2000>();
	
	private BufferedReader botInput;
	private BufferedReader botErrorInput;
	private BufferedWriter botOutput;
	
	public MemeBotInterfacer20000() {
		try {
			//launch bot
			MemeBot2000 bot = new MemeBot2000();
			
			//get bot streams
			botInput = bot.getBotOutput();
			botErrorInput = bot.getBotErrorOutput();
			botOutput = bot.getBotConsoleInput();
			
			//start input bot stream thread
			new inputThread().start();
			new errorInputThread().start();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not start bot, exiting program very ungracefully");
			System.exit(1);
		}
	}
	
	
	public void run() {
		
	}
	
	public void addToOutput(MemeBotMsg2000 message) {
		output.add(message);
	}
	
	public void sendToBot(MemeBotMsg2000 message) {
		try {
			botOutput.write(message.toString());
			botOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles any error coming from the bot
	 * @param error Error message from the bot
	 */
	private void handleError(String error) {
		
	}
	
	private class inputThread extends Thread {
		public void run() {
			while (true) {
				try {
					input.add(new MemeBotMsg2000(new JSONObject(botInput.readLine())));
				} catch (JSONException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class errorInputThread extends Thread{
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
			
			handleError(error);
		}
	}
}
