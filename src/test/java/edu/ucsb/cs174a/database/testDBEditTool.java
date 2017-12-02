package edu.ucsb.cs174a.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static edu.ucsb.cs174a.database.DBEditTool.addDailyBalanceAll;
import static org.junit.Assert.assertEquals;

/**
 * Created by zhanchengqian on 2017/12/1.
 */
public class testDBEditTool {
    @Before
    public void setUp() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    public void testAddDailyBalanceAll() {
        ServerHandler serverHandler = new ServerHandler();
        addDailyBalanceAll(serverHandler, "20130301", "20130315");
        serverHandler.exit();
    }

    @After
    public void closeStream() throws Exception {
        System.setOut(null);
        System.setErr(null);
    }
}
