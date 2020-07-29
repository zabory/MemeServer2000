package app;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MemeDB2000 {
    /*
     *
     * VARS AND SUCH
     *
     */

    static class Column{
        enum ColType{
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

    private String db;
    private Integer headID;
    private Connection conn;
    private String errorMsg;
    static String memeTableName = "memes";
    static String cacheTableName = "cache";
    static String tagLkpTableName = "tag_lkp";
    static String dbName = "meme.db";
    static List<String> tableDefs = Arrays.asList(
            "CREATE TABLE IF NOT EXISTS " + memeTableName + " (" +
                    "id integer PRIMARY KEY UNIQUE," +
                    "link text UNIQUE NOT NULL," +
                    "submitter text NOT NULL," +
                    "curator text NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + tagLkpTableName + " (" +
                    "id integer," +
                    "tag text NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + cacheTableName + " (" +
                    "id integer PRIMARY KEY UNIQUE," +
                    "link text UNIQUE NOT NULL," +
                    "submitter text NOT NULL" +
                    ");"
    );

    /*
     *
     * PUBLIC
     *
     */

    /**
     * Constructor
     * @param filePath The true path where to store this DB
     */
    MemeDB2000(String filePath) {
        this.db = "jdbc:sqlite:" + filePath + dbName;
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

        // Select the max ID from memeDB
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
     * Gets all ids in the cache to load the approve Q
     * @return
     */
    public List<Integer> initialize(){
        List<Integer> retlist = new ArrayList<>();
        try {
            ResultSet rs = executeQuery("SELECT id FROM " + cacheTableName + ";");
            while(rs != null && rs.next()) {
                retlist.add(rs.getInt("id"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
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
    public Integer store(String username, String link, List<String> tags){
        errorMsg = "";
        if(uniqueLink(link)){
            Integer memeID = getID();
            try {
                execute("INSERT INTO " + memeTableName + " (id, link, submitter, curator) VALUES (?,?,?,?)",
                        Arrays.asList(  new Column(memeID, Column.ColType.INT),
                                new Column(link, Column.ColType.STR),
                                new Column(username, Column.ColType.STR),
                                new Column(username, Column.ColType.STR)
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
     * @param username user who submitted meme
     * @param link link to meme
     * @param tags tags for this meme
     * @return the id of the meme
     */
    public Integer cache(String username, String link, List<String> tags){
        if(uniqueLink(link)){
            Integer memeID = getID();
            try {
                execute("INSERT INTO " + cacheTableName + " (id, link, submitter) VALUES (?,?,?)",
                        Arrays.asList(  new Column(memeID, Column.ColType.INT),
                                new Column(link, Column.ColType.STR),
                                new Column(username, Column.ColType.STR)
                        ));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error("ROLLBACK. Failed to insert meme into the cache: (" + tags + ", " + link + ", " + username + ")");
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
     * @param curatorName name of curator
     * @return name of user who submitted the meme or null in case of error
     */
    public String promote(Integer id, String curatorName){
        String link, username;

        // Get the link
        try {
            ResultSet rs = executeQuery("SELECT link, submitter FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
            if(rs != null && rs.next()) {
                link = rs.getString("link");
                username = rs.getString("submitter");
            }
            else
                throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            error("Failed to find a meme with an ID of " + id);
            return null;
        }

        // Put it into the memedb
        if(link != null && username != null){
            try {
                execute("INSERT INTO " + memeTableName + " (id, link, submitter, curator) VALUES (?,?,?,?)",
                        Arrays.asList(  new Column(id, Column.ColType.INT),
                                new Column(link, Column.ColType.STR),
                                new Column(username, Column.ColType.STR),
                                new Column(curatorName, Column.ColType.STR)
                        ));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error("Failed to promote meme to MemeDB: (" + id + ", " + link + ", " + username + ", " + curatorName + ")");
                rollback();
                return null;
            }

        }

        // Remove it from the cache
        try {
            execute("DELETE FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to remove meme from cache: (" + id + ", " + link + ", " + username + ")");
            rollback();
            return null;
        }

        // All changes succeeded, commit to DB and return the submitter
        commit();
        return username;
    }

    /**
     * Demote a meme from the meme to cache table
     * @param id of the meme
     * @return name of user who submitted the meme or null in case of error
     */
    public String demote(Integer id) {
        String link = null, username = null, curator = null;
        // Get the link
        try {
            ResultSet rs = executeQuery("SELECT link, submitter, curator FROM " + memeTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
            if(rs != null && rs.next()) {
                link = rs.getString("link");
                username = rs.getString("submitter");
                curator = rs.getString("curator");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            error("Failed to find a meme with an ID of " + id);
            return null;
        }

        // Put into cache
        try {
            execute("INSERT INTO " + cacheTableName + " (id, link, submitter) VALUES (?,?,?)",
                    Arrays.asList(  new Column(id, Column.ColType.INT),
                                    new Column(link, Column.ColType.STR),
                                    new Column(username, Column.ColType.STR)
                    ));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to demote meme to cache: (" + id + ", " + link + ", " + username + ", " + curator + ")");
            rollback();
            return null;
        }

        // Remove from MemeDB
        try {
            execute("DELETE FROM " + memeTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            error("Failed to remove meme from MemeDB: (" + id + ", " + link + ", " + username + ", " + curator + ")");
            rollback();
            return null;
        }

        // All changes succeeded, commit to DB and return the submitter
        commit();
        return username;
    }

    /**
     * Remove a meme from the cache and all its tags
     * @param id of the meme
     * @return the username of the submitter or null if an error occurred
     */
    public String reject(Integer id){
        String link = null, username = null;

        // Get the link
        try {
            ResultSet rs = executeQuery("SELECT link, submitter FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
            if(rs != null && rs.next()) {
                link = rs.getString("link");
                username = rs.getString("submitter");;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            error("Failed to find a meme in the cache: (" + id + ", " + link + ")");
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
        return username;
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
        System.out.println(errorMsg);
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
                            Arrays.asList(   new Column(memeID, Column.ColType.INT),
                                    new Column(tag, Column.ColType.STR)
                            ));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                errorMsg = "Encountered an error inserting tag " + tag + " into DB, rolling back...";
                System.out.println(errorMsg);
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
            ResultSet rs = executeQuery("SELECT * FROM (" +
                    "SELECT id, link, submitter " +
                    "FROM " + cacheTableName +
                    " UNION " +
                    "SELECT id, link, submitter " +
                    "FROM " + memeTableName + ") " +
                    "WHERE link = ?", Arrays.asList(new Column(link, Column.ColType.STR)));

            if(rs != null && rs.next()) {
                String previousSubmitter = rs.getString("submitter");
                error("This meme was already submitted by " + previousSubmitter);
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
            System.out.println("Performing DB rollback...");
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
            System.out.println("Failed to commit");
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
