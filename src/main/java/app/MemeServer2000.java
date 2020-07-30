package app;

import dataStructures.Logger;
import dataStructures.MemeBotMsg2000;
import dataStructures.MemeDBMsg2000;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
		//logger
		Logger logger = new Logger();
		
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
		dbInputQ.add(new MemeDBMsg2000().type(INITIALIZE));
		
		
		logger.println("Controllers started, meme queue cleared. We're ready to GO!");

		// begin loop
		while(true){
			try {
					
				Thread.sleep(250);
				
			// check bot output for messages
			if(!botOutputQ.isEmpty()){

				MemeDBMsg2000 newMsg = null;
				
				MemeBotMsg2000 msg = botOutputQ.take();
				switch(msg.getCommand()){
					case "deny":
						logger.println("Meme denied from discord bot");
						newMsg = new MemeDBMsg2000().type(REJECT_MEME).id(approveQ.take()).username(msg.getUser());
						botInputQ.add(new MemeBotMsg2000().command("clearQueue"));
						break;
					case "approve":
						logger.println("Meme approved from discord bot");
						newMsg = new MemeDBMsg2000().type(PROMOTE_MEME).id(approveQ.take()).username(msg.getUser());
						botInputQ.add(new MemeBotMsg2000().command("clearQueue"));
						break;
					case "fetchMeme":
						logger.println("Fetching meme for " + msg.getUser());
						newMsg = new MemeDBMsg2000().type(GET_MEME_TAGS).tags(Arrays.asList(msg.getBody().split(","))).username(msg.getUser()).channelID(msg.getChannelID());
						break;
					case "submitMeme":
						logger.println("Meme submitted by " + msg.getUser());
						newMsg = new MemeDBMsg2000().link(msg.getUrl()).tags(Arrays.asList(msg.getBody().split(","))).username(msg.getUser()).channelID(msg.getChannelID());
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
					case REPLENISH_Q:
						logger.println("Replenishing meme queue");
						approveQ.put(msg.getId());
						continue;

						//TODO handle this on bot side
					case ALL_TAGS:
						newMsg.setCommand("sendAllTags");
						newMsg.setBody(msg.getTags().toString());
						break;

					case SUBMIT_ACK:
						if(msg.getId() != null){
							approveQ.put(msg.getId());
							newMsg.setChannelID(approveQ.size());
						}
						newMsg.setCommand("sendToUser");
						newMsg.setUser(msg.getUsername());
						newMsg.setBody(msg.getMessage());
						break;

					case APPROVE_MEME:
						logger.println("Sending meme to be approved");
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
						logger.println("Sending meme to " + msg.getChannelID() + ", requested by " + msg.getUsername());
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
				// TODO handle this on bot side
				botInputQ.add(new MemeBotMsg2000().command("queueSize").channelID(approveQ.size()));
			}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
