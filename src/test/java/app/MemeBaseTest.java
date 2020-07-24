package app;

import org.junit.After;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MemeBaseTest {

    @Test
    public void openCloseTest() throws SQLException {
        MemeBase memebase = new MemeBase("C:\\sqlite\\");
        assertTrue(memebase.open());
        assertTrue(memebase.close());
    }

    @Test
    public void adminTest() throws SQLException {
        MemeBase memebase = new MemeBase("C:\\sqlite\\");
        Integer expecID = 1;
        String link = "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg";
        assertTrue(memebase.open());
        assertEquals(expecID, memebase.store("Ziggy", link, Arrays.asList("meta", "books")));
        assertTrue(memebase.demote(expecID));
        assertEquals(link, memebase.reject(expecID));
        assertTrue(memebase.close());
    }

}
