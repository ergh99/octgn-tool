package com.ergh99.util.octgn;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.abdera.parser.Parser;

public class OCTGNDirectory {

    private static final Abdera abdera = new Abdera();
    private URL feedUrl;

    public OCTGNDirectory(URL feedUrl) {
	this.feedUrl = feedUrl;
    }

    public OCTGNEntry getEntryForName(String gameName) throws IOException, URISyntaxException {
	Optional<OCTGNEntry> entry = getEntriesFromFeed()
		.stream()
		.filter(e -> e.getTitle().equals(gameName))
		.map(e -> new OCTGNEntry(e))
		.filter(OCTGNEntry::isLatest)
		.findAny();
	return (entry.isPresent()) ? entry.get() : null;
    }

    public List<Entry> getEntriesFromFeed() throws IOException, URISyntaxException {
	URLConnection service = feedUrl.openConnection();
	service.addRequestProperty("Accept", "application/xml");
	Parser p = abdera.getParser();
	Document<Service> serviceDoc = p.parse(service.getInputStream()).complete();
	service.getInputStream().close();
	Collection packagesElement = serviceDoc.getRoot().getCollection("Default", "Packages");

	IRI feedIri = new IRI(feedUrl);
	URLConnection packages = feedIri.trailingSlash().resolve(packagesElement.getHref()).toURL().openConnection();
	packages.addRequestProperty("Accept", "application/atom+xml");
	Document<Feed> feedDoc = p.parse(packages.getInputStream()).complete();
	Feed feed = feedDoc.getRoot().complete();
	packages.getInputStream().close();

	return feed.getEntries();
    }
}
