package com.ergh99.util.octgn;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;

public class OCTGNGameTest {

	@Test
	public void testGetCardPaths() {
		OCTGN o = new OCTGN(Paths.get("."));
		OCTGNGame g = o.getGameByTitle(ANRConstants.ANR_TITLE);
		Set<Path> cardPaths = g.getCardPaths();
		assertEquals(463, cardPaths.size());
	}

	@Test
	public void testGetId() {
		OCTGN o = new OCTGN(Paths.get("."));
		OCTGNGame g = o.getGameByTitle(ANRConstants.ANR_TITLE);
		assertEquals(ANRConstants.ANR_ID, g.getId());
	}

	@Test
	public void testGetTitle() {
		OCTGN o = new OCTGN(Paths.get("."));
		OCTGNGame g = o.getGameByTitle(ANRConstants.ANR_TITLE);
		assertEquals(ANRConstants.ANR_TITLE, g.getTitle());
	}

	@Test
	public void testGetSetCount() {
		OCTGN o = new OCTGN(Paths.get("."));
		OCTGNGame g = o.getGameByTitle(ANRConstants.ANR_TITLE);
		assertEquals(Integer.valueOf(15), g.getSetCount());
	}

	@Test
	public void testGetCardCount() {
		OCTGN o = new OCTGN(Paths.get("."));
		OCTGNGame g = o.getGameByTitle(ANRConstants.ANR_TITLE);
		assertEquals(Integer.valueOf(463), g.getCardCount());
	}

}
