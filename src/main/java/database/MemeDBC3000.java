package database;

import app.MemeConfigLoader3000;
import datastructures.MemeDBMsg3000;
import datastructures.MemeLogger3000;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static datastructures.MemeDBMsg3000.MsgDBType.*;

/**
 *
 * Performs actions on the DB based off of instruction in the inputQ and puts the output into the outputQ
 */
public class MemeDBC3000 extends Thread{
    private MemeDB3000 db;
    private MemeConfigLoader3000 config;
    private MemeLogger3000 logger;
    private BlockingQueue<MemeDBMsg3000> outputQ;
    private BlockingQueue<MemeDBMsg3000> inputQ;

    public MemeDBC3000(MemeConfigLoader3000 config, MemeLogger3000 logger, BlockingQueue inQ, BlockingQueue outQ){
        db = new MemeDB3000(config, logger);
        this.logger = logger;
        db.open();
        this.config = config;
        this.outputQ = outQ;
        this.inputQ = inQ;
    }

    public void run(){
        String link;
        Integer id;
        Long userId;
        List<String> tags;
        MemeDBMsg3000 msg;
        while(true){
            try {
                msg = inputQ.take();
                switch(msg.getType()) {
                    case INITIALIZE:
                        logger.println("Initializing...");
                        // Check to see which memes are older and need to be re-cached
                        List<Integer> ids = db.getAllOldMemeIDs();
                        for(Integer theid : ids)
                            db.demote(theid);

                        // any cache memes need to be added to the Q
                        ids = db.getAllCacheIds();
                        for(Integer theid : ids){
                            outputQ.put(new MemeDBMsg3000()
                                    .type(REPLENISH_Q)
                                    .id(theid)
                            );
                        }

                        // send all the existing tags tot he info channel
                        tags = db.getTags();
                        outputQ.put(new MemeDBMsg3000()
                                .type(ALL_TAGS)
                                .tags(tags)
                        );

                        // confirm with switchboard that DB inited
                        outputQ.put(new MemeDBMsg3000().type(INIT_ACK));
                        break;

                    case GET_TAGS:
                        logger.println("Getting all tags");
                        tags = db.getTags();
                        outputQ.put(new MemeDBMsg3000()
                                .type(ALL_TAGS)
                                .tags(tags)
                        );
                        break;

                    case GET_MEME_ID:
                        logger.println("Getting info for meme of ID " + msg.getId() + " to be approved");
                        link = db.getCache(msg.getId());
                        tags = db.getTags(msg.getId());
                        if(link != null){
                            outputQ.put(new MemeDBMsg3000()
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
                        logger.println("Getting a random meme with tags " + msg.getTags());
                        link = db.get(msg.getTags());
                        if(link != null){
                            outputQ.put(new MemeDBMsg3000()
                                    .type(MEME)
                                    .link(link)
                                    .id(msg.getId())
                                    .channelID(msg.getChannelID())
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case STORE_MEME:
                        logger.println("Storing meme " + msg.getLink());
                        id = db.store(msg.getUserID(), msg.getUsername(), msg.getLink(), msg.getTags());
                        if(id != null){
                            outputQ.put(new MemeDBMsg3000()
                                    .type(SUBMIT_ACK)
                                    .userID(msg.getUserID())
                                    .message("Stored meme to MemeDB")
                            );
                        }
                        else
                            getDBError(msg, false);

                        break;

                    case CACHE_MEME:
                        logger.println("Caching meme " + msg.getLink());
                        id = db.cache(msg.getUserID(), msg.getUsername(), msg.getLink(), msg.getTags());
                        if(id != null){
                            outputQ.put(new MemeDBMsg3000()
                                    .type(SUBMIT_ACK)
                                    .id(id)
                                    .userID(msg.getUserID())
                                    .message("Stored meme to the Cache. It is pending admin approval.")
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case PROMOTE_MEME:
                        logger.println("Promoting meme " + msg.getId());
                        userId = db.promote(msg.getId(), msg.getUsername(), msg.getUserID(), msg.getTags());
                        link = db.get(msg.getId());
                        if(link != null && userId != null){
                            outputQ.put(new MemeDBMsg3000()
                                    .type(CURATE_RESULT)
                                    .message("This meme has been approved with tags: " + msg.getTags().toString())
                                    .id(msg.getId())
                                    .link(link)
                                    .userID(userId)
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case DEMOTE_MEME:
                        logger.println("Demoting meme " + msg.getId());
                        link = db.get(msg.getId());
                        userId = db.demote(msg.getId());
                        if(link != null && userId != null){
                            outputQ.put(new MemeDBMsg3000()
                                    .type(REPLENISH_Q)
                                    .id(msg.getId())
                                    .link(link)
                                    .userID(userId)
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case REJECT_MEME:
                        logger.println("Rejecting meme " + msg.getId());
                        link = db.getCache(msg.getId());
                        userId = db.reject(msg.getId());
                        if(link != null && userId != null){
                            outputQ.put(new MemeDBMsg3000()
                                    .type(CURATE_RESULT)
                                    .message("This meme has been rejected.")
                                    .id(msg.getId())
                                    .link(link)
                                    .userID(userId)
                            );
                        }
                        else
                            getDBError(msg, false);
                        break;

                    case TERMINATE:
                        logger.println("Terminating");
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
    private void getDBError(MemeDBMsg3000 msg, Boolean fatal){
        MemeDBMsg3000 errorMsg = new MemeDBMsg3000()
                                    .type(MemeDBMsg3000.MsgDBType.ERROR)
                                    .message((fatal ? "[ FATAL ] " : "") + db.getError())
                                    .tags(msg != null ? msg.getTags() : null)
                                    .userID(msg != null ? msg.getUserID() : null)
                                    .username(msg != null ? msg.getUsername() : null)
                                    .id(msg != null ? msg.getId() : null);
        try {
            outputQ.put(errorMsg);
        } catch (InterruptedException e) {
            logger.println(MemeLogger3000.level.ERROR, "Cannot output error to outputQ." +
                    "\nMsg type: " + msg.getType() +
                    "\nTags: " + msg.getTags() +
                    "\nUsername: " + msg.getUsername() +
                    "\nUserId: " + msg.getUserID() +
                    "\nID: " + msg.getId() +
                    "\nStackTrace: " + e.getStackTrace().toString());
        }
    }

}
