package uk.gov.hmcts.reform.sendletter.zip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;

public final class ZipFileNameHelper {

    public static final DateTimeFormatter dateTimeFormatter = ofPattern("ddMMyyyyHHmmss");
    private static final String SEPARATOR = "_";

    public static String getZipFileName(String name,
                                        LocalDateTime createdAtTime, int index) {
        var newString = new StringBuilder();
        for (var i = 0; i < name.length(); i++) {
            newString.append(name.charAt(i));
            if (i == index) {
                newString.append(createdAtTime.format(dateTimeFormatter));
                newString.append(SEPARATOR);
            }
        }
        return newString.toString().replace(".pdf", ".zip");
    }

    private ZipFileNameHelper() {
        // utility class constructor
    }
}
