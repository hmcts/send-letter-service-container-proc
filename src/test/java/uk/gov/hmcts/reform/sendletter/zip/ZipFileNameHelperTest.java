package uk.gov.hmcts.reform.sendletter.zip;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ZipFileNameHelperTest {

    @Test
    void should_return_zip_name() {
        var time = "2021-05-26T16:03:15.909771";
        var localTimeObj = LocalDateTime.parse(time);
        var expected = "BULKPRINT001_send_letter_tests_26052021160315_ddcea411-42a8-4134-bad8-e9b5bee84d24.zip";
        var pdf = "BULKPRINT001_send_letter_tests_ddcea411-42a8-4134-bad8-e9b5bee84d24.pdf";
        var zipFileName = ZipFileNameHelper.getZipFileName(pdf, localTimeObj, pdf.lastIndexOf("_"));

        assertThat(zipFileName).isEqualTo(expected);
    }
}