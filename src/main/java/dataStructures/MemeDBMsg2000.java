package dataStructures;

import java.util.ArrayList;
import java.util.List;

public class MemeDBMsg2000 {
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
    public MemeDBMsg2000(MsgDBType type, Integer id) {
        this(type, null, null, id, null, null);
    }

    // For GET_MEME_TAGS
    public MemeDBMsg2000(List<String> tags) {
        this(MsgDBType.GET_MEME_TAGS, null, null, null, tags, null);
    }

    // For STORE_MEME and CACHE_MEME
    public MemeDBMsg2000(MsgDBType type, String link, List<String> tags, String username) {
        this(type, null, link, null, tags, username);
    }

    // For PROMOTE_MEME
    public MemeDBMsg2000(Integer id, String username) {
        this(MsgDBType.PROMOTE_MEME, null, null, id, null, username);
    }

    // For SUBMIT_ACK
    public MemeDBMsg2000(String message, Integer id, String username) {
        this(MsgDBType.SUBMIT_ACK, message, null, id, null, username);
    }

    // For APPROVE_MEME
    public MemeDBMsg2000(String message, String link, List<String> tags, String username) {
        this(MsgDBType.APPROVE_MEME, message, link, null, tags, username);
    }

    // For CURATE_RESULT
    public MemeDBMsg2000(String message, String username) {
        this(MsgDBType.CURATE_RESULT, message, null, null, null, username);
    }

    // For MEME
    public MemeDBMsg2000(String message, String link, String username) {
        this(MsgDBType.MEME, message, link, null, null, username);
    }

    // Default
    public MemeDBMsg2000(MsgDBType type, String message, String link, Integer id, List<String> tags, String username) {
        this.type = type;
        this.message = message;
        this.link = link;
        this.id = id;
        this.tags = tags;
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
