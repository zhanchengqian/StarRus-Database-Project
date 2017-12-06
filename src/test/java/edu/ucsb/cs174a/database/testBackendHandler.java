package edu.ucsb.cs174a.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
/**
 * Created by zhanchengqian on 2017/11/15.
 */
public class testBackendHandler {
    @Before
    public void setUp() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    public void testSignupAndLogin() {
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo1 = backendHandler.signup("12345678", "testName", "CA", "1001001000", "testemail", "testusername", "123456");
        Customer customer = new Customer("testusername", "123456");
        boolean boo2 = backendHandler.login(customer);
        assertEquals(true, boo1 & boo2);
    }

//    @Test
//    public void testDeposit(){
//        ServerHandler serverHandler = new ServerHandler();
//        BackendHandler backendHandler = new BackendHandler(serverHandler);
//        Customer customer = new Customer("1234567890", "testName", "CA", "1001001000", "testemail", "testusername", "123456");
//        boolean boo = backendHandler.deposit(customer, 1361);
//    }
//
//    @Test
//    public void testWithdraw(){
//        ServerHandler serverHandler = new ServerHandler();
//        BackendHandler backendHandler = new BackendHandler(serverHandler);
//        Customer customer = new Customer("1234567890", "testName", "CA", "1001001000", "testemail", "testusername", "123456");
//        boolean boo = backendHandler.withdraw(customer, 1800);
//    }

//    @Test
//    public void testOverWithdraw(){
//        ServerHandler serverHandler = new ServerHandler();
//        BackendHandler backendHandler = new BackendHandler(serverHandler);
//        Customer customer = new Customer("1234567890", "testName", "CA", "1001001000", "testemail", "testusername", "123456");
//        boolean boo = backendHandler.withdraw(customer, 200000);
//    }

        @Test
    public void testGenerateMonthlyStatement(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        Customer customer = new Customer("1234567890", "testName", "CA", "1001001000", "testemail", "testusername", "123456");
        boolean boo = backendHandler.generateMonthlyStatement(customer);
    }

    @Test
    public void testGenerateActiveUserList(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo = backendHandler.generateActiveUserList();
    }

    @Test
    public void testGenerateDTER(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo = backendHandler.generateDTER();
    }

    @Test
    public void testGenerateCustomerReport(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo = backendHandler.generateCustomerReport("00000456");
    }

    @Test
    public void testDeleteTransaction(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo = backendHandler.deleteTransaction("20171003");
    }

    @Test
    public void testGetBalance(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        Customer customer = new Customer("1234567890", "testName", "CA", "1001001000", "testemail", "testusername", "123456");
        boolean boo = backendHandler.getBalance(customer);
    }

    @Test
    public void testGetTransactionHistory(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        Customer customer = new Customer("1234567890", "testName", "CA", "1001001000", "testemail", "testusername", "123456");
        boolean boo = backendHandler.getTransactionHistory(customer);
    }

    @Test
    public void testGetAllMovies(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo = backendHandler.getAllMovies();
    }

    @Test
    public void testTopMovies(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo = backendHandler.topMovies("1900", "2010");
    }

    @Test
    public void testGetMovieReviews(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo = backendHandler.getMovieReviews("Perfect Murder");
    }

    @Test
    public void testGetStockInfo(){
        ServerHandler serverHandler = new ServerHandler();
        BackendHandler backendHandler = new BackendHandler(serverHandler);
        boolean boo = backendHandler.getStockInfo("SMD");
    }

    @After
    public void closeStream() throws Exception {
        System.setOut(null);
        System.setErr(null);
    }
}
