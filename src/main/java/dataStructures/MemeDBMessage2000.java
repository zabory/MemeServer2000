package dataStructures;

public class MemeDBMessage2000 {
    enum msgDBType {
        // To MemeDB message types
        GET_MEME_ID,            // Used to get a specific meme to approve
        GET_MEME_TAGS,          // Used to get a random meme of those tags
        STORE_MEME,             // Used to store a meme straight into the DB
        CACHE_MEME,             // Used to cache a meme while waiting for approval
        PROMOTE_MEME,           // Used to promote a meme to the DB from the cache
        DEMOTE_MEME,            // Used to demote a meme from the DB to the cache
        REJECT_MEME,            // Used to remove a meme from the cache

        // To MemeServer message types
        SUBMIT_ACK,             // Used to pass ACK info back to the bot after a submission (cache or store)
        APPROVE_MEME,           // Used to pass a meme to the bot to display in the approval channel
        CURATE_RESULT,          // Used to return the result of the curation back to the bot
        MEME                    // Used to pass the meme link back to the bot
    }
}
