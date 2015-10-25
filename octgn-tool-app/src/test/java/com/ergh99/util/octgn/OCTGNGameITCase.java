package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.ANRConstants.*;
import static com.ergh99.util.octgn.OCTGNToolTestUtility.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OCTGNGameITCase {

	private static Path testDirectory;

	@BeforeClass
	public static void setUp() throws IOException {
		testDirectory = createTestDirectoryStructure();
		installAndroidNetrunner(testDirectory);
	}

	@AfterClass
	public static void tearDown() {
		deleteFileTree(testDirectory);
	}

	private OCTGN o;

	@Before
	public void setUpForTest() {
		o = new OCTGN(testDirectory);
	}

	@Test
    public void testGetCardPaths() {
        OCTGNGame g = o.getGameByTitle(ANR_TITLE);
        Set<Path> cardPaths = g.getCardPaths();
        assertThat(cardPaths, hasSize(greaterThan(800)));
    }

    @Test
    public void testGetId() {
        OCTGNGame g = o.getGameByTitle(ANR_TITLE);
        assertThat(g.getId(), is(ANR_ID));
    }

    @Test
    public void testGetTitle() {
        OCTGNGame g = o.getGameByTitle(ANR_TITLE);
        assertThat(g.getTitle(), is(ANR_TITLE));
    }

    @Test
    public void testGetSetCount() {
        OCTGNGame g = o.getGameByTitle(ANR_TITLE);
        assertThat(g.getSetCount(), is(greaterThan(25)));
    }

    @Test
    public void testGetCardCount() {
        OCTGNGame g = o.getGameByTitle(ANR_TITLE);
        assertThat(g.getCardCount(), is(greaterThan(800)));
    }

}
