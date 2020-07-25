package app;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MemeBase {
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

    String db;
    Integer headID;
    Connection conn;
    static String memeTableName = "memes";
    static String cacheTableName = "cached_memes";
    static String tagLkpTableName = "tag_lkp";
    static String dbName = "meme.db";
    static List<String> tableDefs = Arrays.asList(
            "CREATE TABLE IF NOT EXISTS " + memeTableName + " (" +
                    "id integer PRIMARY KEY UNIQUE," +
                    "link text NOT NULL," +
                    "submitter text NOT NULL," +
                    "curator text NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + tagLkpTableName + " (" +
                    "id integer," +
                    "tag text NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + cacheTableName + " (" +
                    "id integer PRIMARY KEY UNIQUE," +
                    "link text NOT NULL," +
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
    MemeBase(String filePath) {
        this.db = "jdbc:sqlite:" + filePath + dbName;
    }

    /**
     * Connects to DB and builds the DB environment
     * @throws SQLException
     */
    public Boolean open()  {
        try{
            conn = DriverManager.getConnection(db);
            conn.setAutoCommit(false);
            execute(tableDefs);
            Integer cacheMax = null;
            ResultSet rs = executeQuery("SELECT MAX(id) m FROM " + memeTableName + ";");
            if(rs != null && rs.next())
                headID = rs.getInt("m");
            else
                headID = 0;
            rs = executeQuery("SELECT MAX(id) m FROM " + cacheTableName + ";");
            if(rs != null && rs.next())
                cacheMax = rs.getInt("m");

            if(cacheMax > headID)
                headID = cacheMax;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
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
            return false;
        }
        return true;
    }

    /**
     * Get a meme that matches the affiliated tags
     * @param tags The tags that this meme must have
     * @return  null if no meme exists with all provided tags
     *          link to a meme if atleast one exists that exists
     */
    public String get(List<String> tags){
        return null;
    }

    /**
     * Insert into the meme table
     * @param username name of submitting user
     * @param link A link to a meme
     * @param tags Tags associated with this meme
     * @return true if success
     */
    public Integer store(String username, String link, List<String> tags){
        Integer memeID = getID();
        Boolean status = execute("INSERT INTO " + memeTableName + " (id, link, submitter, curator) VALUES (?,?,?,?)",
                Arrays.asList(  new Column(memeID, Column.ColType.INT),
                                new Column(link, Column.ColType.STR),
                                new Column(username, Column.ColType.STR),
                                new Column(username, Column.ColType.STR)
                ));
        if(status)
            status = insertTags(memeID, tags);
        return status ? memeID : null;
    }

    /**
     * Insert into cache table
     * @param username user who submitted meme
     * @param link link to meme
     * @param tags tags for this meme
     * @return the id of the meme
     */
    public Integer cache(String username, String link, List<String> tags){
        Integer memeID = getID();
        Boolean status = execute("INSERT INTO " + cacheTableName + " (id, link, submitter) VALUES (?,?,?)",
                Arrays.asList(  new Column(memeID, Column.ColType.INT),
                        new Column(link, Column.ColType.STR),
                        new Column(username, Column.ColType.STR)
                ));
        if(status)
            status = insertTags(memeID, tags);
        return status ? memeID : null;
    }

    /**
     * Promote a meme from the cache to meme table
     * @param id of the meme
     * @param curatorName name of curator
     * @return
     */
    public Boolean promote(Integer id, String curatorName){
        String link = null;
        String username = null;
        // Get the link
        ResultSet rs = executeQuery("SELECT link, submitter FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
        try {
            if(rs != null && rs.next()) {
                link = rs.getString("link");
                username = rs.getString("submitter");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to find a meme with an ID of " + id);
            return false;
        }

        Boolean status = execute("INSERT INTO " + memeTableName + " (id, link, submitter, curator) VALUES (?,?,?,?)",
                Arrays.asList(  new Column(id, Column.ColType.INT),
                        new Column(link, Column.ColType.STR),
                        new Column(username, Column.ColType.STR),
                        new Column(curatorName, Column.ColType.STR)
                ));

        if(!status){
            System.out.println("Failed to promote meme to MemeDB (" + id + ", " + link + ", " + username + ", " + curatorName + ")");
            return false;
        }

        status = execute("DELETE FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        if(!status){
            System.out.println("Failed to remove meme from cache (" + id + ", " + link + ", " + username + ")");
            return false;
        }

        if(status)
            status = commit();
        else
            rollback();
        return status;
    }

    /**
     * Demote a meme from the meme to cache table
     * @param id of the meme
     * @return
     */
    public Boolean demote(Integer id) {
        String link = null, username = null, curator = null;
        // Get the link
        ResultSet rs = executeQuery("SELECT link, submitter, curator FROM " + memeTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
        try {
            if(rs != null && rs.next()) {
                link = rs.getString("link");
                username = rs.getString("submitter");
                curator = rs.getString("curator");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to find a meme with an ID of " + id);
            return false;
        }

        Boolean status = execute("INSERT INTO " + cacheTableName + " (id, link, submitter) VALUES (?,?,?)",
                Arrays.asList(  new Column(id, Column.ColType.INT),
                                new Column(link, Column.ColType.STR),
                                new Column(username, Column.ColType.STR)
                ));

        if(!status){
            System.out.println("Failed to demote meme to cache(" + id + ", " + link + ", " + username + ", " + curator + ")");
            return false;
        }

        status = execute("DELETE FROM " + memeTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        if(!status){
            System.out.println("Failed to remove meme from MemeDB (" + id + ", " + link + ", " + username + ", " + curator + ")");
            return false;
        }

        if(status)
            status = commit();
        else
            rollback();
        return status;
    }

    /**
     * Remove a meme from the cache and all its tags
     * @param id of the meme
     * @return the link to the meme
     */
    public String reject(Integer id){
        String link = null;
        // Get the link
        ResultSet rs = executeQuery("SELECT link FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(new Column(id, Column.ColType.INT)));
        try {
            if(rs != null && rs.next()) {
                link = rs.getString("link");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to find a meme with an ID of " + id);
            return null;
        }
        Boolean status = execute("DELETE FROM " + cacheTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        if(!status){
            System.out.println("Failed to remove meme from cache: " + id);
            return null;
        }
       status = execute("DELETE FROM " + tagLkpTableName + " WHERE id = ?", Arrays.asList(  new Column(id, Column.ColType.INT)));
        if(!status){
            System.out.println("Failed to remove meme from tag_lkp: " + id);
            return null;
        }
        if(status)
            status = commit();
        else
            rollback();
        return status ? link : null;
    }

    /*
     *
     *  PRIVATE
     *
     */

    /**
     * execute a query that returns data
     * @param sql sql statement to execute
     * @return the results of query
     */
    private ResultSet executeQuery(String sql) {
        ResultSet rs = null;
        try {
            rs = conn.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * execute a query that returns data
     * @param sql sql statement to execute
     * @param cols column values
     * @return the results of query
     */
    private ResultSet executeQuery(String sql, List<Column> cols) {
        ResultSet rs = null;
        try {
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
            rs = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * Execute a bunch of no return querys
     * @param sqls sql statements to execute
     * @return status of execution
     */
    private Boolean execute(List<String> sqls) {
        Boolean retVal = true;
        try {
            for(String sql : sqls)
                conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to execute SQL statments: " + sqls);
            System.out.println("Attempting to rollback...");
            rollback();
            retVal = false;
        }
        return retVal;
    }

    /**
     * execute a query that needs to be built with typed params
     * @param sql sql statement to prepare
     * @param cols column values
     * @return the status of query
     */
    private Boolean execute(String sql, List<Column> cols) {
        Boolean ret = true;
        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to execute SQL statment: " + sql);
            System.out.println("Attempting to rollback...");
            rollback();
            ret = false;
        }
        return ret;
    }

    /**
     * rolls back the database
     */
    private void rollback() {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
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
     *  Inerts the tags into the db
     * @param memeID ID of the meme
     * @param tags tags of the meme
     * @return the status of tag insertion
     */
    private Boolean insertTags(Integer memeID, List<String> tags){
        Boolean status = true;
        for(String tag : tags){
            if(status){
                status &= execute("INSERT INTO " + tagLkpTableName + " (id, tag) VALUES (?,?)",
                        Arrays.asList(   new Column(memeID, Column.ColType.INT),
                                new Column(tag, Column.ColType.STR)
                        ));
            }
        }

        if(status)
            status = commit();
        else {
            System.out.println("Encountered and error, rolling back...");
            rollback();
        }
        return status;
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
