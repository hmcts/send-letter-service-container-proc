package uk.gov.hmcts.reform.sendletter.blob.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sendletter.model.ProcessedBlobInfo;
import uk.gov.hmcts.reform.sendletter.services.SasTokenGeneratorService;
import uk.gov.hmcts.reform.sendletter.zip.ZipFileNameHelper;
import uk.gov.hmcts.reform.sendletter.zip.Zipper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class BlobUpload {

    private static final Logger LOG = LoggerFactory.getLogger(BlobUpload.class);
    private final SasTokenGeneratorService sasTokenGeneratorService;
    private final BlobManager blobManager;
    private final Zipper zipper;
    private static final String ZIPPED_CONTAINER = "zipped";


    public BlobUpload(BlobManager blobManager, SasTokenGeneratorService sasTokenGeneratorService, Zipper zipper) {
        this.blobManager = blobManager;
        this.sasTokenGeneratorService = sasTokenGeneratorService;
        this.zipper = zipper;
    }

    //zip files in the ‘processed container’ and move them to the ‘zipped’ container
    public boolean process(final ProcessedBlobInfo blobInfo) throws IOException {
        var containerName = blobInfo.getContainerName();

        LOG.info("zipAndMove:: containerName {}", containerName);
        var sasToken = sasTokenGeneratorService.generateSasToken(containerName);
        var pdfFile = blobInfo.getBlobName();
        var sourceBlobClient = blobManager.getBlobClient(containerName, sasToken, pdfFile);

        try (var blobInputStream = sourceBlobClient.openInputStream()) {
            byte[] fileContent = blobInputStream.readAllBytes();
            doZipAndUpload(pdfFile, fileContent);
        }

        //delete the source blob
        sourceBlobClient.delete();
        LOG.info("Blob {} delete successfully.", sourceBlobClient.getBlobUrl());
        return true;
    }

    private void doZipAndUpload(String pdfFile, byte[] fileContent) throws IOException {
        var zipFile = ZipFileNameHelper
                .getZipFileName(pdfFile, LocalDateTime.now(), pdfFile.lastIndexOf("_"));
        byte[] zipContent = zipper.zipBytes(pdfFile, fileContent);
        var containerClient = blobManager.getContainerClient(ZIPPED_CONTAINER);
        var zipBlobClient = containerClient.getBlobClient(zipFile);
        var byteArrayInputStream = new ByteArrayInputStream(zipContent);
        zipBlobClient.upload(byteArrayInputStream, zipContent.length);
        LOG.info("Uploaded blob {} to zipped container completed.", zipBlobClient.getBlobUrl());
    }
}
