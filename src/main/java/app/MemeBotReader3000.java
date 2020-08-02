package app;

import datastructures.MemeBotMsg2000;
import datastructures.MemeDBMsg2000;
import datastructures.MemeLogger3000;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import static datastructures.MemeDBMsg2000.MsgDBType.*;

public class MemeBotReader3000 extends Thread{
    private BlockingQueue<MemeBotMsg2000> botOutputQ;
    private BlockingQueue<MemeDBMsg2000> dbInputQ;
    private BlockingQueue<Integer> approveQ;
    private MemeLogger3000 logger;

    MemeBotReader3000(MemeLogger3000 logger, BlockingQueue<MemeBotMsg2000> botOutputQ, BlockingQueue<MemeDBMsg2000> dbInputQ, BlockingQueue<Integer> approveQ){
        this.logger = logger;
        this.botOutputQ = botOutputQ;
        this.dbInputQ = dbInputQ;
        this.approveQ = approveQ;
    }

    public void run(){
        MemeDBMsg2000 newMsg = new MemeDBMsg2000();
        MemeBotMsg2000 msg;
        LinkedList<String> tags;
        while(true){
            try {
                msg = botOutputQ.take();
                switch(msg.getCommand()){
                    case "deny":
                        logger.println("Meme denied from discord bot by " + msg.getUser());
                        newMsg = new MemeDBMsg2000().type(REJECT_MEME).username(msg.getUser()).id(approveQ.peek());
                        break;
                    case "approve":
                        logger.println("Meme approved from discord bot by " + msg.getUser());
                        tags = new LinkedList<String>(new HashSet<String>(Arrays.asList(msg.getTags().split("\\s*,\\s*"))));
                        LinkedList<String> finalTags2 = tags;
                        tags.forEach(e -> {
                            finalTags2.set(finalTags2.indexOf(e), e.trim().replace("\"", "").replace(";", "").replace("[", "").replace("]", ""));
                        });
                        newMsg = new MemeDBMsg2000().type(PROMOTE_MEME).username(msg.getUser()).id(approveQ.peek()).tags(new LinkedList<String>(tags));
                        break;
                    case "fetchMeme":
                        logger.println("Fetching meme for " + msg.getUser());
                        tags = new LinkedList<String>(new HashSet<String>(Arrays.asList(msg.getBody().split("\\s*,\\s*"))));
                        LinkedList<String> finalTags1 = tags;
                        tags.forEach(e -> {
                            finalTags1.set(finalTags1.indexOf(e), e.trim().replace("\"", "").replace(";", "").replace("[", "").replace("]", ""));
                        });
                        newMsg = new MemeDBMsg2000().type(GET_MEME_TAGS).tags(new LinkedList<String>(tags)).username(msg.getUser()).channelID(msg.getChannelID());
                        break;
                    case "submitMeme":
                        logger.println("Meme submitted by " + msg.getUser() + " with a channel ID of " + msg.getChannelID());
                        tags = new LinkedList<String>(new HashSet<String>(Arrays.asList(msg.getBody().split("\\s*,\\s*"))));
                        LinkedList<String> finalTags = tags;
                        tags.forEach(e -> {
                            finalTags.set(finalTags.indexOf(e), e.trim().replace("\"", "").replace(";", "").replace("[", "").replace("]", ""));
                        });
                        newMsg = new MemeDBMsg2000().link(msg.getUrl()).tags(tags).username(msg.getUser()).channelID(msg.getChannelID());
                        if(msg.isAdmin()) {
                            newMsg.type(STORE_MEME);
                        }else {
                            newMsg.type(CACHE_MEME);
                        }
                        break;
                    case "print":
                        logger.println(msg.getBody());
                        break;
                    default:
                        logger.println("Main cannot handle " + msg.getCommand() + " message from the bot. :(");
                }
                dbInputQ.put(newMsg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
