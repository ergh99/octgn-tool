package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.ANRConstants.*;
import static com.ergh99.util.octgn.OCTGNToolApp.FEED_URL;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.abdera.model.Entry;
import org.junit.Test;

public class OCTGNDirectoryITCase {

    @Test
    public void testGetEntryForName() throws MalformedURLException {
        URL feedUrl = new URL(FEED_URL);
        OCTGNDirectory d = new OCTGNDirectory(feedUrl);
        try {
            OCTGNEntry entry = d.getEntryForName(ANR_TITLE);
            assertThat(entry.getTitle(), equalTo(ANR_TITLE));
        } catch (IOException | URISyntaxException e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void testGetEntriesFromFeed() throws MalformedURLException {
        URL feedUrl = new URL(FEED_URL);
        OCTGNDirectory d = new OCTGNDirectory(feedUrl);
        try {
            List<Entry> entries = d.getEntriesFromFeed();
            assertThat(entries.size(), is(greaterThan(0)));
            assertThat(entries, hasItem(hasProperty("id", hasToString(containsString(ANR_ID)))));
        } catch (IOException | URISyntaxException e) {
            fail(e.getLocalizedMessage());
        }
    }

}
