package edu.ucsb.cs174a.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import sun.security.x509.CertificatePolicySet;

import static edu.ucsb.cs174a.database.Helper.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by zhanchengqian on 2017/11/16.
 */
public class testHelper {
    @Before
    public void setUp() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

//    @Test
//    public void testGetStockPrice() {
//        assertEquals(true, getStockPrice("abc") == 55.6);
//    }

    @Test
    public void testGetStockPriceNoRecord() {
        ServerHandler serverHandler = new ServerHandler();
        System.out.print(getStockPrice(serverHandler, "abc"));
        serverHandler.exit();
    }

    @Test
    public void testGetNextTransIDNoRecord() {
        ServerHandler serverHandler = new ServerHandler();
        System.out.print(getNextTransID(serverHandler));
        serverHandler.exit();
    }
//
//    @Test
//    public void testGetNextTransIDSomeRecord() {
//        assertEquals(true, "0070182935".equals(getNextTransID()));
//    }

//    @Test
//    public void testRecordTrans(){
//        System.out.println(recordTrans("1234567890", "20171116", "deposit", "121212", "131313", "abc", 1000));
//    }
//
//    @Test
//    public void testUploadTrans(){
//        System.out.println(uploadTrans("1234567890", "20171116", "deposit", "121212", "131313", "abc", 1000));
//    }

//    @Test
//    public void testSetGetDate(){
//        setDate("20171118");
//        assertEquals("20171118", getDate());
//    }
//
//    @Test
//    public void testSetEarlierDate(){
//        setDate("20171109");
//    }
//
//    @Test
//    public void testGetEmptyDate(){
//        System.out.println(getDate());
//    }
//
//    @Test
//    public void testsetDate(){
//        setDate("20181019");
//    }

//    @Test
//    public void testNoMKTACCHasSTKACC(){
//        ServerHandler serverHandler = new ServerHandler();
//        Customer customer = new Customer("1234567890", "cname", "CA", "1001001000", "12312", "1123123", "123456");
//        System.out.println(getMKTACCNumber(serverHandler, customer));
//        serverHandler.exit();
//    }

//    @Test
//    public void testHasMKTACCNoSTKACC(){
//        ServerHandler serverHandler = new ServerHandler();
//        Customer customer = new Customer("1234567890", "cname", "CA", "1001001000", "12312", "1123123", "123456");
//        System.out.println(getSTKACCNumber(serverHandler, customer));
//        serverHandler.exit();
//    }

//    @Test
//    public void testGetCurrentMKTBalanceNoRecord(){
//        ServerHandler serverHandler = new ServerHandler();
//        Customer customer = new Customer("1234567890", "cname", "CA", "1001001000", "12312", "1123123", "123456");
//        System.out.println(getCurrentMKTBalance(serverHandler, customer, "121212"));
//        serverHandler.exit();
//    }

    @Test
    public void testHasBoughtStockSymbolBefore(){
        ServerHandler serverHandler = new ServerHandler();
        boolean boo = hasBoughtStockSymbolBefore (serverHandler, "131313", "abc");
        System.out.println(boo);
        serverHandler.exit();
    }

    @Test
    public void testOpenMKTACC(){
        ServerHandler serverHandler = new ServerHandler();
        boolean boo = openMKTACC(serverHandler, "1234567899");
        System.out.println(boo);
        serverHandler.exit();
    }

    @Test
    public void testCheckNAAccount(){
        ServerHandler serverHandler = new ServerHandler();
        boolean boo = checkNAAccount(serverHandler, 2);
        System.out.println(boo);
        serverHandler.exit();
    }

    @Test
    public void testCreateNAAccounts(){
        ServerHandler serverHandler = new ServerHandler();
        boolean boo = createNAAccounts(serverHandler);
        System.out.println(boo);
        serverHandler.exit();
    }

    @Test
    public void testSignup(){
        BackendHandler backendHandler = new BackendHandler();
        boolean boo1 = backendHandler.signup("1234567890","tname", "CA", "1001001000", "temail", "tusername", "123456");
        System.out.println(boo1);
        backendHandler.exit();
    }

