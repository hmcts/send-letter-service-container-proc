package uk.gov.hmcts.reform.sendletter.blob.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sendletter.config.AccessTokenProperties;
import uk.gov.hmcts.reform.sendletter.exceptions.UnableToZipException;
import uk.gov.hmcts.reform.sendletter.model.BlobInfo;
import uk.gov.hmcts.reform.sendletter.services.SasTokenGeneratorService;
import uk.gov.hmcts.reform.sendletter.zip.ZipFileNameHelper;
import uk.gov.hmcts.reform.sendletter.zip.Zipper;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

@Component
public class BlobUpload {

    private static final Logger LOG = LoggerFactory.getLogger(BlobUpload.class);
    private final SasTokenGeneratorService sasTokenGeneratorService;
    private final BlobManager blobManager;
    private final Zipper zipper;
    private final String destinationContainer;

    public BlobUpload(BlobManager blobManager,
            SasTokenGeneratorService sasTokenGeneratorService,
            AccessTokenProperties accessTokenProperties,
            Zipper zipper) {
        this.blobManager = blobManager;
        this.sasTokenGeneratorService = sasTokenGeneratorService;
        this.destinationContainer = accessTokenProperties
                .getContainerForGivenType("destination");
        this.zipper = zipper;
    }

    public void process(final BlobInfo blobInfo) {
        var pdfFile = blobInfo.getBlobClient().getBlobName();
        var sourceBlobClient = blobInfo.getBlobClient();
        LOG.info("process zip:: pdfFile {}", pdfFile);

        try (var blobInputStream = sourceBlobClient.openInputStream()) {
            byte[] fileContent = blobInputStream.readAllBytes();

            var zipFile = ZipFileNameHelper
                    .getZipFileName(pdfFile, LocalDateTime.now(), pdfFile.lastIndexOf("_"));

            byte[] zipContent = zipper.zipBytes(pdfFile, fileContent);
            String sasToken = sasTokenGeneratorService.generateSasToken(destinationContainer);
            var zipBlobClient = blobManager.getBlobClient(destinationContainer, sasToken, zipFile);
            var byteArrayInputStream = new ByteArrayInputStream(zipContent);

            zipBlobClient.upload(byteArrayInputStream, zipContent.length);
            LOG.info("Uploaded blob {} to zipped container completed.", zipBlobClient.getBlobUrl());

        } catch (Exception e) {
            throw new UnableToZipException(String.format("Exception in zipping and upload %s", pdfFile), e);
        }
    }

}
