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
                    "id integer PRIMARY KEY," +
                    "link text NOT NULL" +
                    ");",

            "CREATE TABLE ID NOT EXISTS " + cacheTableName + " (" +
                    "id integer PRIMARY KEY," +
                    "link text NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + tagLkpTableName + " (" +
                    "id integer PRIMARY KEY," +
                    "tag text NOT NULL" +
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
        ResultSet rs = executeQuery("SELECT MAX(id)");
        if(rs != null)
            id = rs.getInt("id");
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
     *
     * @param link A link to a meme
     * @param tags Tags associated with this meme
     * @param isCurator whether or not the inserter is a curator
     * @return true if success
     */
    public Boolean submitMeme(String username, Boolean isCurator, String link, List<String> tags){
        String table = cacheTableName;
        if(isCurator){
            table = memeTableName;
        }

        execute("INSERT INTO " + table + "(id, link) VALUES (" + getID() + "," + link + ")");
        for(String tag : tags){
            execute("INSERT INTO " + tagLkpTableName + "(id, tag) VALUES (" + id + "," + tag + ")");
        }

        return false;
    }

    private void execute(String sql) {
        execute(Arrays.asList(sql));
    }
    /**
     *
     * @param sqls sql statements to execute
     */
    private void execute(List<String> sqls) {
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
        }
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
