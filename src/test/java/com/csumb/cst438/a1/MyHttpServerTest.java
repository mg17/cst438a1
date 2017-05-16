/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.csumb.cst438.a1;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sun.net.httpserver.Headers;

/**
 *
 * @author david wisneski
 * @version 1.0
 * last update 3-21-2017
 */

public class MyHttpServerTest {
    
    public MyHttpServerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void test404() {
        Headers header = new Headers();
        try {
            TestHttpExchange t = new TestHttpExchange("/h9.gif", header);
            MyHttpServer.MyHandler handler = new MyHttpServer.MyHandler();
            handler.handle(t);
            System.out.println(t.getResponseCode());
            assertEquals("Did not return 404", 404, t.getResponseCode());
        } catch (Exception e) {
            fail("Unexpected exception in test404(): " + e.getMessage());
        }
    }
    
    /**
     * Test of main method, of class MyHttpServer.
     */
    @Test
    public void testHandle() {
        String expectedBody = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head>" + 
                "<body><h2>Hangman</h2><img src=\"h1.gif\"><h2 style=\"font-family:'Lucida Console', monospace\">";



    Headers header = new Headers();
    try {
        TestHttpExchange t = new TestHttpExchange("/", header);
        MyHttpServer.MyHandler handler = new MyHttpServer.MyHandler();
        handler.handle(t);
        // check response for cookie returned, response code=200, and expected response body 
        Headers response = t.getResponseHeaders();
        String cookie1 = response.getFirst("Set-cookie");
        assertEquals("Bad content type", "text/html", response.getFirst("Content-type"));
        assertNotNull("No cookie returned", cookie1);
        assertEquals("Bad response code.",200, t.getResponseCode());
        assertEquals("Bad response body.",expectedBody, t.getOstream().toString().substring(0, expectedBody.length()));
    } catch (Exception e) {
        fail("unexpected exception in testHandle "+e.getMessage());
    }
    }
    
}
