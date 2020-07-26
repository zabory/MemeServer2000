package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class MemeServer2000 {

	//this class will act as the controller
	// create a bot and memedb
	// create child to monitor approval Q
		// watch the Q, if a meme appears, send it to the approval channel
		// on response, controller will inform DB and this child
	// read input buffer from bot
	// use that to spawn children
	//children perform tasks to populate output buffer and approval Q
	//parallel issues

	public static void main(String[] args) throws IOException {
		// get the bot streams
		Bot bot = new Bot();
		BufferedReader outputReader = bot.getBotOutput();
		BufferedWriter inputWriter = bot.getBotConsoleInput();

		// connect to the DB
		MemeBase2000 db = new MemeBase2000("C:\\sqlite\\");

		// approveQ
		Queue<Integer> approveQ = new LinkedList<>();
		Integer lastID = null;

		// begin loop
		while(true){
			// check bot output for messages
			if(outputReader.ready()){
				String line = outputReader.readLine();
				// read JSON output and pass it off to a child to handle
				// pass the db along so that the child can talk to the DB

			}

			// check approveQ for a new ID
			if(approveQ.peek() != null && approveQ.peek() != lastID){
				// spawn a child to get the meme from the DB
			}

			// check messages from children
			//	if a meme cached/curator submission ACK, send ACK to user
			// 	if an approveQ meme return, send the meme to be assessed by curator
			//	if a meme approved/rejection ACK, clear channel and pop approveQ
			//	if a meme return for a request, send link or error to bot to post

		}

	}

}
