package database;

import app.MemeConfigLoader3000;
import datastructures.MemeLogger3000;
import org.junit.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

public class MemeDB2000Test {
    static MemeDB2000 memebase;
    static MemeConfigLoader3000 config;

    @BeforeClass
    public static void construct(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("app");
        context.refresh();
        config = context.getBean(MemeConfigLoader3000.class);
        context.close();
        memebase = new MemeDB2000(config, new MemeLogger3000());
    }

    @Before
    public void before(){
        assertTrue(memebase.open());
    }

    @After
    public void after(){
        assertTrue(memebase.close());
        // cleanup the DB after a test
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + config.getDatabaseLocation());
            conn.createStatement().execute("DELETE FROM " + config.getMemeTableName());
            conn.createStatement().execute("DELETE FROM " + config.getCacheTableName());
            conn.createStatement().execute("DELETE FROM " + config.getTagLkpTableName());
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void adminSubmitTest() {
        Integer expecID = 1;
        String link = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg";
        assertEquals(expecID, memebase.store("Ziggy", link, Arrays.asList("meta", "books")));
        assertEquals("Ziggy", memebase.demote(expecID));
        assertEquals("Ziggy", memebase.reject(expecID));
    }

    @Test
    public void submitApproveTest() {
        Integer expecID = 1;
        String link = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg";
        assertEquals(expecID, memebase.cache("DANIEL THE FUGLY", link, Arrays.asList("daniel", "ugly", "yeet")));
        assertEquals("DANIEL THE FUGLY", memebase.promote(expecID, "Ziggy", Arrays.asList("daniel", "newtag", "yeet")));
        assertEquals(link, memebase.get(Arrays.asList("daniel", "newtag")));
        assertEquals("DANIEL THE FUGLY", memebase.demote(expecID));
        assertEquals("DANIEL THE FUGLY", memebase.reject(expecID));
    }

    @Test
    public void manyMemeResponseTest() {
        Integer ID1 = 1, ID2 = 2, ID3 = 3;
        String link1 = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg",
                link2 = "https://cdn.discordapp.com/attachments/647667357879107584/736409444577050764/MemeBot2000.jpg",
                link3 = "https://cdn.discordapp.com/attachments/647667357879107584/735864874932109322/xvOzIkw.jpeg";
        assertEquals(ID1, memebase.cache("Owen", link1, Arrays.asList("meta", "books")));
        assertEquals(ID2, memebase.cache("Ethan", link2, Arrays.asList("server", "diagram")));
        assertEquals(ID3, memebase.cache("Ziggy", link3, Arrays.asList("oracle", "sql", "dog", "animal", "test")));

        assertEquals("Owen", memebase.promote(ID1, "Ziggy", Arrays.asList("meta", "books")));
        assertEquals(null, memebase.promote(ID1, "Ziggy", Arrays.asList("meta", "books")));
        assertEquals("Ethan", memebase.promote(ID2, "Ziggy", Arrays.asList("oracle", "sql", "dog", "animal", "test")));
        assertEquals("Ziggy", memebase.reject(ID3));
    }

    @Test
    public void simpleGetTest() {
        Integer expecID = 1;
        String link = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg";
        assertEquals(expecID, memebase.store("Ziggy", link, Arrays.asList("meta", "books")));
        assertEquals(link, memebase.get(new ArrayList<String>()));
    }

    @Test
    public void noMemeTest() {
        Integer expecID = 1;
        String link = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg";
        assertEquals(expecID, memebase.store("Ziggy", link, Arrays.asList("meta", "books")));
        assertEquals("Ziggy", memebase.demote(expecID));
        assertEquals("Ziggy", memebase.reject(expecID));
        assertEquals(null, memebase.get(new ArrayList<String>()));
    }

    @Test
    public void noTagTest() {
        Integer ID1 = 1, ID2 = 2, ID3 = 3;
        String link1 = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg",
                link2 = "https://cdn.discordapp.com/attachments/647667357879107584/736409444577050764/MemeBot2000.jpg",
                link3 = "https://cdn.discordapp.com/attachments/647667357879107584/735864874932109322/xvOzIkw.jpeg";
        assertEquals(ID1, memebase.store("Ziggy", link1, Arrays.asList("meta", "books")));
        assertEquals(ID2, memebase.store("Ziggy", link2, Arrays.asList("server", "diagram")));
        assertEquals(ID3, memebase.store("Ziggy", link3, Arrays.asList("oracle", "sql", "dog", "animal", "test")));

        assertEquals(null, memebase.get(Arrays.asList("YOOOOOOOOO", "didly squat")));
    }

