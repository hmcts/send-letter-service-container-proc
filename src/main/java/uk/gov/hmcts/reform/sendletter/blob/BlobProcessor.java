package uk.gov.hmcts.reform.sendletter.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sendletter.blob.component.BlobManager;
import uk.gov.hmcts.reform.sendletter.blob.component.BlobReader;
import uk.gov.hmcts.reform.sendletter.blob.component.BlobUpload;
import uk.gov.hmcts.reform.sendletter.blob.storage.LeaseClientProvider;
import uk.gov.hmcts.reform.sendletter.model.ProcessedBlobInfo;

@Service
public class BlobProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(BlobProcessor.class);

    private final BlobManager blobManager;
    private final BlobReader blobReader;
    private final LeaseClientProvider leaseClientProvider;
    private final BlobUpload blobUpload;
    private final Integer leaseTime;

    public BlobProcessor(BlobReader blobReader, BlobManager blobManager,
                         LeaseClientProvider leaseClientProvider,
                         BlobUpload blobUpload,
                         @Value("${storage.leaseTime}") Integer leaseTime) {
        this.blobReader = blobReader;
        this.blobManager = blobManager;
        this.blobUpload = blobUpload;
        this.leaseClientProvider =  leaseClientProvider;
        this.leaseTime = leaseTime;
    }

    public boolean read() {
        LOG.info("BlobProcessor:: proccessing blob");

        return blobReader.retrieveManifestsToProcess()
                .stream()
                .map(this::process)
                .filter(Boolean::valueOf)
                .findFirst()
                .orElse(false);
    }

    private boolean process(ProcessedBlobInfo blobInfo) {
        var status = false;
        try {
            var containerClient  = blobManager.getContainerClient(blobInfo.getContainerName());
            var blobClient = containerClient.getBlobClient(blobInfo.getBlobName());
            var leaseClient = leaseClientProvider.get(blobClient);
            var leaseId = leaseClient.acquireLease(leaseTime);
            LOG.info("BlobProcessor::blob {} has been leased for {} seconds with leaseId {}",
                    blobInfo.getBlobName(), leaseTime, leaseId);
            status = blobUpload.process(blobInfo);
            blobClient.deleteWithResponse(
                    DeleteSnapshotsOptionType.INCLUDE,
                    new BlobRequestConditions().setLeaseId(leaseId),
                    null,
                    Context.NONE);
            LOG.info("BlobProcessor:: delete original blobs");
        } catch (Exception e) {
            LOG.error("Exception processing blob {}", blobInfo.getBlobName(), e);
        }
        return status;
    }
}
