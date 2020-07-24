package app;

import org.junit.Test;

import java.sql.SQLException;

public class MemebaseTest {


    @Test
    public void constructorTest() throws SQLException {
        Memebase memebase = new Memebase("C:\\sqlite\\");
        memebase.open();
        memebase.close();
    }
}