    @Test
    public void testSetDate2017(){
        BackendHandler backendHandler = new BackendHandler();
        boolean boo1 = backendHandler.setDate("20171120");
        System.out.println(boo1);
        backendHandler.exit();
    }

    @Test
    public void testSetDate02(){
        BackendHandler backendHandler = new BackendHandler();
        boolean boo1 = backendHandler.setDate("20130320");
        System.out.println(boo1);
        backendHandler.exit();
    }

    @Test
    public void testDepositFrist1000(){
        BackendHandler backendHandler = new BackendHandler();
        Customer customer = new Customer("1234567890","tname", "CA", "1001001000", "temail", "tusername", "123456");
        boolean boo1 = backendHandler.deposit(customer, 300, "deposit");
        boolean boo2 = backendHandler.deposit(customer, 700, "deposit");
        System.out.println(boo1 && boo2);
        backendHandler.exit();
    }

    @Test
    public void testBuyStock(){
        BackendHandler backendHandler = new BackendHandler();
        Customer customer = new Customer("1234567890","tname", "CA", "1001001000", "temail", "tusername", "123456");
        boolean boo1 = backendHandler.buyStock(customer, "abc", 5);
        boolean boo2 = backendHandler.buyStock(customer, "abc", 5);
        boolean boo3 = backendHandler.buyStock(customer, "zxc", 5);
        boolean boo4 = backendHandler.buyStock(customer, "zxc", 5);
        System.out.println(boo1 & boo2 & boo3 & boo4);
        backendHandler.exit();
    }

    @Test
    public void testSellStock(){
        BackendHandler backendHandler = new BackendHandler();
        Customer customer = new Customer("1234567890","tname", "CA", "1001001000", "temail", "tusername", "123456");
        // huge loss
        boolean boo00 = backendHandler.sellStock(customer, "abc", 1000,10);
        boolean boo0 = backendHandler.sellStock(customer, "zxc", 1000,10);
        // loss
        boolean boo1 = backendHandler.sellStock(customer, "abc", 85,2);
        boolean boo2 = backendHandler.sellStock(customer, "zxc", 100,2);
        // gain
        boolean boo3 = backendHandler.sellStock(customer, "abc", 25,2);
        boolean boo4 = backendHandler.sellStock(customer, "zxc", 15,2);
        // no loss no gain
        boolean boo5 = backendHandler.sellStock(customer, "abc", 60,4);
        boolean boo6 = backendHandler.sellStock(customer, "zxc", 45,4);
        // to get 0 shares remaining
        boolean boo7 = backendHandler.sellStock(customer, "abc", 10,2);
        boolean boo8 = backendHandler.sellStock(customer, "zxc", 15,2);
        System.out.println(boo00& boo0 & boo1 & boo2 & boo3 & boo4 & boo5 & boo6 & boo7 & boo8);
        backendHandler.exit();
    }

    @Test
    public void testAccureInterestHelper(){
        ServerHandler serverHandler = new ServerHandler();
        boolean boo = accureInterestHelper(serverHandler, "201711");
        System.out.println(boo);
        serverHandler.exit();
    }

    @Test
    public void testRecordAllDailyBalance(){
        ServerHandler serverHandler = new ServerHandler();
        boolean boo = recordAllDailyBalance(serverHandler, "20171120");
        System.out.println(boo);
        serverHandler.exit();
    }

