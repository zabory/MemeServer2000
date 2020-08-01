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
    private BlockingQueue<MemeBotMsg2000> botInputQ;
    private BlockingQueue<MemeDBMsg2000> dbOutputQ, dbInputQ;

    MemeDBReader3000(MemeLogger3000 logger, BlockingQueue<MemeBotMsg2000> botInputQ, BlockingQueue<MemeDBMsg2000> dbOutputQ, BlockingQueue<MemeDBMsg2000> dbInputQ){

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("app");
        context.refresh();
        MemeConfigLoader3000 botConfig = context.getBean(MemeConfigLoader3000.class);
        this.approvalChannelID = Long.parseLong(botConfig.getApprovalChannel());
        this.logger = logger;
        this.botInputQ = botInputQ;
        this.dbOutputQ = dbOutputQ;
        this.dbInputQ = dbInputQ;
    }

    public void run(){
        BlockingQueue<Integer> approveQ = new LinkedBlockingQueue<>(100);
        MemeDBMsg2000 msg;
        Integer lastID = null;
        boolean sendMsg;

        while(true) {
            MemeBotMsg2000 newMsg = new MemeBotMsg2000();
            sendMsg = true;
            try {
                msg = dbOutputQ.take();
                switch(msg.getType()){
                    case REPLENISH_Q:
                        logger.println("Meme ID of " + msg.getId() + " was put into pending approval.");
                        approveQ.put(msg.getId());
                        sendMsg = false;
                        break;

                    case ALL_TAGS:
                        logger.println("Sending all tags to bot");
                        newMsg.setCommand("sendAllTags");
                        String tagList = "";
                        for(String e : msg.getTags()) {
                            tagList += e + ",";
                        }
                        newMsg.setBody(tagList);
                        break;

                    case SUBMIT_ACK:
                        logger.println("Received ACK for meme of ID " + msg.getId());
                        if(msg.getId() != null){
                            approveQ.put(msg.getId());
                            newMsg.setChannelID(approveQ.size());
                        }
                        newMsg.setCommand("sendToUser");
                        newMsg.setUser(msg.getUsername());
                        newMsg.setBody(msg.getMessage());
                        break;

                    case APPROVE_MEME:
                        logger.println("Sending meme ID " + msg.getId() + " to be approved");
                        newMsg.setCommand("sendToQueue");
                        newMsg.setBody(msg.getLink());
                        newMsg.setChannelID(approvalChannelID);
                        String tags = "**Tags**:\n";
                        for(int i=0;i<msg.getTags().size();i++)
                            tags += i + ": " + msg.getTags().get(i) + "\n";

                        MemeBotMsg2000 approveMsgHeader = new MemeBotMsg2000().channelID(approvalChannelID).body("@everyone\n**Queue count**: " + (approveQ.size()-1) + "\n" + tags).command("sendToChannel");
                        botInputQ.put(approveMsgHeader);
                        break;

                    case CURATE_RESULT:
                        logger.println("Received curation result for meme of ID " + msg.getId());
                        approveQ.take();
                        botInputQ.put(new MemeBotMsg2000().command("clearQueue"));
                        MemeBotMsg2000 newNewMsg = new MemeBotMsg2000();
                        newNewMsg.setCommand("sendToUser");
                        newNewMsg.setBody(msg.getLink());
                        newNewMsg.setUser(msg.getUsername());
                        botInputQ.put(newNewMsg);

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
                        logger.println("Main cannot handle a message of type: " + msg.getType().toString() + " as an output of the DB");
                }
                if(sendMsg)
                    botInputQ.put(newMsg);

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
