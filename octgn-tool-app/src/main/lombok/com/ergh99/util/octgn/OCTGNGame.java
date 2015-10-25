package com.ergh99.util.octgn;

import static javax.xml.xpath.XPathConstants.NODESET;

import static com.ergh99.util.octgn.ANRConstants.SKIPPABLE_SETS;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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

    private static final XPath xpath = XPathFactory.newInstance().newXPath();

    @Getter
    protected String id;
    @Getter
    protected String title;
    private Path gamePath;
    private Path imagesPath;
    private Integer setCount;
    private Integer cardCount;
    private Set<Path> cardPaths;
    private XPathExpression cardIdExpression;
	private XPathExpression setNameExpression;

    protected OCTGNGame(OCTGN o, String id, String title) {
        this.id = id;
        this.title = title;
        gamePath = o.getGameDatabase().resolve(id);
        imagesPath = o.getImageDatabase().resolve(id);
        try {
			cardIdExpression = xpath.compile("/set/cards/card/@id");
			setNameExpression = xpath.compile("/set/@name");
		} catch (XPathExpressionException e) {
			throw log.throwing(new Error(e));
		}
    }

    protected OCTGNGame(OCTGN o, OCTGNEntry entry) {
        this(o, entry.getId(), entry.getTitle());
    }

    public Set<Path> getCardPaths() {
    	if (cardPaths != null) {
			return cardPaths;
		}
        log.entry();
        cardPaths = new HashSet<>();
        Path gameSetsPath = gamePath.resolve("Sets");
        Path imageSetsPath = imagesPath.resolve("Sets");
        int setCounter = 0;
        int cardCounter = 0;

        try (DirectoryStream<Path> setsStream =
        		Files.newDirectoryStream(gameSetsPath, Files::isDirectory)) {
            for (Path setPath : setsStream) {
                Path setXml = setPath.resolve("set.xml");
                String setXmlString = new String(Files.readAllBytes(setXml), "UTF-8");
                String setName = setNameExpression.evaluate(
                		new InputSource(new StringReader(setXmlString)));
                if (SKIPPABLE_SETS.contains(setName)) {
                    continue;
                }
                setCounter++;
                log.info("Parsing {} : {}", setName, setPath);
                Object o = cardIdExpression.evaluate(
                		new InputSource(new StringReader(setXmlString)), NODESET);
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
