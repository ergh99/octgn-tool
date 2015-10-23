package com.ergh99.util.octgn;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.abdera.parser.Parser;

@lombok.extern.slf4j.XSlf4j
public class OCTGNDirectory {

    private static final Parser abderaParser = Abdera.getNewParser();
    private URL feedUrl;

    public OCTGNDirectory(URL feedUrl) {
        this.feedUrl = feedUrl;
    }

    public OCTGNEntry getEntryForName(String gameName) throws IOException, URISyntaxException {
        log.entry(gameName);
        Optional<OCTGNEntry> entry = getEntriesFromFeed()
                .stream()
                .filter(e -> e.getTitle().equals(gameName))
                .map(e -> new OCTGNEntry(e))
                .filter(OCTGNEntry::isLatest)
                .findAny();
        return log.exit(entry.orElse(null));
    }

    public List<Entry> getEntriesFromFeed() throws IOException, URISyntaxException {
        log.entry();
        URLConnection service = feedUrl.openConnection();
        service.addRequestProperty("Accept", "application/xml");
        Document<Service> serviceDoc = abderaParser.parse(service.getInputStream()).complete();
        service.getInputStream().close();
        Collection packagesElement = serviceDoc.getRoot().getCollection("Default", "Packages");

        URL packagesUrl = packagesElement.getResolvedHref().toURL();
        URLConnection packages = packagesUrl.openConnection();
        packages.addRequestProperty("Accept", "application/atom+xml");
        Document<Feed> feedDoc = abderaParser.parse(packages.getInputStream()).complete();
        Feed feed = feedDoc.getRoot().complete();
        packages.getInputStream().close();

        return log.exit(feed.getEntries());
    }
}
