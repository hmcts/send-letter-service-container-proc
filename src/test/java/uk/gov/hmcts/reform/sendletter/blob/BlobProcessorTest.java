package uk.gov.hmcts.reform.sendletter.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sendletter.blob.component.BlobReader;
import uk.gov.hmcts.reform.sendletter.blob.component.BlobUpload;
import uk.gov.hmcts.reform.sendletter.exceptions.LeaseIdNotPresentException;
import uk.gov.hmcts.reform.sendletter.model.BlobInfo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlobProcessorTest {
    @Mock
    private BlobReader blobReader;
    @Mock
    private BlobUpload blobUpload;

    @Mock
    private BlobClient blobClient;
    @Mock
    private BlobProperties blobProperties;

    private BlobProcessor blobProcessor;

    @BeforeEach
    void setUp() {
        blobProcessor =  new BlobProcessor(
                blobReader,
                blobUpload);
    }

    @Test
    void should_process_blob_when_triggered() {

        BlobInfo blobInfo = new BlobInfo(blobClient);
        blobInfo.setLeaseId("LEASE_ID");
        given(blobReader.retrieveBlobToProcess()).willReturn(Optional.of(blobInfo));

        boolean status = blobProcessor.read();

        assertThat(status).isTrue();
        verify(blobReader).retrieveBlobToProcess();
        verify(blobUpload).process(blobInfo);
        verify(blobClient).deleteWithResponse(any(), any(), any(), any());

    }

    @Test
    void should_not_process_when_no_blob_returned() {
        given(blobReader.retrieveBlobToProcess()).willReturn(Optional.empty());
        boolean status = blobProcessor.read();
        assertThat(status).isFalse();
        verify(blobReader).retrieveBlobToProcess();
        verify(blobUpload, never()).process(any());
        verify(blobClient, never()).deleteWithResponse(any(), any(), any(), any());
    }

    @Test
    void should_throw_lease_id_not_present_expection_when_leaseId_absent() {
        BlobInfo blobInfo = new BlobInfo(blobClient);
        given(blobReader.retrieveBlobToProcess()).willReturn(Optional.of(blobInfo));

        assertThatThrownBy(() -> blobProcessor.read())
                .isInstanceOf(LeaseIdNotPresentException.class)
                .hasMessage("Lease not present");
    }
}