package com.ergh99.util.octgn;

import static org.junit.Assert.*;

import java.nio.file.Paths;

import org.junit.Test;

public class OCTGNTest {

	@Test
	public void testGetGameByTitle() {
		OCTGN o = new OCTGN(Paths.get("."));
		OCTGNGame g = o.getGameByTitle("Android-Netrunner");
		assertSame("Android-Netrunner", g.getTitle());
	}

	@Test
	public void testFindGameIdByTitle() {
		OCTGN o = new OCTGN(Paths.get("."));
		String id = o.findGameIdByTitle("Android-Netrunner");
		assertEquals("0f38e453-26df-4c04-9d67-6d43de939c77", id);
	}

	@Test
	public void testGetGameDatabase() {
		OCTGN o = new OCTGN(Paths.get("."));
		assertEquals("./GameDatabase", o.getGameDatabase().toString());
	}

	@Test
	public void testGetImageDatabase() {
		OCTGN o = new OCTGN(Paths.get("."));
		assertEquals("./ImageDatabase", o.getImageDatabase().toString());
	}

}
