package database;

import app.MemeConfigLoader3000;
import datastructures.MemeLogger3000;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static datastructures.MemeLogger3000.level.ERROR;

public class MemeDB3000 {
    /*
     *
     * VARS AND SUCH
     *
     */

    static class Column{
        enum ColType{
            LONG,
            INT,
            STR
        }
        Object var;
        ColType type;

        public Column(Object var, ColType type) {
            this.var = var;
            this.type = type;
        }
    }

    private MemeConfigLoader3000 config;
    private MemeLogger3000 logger;
    private String db;
    static String memeTableName, cacheTableName, tagLkpTableName, userLkpTableName;
    static List<String> tableDefs;
    private Integer headID;
    private Connection conn;
    private String errorMsg;

    /*
     *
     * PUBLIC
     *
     */

    /**
     * Constructor
     * @param config the config object
     */
    MemeDB3000(MemeConfigLoader3000 config, MemeLogger3000 logger) {
        this.config = config;
        this.logger = logger;
        File dataBaseFile = new File(config.getDatabaseLocation());
        if(!dataBaseFile.exists()) {
        	dataBaseFile.getParentFile().mkdirs();
        }
        this.db = "jdbc:sqlite:" + dataBaseFile.getAbsolutePath();
        this.memeTableName = config.getMemeTableName();
        this.cacheTableName = config.getCacheTableName();
        this.tagLkpTableName = config.getTagLkpTableName();
        this.userLkpTableName = config.getUserLkpTableName();
        tableDefs = Arrays.asList(
                config.getMemeTableDef().replace("memeTableName", memeTableName),
                config.getCacheTableDef().replace("cacheTableName", cacheTableName),
                config.getTagLkpTableDef().replace("tagLkpTableName", tagLkpTableName),
                config.getUserLkpTableDef().replace("userLkpTableName", userLkpTableName)
        );
        headID = 0;
        conn = null;
        errorMsg = "";
    }

    /**
     * Error message getter
     * @return
     */
    public String getError() {
        return errorMsg;
    }

    /**
     * Connects to DB and builds the DB environment
     * @throws SQLException
     */
    public Boolean open()  {
        Integer cacheMax = null;
        ResultSet rs;

        // Open the DB and set up the tables
        try{
            conn = DriverManager.getConnection(db);
            conn.setAutoCommit(false);
            execute(tableDefs);
            commit();
        }
        catch(SQLException throwables){
            throwables.printStackTrace();
            error("Failed to initialize the DB");
            return false;
        }

        // Select the max ID from the memeDB
        try {
            rs = executeQuery("SELECT MAX(id) m FROM " + memeTableName + ";");
            if(rs != null && rs.next())
                headID = rs.getInt("m");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to get the MAX id from the MemeDB");
            return false;
        }

        // Select the max ID from cache
        try {
            rs = executeQuery("SELECT MAX(id) m FROM " + cacheTableName + ";");
            if(rs != null && rs.next())
                cacheMax = rs.getInt("m");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to get the MAX id from the Cache");
            return false;
        }

        // Makes sure the highest used ID is stored locally
        if(cacheMax > headID)
            headID = cacheMax;
        return true;
    }

