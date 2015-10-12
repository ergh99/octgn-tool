package com.ergh99.util.octgn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import lombok.Getter;

@lombok.extern.slf4j.XSlf4j
public class OCTGN {

    private static XPath xpath = XPathFactory.newInstance().newXPath();

    @Getter
    private Path gameDatabase;
    @Getter
    private Path imageDatabase;
    private Map<String, String> gameIdMap = new HashMap<>();

    public OCTGN() {
        this(Paths.get("."));
    }

    public OCTGN(Path homeDir) {
        gameDatabase = homeDir.resolve("GameDatabase");
        imageDatabase = homeDir.resolve("ImageDatabase");
    }

    public OCTGNGame installGameFromEntry(OCTGNEntry entry) {
        log.entry(entry);
        OCTGNGame g = new OCTGNGame(this, entry);
        return log.exit(g);
    }

    public OCTGNGame getGameByTitle(String gameTitle) {
        log.entry(gameTitle);
        if (gameIdMap.containsKey(gameTitle) == false) {
            gameIdMap.put(gameTitle, findGameIdByTitle(gameTitle));
        }
        return log.exit(new OCTGNGame(this, gameIdMap.get(gameTitle), gameTitle));
    }

    protected String findGameIdByTitle(String gameTitle) {
        log.entry(gameTitle);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(gameDatabase, Files::isDirectory)) {
            XPathExpression expr = xpath.compile(String.format("/game[@name='%s']/@id", gameTitle));
            for (Path gameDir : stream) {
                log.debug("Looking for {} in {}", gameTitle, gameDir);
                InputStream in = Files.newInputStream(gameDir.resolve("definition.xml"));
                String id = expr.evaluate(new InputSource(in));
                in.close();
                if (id != null && id.length() > 0) {
                    log.info("Found {} id: {}", gameTitle, id);
                    return log.exit(id);
                }
            }

            throw log.throwing(new Error("No game definition found for " + gameTitle));
        } catch (IOException | XPathExpressionException e) {
            log.catching(e);
            throw log.throwing(new Error(e));
        }
    }
}
