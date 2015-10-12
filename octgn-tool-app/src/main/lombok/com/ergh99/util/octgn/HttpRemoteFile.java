package com.ergh99.util.octgn;

import static java.time.ZoneOffset.UTC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;

@lombok.Data
public class HttpRemoteFile {

    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String LAST_MODIFIED = "Last-Modified";

    private Instant localizedLastModifiedTime;
    private long contentLength;

    public HttpRemoteFile(Map<String, List<String>> headers) {
        localizedLastModifiedTime = getLocalizedInstantForRFC1123(headers.get(LAST_MODIFIED).get(0));
        contentLength = Long.parseLong(headers.get(CONTENT_LENGTH).get(0));
    }

    public FileTime getLastModifiedFileTime() {
        return FileTime.from(localizedLastModifiedTime);
    }

    public boolean isDifferentSize(Path localPath) throws IOException {
        long localSize = Files.size(localPath);

        return localSize != contentLength;
    }

    public boolean hasDifferentModificationTime(Path localPath) {
        Instant localLastModified = Instant.ofEpochMilli(localPath.toFile().lastModified());

        return !localLastModified.equals(localizedLastModifiedTime);
    }

    public static Instant getLocalizedInstantForRFC1123(String lastModifiedTime) {
        TemporalAccessor lastModified = DateTimeFormatter.RFC_1123_DATE_TIME.parse(lastModifiedTime);
        LocalDateTime ldt = LocalDateTime.from(lastModified);
        return Instant.from(ldt.atZone(UTC));
    }
}