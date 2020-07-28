package app;

import dataStructures.MemeDBMsg2000;

import java.util.concurrent.BlockingQueue;

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
        String link = null, username = null;
        Integer id = null;
        Boolean status = true;
        MemeDBMsg2000 msg = null;
        while(true){
            if(!inputQ.isEmpty()){
                try {
                    msg = inputQ.take();
                    switch(msg.getType()) {
                        case GET_MEME_ID:
                            link = db.get(msg.getId());
                            if(link != null){
                                outputQ.put(new MemeDBMsg2000()
                                        .type(MemeDBMsg2000.MsgDBType.APPROVE_MEME)
                                        .message("Curate me pls")
                                        .link(link)
                                        .tags(msg.getTags())
                                );
                            }
                            else
                                getDBError(msg, false);
                            break;

                        case GET_MEME_TAGS:
                            link = db.get(msg.getTags());
                            if(link != null){
                                outputQ.put(new MemeDBMsg2000()
                                        .type(MemeDBMsg2000.MsgDBType.MEME)
                                        .link(link)
                                        .username(msg.getUsername())
                                );
                            }
                            else
                                getDBError(msg, false);
                            break;

                        case STORE_MEME:
                                id = db.store(msg.getUsername(), msg.getLink(), msg.getTags());
                                if(id != null){
                                    outputQ.put(new MemeDBMsg2000()
                                            .type(MemeDBMsg2000.MsgDBType.SUBMIT_ACK)
                                            .id(id)
                                            .username(msg.getUsername())
                                    );
                                }
                                else
                                    getDBError(msg, false);

                            break;

                        case CACHE_MEME:
                            id = db.cache(msg.getUsername(), msg.getLink(), msg.getTags());
                            if(id != null){
                                outputQ.put(new MemeDBMsg2000()
                                        .type(MemeDBMsg2000.MsgDBType.SUBMIT_ACK)
                                        .id(id)
                                        .username(msg.getUsername())
                                );
                            }
                            else
                                getDBError(msg, false);
                            break;

                        case PROMOTE_MEME:
                            username = db.promote(msg.getId(), msg.getUsername());
                            link = db.get(msg.getId());
                            if(link != null && username != null){
                                outputQ.put(new MemeDBMsg2000()
                                        .type(MemeDBMsg2000.MsgDBType.CURATE_RESULT)
                                        .message("This meme has been approved.")
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
                                        .type(MemeDBMsg2000.MsgDBType.CURATE_RESULT)
                                        .message("This meme has been demoted.")
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
                                        .type(MemeDBMsg2000.MsgDBType.CURATE_RESULT)
                                        .message("This meme has been rejected.")
                                        .id(msg.getId())
                                        .link(link)
                                        .username(username)
                                );
                            }
                            else
                                getDBError(msg, false);
                            break;
                        default:
                            System.out.println("MemeDBC cannot handle a message of type: " + msg.getType().toString());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Cannot output error to outputQ.\n" +
                            "Msg type: " + msg.getType() +
                            "Tags: " + msg.getTags() +
                            "Username: " + msg.getUsername() +
                            "ID: " + msg.getId());
                    return;
                }
            }
            link = null;
            username = null;
            id = null;
            status = true;
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
            e.printStackTrace();
            System.out.println("Cannot output error to outputQ.\n" +
                    "Msg type: " + msg.getType() +
                    "Tags: " + msg.getTags() +
                    "Username: " + msg.getUsername() +
                    "ID: " + msg.getId());
        }
    }

}
