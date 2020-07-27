package app;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author Ben Shabowski
 * @author Jacob Marszalek
 * @version 2000
 * @since 2000
 * 
 */
public class MemeServer2000 {
	public static void main(String[] args) throws IOException {
		// get the bot streams
		// put bot stuff here

		// create the controller
		MemeDBC2000 controller = new MemeDBC2000("C:\\sqlite\\");
		BlockingQueue dbOutputQ = controller.getOutputQ();
		BlockingQueue dbInputQ = controller.getInputQ();

		// approveQ
		BlockingQueue approveQ = new LinkedBlockingQueue();
		Integer lastID = null;

		// begin loop
		while(true){
			// check bot output for messages
				// write a message to the dbInputQ

			// check messages from controller
			if(!dbOutputQ.isEmpty()){
				//	if a meme cached/curator submission ACK, send ACK to user
				// 	if an approveQ meme return, send the meme to be assessed by curator
				//	if a meme approved/rejection ACK, clear channel and pop approveQ
				//	if a meme return for a request, send link or error to bot to post
			}

			// check approveQ for a new ID
			if(!approveQ.isEmpty() && approveQ.peek() != lastID){
				//
			}


		}

	}

}
