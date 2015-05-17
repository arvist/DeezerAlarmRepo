package com.cikoapps.deezeralarm.tests.units;

import junit.framework.TestCase;


public class QuotesTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void testGetQuote(){
        com.cikoapps.deezeralarm.helpers.Quotes quote = com.cikoapps.deezeralarm.helpers.Quotes.getQuote();
        assertNotNull(quote);
        assertNotNull(quote.author);
        assertNotNull(quote.quote);
    }
}