    @Test
    public void testGetShare(){
        ServerHandler serverHandler = new ServerHandler();
        double share = getShare(serverHandler, "1234567890", "abc");
        System.out.println(share);
        serverHandler.exit();
    }

//    @Test
//    public void testHasBoughtStockSymbolBefore01(){
//        ServerHandler serverHandler = new ServerHandler();
//        boolean boo1 = hasBoughtStockSymbolBefore(serverHandler, "1111222233", "xyz");
//        System.out.println(boo1);
//        serverHandler.exit();
//    }
//
//    @Test
//    public void testGetSpecificStockAccountID() {
//        ServerHandler serverHandler = new ServerHandler();
//        String id = getSpecificStockAccountID(serverHandler, "1111222233", "xyz");
//        System.out.println(id);
//        serverHandler.exit();
//    }
//
//    @Test
//    public void testHasHasAccountEntry() {
//        ServerHandler serverHandler = new ServerHandler();
//        boolean boo = hasHasAccountEntry(serverHandler, "1234567890", "00000001", "00000000");
//        System.out.println(boo);
//        serverHandler.exit();
//    }
//
//    @Test
//    public void testGetStockQuantity(){
//        ServerHandler serverHandler = new ServerHandler();
//        int i = getStockQuantity(serverHandler, "1234567890");
//        boolean boo = hasHasAccountEntry(serverHandler, "1234567890", "00000001", "00000000");
//        System.out.println(i);
//        System.out.println(boo);
//        serverHandler.exit();
//    }
//
//    @Test
//    public void testBuyStockSTKACC(){
//        ServerHandler serverHandler = new ServerHandler();
//        boolean boo1 = buyStockSTKACC(serverHandler, "1234567890", "abc", 5);
//        boolean boo2 = buyStockSTKACC(serverHandler, "1234567890", "abc", 5);
//        boolean boo3 = buyStockSTKACC(serverHandler, "1234567890", "zxc", 5);
//        boolean boo4 = buyStockSTKACC(serverHandler, "1234567890", "zxc", 5);
//        System.out.println(boo1 && boo2 && boo3 && boo4);
//        serverHandler.exit();
//    }
//
//    @Test
//    public void testSellStockSTKACC(){
//        ServerHandler serverHandler = new ServerHandler();
//        boolean boo1 = sellStockSTKACC(serverHandler, "1234567890", "abc", 5);
//        boolean boo2 = sellStockSTKACC(serverHandler, "1234567890", "abc", 5);
//        boolean boo3 = sellStockSTKACC(serverHandler, "1234567890", "zxc", 5);
//        boolean boo4 = sellStockSTKACC(serverHandler, "1234567890", "zxc", 5);
//        System.out.println(boo1 && boo2 && boo3 && boo4);
//        serverHandler.exit();
//    }
//
//    @Test
//    public void testGetNextSTKACCNumber(){
//        ServerHandler serverHandler = new ServerHandler();
//        String s = getNextSTKACCNumber(serverHandler);
//        System.out.println(s);
//        serverHandler.exit();
//    }

    @Test
    public void testIsMonthEnd(){
        System.out.println( isMonthEnd("20170530") );
    }

    @Test
    public void testMonthHelper(){
        System.out.println( monthFront("20170515") );
        System.out.println( monthEnd("20170203") );
    }

    @Test
    public void testPrintOneAccount(){
        ServerHandler serverHandler = new ServerHandler();
        printOneAccount(serverHandler, "1234567890", "00000001", 0, "20171108");
        printOneAccount(serverHandler, "1234567890", "00000001", 1, "20171108");
        serverHandler.exit();

    }

    @Test
    public void testGetInitialBalance(){
        ServerHandler serverHandler = new ServerHandler();
        System.out.println(getInitialBalance(serverHandler, "1234567890", "00000001", "20171108"));
        serverHandler.exit();
    }

    @Test
    public void testGetFinalBalance(){
        ServerHandler serverHandler = new ServerHandler();
        System.out.println(getFinalBalance(serverHandler, "1234567890", "00000001", "20171108"));
        serverHandler.exit();
    }

    @Test
    public void testWhoOwnsThisMKTACC(){
        ServerHandler serverHandler = new ServerHandler();
        System.out.println(whoOwnsThisMKTACC(serverHandler, "00000001"));
        serverHandler.exit();
    }

    @Test
    public void testGetTotalEarnings(){
        ServerHandler serverHandler = new ServerHandler();
        System.out.println(getTotalEarnings(serverHandler, "1234567890", "00000001", "20171108"));
        serverHandler.exit();
    }

    @Test
    public void testGetTotalCommission () {
        ServerHandler serverHandler = new ServerHandler();
        System.out.println(getTotalCommission(serverHandler, "1234567890", "00000001", "20171108"));
        serverHandler.exit();
    }

    @Test
    public void testPrintOnesAllAccounts(){
        ServerHandler serverHandler = new ServerHandler();
        System.out.println(printOnesAllAccounts(serverHandler, "1234567890"));
        serverHandler.exit();
    }

    @After
    public void closeStream() throws Exception {
        System.setOut(null);
        System.setErr(null);
    }
}
