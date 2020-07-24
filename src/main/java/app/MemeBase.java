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
    Integer id;
    Connection conn;
    static String memeTableName = "memes";
    static String cacheTableName = "cache";
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
    public void open() throws SQLException {
        conn = DriverManager.getConnection(db);
        execute(tableDefs);
        ResultSet rs = executeQuery("SELECT MAX(id) m FROM " + memeTableName + ";");
        if(rs != null && rs.next())
            id = rs.getInt("m");
        else
            id = 0;
    }

    /**
     * Closes the DB
     */
    public void close(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        Boolean status = execute("INSERT INTO " + memeTableName + "(id, link, submitter, curator) VALUES (?,?,?,?)",
                Arrays.asList(  new Column(memeID, Column.ColType.INT),
                                new Column(link, Column.ColType.STR),
                                new Column(username, Column.ColType.STR),
                                new Column(username, Column.ColType.STR)
                ));

        for(String tag : tags){
           status &= execute("INSERT INTO " + tagLkpTableName + "(id, tag) VALUES (?,?)",
                   Arrays.asList(   new Column(memeID, Column.ColType.INT),
                                    new Column(tag, Column.ColType.STR)
                   ));
        }
        return status ? memeID : null;
    }

    /**
     * Insert into cache table
     * @param username user who submitted meme
     * @param link link to meme
     * @param tags tags for this meme
     * @return
     */
    public Integer cache(String username, String link, List<String> tags){
        return null;
    }

    /**
     * Promote a meme from the cache to meme table
     * @param id of the meme
     * @return
     */
    public Integer promote(Integer id){
        return null;
    }

    /*
     *
     *  PRIVATE
     *
     */

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
            retVal = false;
        }
        return retVal;
    }

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
            ret = false;
        }
        return ret;
    }

    /**
     * Increments the ID count
     * @return the freshest ID
     */
    private Integer getID() {
        id++;
        return id;
    }
}
