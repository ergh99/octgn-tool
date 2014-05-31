package com.ergh99.util.octgn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class OCTGN {

	private static final Logger log = LoggerFactory.getLogger(OCTGN.class);
	private static XPath xpath;

	@Getter private Path gameDatabase;
	@Getter private Path imageDatabase;
	private Map<String, String> gameIdMap = new HashMap<>();

	public OCTGN(Path homeDir) {
		super();
		xpath = XPathFactory.newInstance().newXPath();
		gameDatabase = homeDir.resolve("GameDatabase");
		imageDatabase = homeDir.resolve("ImageDatabase");
	}

	public OCTGNGame installGameFromEntry(OCTGNEntry entry) {
		OCTGNGame g = new OCTGNGame(this, entry);
		return g;
	}

	public OCTGNGame getGameByTitle(String gameTitle) {
		if (gameIdMap.containsKey(gameTitle) == false) {
			gameIdMap.put(gameTitle, findGameIdByTitle(gameTitle));
		}
		return new OCTGNGame(this, gameIdMap.get(gameTitle), gameTitle);
	}

	protected String findGameIdByTitle(String gameTitle) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(gameDatabase, OCTGNToolApp.DIRS_ONLY)) {
			XPathExpression expr = xpath.compile(
					String.format("/game[@name='%s']/@id", gameTitle));
			for (Path gameDir : stream) {
				log.info("Looking for {} in {}", gameTitle, gameDir);
				InputStream in = Files.newInputStream(gameDir.resolve("definition.xml"));
				String id = expr.evaluate(new InputSource(in));
				in.close();
				if (id != null && id.length() > 0) {
					log.info("Found {} id: {}", gameTitle, id);
					return id;
				}
			}

			Error e = new Error("No game definition found for " + gameTitle);
			log.error(e.getLocalizedMessage());
			throw e;
		} catch (IOException | XPathExpressionException e) {
			log.error(e.getLocalizedMessage());
			throw new Error(e);
		}
	}
}
