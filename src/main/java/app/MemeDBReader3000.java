package app;

import datastructures.MemeBotMsg2000;
import datastructures.MemeDBMsg2000;
import datastructures.MemeLogger3000;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static datastructures.MemeDBMsg2000.MsgDBType.*;

public class MemeDBReader3000 extends Thread{
    private Long approvalChannelID;
    private MemeLogger3000 logger;
    private BlockingQueue<MemeBotMsg2000> botInputQ;
    private BlockingQueue<MemeDBMsg2000> dbOutputQ, dbInputQ;

    MemeDBReader3000(MemeLogger3000 logger, BlockingQueue<MemeBotMsg2000> botInputQ, BlockingQueue<MemeDBMsg2000> dbOutputQ, BlockingQueue<MemeDBMsg2000> dbInputQ){
        this.approvalChannelID = 736022204281520169L;
        this.logger = logger;
        this.botInputQ = botInputQ;
        this.dbOutputQ = dbOutputQ;
        this.dbInputQ = dbInputQ;
    }

    public void run(){
        BlockingQueue<Integer> approveQ = new LinkedBlockingQueue<>();
        MemeBotMsg2000 newMsg = new MemeBotMsg2000();
        MemeDBMsg2000 msg;
        Integer lastID = null;
        boolean sendMsg;

        while(true) {
            sendMsg = true;
            try {
                msg = dbOutputQ.take();
                switch(msg.getType()){
                    case REPLENISH_Q:
                        logger.println("Replenishing meme queue");
                        approveQ.put(msg.getId());
                        sendMsg = false;
                        break;

                    case ALL_TAGS:
                        newMsg.setCommand("sendAllTags");
                        String tagList = "";
                        for(String e : msg.getTags()) {
                            tagList += e + ",";
                        }
                        newMsg.setBody(tagList);
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
                        newMsg.setChannelID(approvalChannelID);

                        MemeBotMsg2000 tagMessage = new MemeBotMsg2000().channelID(approvalChannelID).body(msg.getTags().toArray() + "").command("sendToChannel");
                        botInputQ.add(tagMessage);

                        MemeBotMsg2000 queueCountMessage = new MemeBotMsg2000().channelID(approvalChannelID).body("Queue count " + approveQ.size()).command("sendToChannel");
                        botInputQ.add(queueCountMessage);
                        botInputQ.add(tagMessage);
                        break;

                    case CURATE_RESULT:
                        approveQ.take();
                        botInputQ.add(new MemeBotMsg2000().command("clearQueue"));
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
                if(sendMsg)
                    botInputQ.add(newMsg);

                // check approveQ for a new ID
                if(!approveQ.isEmpty() && approveQ.peek() != lastID){
                    MemeDBMsg2000 approveMsg = new MemeDBMsg2000().type(GET_MEME_ID).id(approveQ.peek());
                    lastID = approveMsg.getId();
                    dbInputQ.put(approveMsg);
                    botInputQ.put(new MemeBotMsg2000().command("queueSize").body(approveQ.size() + ""));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
