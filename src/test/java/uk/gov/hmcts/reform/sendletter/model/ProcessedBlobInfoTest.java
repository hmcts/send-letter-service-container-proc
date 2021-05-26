package uk.gov.hmcts.reform.sendletter.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedBlobInfoTest {

    @Test
    void should_serialize_when_manifest_created_for_parsing()  {
        var containerName = "testContainer";
        var blobName = "testBlob";

        ProcessedBlobInfo blobInfo = new ProcessedBlobInfo(containerName, blobName);

        assertThat(blobInfo.getBlobName()).isEqualTo(blobName);
        assertThat(blobInfo.getContainerName()).isEqualTo(containerName);
    }
}