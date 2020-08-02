package app;

import datastructures.MemeBotMsg2000;
import datastructures.MemeDBMsg2000;
import datastructures.MemeLogger3000;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static datastructures.MemeDBMsg2000.MsgDBType.*;

public class MemeDBReader3000 extends Thread{
    private Long approvalChannelID;
    private MemeLogger3000 logger;
    private MemeConfigLoader3000 config;
    private BlockingQueue<MemeBotMsg2000> botInputQ;
    private BlockingQueue<MemeDBMsg2000> dbOutputQ, dbInputQ;
    private BlockingQueue<Integer> approveQ;

    MemeDBReader3000(MemeLogger3000 logger, MemeConfigLoader3000 config, BlockingQueue<MemeBotMsg2000> botInputQ, BlockingQueue<MemeDBMsg2000> dbOutputQ, BlockingQueue<MemeDBMsg2000> dbInputQ, BlockingQueue<Integer> approveQ){
        this.config = config;
        this.approvalChannelID = Long.parseLong(config.getApprovalChannel());
        this.logger = logger;
        this.botInputQ = botInputQ;
        this.dbOutputQ = dbOutputQ;
        this.dbInputQ = dbInputQ;
        this.approveQ = approveQ;
    }

    public void run(){
        MemeDBMsg2000 msg;
        Integer lastID = null;

        while(true) {
            try {
                msg = dbOutputQ.take();
                switch(msg.getType()){
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
                        botInputQ.put(new MemeBotMsg2000().command("sendAllTags").body(tagList));
                        break;

                    case SUBMIT_ACK:
                        if(msg.getId() != null){
                            logger.println("Received ACK for cached meme of ID " + msg.getId());
                            approveQ.put(msg.getId());
                        }
                        else
                            logger.println("Received ACK for meme submitted by " + msg.getUsername());
                        botInputQ.put(new MemeBotMsg2000().command("sendToUser").body(msg.getMessage()).user(msg.getUsername()));
                        break;

                    case APPROVE_MEME:
                        logger.println("Sending meme ID " + msg.getId() + " to be approved");
                        String tags = "**Tags**:\n";
                        for(int i=0;i<msg.getTags().size();i++)
                            tags += i + ": " + msg.getTags().get(i) + "\n";

                        botInputQ.put(new MemeBotMsg2000().channelID(approvalChannelID).body("**Queue count**: " + (approveQ.size()-1) + "\n" + tags).command("sendToChannel"));
                        botInputQ.put(new MemeBotMsg2000().command("sendToQueue").body(msg.getLink()).channelID(approvalChannelID));
                        break;

                    case CURATE_RESULT:
                        logger.println("Received curation result for meme of ID " + msg.getId());
                        approveQ.take();
                        botInputQ.put(new MemeBotMsg2000().command("clearQueue"));
                        botInputQ.put(new MemeBotMsg2000().command("sendToUser").body(msg.getLink()).user(msg.getUsername()));
                        botInputQ.put(new MemeBotMsg2000().command("sendToUser").body(msg.getMessage()).user(msg.getUsername()));
                        break;

                    case MEME:
                        logger.println("Sending meme to " + msg.getChannelID() + ", requested by " + msg.getUsername());
                        botInputQ.put(new MemeBotMsg2000().command("sendToChannel").body(msg.getLink()).user(msg.getUsername()).channelID(msg.getChannelID()));
                        break;

                    case ERROR:
                        botInputQ.put(new MemeBotMsg2000().command("sendToUser").body(msg.getMessage() + msg.getTags()).user(msg.getUsername()));
                        break;

                    default:
                        logger.println("Main cannot handle a message of type: " + msg.getType().toString() + " as an output of the DB");
                }

                // check approveQ for a new ID
                if(!approveQ.isEmpty() && approveQ.peek() != lastID){
                    MemeDBMsg2000 approveMsg = new MemeDBMsg2000().type(GET_MEME_ID).id(approveQ.peek());
                    lastID = approveMsg.getId();
                    dbInputQ.put(approveMsg);
                    botInputQ.put(new MemeBotMsg2000().command("queueSize").body(approveQ.size() + ""));
                }
            } catch (InterruptedException e) {
                logger.println(MemeLogger3000.level.ERROR, getStackTrace().toString());
            }
        }
    }
}
