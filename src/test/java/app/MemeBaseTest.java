package app;

import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;

public class MemeBaseTest {


    @Test
    public void initializeTest() throws SQLException {
        MemeBase memebase = new MemeBase("C:\\sqlite\\");
        memebase.open();
        memebase.close();
    }

    @Test
    public void storeTest() throws SQLException {
        MemeBase memebase = new MemeBase("C:\\sqlite\\");
        memebase.open();
        memebase.store("Ziggy", "https://cdn.discordapp.com/attachments/647667357879107584/735884634818215936/p1Uoukq.jpeg", Arrays.asList("meta", "books"));
        memebase.close();
    }
}
