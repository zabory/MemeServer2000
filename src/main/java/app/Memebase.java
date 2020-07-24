package app;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Memebase {
    String db;
    Integer id;
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

    /**
     * Establishes a fresh database if none exists,
     * else resumes connection to existing db
     * @param filePath The true path where to store this DB
     */
    Memebase(String filePath) throws SQLException {
        this.db = "jdbc:sqlite:" + filePath + dbName;
        System.out.println("Validating tables");
        execute(tableDefs);
        ResultSet rs = executeQuery("SELECT MAX(id) FROM " + memeTableName + ";");
        if(rs != null && rs.getFetchSize() != 0)
            id = rs.getInt("MAX(id)");
        else
            id = 0;
    }

    /**
     *
     * @param tags The tags that this meme must have
     * @return  null if no meme exists with all provided tags
     *          link to a meme if atleast one exists that exists
     */
    public String getMeme(List<String> tags){
        return null;
    }

    /**
     * Insert into the meme table
     * @param username name of submitting user
     * @param link A link to a meme
     * @param tags Tags associated with this meme
     * @return true if success
     */
    public Boolean submitMeme(String username, String link, List<String> tags){
        Integer memeID = getID();
        Boolean status = execute("INSERT INTO " + memeTableName + "(id, link, submitter, curator) " +
                "VALUES (" + memeID + "," + link + "," + username + "," + username + ")");
        for(String tag : tags){
           status &= execute("INSERT INTO " + tagLkpTableName + "(id, tag) VALUES (" + memeID + "," + tag + ")");
        }

        return status;
    }

    public Boolean cacheMeme(String username, String link, List<String> tags){
        return false;
    }

    public Boolean promoteMeme(Integer id){
        return false;
    }

    private Boolean execute(String sql) {
        return execute(Arrays.asList(sql));
    }
    /**
     *
     * @param sqls sql statements to execute
     */
    private Boolean execute(List<String> sqls) {
        try (Connection conn = DriverManager.getConnection(db)) {
            if (conn != null) {
                for(String sql : sqls){
                    conn.createStatement().execute(sql);
                }
            }
            else{
                System.out.println("Failed to connect to " + db);
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     *
     * @param sql sql statement to execute
     */
    private ResultSet executeQuery(String sql) {
        ResultSet rs = null;
        try (Connection conn = DriverManager.getConnection(db)) {
            if (conn != null) {
                rs = conn.createStatement().executeQuery(sql);
            }
            else{
                System.out.println("Failed to connect to " + db);
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    private Integer getID() {
        id++;
        return id;
    }
}
