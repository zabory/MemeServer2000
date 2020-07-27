package dataStructures;

import java.util.ArrayList;
import java.util.List;

public class MemeDBMessage2000 {
    enum MsgDBType {
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

    private MsgDBType type;
    private String message;
    private String link;
    private Integer id;
    private List<String> tags;
    private String username;

    // For GET_MEME_ID, DEMOTE_MEME, or REJECT_MEME
    public MemeDBMessage2000(MsgDBType type, Integer id) {
        this.type = type;
        this.message = null;
        this.link = null;
        this.id = id;
        this.tags = null;
        this.username = null;
    }

    // For GET_MEME_TAGS
    public MemeDBMessage2000(List<String> tags) {
        this.type = MsgDBType.GET_MEME_TAGS;
        this.message = null;
        this.link = null;
        this.id = null;
        this.tags = tags;
        this.username = null;
    }

    // For STORE_MEME and CACHE_MEME
    public MemeDBMessage2000(MsgDBType type, String link, List<String> tags, String username) {
        this.type = type;
        this.message = null;
        this.link = link;
        this.id = null;
        this.tags = tags;
        this.username = username;
    }

    // For PROMOTE_MEME
    public MemeDBMessage2000(Integer id, String username) {
        this.type = MsgDBType.PROMOTE_MEME;
        this.message = null;
        this.link = null;
        this.id = id;
        this.tags = null;
        this.username = username;
    }

    // For SUBMIT_ACK
    public MemeDBMessage2000(String message, Integer id, String username) {
        this.type = MsgDBType.SUBMIT_ACK;
        this.message = message;
        this.link = null;
        this.id = id;
        this.tags = null;
        this.username = username;
    }

    // For APPROVE_MEME
    public MemeDBMessage2000(String message, String link, List<String> tags, String username) {
        this.type = MsgDBType.APPROVE_MEME;
        this.message = message;
        this.link = link;
        this.id = null;
        this.tags = tags;
        this.username = username;
    }

    // For CURATE_RESULT
    public MemeDBMessage2000(String message, String username) {
        this.type = MsgDBType.CURATE_RESULT;
        this.message = message;
        this.link = null;
        this.id = null;
        this.tags = null;
        this.username = username;
    }

    // For MEME
    public MemeDBMessage2000(String message, String link, String username) {
        this.type = MsgDBType.MEME;
        this.message = message;
        this.link = link;
        this.id = null;
        this.tags = null;
        this.username = username;
    }

    public MsgDBType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getLink() {
        return link;
    }

    public Integer getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getUsername() {
        return username;
    }
}
