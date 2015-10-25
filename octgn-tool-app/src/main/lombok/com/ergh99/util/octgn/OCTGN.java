package com.ergh99.util.octgn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

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

    private static final int ZIP_PREFIX_LENGTH = "def/".length();
    public OCTGNGame installGameFromEntry(OCTGNEntry entry) {
        log.entry(entry);
        try (ZipFile nuPkg = new ZipFile(entry.getNuPkg().toFile())) {
            if (Files.exists(gameDatabase) && Files.isDirectory(gameDatabase)) {
                Path gameDir = gameDatabase.resolve(entry.getId());
                Files.createDirectory(gameDir);
                nuPkg
                .stream()
                .filter(e -> e.getName().startsWith("def"))
                .forEach(zipEntry -> {
                	try (InputStream in = nuPkg.getInputStream(zipEntry)) {
                		// Get path stripped of def/ (4 characters)
                		String relative = zipEntry.getName().substring(ZIP_PREFIX_LENGTH);
                        Path absolute = gameDir.resolve(relative);
                        log.info("Creating {}", absolute);
                        Files.createDirectories(absolute.getParent());
                        if (zipEntry.isDirectory()) {
                        	Files.createDirectory(absolute);
                        } else {
                        	Files.copy(in, absolute);
                        }
                    } catch (FileAlreadyExistsException e) {
                        log.debug(e.getLocalizedMessage());
                    } catch (IOException e) {
                    	throw new Error(e);
                    }
                });
            } else {
                throw new FileNotFoundException(gameDatabase.toString());
            }
        } catch (IOException e) {
            throw new Error(e);
        }
        return log.exit(new OCTGNGame(this, entry));
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
            throw log.throwing(new Error(e));
        }
    }
}
