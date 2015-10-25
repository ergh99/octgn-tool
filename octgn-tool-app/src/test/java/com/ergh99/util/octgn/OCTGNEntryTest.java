package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.ANRConstants.*;
import static com.ergh99.util.octgn.OCTGNToolTestUtility.marshallAndroidNetrunnerEntry;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.abdera.model.Entry;
import org.junit.BeforeClass;
import org.junit.Test;

public class OCTGNEntryTest {

	private static Entry e;

	@BeforeClass
	public static void setUp() {
		e = marshallAndroidNetrunnerEntry();
	}

	@Test
	public void testOCTGNEntry() {
		OCTGNEntry entry = new OCTGNEntry(e);
		assertThat(entry, is(notNullValue()));
	}

	@Test
	public void testGetId() {
		OCTGNEntry entry = new OCTGNEntry(e);
		assertThat(entry.getId(), is(ANR_ID));
	}

	@Test
	public void testGetTitle() {
		OCTGNEntry entry = new OCTGNEntry(e);
		assertThat(entry.getTitle(), is(ANR_TITLE));
	}

	@Test
	public void testGetVersion() {
		OCTGNEntry entry = new OCTGNEntry(e);
		assertThat(entry.getVersion(), is("3.22.1.0"));
	}

	@Test
	public void testIsLatest() {
		OCTGNEntry entry = new OCTGNEntry(e);
		assertThat(entry.isLatest(), is(false));
	}
}
