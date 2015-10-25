package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.ANRConstants.*;
import static com.ergh99.util.octgn.OCTGNToolTestUtility.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OCTGNITCase {

	private static Path testDirectory;

	@BeforeClass
	public static void setUp() throws IOException {
		testDirectory = createTestDirectoryStructure();
		installAndroidNetrunnerFacade(testDirectory);
	}

	private OCTGN o;

	@Before
	public void setUpForTest() {
		o = new OCTGN(testDirectory);
	}

	@Test
    public void testGetGameByTitle() {
        OCTGNGame g = o.getGameByTitle(ANR_TITLE);
        assertThat(g.getTitle(), is(ANR_TITLE));
    }

    @Test
    public void testFindGameIdByTitle() {
        String id = o.findGameIdByTitle(ANR_TITLE);
        assertThat(id, is(ANR_ID));
    }

    @Test
    public void testGetGameDatabase() {
    	Path gameDatabase = o.getGameDatabase();
        assertThat(Files.exists(gameDatabase),  is(true));
        assertThat(Files.isDirectory(gameDatabase),  is(true));
    }

    @Test
    public void testGetImageDatabase() {
    	Path imageDatabase = o.getImageDatabase();
        assertThat(Files.exists(imageDatabase),  is(true));
        assertThat(Files.isDirectory(imageDatabase),  is(true));
    }

}
