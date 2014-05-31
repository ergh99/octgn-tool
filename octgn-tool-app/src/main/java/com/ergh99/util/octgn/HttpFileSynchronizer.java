package com.ergh99.util.octgn;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.time.ZoneOffset.UTC;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpFileSynchronizer implements Runnable {

	private URL source;
	private Path destination;
	private CountDownLatch doneSignal;
	private final Logger log = LoggerFactory.getLogger(HttpFileSynchronizer.class);

	public HttpFileSynchronizer(URL source, Path destination, CountDownLatch doneSignal) {
		this.source = source;
		this.destination = destination;
		this.doneSignal = doneSignal;
	}

	@Override
	public void run() {
		String currentThreadName = Thread.currentThread().getName();
		String currentThreadNumber = currentThreadName.substring(currentThreadName.length() -1);
		String file = source.getFile();
		file = file.substring(file.lastIndexOf('/') +1);
		Thread.currentThread().setName("http-sync-" + file + "-" + currentThreadNumber);
		try {
			HttpURLConnection conn =
					(HttpURLConnection) source.openConnection();
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

			Map<String,List<String>> headers = conn.getHeaderFields();

			if (Files.exists(destination)) {
				if (needLocalUpdate(headers, destination)) {
					log.info("Updating {}", destination);
					Files.delete(destination);
				} else {
					log.info("No need to update {}", destination);
					return;
				}
			}
			log.info("Downloading {} ==> {}", source, destination);
			Files.copy(source.openStream(), destination);
			FileTime lastModified = getLastModifiedTime(headers);
			Files.setLastModifiedTime(destination, lastModified);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
			throw new Error(e);
		} finally {
			doneSignal.countDown();
		}
	}

	private boolean needLocalUpdate(Map<String, List<String>> headers, Path localPath) throws IOException {
		Map<String,Object> attrbs = Files.readAttributes(localPath, "*");

		String lastModifiedHeader = headers.get("Last-Modified").get(0);
		Instant remoteLastModified = getInstantForRFC1123(lastModifiedHeader);
		Instant localLastModified =
				Instant.parse(attrbs.get("lastModifiedTime").toString());

		boolean dateMismatch = ! localLastModified.equals(remoteLastModified);

		String contentLengthHeader = headers.get("Content-Length").get(0);
		long remoteSize = Long.parseLong(contentLengthHeader);
		long localSize = Files.size(localPath);
		boolean sizeMismatch = localSize != remoteSize;

		return dateMismatch || sizeMismatch;
	}

	private Instant getInstantForRFC1123(String lastModifiedHeader) {
		TemporalAccessor lastModified =
				DateTimeFormatter.RFC_1123_DATE_TIME.parse(lastModifiedHeader);
		LocalDateTime ldt = LocalDateTime.from(lastModified);
		return Instant.from(ldt.atZone(UTC));
	}

	private FileTime getLastModifiedTime(Map<String, List<String>> headers) {
		String lastModifiedHeader = headers.get("Last-Modified").get(0);
		return FileTime.from(getInstantForRFC1123(lastModifiedHeader));
	}
}
