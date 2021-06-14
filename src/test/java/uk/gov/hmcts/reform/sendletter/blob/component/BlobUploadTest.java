package uk.gov.hmcts.reform.sendletter.blob.component;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sendletter.config.AccessTokenProperties;
import uk.gov.hmcts.reform.sendletter.exceptions.UnableToZipException;
import uk.gov.hmcts.reform.sendletter.model.BlobInfo;
import uk.gov.hmcts.reform.sendletter.services.SasTokenGeneratorService;
import uk.gov.hmcts.reform.sendletter.zip.Zipper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.BiFunction;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlobUploadTest {
    private static final String TEST_BLOB_NAME = "test-mypdf.pdf";
    private static final String ZIP_BLOB_NAME = "test-mypdf.zip";
    @Mock
    private BlobManager blobManager;
    @Mock
    private SasTokenGeneratorService sasTokenGeneratorService;
    @Mock
    private BlobClient blobClient;
    @Mock
    private BlobClient zipBlobClient;
    @Mock
    private Zipper zipper;
    @Mock
    private BlobInputStream blobInputStream;
    private BlobUpload blobUpload;
    private AccessTokenProperties accessTokenProperties;

    @BeforeEach
    void setUp() {
        createAccessTokenConfig();
        blobUpload = new BlobUpload(
                blobManager,
                sasTokenGeneratorService,
                accessTokenProperties,
                zipper
        );
    }

    @Test
    void should_upload_blob_when_able_to_zip() throws IOException {
        given(blobClient.getBlobName()).willReturn(TEST_BLOB_NAME);
        var blobInfo = new BlobInfo(blobClient);
        blobInfo.setLeaseId("LEASE_ID");
        given(zipper.zipBytes(anyString(), any())).willReturn("zipContent".getBytes());
        given(blobClient.openInputStream()).willReturn(blobInputStream);
        String sasToken = "sasToken";
        String zipped = "zipped";
        given(sasTokenGeneratorService.generateSasToken(zipped))
                .willReturn(sasToken);
        given(blobManager.getBlobClient(
                zipped,
                sasToken,
                ZIP_BLOB_NAME
        )).willReturn(this.zipBlobClient);

        blobUpload.process(blobInfo);

        verify(sasTokenGeneratorService).generateSasToken(zipped);
        verify(blobManager).getBlobClient(
                zipped,
                sasToken,
                ZIP_BLOB_NAME
        );
        ArgumentCaptor<ByteArrayInputStream> byteCaptor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
        long dataLength = "zipContent".getBytes().length;
        verify(zipBlobClient).upload(
                byteCaptor.capture(),
                eq(dataLength));
        var value = byteCaptor.getValue();
        byte[] bytes = value.readAllBytes();
        assertThat(bytes).contains("zipContent".getBytes());
    }

    @Test
    void should_not_upload_blob_when_unable_to_zip() throws IOException {
        given(blobClient.getBlobName()).willReturn(TEST_BLOB_NAME);
        var blobInfo = new BlobInfo(blobClient);
        blobInfo.setLeaseId("LEASE_ID");
        given(zipper.zipBytes(anyString(), any()))
                .willThrow(new RuntimeException("Exception in zipping and upload test-mypdf.pdf"));
        given(blobClient.openInputStream()).willReturn(blobInputStream);

        assertThatThrownBy(() -> blobUpload.process(blobInfo))
                .isInstanceOf(UnableToZipException.class)
                .hasMessage("Exception in zipping and upload test-mypdf.pdf");
    }

    private void createAccessTokenConfig() {
        BiFunction<String, String, AccessTokenProperties.TokenConfig> tokenFunction = (type, container) -> {
            AccessTokenProperties.TokenConfig tokenConfig = new AccessTokenProperties.TokenConfig();
            tokenConfig.setValidity(300);
            tokenConfig.setContainerType(type);
            tokenConfig.setContainerName(container);
            return tokenConfig;
        };
        accessTokenProperties = new AccessTokenProperties();
        accessTokenProperties.setServiceConfig(
                of(
                        tokenFunction.apply("source", "processed"),
                        tokenFunction.apply("destination", "zipped")

                )
        );
    }
}