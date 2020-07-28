package app;

import dataStructures.MemeDBMsg2000;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Performs actions on the DB based off of instruction in the inputQ and puts the output into the outputQ
 */
public class MemeDBC2000 {
    private MemeDB2000 db;
    private BlockingQueue<MemeDBMsg2000> outputQ;
    private BlockingQueue<MemeDBMsg2000> inputQ;

    MemeDBC2000(String filePath, BlockingQueue inQ, BlockingQueue outQ){
        db = new MemeDB2000(filePath);
        db.open();
        outputQ = outQ;
        inputQ = inQ;
    }

    public void spin(){
        while(true){
            if(!inputQ.isEmpty()){
                // Get the msg
                try {
                    MemeDBMsg2000 msg = inputQ.take();
                    switch(msg.getType()) {
                        case GET_MEME_ID:
                            String link = db.get(msg.getId());
                            if(link != null){
                                outputQ.put(new MemeDBMsg2000()
                                        .type(MemeDBMsg2000.MsgDBType.APPROVE_MEME)
                                        .message("Curate me pls")
                                        .link(link)
                                        .tags(msg.getTags()));
                                break;
                            }
                            else{
                                outputQ.put(new MemeDBMsg2000()
                                        .type(MemeDBMsg2000.MsgDBType.ERROR)
                                        .message("Failed to get meme of ID: " + msg.getId()));
                            }
                        case GET_MEME_TAGS:

                            break;
                        case STORE_MEME:

                            break;
                        case CACHE_MEME:

                            break;
                        case PROMOTE_MEME:

                            break;
                        case DEMOTE_MEME:

                            break;
                        case REJECT_MEME:

                            break;
                        case ERROR:

                            break;
                        default:
                            System.out.println("MemeDBC cannot handle a message of type: " + msg.getType().toString());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
