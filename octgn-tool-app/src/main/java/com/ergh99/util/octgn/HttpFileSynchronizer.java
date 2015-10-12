package com.ergh99.util.octgn;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.CountDownLatch;

@lombok.extern.slf4j.XSlf4j
public class HttpFileSynchronizer implements Runnable {

    private URL source;
    private Path destination;
    private CountDownLatch doneSignal;

    public HttpFileSynchronizer(URL source, Path destination, CountDownLatch doneSignal) {
        this.source = source;
        this.destination = destination;
        this.doneSignal = doneSignal;
    }

    @Override
    public void run() {
        String currentThreadName = Thread.currentThread().getName();
        String currentThreadNumber = currentThreadName.substring(currentThreadName.length() - 1);
        String file = source.getFile();
        file = file.substring(file.lastIndexOf('/') + 1);
        Thread.currentThread().setName("http-sync-" + file + "-" + currentThreadNumber);
        try {
            HttpURLConnection conn = (HttpURLConnection) source.openConnection();
            conn.setRequestMethod("HEAD");
            conn.connect();
            conn.disconnect();

            switch (conn.getResponseCode()) {
            case HTTP_NOT_FOUND:
                log.error("Received 404 Not found for {}", source);
                log.debug("Error received for {}", destination);
                return;

            case HTTP_OK:
                break;

            default:
                log.warn("Received {} for {}", conn.getResponseCode(), source);
                break;
            }

            HttpRemoteFile remoteFile = new HttpRemoteFile(conn.getHeaderFields());

            if (Files.exists(destination)) {
                if (areFilesDifferent(remoteFile, destination)) {
                    log.info("Updating {}", destination);
                    Files.delete(destination);
                } else {
                    log.info("No need to update {}", destination);
                    return;
                }
            }
            log.info("Downloading {}", source);
            Files.copy(source.openStream(), destination);
            FileTime lastModified = remoteFile.getLastModifiedFileTime();
            Files.setLastModifiedTime(destination, lastModified);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            throw new Error(e);
        } finally {
            doneSignal.countDown();
        }
    }

    private boolean areFilesDifferent(HttpRemoteFile remoteFile, Path localPath) throws IOException {
        return remoteFile.hasDifferentModificationTime(localPath) || remoteFile.isDifferentSize(localPath);
    }
}
