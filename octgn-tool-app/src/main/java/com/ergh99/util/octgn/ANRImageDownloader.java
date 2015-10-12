package com.ergh99.util.octgn;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@lombok.extern.slf4j.XSlf4j
public class ANRImageDownloader {

    private static ANRImageDownloader instance;

    public static synchronized ANRImageDownloader getInstance() {
        if (instance == null) {
            instance = new ANRImageDownloader();
        }
        return instance;
    }

    private ANRImageDownloader() {
        super();
    };

    public static void downloadCards(Set<Path> cardPaths) throws InterruptedException {
        log.entry(cardPaths);
        String imageUrl = "http://netrunnerdb.com/bundles/netrunnerdbcards/images/cards/en/#.png";
        log.info("Downloading from {}", imageUrl);
        // Netiquette standard limit is 4 simultaneous connections
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        CountDownLatch doneSignal = new CountDownLatch(cardPaths.size());
        for (Path cardPath : cardPaths) {
            String cardId = getIdFromPath(cardPath);
            try {
                URL cardUrl = new URL(imageUrl.replace("#", cardId));
                threadPool.execute(
                        new HttpFileSynchronizer(cardUrl, cardPath, doneSignal));
                Path jpgPath = getJPGPath(cardPath);
                if (Files.deleteIfExists(jpgPath)) {
                    log.warn("Deleting old format file: {}", jpgPath);
                }
            } catch (IOException e) {
                log.catching(e);
                throw log.throwing(new Error(e));
            }
        }
        threadPool.shutdown();
        doneSignal.await();
        log.exit();
    }

    private static Path getJPGPath(Path cardPath) {
        String rawCardPath = cardPath.toString();
        String noExtPath = rawCardPath.substring(0, rawCardPath.lastIndexOf(ANRConstants.FORMAT_EXT));
        return Paths.get(noExtPath + ANRConstants.OLD_FORMAT_EXT);
    }

    private static String getIdFromPath(Path cardPath) {
        Path cardPathName = cardPath.getFileName();
        if (cardPathName == null) {
            return log.exit(null);
        }
        String cardFileName = cardPathName.toString();
        String cardId = cardFileName.substring(0, cardFileName.lastIndexOf(ANRConstants.FORMAT_EXT));
        String cardShortId = cardId.substring(cardId.length() - 5);
        return cardShortId;
    }
}