    @Test
    public void specMemeTest() {
        Integer ID1 = 1, ID2 = 2, ID3 = 3;
        String link1 = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg",
                link2 = "https://cdn.discordapp.com/attachments/647667357879107584/736409444577050764/MemeBot2000.jpg",
                link3 = "https://cdn.discordapp.com/attachments/647667357879107584/735864874932109322/xvOzIkw.jpeg";
        assertEquals(ID1, memebase.store("Ziggy", link1, Arrays.asList("meta", "books")));
        assertEquals(ID2, memebase.store("Ziggy", link2, Arrays.asList("server", "diagram")));
        assertEquals(ID3, memebase.store("Ziggy", link3, Arrays.asList("oracle", "sql", "dog", "animal", "test")));

        assertEquals(link1, memebase.get(Arrays.asList("books")));
        assertEquals(link2, memebase.get(Arrays.asList("diagram")));
        assertEquals(null, memebase.get(Arrays.asList("nudes")));
        assertEquals(link3, memebase.get(Arrays.asList("animal", "oracle", "dog")));
    }

    @Test
    public void getByIDTest() {
        Integer ID1 = 1, ID2 = 2, ID3 = 3, ID4 = 4;
        String link1 = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg",
                link2 = "https://cdn.discordapp.com/attachments/647667357879107584/736409444577050764/MemeBot2000.jpg",
                link3 = "https://cdn.discordapp.com/attachments/647667357879107584/735864874932109322/xvOzIkw.jpeg";
        assertEquals(ID1, memebase.store("Ziggy", link1, Arrays.asList("meta", "books")));
        assertEquals(ID2, memebase.store("Ziggy", link2, Arrays.asList("server", "diagram")));
        assertEquals(ID3, memebase.store("Ziggy", link3, Arrays.asList("oracle", "sql", "dog", "animal", "test")));

        assertEquals(link3, memebase.get(ID3));
        assertEquals(link2, memebase.get(ID2));
        assertEquals(null, memebase.get(ID4));

        assertEquals("Ziggy", memebase.demote(ID3));
        assertEquals(null, memebase.get(ID3));
        assertEquals("Ziggy", memebase.reject(ID3));
        assertEquals(null, memebase.get(ID3));

        assertEquals("Ziggy", memebase.demote(ID1));
        assertEquals(null, memebase.get(ID1));
        assertEquals("Ziggy", memebase.promote(ID1, "Zabory", Arrays.asList("oracle", "sql", "dog", "animal", "test")));
        assertEquals(link1, memebase.get(ID1));
    }

    @Test
    public void uniqueLinkTest() {
        Integer ID1 = 1, ID2 = 2;
        String link1 = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg",
                link2 = "https://cdn.discordapp.com/attachments/647667357879107584/736409444577050764/MemeBot2000.jpg";
        assertEquals(ID1, memebase.store("Ziggy", link1, Arrays.asList("meta", "books")));
        assertEquals(null, memebase.store("Ziggy", link1, Arrays.asList("meta", "books")));

        assertEquals(ID2, memebase.cache("Ziggy", link2, Arrays.asList("server", "diagram")));
        assertEquals(null, memebase.cache("Ziggy", link2, Arrays.asList("server", "diagram")));

        assertEquals(null, memebase.cache("Ziggy", link1, Arrays.asList("meta", "books")));
        assertEquals(null, memebase.store("Ziggy", link2, Arrays.asList("server", "diagram")));
    }

    @Test
    public void getTags() {
        Integer ID1 = 1, ID2 = 2, ID3 = 3, ID4 = 4;
        String link1 = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg",
                link2 = "https://cdn.discordapp.com/attachments/647667357879107584/736409444577050764/MemeBot2000.jpg",
                link3 = "https://cdn.discordapp.com/attachments/647667357879107584/735864874932109322/xvOzIkw.jpeg",
                link4 = "https://cdn.discordapp.com/attachments/647667357879107584/735864874932109322/xvOzIke.jpeg";
        assertEquals(ID1, memebase.store("Ziggy", link1, Arrays.asList("meta", "books")));
        assertEquals(ID2, memebase.store("Ziggy", link2, Arrays.asList("server", "diagram")));
        assertEquals(ID3, memebase.cache("Ziggy", link3, Arrays.asList("oracle", "sql", "dog", "animal", "test")));
        assertEquals(ID4, memebase.store("Ziggy", link4, Arrays.asList("fake", "books", "cat", "server")));

        Set<String> tagSet = new HashSet<String>();
        tagSet.add("meta (1)");
        tagSet.add("books (2)");
        tagSet.add("diagram (1)");
        tagSet.add("cat (1)");
        tagSet.add("fake (1)");
        tagSet.add("server (2)");
        List<String> tags = memebase.getTags();
        assertEquals(6, tags.size());
        for(String tag : tags){
            if(tagSet.contains(tag))
                tagSet.remove(tag);
            else
                assertTrue(false);
        }

        tagSet = new HashSet<String>();
        tagSet.add("oracle");
        tagSet.add("sql");
        tagSet.add("dog");
        tagSet.add("animal");
        tagSet.add("test");
        tags = memebase.getTags(ID3);
        assertEquals(5, tags.size());
        for(String tag : tags){
            if(tagSet.contains(tag))
                tagSet.remove(tag);
            else
                assertTrue(false);
        }



    }
}
