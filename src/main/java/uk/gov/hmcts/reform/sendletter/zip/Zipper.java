package uk.gov.hmcts.reform.sendletter.zip;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class Zipper {

    public byte[] zipBytes(String filename, byte[] input) throws IOException {

        var entry = new ZipEntry(filename);
        entry.setSize(input.length);

        var baos = new ByteArrayOutputStream();

        try (var zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(entry);
            zos.write(input);
            zos.closeEntry();
        }

        return baos.toByteArray();
    }
}
