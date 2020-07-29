package app;

import dataStructures.MemeBotMsg2000;
import dataStructures.MemeDBMsg2000;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static dataStructures.MemeDBMsg2000.MsgDBType.*;

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
		// put bot stuff here
		BlockingQueue<MemeBotMsg2000> botOutputQ = new LinkedBlockingQueue<MemeBotMsg2000>(100);
		BlockingQueue<MemeBotMsg2000> botInputQ = new LinkedBlockingQueue<MemeBotMsg2000>(100);
		MemeBotInterfacer2000 memeBotInterfacer = new MemeBotInterfacer2000(botInputQ, botOutputQ);
		
		// create the controller
		BlockingQueue<MemeDBMsg2000> dbOutputQ = new LinkedBlockingQueue<MemeDBMsg2000>(100);
		BlockingQueue<MemeDBMsg2000> dbInputQ = new LinkedBlockingQueue<MemeDBMsg2000>(100);
		MemeDBC2000 controller = new MemeDBC2000("C:\\MemeDBFolder2000\\", dbInputQ, dbOutputQ);
        controller.start();

		// approveQ
		BlockingQueue<Integer> approveQ = new LinkedBlockingQueue<Integer>();
		Integer lastID = null;
		
		botInputQ.add(new MemeBotMsg2000().command("clearQueue"));

		// begin loop
		while(true){
			try {

			// check bot output for messages
			if(!botOutputQ.isEmpty()){
				
			
				
				MemeDBMsg2000 newMsg = null;
				
				MemeBotMsg2000 msg = botOutputQ.take();
				switch(msg.getCommand()){
					case "deny":
						newMsg = new MemeDBMsg2000().type(REJECT_MEME).id(approveQ.take()).username(msg.getUser());
						botInputQ.add(new MemeBotMsg2000().command("clearQueue"));
						break;
					case "approve":
						newMsg = new MemeDBMsg2000().type(PROMOTE_MEME).id(approveQ.take()).username(msg.getUser());
						botInputQ.add(new MemeBotMsg2000().command("clearQueue"));
						break;
					case "fetchMeme":
						newMsg = new MemeDBMsg2000().type(GET_MEME_TAGS).tags(Arrays.asList(msg.getBody().split(" "))).username(msg.getUser()).channelID(msg.getChannelID());
						break;
					case "submitMeme":
						newMsg = new MemeDBMsg2000().link(msg.getUrl()).tags(Arrays.asList(msg.getBody().split(" "))).username(msg.getUser()).channelID(msg.getChannelID());
						if(msg.isAdmin()) {
							newMsg.type(STORE_MEME);
						}else {
							newMsg.type(CACHE_MEME);
						}
						break;
					default:
						System.out.println("Main cannot handle " + msg.getCommand() + " message from the bot. :(");
				}
				
				dbInputQ.add(newMsg);
				
			}

			// check messages from controller
			if(!dbOutputQ.isEmpty()){
				MemeBotMsg2000 newMsg = new MemeBotMsg2000();
				MemeDBMsg2000 msg = dbOutputQ.take();
				switch(msg.getType()){
					case SUBMIT_ACK:
						if(msg.getId() != null){
							approveQ.put(msg.getId());
						}
						newMsg.setCommand("sendToUser");
						newMsg.setUser(msg.getUsername());
						newMsg.setBody(msg.getMessage());
						break;

					case APPROVE_MEME:
						newMsg.setCommand("sendToQueue");
						newMsg.setBody(msg.getLink());
						newMsg.setChannelID(736022204281520169L);
						break;

					case CURATE_RESULT:
						MemeBotMsg2000 newNewMsg = new MemeBotMsg2000();
						newNewMsg.setCommand("sendToUser");
						newNewMsg.setBody(msg.getLink());
						newNewMsg.setUser(msg.getUsername());
						botInputQ.add(newNewMsg);

						newMsg.setCommand("sendToUser");
						newMsg.setUser(msg.getUsername());
						newMsg.setBody(msg.getMessage());
						break;

					case MEME:
						newMsg.setCommand("sendToChannel");
						newMsg.setUser(msg.getUsername());
						newMsg.setBody(msg.getLink());
						newMsg.setChannelID(msg.getChannelID());
						break;

					case ERROR:
						newMsg.setCommand("sendToUser");
						newMsg.setUser(msg.getUsername());
						newMsg.setBody(msg.getMessage() + msg.getTags());
						break;

					default:
						System.out.println("Main cannot handle a message of type: " + msg.getType().toString() + " as an output of the DB");
				}

				botInputQ.add(newMsg);
			}

			// check approveQ for a new ID
			if(!approveQ.isEmpty() && approveQ.peek() != lastID){
				MemeDBMsg2000 approveMsg = new MemeDBMsg2000().type(GET_MEME_ID).id(approveQ.peek());
				lastID = approveMsg.getId();
				dbInputQ.add(approveMsg);
			}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
