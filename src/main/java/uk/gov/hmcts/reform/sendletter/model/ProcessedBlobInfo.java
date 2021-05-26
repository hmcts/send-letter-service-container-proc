package uk.gov.hmcts.reform.sendletter.model;

public class ProcessedBlobInfo {

    private final String containerName;
    private final String blobName;

    public ProcessedBlobInfo(String containerName, String blobName) {
        this.containerName = containerName;
        this.blobName = blobName;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getBlobName() {
        return blobName;
    }
}
