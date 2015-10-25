package com.ergh99.util.octgn;

import static com.ergh99.util.octgn.ANRConstants.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;

@lombok.extern.slf4j.XSlf4j
public class OCTGNToolTestUtility {

	public static Path createTestDirectoryStructure() throws IOException {
		log.entry();
		String basedir = System.getProperty("basedir", ".");
		Path tempDir = Files.createTempDirectory(Paths.get(basedir), "octgn-tool-test");
		tempDir.toFile().deleteOnExit();
		log.info("Test directory: {}", tempDir);
		Files.createDirectory(tempDir.resolve("GameDatabase")).toFile().deleteOnExit();
		Files.createDirectory(tempDir.resolve("ImageDatabase")).toFile().deleteOnExit();

		return log.exit(tempDir);
	}

	public static Entry marshallAndroidNetrunnerEntry() {
		java.io.InputStream input = ClassLoader.getSystemResourceAsStream("android-netrunner-entry.xml");
		Document<Entry> eDoc = Abdera.getNewParser().parse(input).complete();
		return eDoc.getRoot();
	}

	public static void installAndroidNetrunner(Path testDirectory) {
    	OCTGN o = new OCTGN(testDirectory);
    	OCTGNEntry e = new OCTGNEntry(marshallAndroidNetrunnerEntry());
    	o.installGameFromEntry(e);
	}

	public static void deleteFileTree(Path path) {
		if (Files.isDirectory(path)) {
			try {
				Files.list(path).forEach(OCTGNToolTestUtility::deleteFileTree);
			} catch (IOException e) {
				log.catching(e);
			}
		}
		try {
			Files.delete(path);
		} catch (IOException e) {
			log.catching(e);
		}
	}

	public static void installAndroidNetrunnerFacade(Path testDirectory) throws IOException {
		Files.createDirectory(testDirectory.resolve("GameDatabase/" + ANR_ID)).toFile().deleteOnExit();
		Files.createDirectory(testDirectory.resolve("ImageDatabase/" + ANR_ID)).toFile().deleteOnExit();
		Path definitionPath = testDirectory.resolve("GameDatabase/" + ANR_ID + "/definition.xml");
		try (BufferedWriter out = Files.newBufferedWriter(definitionPath)) {
			out.write("<game name=\"" + ANR_TITLE + "\" id=\"" + ANR_ID + "\" />");
			definitionPath.toFile().deleteOnExit();
		}
	}
}
