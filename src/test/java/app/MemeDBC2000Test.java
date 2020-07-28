package app;

import dataStructures.MemeDBMsg2000;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static dataStructures.MemeDBMsg2000.MsgDBType.*;
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
                conn.createStatement().execute("DELETE FROM cached_memes");
                conn.close();

            controller.join();
        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void basicSubmitTest(){
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


}
