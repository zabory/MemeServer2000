package dataStructures;

import java.util.List;

public class MemeDBMsg2000 {
    public enum MsgDBType {
        // To MemeDB message types
        GET_MEME_ID,            // Used to get a specific meme to approve
        GET_MEME_TAGS,          // Used to get a random meme of those tags
        STORE_MEME,             // Used to store a meme straight into the DB
        CACHE_MEME,             // Used to cache a meme while waiting for approval
        PROMOTE_MEME,           // Used to promote a meme to the DB from the cache
        DEMOTE_MEME,            // Used to demote a meme from the DB to the cache
        REJECT_MEME,            // Used to remove a meme from the cache
        TERMINATE,              // Used to kill the controller and MemeDB

        // To MemeServer message types
        SUBMIT_ACK,             // Used to pass ACK info back to the bot after a submission (cache or store)
        APPROVE_MEME,           // Used to pass a meme to the bot to display in the approval channel
        CURATE_RESULT,          // Used to return the result of the curation back to the bot (promote, demote, or reject)
        MEME,                   // Used to pass the meme link back to the bot
        ERROR                   // Used to pass an error string back to main
    }

    private MsgDBType type;
    private String message;
    private String link;
    private Integer id;
    private List<String> tags;
    private String username;

    // Default
    public MemeDBMsg2000() {
        this.type = null;
        this.message = null;
        this.link = null;
        this.id = null;
        this.tags = null;
        this.username = null;
    }

    public MemeDBMsg2000 type(MsgDBType type) {
        this.type = type;
        return this;
    }

    public MemeDBMsg2000 message(String message) {
        this.message = message;
        return this;
    }

    public MemeDBMsg2000 link(String link) {
        this.link = link;
        return this;
    }

    public MemeDBMsg2000 id(Integer id) {
        this.id = id;
        return this;
    }

    public MemeDBMsg2000 tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public MemeDBMsg2000 username(String username) {
        this.username = username;
        return this;
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
