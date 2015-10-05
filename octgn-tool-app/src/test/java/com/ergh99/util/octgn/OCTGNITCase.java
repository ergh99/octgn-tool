package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.ANRConstants.ANR_ID;
import static com.ergh99.util.octgn.ANRConstants.ANR_TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class OCTGNTest {

    @Test
    public void testGetGameByTitle() {
        OCTGN o = new OCTGN();
        OCTGNGame g = o.getGameByTitle(ANR_TITLE);
        assertSame(ANR_TITLE, g.getTitle());
    }

    @Test
    public void testFindGameIdByTitle() {
        OCTGN o = new OCTGN();
        String id = o.findGameIdByTitle(ANR_TITLE);
        assertEquals(ANR_ID, id);
    }

    @Test
    public void testGetGameDatabase() {
        OCTGN o = new OCTGN();
        assertEquals("." + java.io.File.separator + "GameDatabase", o.getGameDatabase().toString());
    }

    @Test
    public void testGetImageDatabase() {
        OCTGN o = new OCTGN();
        assertEquals("." + java.io.File.separator + "ImageDatabase", o.getImageDatabase().toString());
    }

}