    /**
     * Closes the DB
     */
    public Boolean close(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            error("Failed to close the DB");
            return false;
        }
        return true;
    }

    /**
     * Gets all ids in the cache
     * @return
     */
    public List<Integer> getAllCacheIds(){
        List<Integer> retlist = new ArrayList<>();
        try {
            ResultSet rs = executeQuery("SELECT id FROM " + cacheTableName + ";");
            while(rs != null && rs.next()) {
                retlist.add(rs.getInt("id"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed retrieving all ids from the cache");
            return null;
        }
        return retlist;
    }

    /**
     * Gets all ids of memes older that property file or NULL
     * @return
     */
    public List<Integer> getAllOldMemeIDs(){
        List<Integer> retlist = new ArrayList<>();
        try {
            ResultSet rs = executeQuery("SELECT id FROM " + memeTableName + " WHERE timestamp IS NULL OR timestamp < ?;", Arrays.asList(new Column(config.getTime(), Column.ColType.STR)));
            while(rs != null && rs.next()) {
                retlist.add(rs.getInt("id"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed retrieving all ids from the cache");
            return null;
        }
        return retlist;
    }

    /**
     * Get a meme that matches the affiliated tags
     * @param tags The tags that this meme must have
     * @return  null if no meme exists with all provided tags
     *          link to a meme if atleast one exists that exists
     */
    public String get(List<String> tags){
        errorMsg = "";
        ResultSet rs;
        String link = null;

        if(tags != null && tags.size()>0){
            // Build the query
            String sql = "SELECT m.link FROM (&&) n INNER JOIN " + memeTableName + " m ON m.id = n.id ORDER BY RANDOM() LIMIT 1;";
            String subQuery = "SELECT id FROM (SELECT COUNT(*) c, id FROM " + tagLkpTableName + " WHERE tag IN (&&) GROUP BY id HAVING c = " + tags.size() + ")";
            String tagList = "";
            for(int i=0;i<tags.size();i++){
                tagList +=  "?,";
            }
            subQuery = subQuery.replace("&&", tagList.substring(0, tagList.length()-1));
            sql = sql.replace("&&", subQuery);
            List<Column> cols = new ArrayList<Column>();
            for(int i=0;i<tags.size();i++)
                cols.add(i, new Column(tags.get(i), Column.ColType.STR));

            try {
                rs = executeQuery(sql, cols);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error("Failed extract a random meme with tags: " + tags);
                return null;
            }
        }
        // Default query for no tags
        else {
            try {
                rs = executeQuery("SELECT m.link FROM " + tagLkpTableName + " n INNER JOIN " + memeTableName + " m ON m.id = n.id ORDER BY RANDOM() LIMIT 1;");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error("Failed extract a random meme");
                return null;
            }
        }

        try {
            if(rs != null && rs.next()) {
                link = rs.getString("link");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            error("No meme exists that contains that tags: " + tags);
            return null;
        }
        return link;
    }

    /**
     * Gets a meme if the provided ID exists
     * @param id of a meme
     * @return the link to the meme or null
     */
    public String get(Integer id){
        errorMsg = "";
        String link = null;
        try {
            ResultSet rs = executeQuery("SELECT link FROM " + memeTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
            if(rs != null && rs.next()) {
                link = rs.getString("link");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("No meme exists in MemeDB with ID: " + id);
        }
        return link;
    }

    /**
     * Returns all tags existing
     * @return
     */
    public List<String> getTags(){
        return getTags(null);
    }

    /**
     * Return all tags or all tags that have id
     * @param id
     * @return
     */
    public List<String> getTags(Integer id){
        List<String> retList = new ArrayList<>();
        errorMsg = "";
        try {
            ResultSet rs;
            if(id == null) {
                rs = executeQuery("SELECT COUNT(*) c, tag FROM (SELECT tag, link FROM " + tagLkpTableName + " t LEFT JOIN " + memeTableName + " m ON t.id = m.id) WHERE link IS NOT NULL GROUP BY tag ORDER BY tag ASC");
                while(rs != null && rs.next()) {
                    retList.add(rs.getString("tag") + " (" + rs.getString("c") + ")");
                }
            }
            else {
                rs = executeQuery("SELECT tag FROM " + tagLkpTableName + " WHERE id = ? ORDER BY tag ASC", Arrays.asList(new Column(id, Column.ColType.INT)));
                while(rs != null && rs.next()) {
                    retList.add(rs.getString("tag"));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed getting tags");
            return null;
        }
        return retList;
    }

    /**
     * Gets a meme from cache
     * @param id of a meme
     * @return the link to the meme or null
     */
    public String getCache(Integer id){
        errorMsg = "";
        String link = null;
        try {
            ResultSet rs = executeQuery("SELECT link FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
            if(rs != null && rs.next()) {
                link = rs.getString("link");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("No meme exists within the Cache with ID: " + id);
        }
        return link;
    }

    /**
     * Insert into the meme table
     * @param username name of submitting user
     * @param link A link to a meme
     * @param tags Tags associated with this meme
     * @return the id of the meme
     */
    public Integer store(Long userId, String username, String link, List<String> tags){
        errorMsg = "";
        if(uniqueLink(link) && validUser(userId, username)){
            Integer memeID = getID();
            try {
                execute("INSERT INTO " + memeTableName + " (id, link, submitterId, curatorId, timestamp) VALUES (?,?,?,?,?)",
                        Arrays.asList(  new Column(memeID, Column.ColType.INT),
                                        new Column(link, Column.ColType.STR),
                                        new Column(userId, Column.ColType.LONG),
                                        new Column(userId, Column.ColType.LONG),
                                        new Column(new Timestamp(System.currentTimeMillis()).toString(), Column.ColType.STR)
                        ));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error("ROLLBACK. Failed to insert meme into the DB: " + tags);
                rollback();
                return null;
            }

            if(insertTags(memeID, tags)){
                commit();
                return memeID;
            }
        }

        return null;
    }

    /**
     * Insert into cache table
     * @param userId user who submitted meme
     * @param link link to meme
     * @param tags tags for this meme
     * @return the id of the meme
     */
    public Integer cache(Long userId, String username, String link, List<String> tags){
        errorMsg = "";
        if(uniqueLink(link) && validUser(userId, username)){
            Integer memeID = getID();
            try {
                execute("INSERT INTO " + cacheTableName + " (id, link, submitterId) VALUES (?,?,?)",
                        Arrays.asList(  new Column(memeID, Column.ColType.INT),
                                        new Column(link, Column.ColType.STR),
                                        new Column(userId, Column.ColType.LONG)
                        ));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error("ROLLBACK. Failed to insert meme into the cache: (" + tags + ", " + link + ", " + userId + ")");
                rollback();
                return null;
            }

            if(insertTags(memeID, tags)){
                // All changes succeeded, commit to DB and return the ID
                commit();
                return memeID;
            }
        }

        return null;
    }

    /**
     * Promote a meme from the cache to meme table
     * @param id of the meme
     * @param curatorId id of curator
     * @param tags tags of the promoted meme
     * @return id of user who submitted the meme or null in case of error
     */
    public Long promote(Integer id, String curator, Long curatorId, List<String> tags){
        String link;
        Long userId;

        if(!validUser(curatorId, curator))
            return null;

        // Get the link
        try {
            ResultSet rs = executeQuery("SELECT link, submitterId FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
            if(rs != null && rs.next()) {
                link = rs.getString("link");
                userId = rs.getLong("submitterId");
            }
            else
                throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            error("Failed to find a meme with an ID of " + id);
            return null;
        }

        // Put it into the memedb
        if(link != null && userId != null){
            try {
                execute("INSERT INTO " + memeTableName + " (id, link, submitterId, curatorId, timestamp) VALUES (?,?,?,?,?)",
                        Arrays.asList(  new Column(id, Column.ColType.INT),
                                        new Column(link, Column.ColType.STR),
                                        new Column(userId, Column.ColType.LONG),
                                        new Column(curatorId, Column.ColType.LONG),
                                        new Column(new Timestamp(System.currentTimeMillis()).toString(), Column.ColType.STR)
                        ));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error("Failed to promote meme to MemeDB: (" + id + ", " + link + ", " + userId + ", " + curatorId + ")");
                rollback();
                return null;
            }
        }

        // Remove it from the cache
        try {
            execute("DELETE FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to remove meme from cache: (" + id + ", " + link + ", " + userId + ")");
            rollback();
            return null;
        }

        // Delete all the existing tags
        try {
            execute("DELETE FROM " + tagLkpTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to remove old tags from the tag lookup: (" + id + ", " + link + ", " + userId + ")");
            rollback();
            return null;
        }

        // Insert all confirmed tags
        try {
            for(String tag : tags){
                execute("INSERT INTO " + tagLkpTableName + " (id, tag) VALUES (?,?)",
                        Arrays.asList(  new Column(id, Column.ColType.INT),
                                        new Column(tag, Column.ColType.STR)
                        ));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to promote meme to MemeDB: (" + id + ", " + link + ", " + userId + ", " + curatorId + ")");
            rollback();
            return null;
        }

        // All changes succeeded, commit to DB and return the submitter
        commit();
        return userId;
    }

    /**
     * Demote a meme from the meme to cache table
     * @param id of the meme
     * @return id of user who submitted the meme or null in case of error
     */
    public Long demote(Integer id) {
        String link;
        Long userId, curatorId;
        // Get the link
        try {
            ResultSet rs = executeQuery("SELECT link, submitterId, curatorId FROM " + memeTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
            if(rs != null && rs.next()) {
                link = rs.getString("link");
                userId = rs.getLong("submitterId");
                curatorId = rs.getLong("curatorId");
            }
            else
                throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            error("Failed to find a meme with an ID of " + id);
            return null;
        }

        // Put into cache
        try {
            execute("INSERT INTO " + cacheTableName + " (id, link, submitterId) VALUES (?,?,?)",
                    Arrays.asList(  new Column(id, Column.ColType.INT),
                                    new Column(link, Column.ColType.STR),
                                    new Column(userId, Column.ColType.LONG)
                    ));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to demote meme to cache: (" + id + ", " + link + ", " + userId + ", " + curatorId + ")");
            rollback();
            return null;
        }

        // Remove from MemeDB
        try {
            execute("DELETE FROM " + memeTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to remove meme from MemeDB: (" + id + ", " + link + ", " + userId + ", " + curatorId + ")");
            rollback();
            return null;
        }

        // All changes succeeded, commit to DB and return the submitter
        commit();
        return userId;
    }

    /**
     * Remove a meme from the cache and all its tags
     * @param id of the meme
     * @return the id of the submitter or null if an error occurred
     */
    public Long reject(Integer id){
        String link;
        Long userId;

        // Get the link
        try {
            ResultSet rs = executeQuery("SELECT link, submitterId FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
            if(rs != null && rs.next()) {
                link = rs.getString("link");
                userId = rs.getLong("submitterId");;
            }
            else
                throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            error("Failed to find a meme in the cache: (" + id + ")");
            return null;
        }

        // Delete from the cache
        try {
            execute("DELETE FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to remove meme from cache: (" + id + ", " + link + ")");
            rollback();
            return null;
        }

        // Delete from tag lookup
        try {
            execute("DELETE FROM " + tagLkpTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to remove meme from tag lookup: (" + id + ", " + link + ")");
            rollback();
            return null;
        }

        // All changes succeeded, commit to DB and return the submitter
        commit();
        return userId;
    }

    /*
     *
     *  PRIVATE
     *
     */

    /**
     * Print the error message and store it
     * @param error
     */
    private void error(String error){
        errorMsg = error;
        logger.println(ERROR, errorMsg);
    }

    /**
     *  Inerts the tags into the db
     * @param memeID ID of the meme
     * @param tags tags of the meme
     * @return the status of tag insertion
     */
    private Boolean insertTags(Integer memeID, List<String> tags){
        Boolean status = true;
        for(String tag : tags){
            try {
                if(status){
                    execute("INSERT INTO " + tagLkpTableName + " (id, tag) VALUES (?,?)",
                            Arrays.asList(  new Column(memeID, Column.ColType.INT),
                                            new Column(tag, Column.ColType.STR)
                            ));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error("Encountered an error inserting tag " + tag + " into DB, rolling back...");
                rollback();
                return false;
            }
        }
        return true;
    }

    /**
     * Validates the uniqueness of the link across the cache and meme tables
     * @param link
     * @return boolean dictating uniqueness
     */
    private boolean uniqueLink(String link) {
        try {
            ResultSet rs = executeQuery("SELECT name FROM (" +
                    "SELECT id, link, submitterId " +
                    "FROM " + cacheTableName +
                    " UNION " +
                    "SELECT id, link, submitterId " +
                    "FROM " + memeTableName + ") " +
                    "INNER JOIN " + userLkpTableName + " lkp" +
                    " ON submitterId = lkp.id "  +
                    "WHERE link = ? ", Arrays.asList(new Column(link, Column.ColType.STR))
            );

            if(rs != null && rs.next()) {
                String prevSubmitter = rs.getString("name");
                error("This meme was already submitted by " + prevSubmitter);
                return false;
            }
            else
                return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Encountered an error validating uniqueness for " + link);
            return false;
        }
    }

    /**
     * Inserts new ID lookup or validates it already exists
     * @param userId id of user
     * @param username name of user
     * @return whether it is ok to proceed
     */
    private boolean validUser(Long userId, String username) {
        String name;
        if(userId == null || username == null){
            error("Need a non-null username: [" + username + "] and userID: [" + userId + "] to modify the DB");
            return false;
        }

        // Check to see if it already exists in the DB
        try {
            ResultSet rs = executeQuery("SELECT name FROM " + userLkpTableName + " WHERE id = ?", Arrays.asList(new Column(userId, Column.ColType.LONG)));
            if(rs != null && rs.next()) {
                name = rs.getString("name");
                if(!username.equals(name)) {
                    error("Username mismatch for ID: [" + userId + "]. DB has name: [" + name + "], provided: [" + username + "]");
                    return false;
                }
            }
            else{
                try {
                    execute("INSERT INTO " + userLkpTableName + " (id, name) VALUES (?,?)",
                            Arrays.asList(  new Column(userId, Column.ColType.LONG),
                                    new Column(username, Column.ColType.STR)
                            ));
                    commit();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    error("Encountered an error inserting user (" + userId + ", " + username + ") into DB, rolling back...");
                    rollback();
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error("Encountered an error validating user (" + userId + ", " + username + ")");
            return false;
        }
        return true;
    }

    /**
     * execute a query that returns data
     * @param sql sql statement to execute
     * @return the results of query
     */
    private ResultSet executeQuery(String sql) throws SQLException {
        return conn.createStatement().executeQuery(sql);
    }

    /**
     * execute a query that returns data
     * @param sql sql statement to execute
     * @param cols column values
     * @return the results of query
     */
    private ResultSet executeQuery(String sql, List<Column> cols) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for(int i=0;i<cols.size();i++){
            Column col = cols.get(i);
            switch(col.type){
                case LONG:
                    ps.setLong(i+1, (Long) col.var);
                    break;
                case INT:
                    ps.setInt(i+1, (Integer) col.var);
                    break;
                case STR:
                    ps.setString(i+1, (String) col.var);
                    break;
            }
        }
       return ps.executeQuery();
    }

    /**
     * Execute a bunch of no return querys
     * @param sqls sql statements to execute
     * @return status of execution
     */
    private void execute(List<String> sqls) throws SQLException {
        for(String sql : sqls)
            conn.createStatement().execute(sql);
    }

    /**
     * execute a query that needs to be built with typed params
     * @param sql sql statement to prepare
     * @param cols column values
     * @return the status of query
     */
    private void execute(String sql, List<Column> cols) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for(int i=0;i<cols.size();i++){
            Column col = cols.get(i);
            switch(col.type){
                case LONG:
                    ps.setLong(i+1, (Long) col.var);
                    break;
                case INT:
                    ps.setInt(i+1, (Integer) col.var);
                    break;
                case STR:
                    ps.setString(i+1, (String) col.var);
                    break;
            }
        }
        ps.executeUpdate();
    }

    /**
     * rolls back the database
     */
    private void rollback() {
        try {
            conn.rollback();
            errorMsg = "[ ROLLBACK ] " + errorMsg;
            error("Performing DB rollback...");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Commits changes to DB
     * @return whether commit occurred
     */
    private Boolean commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            error("Failed to commit");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Increments the ID count
     * @return the freshest ID
     */
    private Integer getID() {
        headID++;
        return headID;
    }
}
