package database;

import app.MemeConfigLoader3000;
import datastructures.MemeDBMsg3000;
import datastructures.MemeLogger3000;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static datastructures.MemeDBMsg3000.MsgDBType.*;
import static datastructures.MemeDBMsg3000.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MemeDBC3000Test {
    static MemeDBC3000 controller;
    static BlockingQueue inputQ, outputQ;
    static MemeConfigLoader3000 config;

    @BeforeClass
    public static void construct(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("app");
        context.refresh();
        config = context.getBean(MemeConfigLoader3000.class);
        context.close();
    }

    @Before
    public void before(){
        // connect to the db
        inputQ = new LinkedBlockingQueue(100);
        outputQ = new LinkedBlockingQueue(100);
        controller = new MemeDBC3000(config, new MemeLogger3000(), inputQ, outputQ);
        controller.start();
    }

    @After
    public void after(){
        try {
            inputQ.put(new MemeDBMsg3000().type(TERMINATE));

            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + config.getDatabaseLocation());
            conn.createStatement().execute("DELETE FROM " + config.getMemeTableName());
            conn.createStatement().execute("DELETE FROM " + config.getCacheTableName());
            conn.createStatement().execute("DELETE FROM " + config.getTagLkpTableName());
            conn.createStatement().execute("DELETE FROM " + config.getUserLkpTableName());
            conn.close();

            controller.join();
        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicStoreTest(){
        try {
            inputQ.put(new MemeDBMsg3000()
                    .type(STORE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .userID(1L)
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg3000 msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals(null, msg.getId());
            assertEquals(Long.valueOf(1), msg.getUserID());
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicPromoteTest(){
        try {
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .userID(1L)
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg3000 msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals(Long.valueOf(1L), msg.getUserID());

            inputQ.put(new MemeDBMsg3000()
                    .type(PROMOTE_MEME).id(1)
                    .username("Ziggy")
                    .userID(2L)
                    .tags(Arrays.asList("bread", "seals", "Charlie")));
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(CURATE_RESULT, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());
            assertEquals(Long.valueOf(1L), msg.getUserID());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicRejectTest(){
        try {
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .userID(1L)
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg3000 msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals(Long.valueOf(1L), msg.getUserID());

            inputQ.put(new MemeDBMsg3000()
                    .type(REJECT_MEME).id(1)
                    .username("Ziggy")
                    .userID(1L));
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(CURATE_RESULT, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());
            assertEquals(Long.valueOf(1L), msg.getUserID());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicDemoteTest(){
        try {
            inputQ.put(new MemeDBMsg3000()
                    .type(STORE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .userID(1L)
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg3000 msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals(null, msg.getId());
            assertEquals(Long.valueOf(1L), msg.getUserID());

            inputQ.put(new MemeDBMsg3000()
                    .type(DEMOTE_MEME).id(1));
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(REPLENISH_Q, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());
            assertEquals(Long.valueOf(1L), msg.getUserID());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicGetMemeToApproveTest(){
        try {
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .userID(10L)
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg3000 msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals(Long.valueOf(10), msg.getUserID());

            inputQ.put(new MemeDBMsg3000().type(GET_MEME_ID).id(1));
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(APPROVE_MEME, msg.getType());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicGetMemeTagsTest(){
        try {
            inputQ.put(new MemeDBMsg3000()
                    .type(STORE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .userID(25L)
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg3000 msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals(null, msg.getId());
            assertEquals(Long.valueOf(25), msg.getUserID());

            inputQ.put(new MemeDBMsg3000()
                    .type(GET_MEME_TAGS)
                    .tags(Arrays.asList("seals"))
                    .username("Bendu"));
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(MEME, msg.getType());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicGetTagsTest(){
        try {
            String link = "a";
            for(int i=0;i<10;i++){
                inputQ.put(new MemeDBMsg3000()
                        .type(STORE_MEME)
                        .link(link)
                        .username("Ziggy")
                        .userID(1L)
                        .tags(Arrays.asList("dog", "cat"))
                );
                link += "a";
            }
            inputQ.put(new MemeDBMsg3000()
                    .type(STORE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .userID(100L)
                    .tags(Arrays.asList("bread", "seals")));
            for(int i=0;i<11;i++)
                outputQ.take();

            inputQ.put(new MemeDBMsg3000().type(GET_TAGS));
            MemeDBMsg3000 msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ALL_TAGS, msg.getType());
            assertEquals(4, msg.getTags().size());
            Set<String> tagSet = new HashSet<String>();
            tagSet.add("bread (1)");
            tagSet.add("seals (1)");
            tagSet.add("cat (10)");
            tagSet.add("dog (10)");
            for(String tag : msg.getTags()){
                if(tagSet.contains(tag))
                    tagSet.remove(tag);
                else
                    assertTrue(false);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void errorTest(){

        // run 50 CACHE commands
        String link = "b";
        try{
            for(int i=0;i<50;i++){
                String user = getRandomName();
                Long id = getUserId(user);
                inputQ.put(new MemeDBMsg3000()
                        .type(CACHE_MEME)
                        .link(link)
                        .username(user)
                        .userID(id)
                        .tags(getRandomTagList(4))
                );
                link += "b";
            }

            String user = getRandomName();
            Long id = getUserId(user);
            inputQ.put(new MemeDBMsg3000()
                    .type(STORE_MEME)
                    .link("testlinkstore")
                    .username(user)
                    .userID(id)
                    .tags(getRandomTagList(4))
            );

            user = getRandomName();
            id = getUserId(user);
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("testlink")
                    .username(user)
                    .userID(id)
                    .tags(getRandomTagList(4))
            );

            for(int i=0;i<52;i++)
                outputQ.take();

            // cant extract a meme for a tag that doesnt exist
            inputQ.put(new MemeDBMsg3000()
                    .type(GET_MEME_TAGS)
                    .username(getRandomName())
                    .tags(Arrays.asList("idontexist"))
            );
            MemeDBMsg3000 msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            // cant extract a meme for an id that doesnt exist
            inputQ.put(new MemeDBMsg3000()
                    .type(GET_MEME_ID)
                    .username(getRandomName())
                    .id(100)
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            user = getRandomName();
            id = getUserId(user);
            // cant store a meme that exists
            inputQ.put(new MemeDBMsg3000()
                    .type(STORE_MEME)
                    .username(user)
                    .userID(id)
                    .link("testlinkstore")
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            user = getRandomName();
            id = getUserId(user);
            // cant cache a meme that exists
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .username(user)
                    .userID(id)
                    .link("testlink")
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            user = getRandomName();
            id = getUserId(user);
            // cant store a meme that exists in the cache
            inputQ.put(new MemeDBMsg3000()
                    .type(STORE_MEME)
                    .username(user)
                    .userID(id)
                    .link("testlink")
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            user = getRandomName();
            id = getUserId(user);
            // cant cache a meme that exists
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .username(user)
                    .userID(id)
                    .link("testlinkstore")
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            user = getRandomName();
            id = getUserId(user);
            // cant promote a meme that isnt in the cache
            inputQ.put(new MemeDBMsg3000()
                    .type(PROMOTE_MEME)
                    .id(100)
                    .username(user)
                    .userID(id)
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            // cant demote a meme that isnt in the meme table
            inputQ.put(new MemeDBMsg3000()
                    .type(DEMOTE_MEME)
                    .id(100)
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            user = getRandomName();
            id = getUserId(user);
            // cant reject a meme that isnt in the cache
            inputQ.put(new MemeDBMsg3000()
                    .type(REJECT_MEME)
                    .id(100)
                    .username(user)
                    .userID(id)
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            // cannot submit a meme without an id
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("testlink")
                    .username("screee")
                    .userID(null)
                    .tags(getRandomTagList(4))
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            // cannot submit a meme without a username
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("testlink")
                    .username(null)
                    .userID(1L)
                    .tags(getRandomTagList(4))
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            // cannot submit a meme with a different pair of ID and usernames
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("userLink")
                    .username("ADMIN")
                    .userID(100L)
                    .tags(getRandomTagList(4))
            );
            outputQ.take();
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("userLink2")
                    .username("NOT_ADMIN")
                    .userID(100L)
                    .tags(getRandomTagList(4))
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

            // cannot move a meme around without a valid user and id combo
            inputQ.put(new MemeDBMsg3000()
                    .type(CACHE_MEME)
                    .link("userLink2")
                    .username("ADMIN")
                    .userID(100L)
                    .tags(getRandomTagList(4))
            );
            outputQ.take();
            inputQ.put(new MemeDBMsg3000()
                    .type(PROMOTE_MEME)
                    .id(54)
                    .username("NOT_ADMIN")
                    .userID(100L)
            );
            msg = (MemeDBMsg3000) outputQ.take();
            assertEquals(ERROR, msg.getType());

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void stressTest(){
        String link = "a";

        try {
            // run 10 STORE commands
            for(int i=0;i<10;i++){
                inputQ.put(new MemeDBMsg3000()
                        .type(STORE_MEME)
                        .link(link)
                        .username("Ziggy")
                        .userID(100L)
                        .tags(getRandomTagList(4))
                );
                link += "a";
            }

            // run 10 CACHE commands
            link = "b";
            for(int i=0;i<10;i++){
                String user = getRandomName();
                Long id = getUserId(user);

                inputQ.put(new MemeDBMsg3000()
                        .type(CACHE_MEME)
                        .link(link)
                        .username(user)
                        .userID(id)
                        .tags(getRandomTagList(4))
                );
                link += "b";
                outputQ.take();
            }

            // take the rest of the messages out
            for(int i=1;i<=10;i++)
                outputQ.take();

            // perform random actions on all inserted
            for(int i=11;i<=20;i++){
                inputQ.put(new MemeDBMsg3000()
                        .type(getRandMemeMsg())
                        .id(i)
                        .tags(getRandomTagList(8))
                );
            }

            // perform random actions on all inserted
            for(int i=11;i<=20;i++){
                inputQ.put(new MemeDBMsg3000()
                        .type(getRandCurateMsg())
                        .id(i)
                        .username("Ziggy")
                        .userID(100L)
                        .tags(Arrays.asList("testtag", "testing tags"))
                );
                outputQ.take();
            }

            // take the rest of the messages out
            for(int i=1;i<=10;i++)
                outputQ.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    List<String> getRandomTagList(Integer factor){
        List<String> tags = Arrays.asList("yugioh", "dragon", "charlie", "minecraft", "art", "template", "world", "pitbull", "dog", "pirates",
                                            "bendu", "computer", "fruit", "meta", "mario", "pencil", "bread", "seals", "farts", "sunglasses");
        List<String> rettags = new ArrayList();
        Random rand = new Random();
        for(int i=0;i<tags.size()/factor;i++){
            String pick = tags.get(rand.nextInt(tags.size()-1));
            if(!rettags.contains(pick))
                rettags.add(pick);
        }
        return rettags;
    }

    String getRandomName(){
        List<String> names = Arrays.asList("Owen", "Ethan", "Anthony", "Kat", "Mike", "BenDu", "BenShu", "BenMu", "Reba", "Charlie", "Mario");
        Random rand = new Random();
        return names.get(rand.nextInt(names.size()-1));
    }

    private Long getUserId(String user) {
        return Long.valueOf(Arrays.asList("Owen", "Ethan", "Anthony", "Kat", "Mike", "BenDu", "BenShu", "BenMu", "Reba", "Charlie", "Mario").indexOf(user));
    }

    MsgDBType getRandMemeMsg(){
        List<MsgDBType> msgs = Arrays.asList(GET_MEME_ID, GET_MEME_TAGS, DEMOTE_MEME);
        Random rand = new Random();
        return msgs.get(rand.nextInt(msgs.size()-1));
    }

    MsgDBType getRandCurateMsg(){
        List<MsgDBType> msgs = Arrays.asList(PROMOTE_MEME, REJECT_MEME);
        Random rand = new Random();
        return msgs.get(rand.nextInt(msgs.size()-1));
    }

}
