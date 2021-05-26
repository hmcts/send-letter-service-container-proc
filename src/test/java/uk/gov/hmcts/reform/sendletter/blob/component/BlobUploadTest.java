package uk.gov.hmcts.reform.sendletter.blob.component;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sendletter.config.AccessTokenProperties;
import uk.gov.hmcts.reform.sendletter.model.ProcessedBlobInfo;
import uk.gov.hmcts.reform.sendletter.services.SasTokenGeneratorService;
import uk.gov.hmcts.reform.sendletter.zip.Zipper;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BlobUploadTest {
    private static final String ZIPPED_CONTAINER = "zipped";
    private static final String PROCESSED_CONTAINER = "processed";
    private static final String BLOB_NAME = "test-mypdf.pdf";

    private AccessTokenProperties accessTokenProperties;
    private BlobUpload blobUpload;
    private ProcessedBlobInfo blobInfo;

    @Mock
    private BlobManager blobManager;

    @Mock
    private BlobClient client;
    @Mock
    private Zipper zipper;
    @Mock
    private BlobInputStream blobInputStream;

    @BeforeEach
    void setUp() {
        blobInfo = new ProcessedBlobInfo(PROCESSED_CONTAINER, "manifests-xyz.pdf");

        given(blobManager.getAccountUrl()).willReturn("http://test.account");

        StorageSharedKeyCredential storageCredentials =
                new StorageSharedKeyCredential("testAccountName", "dGVzdGtleQ==");

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .credential(storageCredentials)
                .endpoint("http://test.account")
                .buildClient();

        createAccessTokenConfig();

        SasTokenGeneratorService sasTokenGeneratorService = new SasTokenGeneratorService(blobServiceClient,
                accessTokenProperties);

        blobUpload = new BlobUpload(blobManager, sasTokenGeneratorService, zipper);

        var sasToken = sasTokenGeneratorService.generateSasToken(PROCESSED_CONTAINER);

        BlobClient sourceBlobClient = new BlobClientBuilder()
                .endpoint(blobManager.getAccountUrl())
                .sasToken(sasToken)
                .containerName(PROCESSED_CONTAINER)
                .blobName(BLOB_NAME)
                .buildClient();

    }

    @Test
    void should_zip_blob_and_upload() throws IOException {

        given(client.openInputStream()).willReturn(blobInputStream);
        given(blobManager.getBlobClient(any(), any(), any())).willReturn(client);
        given(zipper.zipBytes(anyString(), any())).willReturn("zipContent".getBytes());

        boolean process = blobUpload.process(blobInfo);
        assertTrue(process);
        //verify(client).upload(any(), any());
    }

    private void createAccessTokenConfig() {
        AccessTokenProperties.TokenConfig tokenConfig = new AccessTokenProperties.TokenConfig();
        tokenConfig.setValidity(300);
        tokenConfig.setContainerName(PROCESSED_CONTAINER);

        accessTokenProperties = new AccessTokenProperties();
        accessTokenProperties.setServiceConfig(singletonList(tokenConfig));
    }
}