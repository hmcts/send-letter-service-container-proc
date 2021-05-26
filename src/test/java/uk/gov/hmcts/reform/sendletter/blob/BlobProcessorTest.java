package uk.gov.hmcts.reform.sendletter.blob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class BlobProcessorTest {

    private BlobProcessor blobProcessor;

    @BeforeEach
    void setUp() {
        blobProcessor =  new BlobProcessor();
    }

    @Test
    void should_process_blob_when_triggered() {
        assertTrue(blobProcessor.read());
    }
}