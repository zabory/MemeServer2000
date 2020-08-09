package app;

import datastructures.MemeBotMsg3000;
import datastructures.MemeDBMsg3000;
import datastructures.MemeLogger3000;
import datastructures.MemeLogger3000.level;

import java.util.concurrent.BlockingQueue;

import static datastructures.MemeDBMsg3000.MsgDBType.*;
import static datastructures.MemeBotMsg3000.MemeBotType.*;

public class MemeDBReader3000 extends Thread{
    private Long approvalChannelID;
    private MemeLogger3000 logger;
    private MemeConfigLoader3000 config;
    private BlockingQueue<MemeBotMsg3000> botInputQ;
    private BlockingQueue<MemeDBMsg3000> dbOutputQ, dbInputQ;
    private BlockingQueue<Integer> approveQ;

    MemeDBReader3000(MemeLogger3000 logger, MemeConfigLoader3000 config, BlockingQueue<MemeBotMsg3000> botInputQ, BlockingQueue<MemeDBMsg3000> dbOutputQ, BlockingQueue<MemeDBMsg3000> dbInputQ, BlockingQueue<Integer> approveQ){
        this.config = config;
        this.approvalChannelID = Long.parseLong(config.getApprovalChannel());
        this.logger = logger;
        this.botInputQ = botInputQ;
        this.dbOutputQ = dbOutputQ;
        this.dbInputQ = dbInputQ;
        this.approveQ = approveQ;
    }

    public void run(){
        MemeDBMsg3000 msg;
        Integer lastID = null;

        while(true) {
            try {
                msg = dbOutputQ.take();
                switch(msg.getType()){
                    case INIT_ACK:
                        logger.println("Received INIT_ACK");
                        botInputQ.put(new MemeBotMsg3000().type(INIT));
                        break;

                    case REPLENISH_Q:
                        logger.println("Meme ID of " + msg.getId() + " was put into pending approval.");
                        approveQ.put(msg.getId());
                        break;

                    case ALL_TAGS:
                        logger.println("Sending all tags to bot");
                        String tagList = "";
                        for(String e : msg.getTags()) {
                            tagList += e + ",";
                        }
                        if(!tagList.equals(""))
                            botInputQ.put(new MemeBotMsg3000().type(Send_Tags).body(tagList.substring(0, tagList.length()-1)));
                        break;

                    case SUBMIT_ACK:
                        if(msg.getId() != null){
                            logger.println("Received ACK for cached meme of ID " + msg.getId());
                            botInputQ.put(new MemeBotMsg3000().type(Queue_Size).body((approveQ.size() + 1) + ""));
                            approveQ.put(msg.getId());
                        }
                        else
                            logger.println("Received ACK for meme submitted by " + msg.getUsername());
                        botInputQ.put(new MemeBotMsg3000().type(Send_User).body(msg.getMessage()).user(msg.getUsername()));
                        break;

                    case APPROVE_MEME:
                        logger.println("Sending meme ID " + msg.getId() + " to be approved");
                        String tags = "**Tags**:\n";
                        for(int i=0;i<msg.getTags().size();i++)
                            tags += (i+1) + ": " + msg.getTags().get(i) + "\n";

                        botInputQ.put(new MemeBotMsg3000().channelID(approvalChannelID).body("**Queue count**: " + (approveQ.size())).type(Send_Channel));
                        botInputQ.put(new MemeBotMsg3000().channelID(approvalChannelID).body(tags).type(Send_Channel));
                        botInputQ.put(new MemeBotMsg3000().type(Send_Queue).body(msg.getLink()).channelID(approvalChannelID));
                        break;

                    case CURATE_RESULT:
                        logger.println("Received curation result for meme of ID " + msg.getId());
                        approveQ.take();
                        botInputQ.put(new MemeBotMsg3000().type(Clear_Queue));
                        botInputQ.put(new MemeBotMsg3000().type(Send_User).body(msg.getLink()).user(msg.getUsername()));
                        botInputQ.put(new MemeBotMsg3000().type(Send_User).body(msg.getMessage()).user(msg.getUsername()));
                        dbInputQ.put(new MemeDBMsg3000().type(GET_TAGS));
                        break;

                    case MEME:
                        logger.println("Sending meme to " + msg.getChannelID() + ", requested by " + msg.getUsername());
                        botInputQ.put(new MemeBotMsg3000().type(Send_Channel).body(msg.getLink()).user(msg.getUsername()).channelID(msg.getChannelID()));
                        break;

                    case ERROR:
                    	logger.println(level.ERROR, msg.getMessage() + msg.getTags());
                        botInputQ.put(new MemeBotMsg3000().type(Send_User).body(msg.getMessage() + msg.getTags()).user(msg.getUsername()));
                        break;

                    default:
                        logger.println("Main cannot handle a message of type: " + msg.getType().toString() + " as an output of the DB");
                }

                // check approveQ for a new ID
                if(!approveQ.isEmpty() && approveQ.peek() != lastID){
                    MemeDBMsg3000 approveMsg = new MemeDBMsg3000().type(GET_MEME_ID).id(approveQ.peek());
                    lastID = approveMsg.getId();
                    dbInputQ.put(approveMsg);
                    botInputQ.put(new MemeBotMsg3000().type(Queue_Size).body((approveQ.size()) + ""));
                    logger.println("Meme queue size increased");
                }
            } catch (InterruptedException e) {
                logger.println(MemeLogger3000.level.ERROR, getStackTrace().toString());
            }
        }
    }
}
