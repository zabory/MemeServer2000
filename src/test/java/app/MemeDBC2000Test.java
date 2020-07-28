package app;

import dataStructures.MemeDBMsg2000;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static dataStructures.MemeDBMsg2000.MsgDBType.*;
import static dataStructures.MemeDBMsg2000.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MemeDBC2000Test {
    static MemeDBC2000 controller;
    static BlockingQueue inputQ, outputQ;

    @Before
    public void before(){
        // connect to the db
        String db = "C:\\sqlite\\";
        inputQ = new LinkedBlockingQueue(100);
        outputQ = new LinkedBlockingQueue(100);
        controller = new MemeDBC2000(db, inputQ, outputQ);
        controller.start();
    }

    @After
    public void after(){
        try {
            inputQ.put(new MemeDBMsg2000().type(TERMINATE));

                Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\sqlite\\meme.db");
                conn.createStatement().execute("DELETE FROM memes");
                conn.createStatement().execute("DELETE FROM tag_lkp");
                conn.createStatement().execute("DELETE FROM cache");
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
            inputQ.put(new MemeDBMsg2000()
                    .type(STORE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg2000 msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("Zabory", msg.getUsername());
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicPromoteTest(){
        try {
            inputQ.put(new MemeDBMsg2000()
                    .type(CACHE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg2000 msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("Zabory", msg.getUsername());

            inputQ.put(new MemeDBMsg2000()
                    .type(PROMOTE_MEME).id(1)
                    .username("Ziggy"));
            msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(CURATE_RESULT, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());
            assertEquals("Zabory", msg.getUsername());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicRejectTest(){
        try {
            inputQ.put(new MemeDBMsg2000()
                    .type(CACHE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg2000 msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("Zabory", msg.getUsername());

            inputQ.put(new MemeDBMsg2000()
                    .type(REJECT_MEME).id(1)
                    .username("Ziggy"));
            msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(CURATE_RESULT, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());
            assertEquals("Zabory", msg.getUsername());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicDemoteTest(){
        try {
            inputQ.put(new MemeDBMsg2000()
                    .type(STORE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg2000 msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("Zabory", msg.getUsername());

            inputQ.put(new MemeDBMsg2000()
                    .type(DEMOTE_MEME).id(1));
            msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(CURATE_RESULT, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());
            assertEquals("Zabory", msg.getUsername());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicGetMemeToApproveTest(){
        try {
            inputQ.put(new MemeDBMsg2000()
                    .type(STORE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg2000 msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("Zabory", msg.getUsername());

            inputQ.put(new MemeDBMsg2000().type(GET_MEME_ID).id(1));
            msg = (MemeDBMsg2000) outputQ.take();
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
            inputQ.put(new MemeDBMsg2000()
                    .type(STORE_MEME)
                    .link("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg")
                    .username("Zabory")
                    .tags(Arrays.asList("bread", "seals")));
            MemeDBMsg2000 msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(SUBMIT_ACK, msg.getType());
            assertEquals((Integer) 1, msg.getId());
            assertEquals("Zabory", msg.getUsername());

            inputQ.put(new MemeDBMsg2000()
                    .type(GET_MEME_TAGS)
                    .tags(Arrays.asList("seals"))
                    .username("Bendu"));
            msg = (MemeDBMsg2000) outputQ.take();
            assertEquals(MEME, msg.getType());
            assertEquals("https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", msg.getLink());

        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void stressTest(){
        String link = "a";

        try {
            // run 50 STORE commands
            for(int i=0;i<50;i++){
                inputQ.put(new MemeDBMsg2000()
                        .type(STORE_MEME)
                        .link(link)
                        .username("Ziggy")
                        .tags(getRandomTagList(4))
                );
                link += "a";
            }

            // run 50 CACHE commands
            link = "b";
            for(int i=0;i<50;i++){
                inputQ.put(new MemeDBMsg2000()
                        .type(CACHE_MEME)
                        .link(link)
                        .username(getRandomName())
                        .tags(getRandomTagList(4))
                );
                link += "b";
                outputQ.take();
            }

            // take the rest of the messages out
            for(int i=1;i<=50;i++)
                outputQ.take();

            // perform random actions on all inserted
            for(int i=0;i<50;i++){
                inputQ.put(new MemeDBMsg2000()
                        .type(getRandMemeMsg())
                        .id(i)
                        .tags(getRandomTagList(8))
                );
            }

            // perform random actions on all inserted
            for(int i=51;i<=100;i++){
                inputQ.put(new MemeDBMsg2000()
                        .type(getRandCurateMsg())
                        .id(i)
                        .username("Ziggy")
                );
                outputQ.take();
            }

            // take the rest of the messages out
            for(int i=1;i<=50;i++)
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
