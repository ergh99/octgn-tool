package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.ANRConstants.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.apache.abdera.model.Entry;
import org.junit.Test;

public class OCTGNEntryITCase {

	@Test
	public void testGetNuPkg() throws IOException, URISyntaxException {
		OCTGNDirectory dir = new OCTGNDirectory();
		List<Entry> entries = dir.getEntriesFromFeed();
		Optional<Entry> entry = entries.stream().filter(e -> e.getTitle().equals(ANR_TITLE)).findAny();
		if (entry.isPresent()) {
			OCTGNEntry e = new OCTGNEntry(entry.get());
			assertThat(e.getNuPkg(), is(notNullValue()));
			assertTrue(Files.exists(e.getNuPkg()));
			assertThat(Files.size(e.getNuPkg()), is(greaterThan(0l)));
		} else {
			fail("Unable to load entry from feed.");
		}
	}
}
