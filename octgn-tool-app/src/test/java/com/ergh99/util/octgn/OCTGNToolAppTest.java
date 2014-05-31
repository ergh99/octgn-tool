package com.ergh99.util.octgn;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class OCTGNToolAppTest {

	@Test
	public void testPrintUsageAndExit() {
		OCTGNToolApp.printUsage();
	}

	@Test
	public void testGetJarFromUrl() throws MalformedURLException {
		String classPath = "/" + OCTGNToolApp.class.getName().replace('.', '/') + ".class";
		URL jarUrl = new URL("jar:file:/path/to/jar.jar!/" + classPath);

		String jarPath = OCTGNToolApp.getJarFromUrl(jarUrl);
		assertEquals("jar.jar", jarPath);
	}
}
