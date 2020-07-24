package app;

import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MemeBaseTest {

    @Test
    public void initializeTest() throws SQLException {
        try {
            MemeBase memebase = new MemeBase("C:\\sqlite\\");
            memebase.open();
            memebase.close();
        }
        catch (Exception e){
            System.out.println("Failed to open DB");
            assertTrue(false);
        }
    }

    @Test
    public void storeTest() throws SQLException {
        MemeBase memebase = new MemeBase("C:\\sqlite\\");
        memebase.open();
        assertTrue(memebase.store("Ziggy", "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", Arrays.asList("meta", "books")));
        memebase.close();
    }
}
