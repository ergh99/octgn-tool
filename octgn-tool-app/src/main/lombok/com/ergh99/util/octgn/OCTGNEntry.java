package com.ergh99.util.octgn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import lombok.Getter;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OCTGNEntry {

	public static final String M$_METADATA = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";

	private static final Logger log = LoggerFactory.getLogger(OCTGNEntry.class);

	@Getter private String id;
	@Getter private String title;
	private IRI src;
	@Getter private String version;
	private boolean isLatest;
	private Path nuPkg;

	public OCTGNEntry(Entry e) {
		super();
		src = e.getContentSrc();
		version = src.getPath().substring(src.getPath().lastIndexOf('/') +1);
		for (Element element : e.getExtensions(M$_METADATA)) {
			if ("properties".equals(element.getQName().getLocalPart())) {
				processProperties(element);
			} else {
				log.error("Unknown element: {}", element);
			}
		}
	}

	private void processProperties(Element element) {
		for (Element e : element.getElements()) {
			if ("IsAbsoluteLatestVersion".equals(e.getQName().getLocalPart())) {
				String val = e.getText();
				isLatest = Boolean.parseBoolean(val);
			}
			if ("Id".equals(e.getQName().getLocalPart())) {
				id = e.getText();
			}
			if ("Title".equals(e.getQName().getLocalPart())) {
				title = e.getText();
			}
		}
	}

	public boolean isLatest() {
		return isLatest;
	}

	public Path getNuPkg() {
		if (nuPkg == null) {
			downloadNuPkg();
		}
		return nuPkg;
	}

	private void downloadNuPkg() {
		InputStream remote;
		try {
			remote = src.toURL().openStream();
			File local = File.createTempFile("octgn-", ".zip");
			local.deleteOnExit();
			nuPkg = Paths.get(local.getAbsolutePath());
			log.info("Copying {}", src);
			long bytesCopied = Files.copy(remote, nuPkg, StandardCopyOption.REPLACE_EXISTING);
			remote.close();
			log.info("Copied {} bytes", bytesCopied);
		} catch (URISyntaxException | IOException e) {
			throw new Error(e);
		}
	}
}