package app;

import java.sql.*;
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
    Memebase(String filePath){
        this.db = "jdbc:sqlite:" + filePath + dbName;
        System.out.println("Validating tables");
        executeSql(tableDefs);
        // get max ID
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

        executeSql("INSERT INTO " + table + "(id, link) VALUES ()");


    }


    /**
     *
     * @param sql sql statment to execute
     */
    private void executeSql(String sql) {
        executeSql(Arrays.asList(sql));
    }

    /**
     *
     * @param sqls sql statements to execute
     */
    private void executeSql(List<String> sqls) {
        try (Connection conn = DriverManager.getConnection(db)) {
            if (conn != null) {
                for(String sql : sqls){
                    ResultSet rs = conn.createStatement().executeQuery(sql);
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
}
