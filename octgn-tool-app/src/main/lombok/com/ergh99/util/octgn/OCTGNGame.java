package com.ergh99.util.octgn;

import static javax.xml.xpath.XPathConstants.NODESET;

import static com.ergh99.util.octgn.ANRConstants.SKIPPABLE_SETS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import lombok.Getter;

@lombok.extern.slf4j.XSlf4j
public class OCTGNGame {

    private static final int ZIP_PREFIX_LENGTH = "def/".length();
    private static final XPath xpath = XPathFactory.newInstance().newXPath();

    @Getter
    protected String id;
    @Getter
    protected String title;
    private Path gamePath;
    private Path imagesPath;
    private Integer setCount;
    private Integer cardCount;

    protected OCTGNGame(OCTGN o, String id, String title) {
        this.id = id;
        this.title = title;
        gamePath = o.getGameDatabase().resolve(id);
        imagesPath = o.getImageDatabase().resolve(id);
    }

    protected OCTGNGame(OCTGN o, OCTGNEntry entry) {
        this(o, entry.getId(), entry.getTitle());
        try (ZipFile nuPkg = new ZipFile(entry.getNuPkg().toFile())) {
            Path gameDatabase = o.getGameDatabase();
            if (Files.exists(gameDatabase) && Files.isDirectory(gameDatabase)) {
                Path gameDir = gameDatabase.resolve(id);
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
    }

    public Set<Path> getCardPaths() {
        log.entry();
        Set<Path> cardPaths = new HashSet<>();
        Path gameSetsPath = gamePath.resolve("Sets");
        Path imageSetsPath = imagesPath.resolve("Sets");
        int setCounter = 0;
        int cardCounter = 0;

        try (DirectoryStream<Path> setsStream = Files.newDirectoryStream(gameSetsPath, Files::isDirectory)) {
            XPathExpression expr = xpath.compile("/set/cards/card/@id");
            XPathExpression setNameExpr = xpath.compile("/set/@name");
            for (Path setPath : setsStream) {
                Path setXml = setPath.resolve("set.xml");
                InputStream in = Files.newInputStream(setXml);
                String setName = setNameExpr.evaluate(new InputSource(in));
                if (SKIPPABLE_SETS.contains(setName)) {
                    continue;
                }
                setCounter++;
                log.info("Parsing {} : {}", setName, setPath);
                in = Files.newInputStream(setXml);
                Object o = expr.evaluate(new InputSource(in), NODESET);
                if (o instanceof NodeList) {
                    NodeList cardList = (NodeList) o;
                    for (int i = 0; i < cardList.getLength(); i++) {
                        Node n = cardList.item(i);
                        Path setIdPath = setPath.getFileName();
                        if (setIdPath == null) {
                            throw log.throwing(new java.io.FileNotFoundException(setPath.toString()));
                        }
                        String setId = setIdPath.toString();
                        String cardFile = n.getNodeValue() + ANRConstants.FORMAT_EXT;
                        cardPaths.add(imageSetsPath.resolve(setId).resolve("Cards/" + cardFile));
                        cardCounter++;
                    }
                }
                in.close();
            }
        } catch (XPathExpressionException | IOException e) {
            throw log.throwing(new Error(e));
        }
        setCount = setCounter;
        cardCount = cardCounter;
        return log.exit(cardPaths);
    }

    public Integer getSetCount() {
        if (setCount == null) {
            getCardPaths();
        }
        return setCount;
    }

    public Integer getCardCount() {
        if (cardCount == null) {
            getCardPaths();
        }
        return cardCount;
    }
}
