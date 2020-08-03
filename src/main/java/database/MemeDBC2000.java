package database;

import app.MemeConfigLoader3000;
import datastructures.MemeDBMsg2000;
import datastructures.MemeLogger3000;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static datastructures.MemeDBMsg2000.MsgDBType.*;

/**
 *
 * Performs actions on the DB based off of instruction in the inputQ and puts the output into the outputQ
 */
public class MemeDBC2000 extends Thread{
    private MemeDB2000 db;
    private MemeConfigLoader3000 config;
    private MemeLogger3000 logger;
    private BlockingQueue<MemeDBMsg2000> outputQ;
    private BlockingQueue<MemeDBMsg2000> inputQ;

    public MemeDBC2000(MemeConfigLoader3000 config, MemeLogger3000 logger, BlockingQueue inQ, BlockingQueue outQ){
        db = new MemeDB2000(config, logger);
        db.open();
        this.config = config;
        this.outputQ = outQ;
        this.inputQ = inQ;
    }

    public void run(){
        String link, username;
        Integer id ;
        List<String> tags;
        MemeDBMsg2000 msg = new MemeDBMsg2000();
        while(true){
            try {
                msg = inputQ.take();
                switch(msg.getType()) {
                    case INITIALIZE:
                        List<Integer> ids = db.initialize();
                        for(Integer theid : ids){
                            outputQ.add(new MemeDBMsg2000()
                                    .type(REPLENISH_Q)
                                    .id(theid)
                            );
                        }

                        tags = db.getTags();
                        outputQ.add(new MemeDBMsg2000()
                                .type(ALL_TAGS)
                                .tags(tags)
                        );
                        break;

                    case GET_TAGS:
                        tags = db.getTags();
                        outputQ.add(new MemeDBMsg2000()
                                .type(ALL_TAGS)
                                .tags(tags)
                        );
                        break;

                    case GET_MEME_ID:
                        link = db.getCache(msg.getId());
                        tags = db.getTags(msg.getId());
                        if(link != null){
                            outputQ.put(new MemeDBMsg2000()
                                    .type(APPROVE_MEME)
                                    .id(msg.getId())
                                    .link(link)
                                    .tags(tags)
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case GET_MEME_TAGS:
                        link = db.get(msg.getTags());
                        if(link != null){
                            outputQ.put(new MemeDBMsg2000()
                                    .type(MEME)
                                    .link(link)
                                    .id(msg.getId())
                                    .username(msg.getUsername())
                                    .channelID(msg.getChannelID())
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case STORE_MEME:
                            id = db.store(msg.getUsername(), msg.getLink(), msg.getTags());
                            if(id != null){
                                outputQ.put(new MemeDBMsg2000()
                                        .type(SUBMIT_ACK)
                                        .username(msg.getUsername())
                                        .message("Stored meme to MemeDB")
                                );
                            }
                            else
                                getDBError(msg, false);

                        break;

                    case CACHE_MEME:
                        id = db.cache(msg.getUsername(), msg.getLink(), msg.getTags());
                        if(id != null){
                            outputQ.put(new MemeDBMsg2000()
                                    .type(SUBMIT_ACK)
                                    .id(id)
                                    .username(msg.getUsername())
                                    .message("Stored meme to the Cache. It is pending admin approval.")
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case PROMOTE_MEME:
                        username = db.promote(msg.getId(), msg.getUsername(), msg.getTags());
                        link = db.get(msg.getId());
                        if(link != null && username != null){
                            outputQ.put(new MemeDBMsg2000()
                                    .type(CURATE_RESULT)
                                    .message("This meme has been approved with tags: " + msg.getTags().toString())
                                    .id(msg.getId())
                                    .link(link)
                                    .username(username)
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case DEMOTE_MEME:
                        link = db.get(msg.getId());
                        username = db.demote(msg.getId());
                        if(link != null && username != null){
                            outputQ.put(new MemeDBMsg2000()
                                    .type(REPLENISH_Q)
                                    .id(msg.getId())
                                    .link(link)
                                    .username(username)
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case REJECT_MEME:
                        link = db.getCache(msg.getId());
                        username = db.reject(msg.getId());
                        if(link != null && username != null){
                            outputQ.put(new MemeDBMsg2000()
                                    .type(CURATE_RESULT)
                                    .message("This meme has been rejected.")
                                    .id(msg.getId())
                                    .link(link)
                                    .username(username)
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case TERMINATE:
                        db.close();
                        return;

                    default:
                        logger.println(MemeLogger3000.level.ERROR, "MemeDBC cannot handle a message of type: " + msg.getType().toString());
                }
            } catch (InterruptedException e) {
                logger.println(MemeLogger3000.level.ERROR, e.getStackTrace().toString());
                db.close();
                return;
            }
        }
    }

    /**
     * Puts an error message into the output
     * @param msg the error message from the DB
     * @param fatal indicates if this error breaks the controller
     * @throws InterruptedException
     */
    private void getDBError(MemeDBMsg2000 msg, Boolean fatal){
        MemeDBMsg2000 errorMsg = new MemeDBMsg2000()
                                    .type(MemeDBMsg2000.MsgDBType.ERROR)
                                    .message((fatal ? "[ FATAL ] " : "") + db.getError())
                                    .tags(msg.getTags())
                                    .username(msg.getUsername())
                                    .id(msg.getId());
        try {
            outputQ.put(errorMsg);
        } catch (InterruptedException e) {
            logger.println(MemeLogger3000.level.ERROR, "Cannot output error to outputQ." +
                    "\nMsg type: " + msg.getType() +
                    "\nTags: " + msg.getTags() +
                    "\nUsername: " + msg.getUsername() +
                    "\nID: " + msg.getId() +
                    "\n Stacktrace: " + e.getStackTrace().toString());
        }
    }

}
