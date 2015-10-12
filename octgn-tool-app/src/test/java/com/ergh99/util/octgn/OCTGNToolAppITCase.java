package com.ergh99.util.octgn;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class OCTGNToolAppITCase {

    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

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

    @Test
    public void testOCTGNToolApp() {
        exit.expectSystemExit();
        OCTGNToolApp.main(new String[] {});
    }

    @Test
    public void testProcessArguments() {
        OCTGNToolApp.processArguments(new String[] { "-g", "Android-Netrunner" });
    }
}
