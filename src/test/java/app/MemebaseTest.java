package app;

import org.junit.Test;

import java.sql.SQLException;

public class MemebaseTest {


    @Test
    public void constructorTest() throws SQLException {
        Memebase memebade = new Memebase("C:\\sqlite\\");
    }
}
