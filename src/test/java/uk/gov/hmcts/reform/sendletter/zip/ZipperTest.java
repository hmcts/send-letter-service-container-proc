package uk.gov.hmcts.reform.sendletter.zip;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipInputStream;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;


class ZipperTest {
    @Test
    void should_zip_file() throws Exception {
        byte[] fileContent = toByteArray(getResource("merged.pdf"));
        byte[] expectedZipFileContent = toByteArray(getResource("merged.zip"));

        byte[] result = new Zipper().zipBytes("merged.pdf", fileContent);

        assertThat(result).isNotNull();
        assertThat(asZip(result)).hasSameContentAs(asZip(expectedZipFileContent));
    }

    private ZipInputStream asZip(byte[] bytes) {
        return new ZipInputStream(new ByteArrayInputStream(bytes));
    }
}