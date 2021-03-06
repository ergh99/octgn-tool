package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.ANRConstants.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.abdera.model.Entry;
import org.junit.Test;

public class OCTGNDirectoryITCase {

    @Test
    public void testGetEntryForName() throws MalformedURLException {
        OCTGNDirectory d = new OCTGNDirectory();
        try {
            OCTGNEntry entry = d.getEntryForName(ANR_TITLE);
            assertThat(entry.getTitle(), equalTo(ANR_TITLE));
        } catch (IOException | URISyntaxException e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void testGetEntriesFromFeed() throws MalformedURLException {
        OCTGNDirectory d = new OCTGNDirectory();
        try {
            List<Entry> entries = d.getEntriesFromFeed();
            assertThat(entries, hasSize(greaterThan(0)));
            assertThat(entries, hasItem(hasProperty("id", hasToString(containsString(ANR_ID)))));
        } catch (IOException | URISyntaxException e) {
            fail(e.getLocalizedMessage());
        }
    }

}
