package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.HttpRemoteFile.CONTENT_LENGTH;
import static com.ergh99.util.octgn.HttpRemoteFile.LAST_MODIFIED;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class HttpRemoteFileTest {

    private final Map<String, List<String>> mockHeaders = new java.util.HashMap<>();

    @Before
    public void setUp() {
        mockHeaders.put(CONTENT_LENGTH, Collections.singletonList("10"));
        mockHeaders.put(LAST_MODIFIED, Collections.singletonList("Tue, 3 Jun 2008 11:05:30 GMT"));
    }

    @Test
    public void testContentLength() {
        HttpRemoteFile remoteFile = new HttpRemoteFile(mockHeaders);
        assertThat(remoteFile.getContentLength(), is(equalTo(10l)));
    }

    @Test
    public void testLastModified() {
        HttpRemoteFile remoteFile = new HttpRemoteFile(mockHeaders);
        Instant expectedInstant = Instant.parse("2008-06-03T11:05:30.00Z");
        assertThat(remoteFile.getLocalizedLastModifiedTime(), is(equalTo(expectedInstant)));
    }
}
