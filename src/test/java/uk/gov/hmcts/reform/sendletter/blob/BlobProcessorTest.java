package uk.gov.hmcts.reform.sendletter.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sendletter.blob.component.BlobManager;
import uk.gov.hmcts.reform.sendletter.blob.component.BlobReader;
import uk.gov.hmcts.reform.sendletter.blob.component.BlobUpload;
import uk.gov.hmcts.reform.sendletter.blob.storage.LeaseClientProvider;
import uk.gov.hmcts.reform.sendletter.model.ProcessedBlobInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlobProcessorTest {
    private static final String CONTAINER = "processed";

    private BlobProcessor blobProcessor;
    @Mock
    private BlobManager blobManager;
    @Mock
    private BlobReader blobReader;
    @Mock
    private BlobUpload blobUpload;

    private List<ProcessedBlobInfo> blobInfos;
    @Mock
    private BlobClient blobClient;
    @Mock
    private BlobContainerClient blobContainerClient;
    @Mock
    private LeaseClientProvider leaseClientProvider;
    @Mock
    private BlobLeaseClient blobLeaseClient;

    @BeforeEach
    void setUp() {

        blobInfos = List.of(
                new ProcessedBlobInfo(CONTAINER,
                        "manifests-xyz.pdf")
        );

        blobProcessor = new BlobProcessor(blobReader, blobManager, leaseClientProvider, blobUpload, 10);
        given(blobReader.retrieveManifestsToProcess())
                .willReturn(blobInfos);
    }

    @Test
    void should_process_blob_when_triggered() throws IOException {
        given(blobManager.getContainerClient(any())).willReturn(blobContainerClient);
        given(blobContainerClient.getBlobClient(any())).willReturn(blobClient);
        given(leaseClientProvider.get(blobClient)).willReturn(blobLeaseClient);

        given(blobUpload.process(blobInfos.get(0))).willReturn(true);

        boolean processed = blobProcessor.read();
        assertTrue(processed);

        ProcessedBlobInfo firstManifestBlobInfo = blobInfos.get(0);
        verify(blobManager).getContainerClient(firstManifestBlobInfo.getContainerName());
        verify(blobLeaseClient).acquireLease(anyInt());
    }

    @Test
    void should_not_triggered_when_no_matching_blob_available() throws IOException {
        given(blobReader.retrieveManifestsToProcess())
                .willReturn(Collections.emptyList());

        boolean processed = blobProcessor.read();
        assertFalse(processed);
        verify(blobManager, never()).getContainerClient(anyString());
    }

}