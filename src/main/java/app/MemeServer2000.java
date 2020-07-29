package app;

import dataStructures.MemeDBMsg2000;

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
		BlockingQueue dbOutputQ = new LinkedBlockingQueue();
		BlockingQueue dbInputQ = new LinkedBlockingQueue();
		MemeDBC2000 controller = new MemeDBC2000("C:\\sqlite\\", dbInputQ, dbOutputQ);

		// approveQ
		BlockingQueue approveQ = new LinkedBlockingQueue();
		Integer lastID = null;

		// begin loop
		while(true){
			try {
			// check bot output for messages
				// write a message to the dbInputQ

			// check messages from controller
			if(!dbOutputQ.isEmpty()){
				MemeDBMsg2000 msg = (MemeDBMsg2000) dbOutputQ.take();
				//	if a meme cached/curator submission ACK, send ACK to user

				// 	if an approveQ meme return, send the meme to be assessed by curator

				//	if a meme approved/rejection ACK, clear channel and pop approveQ

				//	if a meme return for a request, send link or error to bot to post
				switch(msg.getType()){
					case SUBMIT_ACK:

						break;

					case APPROVE_MEME:

						break;

					case CURATE_RESULT:

						break;

					case MEME:

						break;

					case ERROR:

						break;

					default:
						System.out.println("Main cannot handle a message of type: " + msg.getType().toString() + " as an output of");
				}
			}

			// check approveQ for a new ID
			if(!approveQ.isEmpty() && approveQ.peek() != lastID){
				//
			}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
